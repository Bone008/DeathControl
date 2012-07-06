package bone008.bukkit.deathcontrol.commands;

import org.bukkit.ChatColor;

import bone008.bukkit.deathcontrol.DeathControl;
import bone008.bukkit.deathcontrol.MessageHelper;
import bone008.bukkit.deathcontrol.commandhandler.CommandContext;
import bone008.bukkit.deathcontrol.commandhandler.SubCommand;
import bone008.bukkit.deathcontrol.exceptions.CommandException;

public class ReloadCommand extends SubCommand {

	public ReloadCommand() {
		this.description = "Reloads the config files of the plugin.";
		this.permission = DeathControl.PERMISSION_ADMIN;
	}
	
	@Override
	public void execute(CommandContext context) throws CommandException {
		DeathControl.instance.loadConfig();
		MessageHelper.sendMessage(context.sender, ChatColor.GREEN + "Reloaded config files!");
	}

}
