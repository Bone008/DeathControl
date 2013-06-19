package bone008.bukkit.deathcontrol;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.permissions.Permissible;
import org.bukkit.permissions.ServerOperator;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import bone008.bukkit.deathcontrol.commandhandler.CommandHandler;
import bone008.bukkit.deathcontrol.commands.BackCommand;
import bone008.bukkit.deathcontrol.commands.CancelCommand;
import bone008.bukkit.deathcontrol.commands.HelpCommand;
import bone008.bukkit.deathcontrol.commands.ReloadCommand;
import bone008.bukkit.deathcontrol.config.DeathContext;
import bone008.bukkit.deathcontrol.config.ItemLists;
import bone008.bukkit.deathcontrol.config.NewConfiguration;
import bone008.bukkit.deathcontrol.exceptions.ResourceNotFoundError;
import bone008.bukkit.deathcontrol.util.DPermission;
import bone008.bukkit.deathcontrol.util.EconomyUtil;

public class DeathControl extends JavaPlugin {

	public static final DPermission PERMISSION_USE = new DPermission("deathcontrol.use", false);
	public static final DPermission PERMISSION_FREE = new DPermission("deathcontrol.free", true);
	public static final DPermission PERMISSION_CROSSWORLD = new DPermission("deathcontrol.crossworld", true);
	public static final DPermission PERMISSION_NOLIMITS = new DPermission("deathcontrol.nolimits", true);
	public static final DPermission PERMISSION_INFO = new DPermission("deathcontrol.info", true);
	public static final DPermission PERMISSION_ADMIN = new DPermission("deathcontrol.admin", true);

	public static DeathControl instance;
	private File messagesFile = null;

	public NewConfiguration config;
	public ItemLists itemLists;
	public YamlConfiguration messagesData;
	public PluginDescriptionFile pdfFile;

	private Map<String, DeathContextImpl> activeDeaths = new HashMap<String, DeathContextImpl>();

	public DeathControl() {
		instance = this;
	}

	@Override
	public void onDisable() {
		if (!this.activeDeaths.isEmpty()) {
			// avoid CME by iterating over a stand-alone list
			for (DeathContextImpl context : new ArrayList<DeathContextImpl>(this.activeDeaths.values())) {
				context.cancel();
			}
		}

		instance = null;
	}

	@Override
	public void onEnable() {
		messagesFile = new File(getDataFolder(), "messages.yml");
		pdfFile = getDescription();

		// load/generate the configurations and setup permissions if needed
		loadConfig();

		// register events
		PluginManager pm = getServer().getPluginManager();
		pm.registerEvents(new BukkitDeathHandler(), this);
		pm.registerEvents(new BukkitReconnectHandler(), this);
		pm.registerEvents(new BukkitRuleNotifHandler(), this);

		// initially notify everyone of potentially wrong gamerules
		BukkitRuleNotifHandler.warnAll();

		// setup commands
		CommandHandler deathCmd = new CommandHandler();

		deathCmd.addSubCommand("help", new HelpCommand(), "?");
		deathCmd.addSubCommand("back", new BackCommand(), "restore");
		deathCmd.addSubCommand("cancel", new CancelCommand(), "drop", "expire");
		deathCmd.addSubCommand("reload", new ReloadCommand());
		//		deathCmd.addSubCommand("info", new InfoCommand(), "status");

		getCommand("death").setExecutor(deathCmd);

		// setup economy
		EconomyUtil.init();
	}

	/**
	 * Loads or reloads the config files and generates the default files if necessary.
	 */
	public void loadConfig() {
		// create the default files
		writeDefault("config.yml", "config.yml", false); // only write the default if no file exists
		writeDefault("lists.txt", "lists.txt", false);
		writeDefault("messages.yml", "messages.yml", false);

		// now load the config, otherwise it would be created before the exists check
		reloadConfig();
		FileConfiguration cfg = getConfig();
		cfg.options().copyDefaults(true);
		cfg.options().copyHeader(true);
		cfg.set("show-messages", null); // remove deprecated option

		// parse the config & lists files		
		itemLists = new ItemLists(this, new File(getDataFolder(), "lists.txt"));
		config = new NewConfiguration(cfg);
		//saveConfig(); TODO reenable saveConfig()

		messagesData = YamlConfiguration.loadConfiguration(messagesFile);
		checkMessagesIntegrity();

		log(Level.CONFIG, "is now using " + (config.usesBukkitPerms() ? "bukkit permissions" : "the OP-system") + "!");
	}

