package bone008.bukkit.deathcontrol.command;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import bone008.bukkit.deathcontrol.DeathManager;
import bone008.bukkit.deathcontrol.exceptions.CommandException;

public class BackCommand extends SubCommand {

	public BackCommand(CommandManager manager) {
		super(manager);
	}

	@Override
	public void execute(CommandSender sender, Command mainCmd, String mainLabel, String[] args) throws CommandException {
		
		Player ply = getPlayer(sender);
		DeathManager m = manager.plugin.managers.get(ply);
		if(m != null && m.commandIssued())
			return;
		ply.sendMessage(ChatColor.RED+"You don't have any items to get back!");
	}

	@Override
	public String getDescription() {
		return "Returns saved items to the player.";
	}

}
