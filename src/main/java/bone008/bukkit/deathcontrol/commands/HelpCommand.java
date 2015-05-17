package bone008.bukkit.deathcontrol.commands;

import java.util.Map.Entry;

import org.bukkit.ChatColor;

import bone008.bukkit.deathcontrol.DeathControl;
import bone008.bukkit.deathcontrol.commandhandler.CommandContext;
import bone008.bukkit.deathcontrol.commandhandler.SubCommand;
import bone008.bukkit.deathcontrol.exceptions.CommandException;
import bone008.bukkit.deathcontrol.util.MessageUtil;
import bone008.bukkit.deathcontrol.util.Util;

import static org.bukkit.ChatColor.*;

public class HelpCommand extends SubCommand {

	public HelpCommand() {
		this.description = "Displays the help menu.";
	}

	@Override
	public void execute(CommandContext context) throws CommandException {
		// TODO externalize help command
		MessageUtil.sendMessage(context.sender, GRAY + DeathControl.instance.pdfFile.getFullName() + " by Bone008", null);

		String subCmdPrefix = ChatColor.BLUE + "/" + context.mainLabel + " ";

		for (Entry<String, SubCommand> cmdEntry : context.cmdHandler.commandMap.entrySet()) {
			SubCommand cmd = cmdEntry.getValue();
			String cmdName = cmdEntry.getKey();

			if (cmd.getPermission() != null && !DeathControl.instance.hasPermission(context.sender, cmd.getPermission()))
				continue;

			StringBuilder sb = new StringBuilder();

			if (cmd.getUsage() == null)
				sb.append(subCmdPrefix).append(cmdName);
			else
				sb.append(Util.wrapPrefixed(cmd.getUsage(), subCmdPrefix));

			if (cmd.getDescription() != null) {
				sb.append('\n');
				sb.append(Util.wrapPrefixed(cmd.getDescription(), "   " + ChatColor.GRAY));
			}

			MessageUtil.sendMessage(context.sender, sb.toString(), "> ");
			/*
			 * sender.sendMessage( new StringBuilder() .append(ChatColor.GRAY) .append("| ") .append(ChatColor.BLUE) .append("/") .append(mainLabel) .append(" ") .append(entry.getKey()) .append(ChatColor.GRAY) .append(" - ") .append(ChatColor.WHITE) .append(entry.getValue().getDescription()) .toString() );
			 */
		}
	}

}
