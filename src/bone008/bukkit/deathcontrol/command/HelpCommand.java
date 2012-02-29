package bone008.bukkit.deathcontrol.command;

import java.util.Map.Entry;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import bone008.bukkit.deathcontrol.exceptions.CommandException;

public class HelpCommand extends SubCommand {

	public HelpCommand(CommandManager manager) {
		super(manager);
	}

	@Override
	public void execute(CommandSender sender, Command mainCmd, String mainLabel, String[] args) throws CommandException {
		sender.sendMessage(ChatColor.GRAY + manager.plugin.pdfFile.getFullName()+" by Bone008");
		for(Entry<String, SubCommand> entry: manager.commandMap.entrySet())
			sender.sendMessage(
					new StringBuilder()
					.append(ChatColor.GRAY)
					.append("| ")
					.append(ChatColor.BLUE)
					.append("/")
					.append(mainLabel)
					.append(" ")
					.append(entry.getKey())
					.append(ChatColor.GRAY)
					.append(" - ")
					.append(ChatColor.WHITE)
					.append(entry.getValue().getDescription())
					.toString()
				);
	}
	
	@Override
	public String getDescription() {
		return "Displays the help menu.";
	}
	
}
