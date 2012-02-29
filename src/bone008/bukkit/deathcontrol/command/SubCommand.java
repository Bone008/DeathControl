package bone008.bukkit.deathcontrol.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import bone008.bukkit.deathcontrol.DeathPermission;
import bone008.bukkit.deathcontrol.exceptions.CommandException;

public abstract class SubCommand {
	
	protected CommandManager manager;
	
	protected SubCommand(CommandManager manager){
		this.manager = manager;
	}
	
	public abstract void execute(CommandSender sender, Command mainCmd, String mainLabel, String[] args)
			throws CommandException;
	
	/**
	 * Gets a brief description of the command
	 * @return Description or null if there is none
	 */
	public abstract String getDescription();
	/**
	 * Gets the permission required to execute the command
	 * @return The Permission or null if it is free to use
	 */
	public DeathPermission getPermission(){
		return null;
	}

	public Player getPlayer(CommandSender sender) throws CommandException {
		if (sender instanceof Player) {
			return (Player) sender;
		}
		throw new CommandException("You have to be a player to do this!");
	}
	
	public void checkPermission(CommandSender sender, DeathPermission perm) throws CommandException {
		if(perm == null)
			return;
		if(!manager.plugin.hasPermission(sender, perm))
			throw new CommandException("You don't have permission to do that!");
	}
}