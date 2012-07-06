package bone008.bukkit.deathcontrol;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.logging.Level;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permissible;
import org.bukkit.permissions.ServerOperator;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import bone008.bukkit.deathcontrol.commandhandler.CommandHandler;
import bone008.bukkit.deathcontrol.commands.BackCommand;
import bone008.bukkit.deathcontrol.commands.DropCommand;
import bone008.bukkit.deathcontrol.commands.HelpCommand;
import bone008.bukkit.deathcontrol.commands.InfoCommand;
import bone008.bukkit.deathcontrol.commands.ReloadCommand;
import bone008.bukkit.deathcontrol.config.DeathConfiguration;
import bone008.bukkit.deathcontrol.config.DeathLists;
import bone008.bukkit.deathcontrol.exceptions.ResourceNotFoundError;

public class DeathControl extends JavaPlugin {
	public static final long HELP_SIZE = 227;
	public static final DeathPermission PERMISSION_USE = new DeathPermission("deathcontrol.use", false);
	public static final DeathPermission PERMISSION_FREE = new DeathPermission("deathcontrol.free", true);
	public static final DeathPermission PERMISSION_CROSSWORLD = new DeathPermission("deathcontrol.crossworld", true);
	public static final DeathPermission PERMISSION_NOLIMITS = new DeathPermission("deathcontrol.nolimits", true);
	public static final DeathPermission PERMISSION_INFO = new DeathPermission("deathcontrol.info", true);
	public static final DeathPermission PERMISSION_ADMIN = new DeathPermission("deathcontrol.admin", true);
	
	public static DeathControl instance;

	private final BukkitDeathHandler evtDeathHandler = new BukkitDeathHandler();
	private final BukkitReconnectHandler evtReconnectHandler = new BukkitReconnectHandler();

	private File helpFile = null;

	public DeathConfiguration config;
	public DeathLists deathLists;
	public PluginDescriptionFile pdfFile;
	private String prefix;

	private HashMap<String, DeathManager> managers = new HashMap<String, DeathManager>();

	public DeathControl() {
		instance = this;
	}

	@Override
	public void onDisable() {
		if (this.managers.size() > 0) {
			for (DeathManager m : managers.values()) {
				m.expire(true);
			}
		}
		log(Level.INFO, "is disabled!", true);
	}

	@Override
	public void onEnable() {
		helpFile = new File(getDataFolder(), "help.txt");
		pdfFile = getDescription();
		prefix = "[" + pdfFile.getName() + "] ";

		// load/generate the configurations and setup permissions if needed
		loadConfig();

		// register events
		PluginManager pm = getServer().getPluginManager();
		pm.registerEvents(evtDeathHandler, this);
		pm.registerEvents(evtReconnectHandler, this);

		// setup commands
		CommandHandler deathCmd = new CommandHandler();

		deathCmd.addSubCommand("help", new HelpCommand(), "?");
		deathCmd.addSubCommand("back", new BackCommand(), "restore");
		deathCmd.addSubCommand("drop", new DropCommand(), "expire");
		deathCmd.addSubCommand("reload", new ReloadCommand());
		deathCmd.addSubCommand("info", new InfoCommand(), "status");
		
		getCommand("death").setExecutor(deathCmd);

		// setup economy
		EconomyUtils.init();
	}

	/**
	 * Loads or reloads the config files and generates the default files if necessary.
	 */
	public void loadConfig() {
		// create the default files
		writeDefault("config.yml", "config.yml", false); // only write the default if no file exists
		writeDefault("lists.txt", "lists.txt", false);

		// now load the config, otherwise it would be created before the exists check
		reloadConfig();
		FileConfiguration cfg = getConfig();
		cfg.options().copyDefaults(true);
		cfg.options().copyHeader(true);

		// only update the help file if there currently is one, as it is deprecated.
		if (helpFile.exists() && helpFile.isFile()) {
			writeDefault("help.txt", "help.txt", checkHelpUpdate());
		}

		// parse the config & lists files
		deathLists = new DeathLists(this, new File(getDataFolder(), "lists.txt"));
		config = new DeathConfiguration(this, cfg);
		saveConfig();

		log(Level.CONFIG, "is now using " + (config.bukkitPerms ? "bukkit permissions" : "the OP-system") + "!");
	}

	/**
	 * Writes a resource contained in the jar to a specified destination.
	 * 
	 * @param resourceName
	 *            The name of the resource in the jar
	 * @param destination
	 *            The destination path, relative to the plugin's data folder
	 * @param force
	 *            Specifies if the file should be overwritten when already existing
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
	 * Checks if the help file is outdated by comparing its length with the internal resource.
	 * 
	 * @return true, if help.txt needs to be updated, otherwise false
	 */
	private boolean checkHelpUpdate() {
		if (!helpFile.exists() || !helpFile.isFile())
			return true;
		return helpFile.length() != HELP_SIZE;
	}

	public void addManager(String name, DeathManager m) {
		managers.put(name, m);
	}

	public DeathManager getManager(String name) {
		return managers.get(name);
	}

	public void removeManager(String name) {
		managers.remove(name);
	}

	public void expireManager(String name) {
		DeathManager m = managers.get(name);
		if (m != null) {
			m.expire(true);
			removeManager(name);
		}
	}

	public boolean hasPermission(Permissible who, DeathPermission perm) {
		if(perm == null)
			return true;
		
		if(who == null)
			return false;
		if (config.bukkitPerms)
			return who.hasPermission(perm.node);
		else {
			if (!perm.opOnly)
				return true;
			if (who instanceof ServerOperator) {
				return ((ServerOperator) who).isOp();
			} else {
				log(Level.WARNING, "Could not check permission " + perm.node + " for " + who.toString() + ": unsupported type! Denying access ...");
				return false;
			}
		}
	}

	// displays a message to the player
	public void display(Player ply, String message) {
		if(ply == null)
			return;
		ply.sendMessage(ChatColor.GRAY + prefix + ChatColor.WHITE + message);
	}

	// logs a message to console
	public void log(String msg) {
		log(Level.INFO, msg);
	}

	public void log(Level lvl, String msg) {
		log(lvl, msg, false);
	}

	public void log(Level lvl, String msg, boolean overrideLevel) {
		if (!overrideLevel && lvl.intValue() < config.loggingLevel)
			return;
		
		// levels below INFO don't get properly displayed by the minecraft logger
		if(lvl.intValue() < Level.INFO.intValue())
			lvl = Level.INFO;
		
		String[] lines = msg.split("\n");
		for (String line : lines)
			getLogger().log(lvl, line); // our logger now takes care of prefixes
	}

}
