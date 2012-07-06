package bone008.bukkit.deathcontrol.commands;


import org.bukkit.entity.Player;

import bone008.bukkit.deathcontrol.DeathControl;
import bone008.bukkit.deathcontrol.DeathManager;
import bone008.bukkit.deathcontrol.commandhandler.CommandContext;
import bone008.bukkit.deathcontrol.commandhandler.SubCommand;
import bone008.bukkit.deathcontrol.exceptions.CommandException;

public class BackCommand extends SubCommand {

	public BackCommand() {
		this.description = "Returns saved items to the player.";
	}

	@Override
	public void execute(CommandContext context) throws CommandException {
		Player ply = context.getPlayerSender();
		DeathManager m = DeathControl.instance.getManager(ply.getName());
		if (m != null && m.commandIssued())
			return;
		
		throw new CommandException("You don't have any items to get back!");
	}

}