	/**
	 * Writes a resource contained in the jar to a specified destination.
	 * 
	 * @param resourceName The name of the resource in the jar
	 * @param destination The destination path, relative to the plugin's data folder
	 * @param force Specifies if the file should be overwritten when already existing
	 * @return true, if and only if the file was successfully written
	 */
	public boolean writeDefault(String resourceName, String destination, boolean force) {
		boolean ret = false;
		File file = new File(getDataFolder(), destination);

		if (!force && file.exists())
			return false;

		InputStream in = getClass().getResourceAsStream("/resources/" + resourceName);
		if (in == null) {
			throw new ResourceNotFoundError(resourceName);
		}

		FileOutputStream out = null;
		try {

			getDataFolder().mkdirs();
			file.delete();
			file.createNewFile();

			out = new FileOutputStream(file);
			byte[] buffer = new byte[8192];
			int remaining = 0;
			while ((remaining = in.read(buffer)) > 0) {
				out.write(buffer, 0, remaining);
			}

			log(Level.INFO, "default file " + resourceName + " created/updated!", true);
			ret = true;

		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (in != null)
					in.close();
			} catch (IOException e) {
			}
			try {
				if (out != null)
					out.close();
			} catch (IOException e) {
			}
		}

		return ret;
	}

	/**
	 * Checks messages.yml for completeness. If action needs to be taken, the old messages.yml file is backed up and the default one is written.
	 * We can't use YAML saving, as it will severely screw up multi-line options.
	 */
	private void checkMessagesIntegrity() {
		InputStream messageDefaultsStream = getClass().getResourceAsStream("/resources/messages.yml");
		if (messageDefaultsStream == null) // we might have been hot-swapped and don't want to crash, checks are aborted and done at another point of time
			return;

		boolean needsUpdate = false, needsBackup = false;

		YamlConfiguration defaultMessages = YamlConfiguration.loadConfiguration(messageDefaultsStream);
		for (String msgKey : defaultMessages.getKeys(true)) {
			if (messagesData.isSet(msgKey)) {
				Object defaultVal = messagesData.get(msgKey);
				if (!(defaultVal instanceof ConfigurationSection) && !defaultVal.equals(defaultMessages.get(msgKey)))
					needsBackup = true;
			}
			else {
				needsUpdate = true;
			}
		}

		// some property was missing so we need to update
		if (needsUpdate) {
			log(Level.WARNING, "Your messages.yml file is out of date. It will now be updated.");

			if (needsBackup) {
				log(Level.INFO, "Creating backup of your old messages ...");

				// backup old file
				File backupFile = new File(getDataFolder(), "messages-old-backup.yml");
				if (!messagesFile.renameTo(backupFile)) {
					log(Level.WARNING, "Unable to backup old messages.yml file! Automatic updating failed!");
					return;
				}

				log(Level.INFO, "Old messages have been backed up to " + backupFile.getPath());
			}

			// now write the new default
			writeDefault("messages.yml", "messages.yml", true);
			// reload the fresh data
			messagesData = YamlConfiguration.loadConfiguration(messagesFile);
		}
	}

	public void addActiveDeath(String playerName, DeathContextImpl context) {
		activeDeaths.put(playerName.toLowerCase(), context);
	}

	public DeathContextImpl getActiveDeath(String playerName) {
		return activeDeaths.get(playerName.toLowerCase());
	}

	/**
	 * Removes the {@link DeathContext} from the collection, but doesn't cancel it properly. Calling cancel calls this.
	 */
	public void clearActiveDeath(String playerName) {
		activeDeaths.remove(playerName.toLowerCase());
	}

	public boolean hasPermission(Permissible who, DPermission perm) {
		if (perm == null)
			return true;

		if (who == null)
			return false;
		if (config.usesBukkitPerms())
			return who.hasPermission(perm.node);
		else {
			if (!perm.opOnly)
				return true;
			if (who instanceof ServerOperator)
				return ((ServerOperator) who).isOp();

			log(Level.WARNING, "Could not check permission " + perm.node + " for " + who.toString() + ": unsupported type! Denying access ...");
			return false;
		}
	}

	// logs a message to console
	public void log(String msg) {
		log(Level.INFO, msg);
	}

	public void log(Level lvl, String msg) {
		log(lvl, msg, false);
	}

	public void log(Level lvl, String msg, boolean overrideLevel) {
		if (!overrideLevel && config != null && lvl.intValue() < config.getLoggingLevel())
			return;

		// levels below INFO don't get properly displayed by the minecraft logger
		if (lvl.intValue() < Level.INFO.intValue())
			lvl = Level.INFO;

		String[] lines = msg.split("\n");
		for (String line : lines)
			getLogger().log(lvl, line); // our logger now takes care of prefixes
	}
}
