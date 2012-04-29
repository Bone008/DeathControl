package bone008.bukkit.deathcontrol.command;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import bone008.bukkit.deathcontrol.DeathManager;
import bone008.bukkit.deathcontrol.exceptions.CommandException;

public class DropCommand extends SubCommand {

	public DropCommand(CommandManager manager) {
		super(manager);
	}

	@Override
	public void execute(CommandSender sender, Command mainCmd, String mainLabel, String[] args) throws CommandException {

		Player ply = getPlayer(sender);
		DeathManager m = manager.plugin.getManager(ply.getName());
		if (m != null && m.expire(false)){
			manager.plugin.display(ply, "Your items were dropped at your death location.");
			return;
		}
		manager.plugin.display(ply, ChatColor.RED + "You don't have any stored items to drop!");
	}

	@Override
	public String getDescription() {
		return "Drops saved items.";
	}
	

}
