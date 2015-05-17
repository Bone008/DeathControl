package bone008.bukkit.deathcontrol.commands;

import org.bukkit.ChatColor;

import bone008.bukkit.deathcontrol.DeathControl;
import bone008.bukkit.deathcontrol.commandhandler.CommandContext;
import bone008.bukkit.deathcontrol.commandhandler.SubCommand;
import bone008.bukkit.deathcontrol.exceptions.CommandException;
import bone008.bukkit.deathcontrol.util.MessageUtil;

public class ReloadCommand extends SubCommand {

	public ReloadCommand() {
		this.description = "Reloads the config files of the plugin.";
		this.permission = DeathControl.PERMISSION_ADMIN;
	}

	@Override
	public void execute(CommandContext context) throws CommandException {
		DeathControl.instance.loadConfig();
		MessageUtil.sendMessage(context.sender, ChatColor.GREEN + "Reloaded all config files!");
	}

}
