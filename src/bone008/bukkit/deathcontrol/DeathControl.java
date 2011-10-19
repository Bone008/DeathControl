package bone008.bukkit.deathcontrol;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.ChatColor;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.Event.Priority;
import org.bukkit.permissions.Permissible;
import org.bukkit.permissions.ServerOperator;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import com.nijiko.permissions.PermissionHandler;
import com.nijikokun.bukkit.Permissions.Permissions;
import com.nijikokun.register.payment.Method;
import com.nijikokun.register.payment.Methods;

import bone008.bukkit.deathcontrol.command.CommandManager;
import bone008.bukkit.deathcontrol.config.DeathConfiguration;
import bone008.bukkit.deathcontrol.config.DeathLists;
import bone008.bukkit.deathcontrol.exceptions.ResourceNotFoundError;


public class DeathControl extends JavaPlugin {
	static final Logger logger = Logger.getLogger("Minecraft");
	
	private final DeathControlEntityListener entityListener = new DeathControlEntityListener(this);
	private final DeathControlPlayerListener playerListener = new DeathControlPlayerListener(this);

	private File configFile = null;
	private File helpFile = null;
	
	public DeathConfiguration config;
	public DeathLists deathLists;
	public PluginDescriptionFile pdfFile;
	String prefix;

	public HashMap<Player, DeathManager> managers = new HashMap<Player, DeathManager>();
	
	public PermissionHandler permissionHandler;
	
	public static final long helpSize = 5981;
	public static final DeathPermission PERMISSION_USE		= new DeathPermission("deathcontrol.use", false);
	public static final DeathPermission PERMISSION_FREE		= new DeathPermission("deathcontrol.free", true);
	public static final DeathPermission PERMISSION_ADMIN	= new DeathPermission("deathcontrol.admin", true);
	
	
	
	@Override
	public void onDisable() {
		if(this.managers.size() > 0){
			for(DeathManager m: managers.values()){
				m.expire(true);
			}
		}
		
		log("Plugin was disabled!");
	}
	
	
	@Override
	public void onEnable() {
		configFile = new File(getDataFolder(), "config.yml");
		helpFile = new File(getDataFolder(), "help.txt");
		pdfFile = getDescription();
		prefix = "["+pdfFile.getName()+"] ";
		
		// load/generate the configurations and setup permissions if needed
		loadConfig();
		
		// register events
		PluginManager pm = getServer().getPluginManager();
		
		pm.registerEvent(Event.Type.ENTITY_DEATH, entityListener, Priority.Normal, this);
		pm.registerEvent(Event.Type.PLAYER_RESPAWN, playerListener, Priority.Normal, this);
		pm.registerEvent(Event.Type.PLAYER_QUIT, playerListener, Priority.Monitor, this);
		
		getCommand("death").setExecutor(new CommandManager(this));
		
		log("version "+pdfFile.getVersion()+" is enabled!");
	}

	


	/**
	 * Loads or reloads the config files and generates the default files if necessary.
	 */
	public void loadConfig(){
		reloadConfig();
		FileConfiguration cfg = getConfig();
		
		boolean forceConfig = false;
		// check for an outdated config.yml
		if(checkConfigUpdate()){
			log(Level.WARNING, "Outdated config file detected! Writing an updated one ...");
			boolean updateSuccess = backupConfig();
			forceConfig = true;
			if(updateSuccess)
				log("Your old config.yml was saved to config.old.yml to copy your settings!");
			else
				log(Level.SEVERE, "Unable to backup your old config.yml! It was overwritten!");
		}
		
		// create the default files
		writeDefault("config.yml", "config.yml", forceConfig);
		writeDefault("lists.txt", "lists.txt", false);
		writeDefault("help.txt", "help.txt", checkHelpUpdate());
		// parse the config & lists files
		deathLists = new DeathLists(this, new File(getDataFolder(), "lists.txt"));
		config = new DeathConfiguration(this, cfg);
		
		if(!config.bukkitPerms)
			setupPermissions();
		
		log( "is now using " + (config.bukkitPerms ? "bukkit permissions" : (permissionHandler==null ? "the OP-system" : "the Permissions plugin") ) +"!" );
	}
	
	
	/**
	 * Writes a resource contained in the jar to a specified destination.
	 * 
	 * @param resourceName The name of the resource in the jar
	 * @param destination The destination path, relative to the plugin's data folder
	 * @param force Specifies if the file should be overwritten when already existing
	 * @return true, if and only if the file was successfully written
	 */
	public boolean writeDefault(String resourceName, String destination, boolean force){
		boolean ret = false;
		File file = new File(getDataFolder(), destination);
		
		if(!force && file.exists())
			return false;
		
		InputStream in = getClass().getResourceAsStream("/resources/"+resourceName);
		if(in == null){
			throw new ResourceNotFoundError(resourceName);
		}
		
		FileOutputStream out = null;
		try{
			
			getDataFolder().mkdirs();
			file.delete();
			file.createNewFile();
			
			out = new FileOutputStream(file);
			byte[] buffer = new byte[8192];
			int remaining = 0;
			while( (remaining = in.read(buffer)) > 0 ) {
				out.write(buffer, 0, remaining);
			}
			
			log("default file "+resourceName+" created/updated!");
			ret = true;
			
		}
		catch(IOException e){
			e.printStackTrace();
		}
		finally{
			try{ if(in != null) in.close(); }
			catch(IOException e){}
			try{ if(out != null) out.close(); }
			catch(IOException e){}
		}
		
		return ret;
	}
	
	
	
