package bone008.bukkit.deathcontrol.command;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import bone008.bukkit.deathcontrol.DeathControl;
import bone008.bukkit.deathcontrol.DeathPermission;
import bone008.bukkit.deathcontrol.exceptions.CommandException;

public class ReloadCommand extends SubCommand {

	public ReloadCommand(CommandManager manager) {
		super(manager);
	}

	@Override
	public void execute(CommandSender sender, Command mainCmd, String mainLabel, String[] args)
	throws CommandException {
		
		checkPermission(sender, DeathControl.PERMISSION_ADMIN);
		manager.plugin.loadConfig();
		sender.sendMessage(ChatColor.GREEN + "Reloaded config files!");
	}

	@Override
	public String getDescription() {
		return "Reloads the config files of the plugin.";
	}

	@Override
	public DeathPermission getPermission() {
		return DeathControl.PERMISSION_ADMIN;
	}

}