	/**
	 * Checks if the help file is outdated by comparing its length with the internal resource.
	 * @return true, if help.txt needs to be updated, otherwise false
	 */
	private boolean checkHelpUpdate(){
		if(!helpFile.exists() || !helpFile.isFile())
			return true;
		return helpFile.length() != helpSize;
	}
	
	/**
	 * Checks if the config file is outdated.
	 * Currently checks for changes implemented in v1.3.<br>
	 * <u><i>Note: Must be called before reading from the config!</i></u>
	 * @return true, if the config needs to be updated, otherwise false
	 */
	private boolean checkConfigUpdate(){
		if(new File(getDataFolder(), "config.yml").exists())
			//return (getConfiguration().getProperty("use-bukkit-permissions") == null || getConfiguration().getProperty("logging-level") == null);
			return !(getConfig().isSet("use-bukkit-permissions") || getConfig().isSet("logging-level"));
		else
			return false;
	}
	
	/**
	 * Backups the current config.yml to config.old.yml
	 * @return if the renaming succeeded
	 */
	private boolean backupConfig(){
		File backup = new File(getDataFolder(), "config.old.yml");
		return configFile.renameTo(backup);
	}
	
	
	public boolean hasPermission(Permissible who, DeathPermission perm){
		if(config.bukkitPerms)
			return who.hasPermission(perm.node);
		else if(permissionHandler == null){
			if(!perm.opOnly)
				return true;
			if(who instanceof ServerOperator){
				return ((ServerOperator)who).isOp();
			}
			else{
				log(Level.WARNING, "Could not check permissions for "+who.toString()+": unsupported type! Denying access ...");
				return false;
			}
		}
		else if(who instanceof Player){
			return permissionHandler.has((Player)who, perm.node);
		}
		else if(who instanceof ConsoleCommandSender){
			return true;
		}
		else{
			log(Level.WARNING, "Could not check permissions for "+who.toString()+": unsupported type! Denying access ...");
			return false;
		}
	}
	
	
	/**
	 * Attempts to get the active Register Method. 
	 * @return The Method, or null if there is no active one or Register is not loaded.
	 */
	public Method getRegisterMethod(){
		try{
			return Methods.getMethod();
		} catch(NoClassDefFoundError err){
		} // ugly solution, I know ...
		return null;
	}
	
	private void setupPermissions(){
		Plugin permissionsPlugin = this.getServer().getPluginManager().getPlugin("Permissions");
		
		if(this.permissionHandler == null){
			if(permissionsPlugin != null){
				this.permissionHandler = ((Permissions) permissionsPlugin).getHandler();
			}
		}
	}
	
	
	
	// displays a message to the player
	public void display(Player ply, String message){
		ply.sendMessage(ChatColor.GRAY+prefix + ChatColor.WHITE+message);
	}
	
	// logs a message to console
	public void log(String msg){
		log(Level.INFO, msg);
	}
	public void log(Level lvl, String msg){
		log(lvl, msg, true);
	}
	public void log(Level lvl, String msg, boolean usePrefix){
		String[] lines = msg.split("\n");
		if(lines.length > 1){
			for(String line: lines)
				log(lvl, line.trim(), usePrefix);
		}
		else
			logger.log(lvl, (usePrefix ? prefix:"") + msg);
	}

	
}
