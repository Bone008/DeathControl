package bone008.bukkit.deathcontrol.commands;

import org.bukkit.entity.Player;

import bone008.bukkit.deathcontrol.DeathControl;
import bone008.bukkit.deathcontrol.DeathManager;
import bone008.bukkit.deathcontrol.MessageHelper;
import bone008.bukkit.deathcontrol.commandhandler.CommandContext;
import bone008.bukkit.deathcontrol.commandhandler.SubCommand;
import bone008.bukkit.deathcontrol.exceptions.CommandException;

public class DropCommand extends SubCommand {

	public DropCommand() {
		this.description = "Drops saved items to the ground.";
	}

	@Override
	public void execute(CommandContext context) throws CommandException {
		Player ply = context.getPlayerSender();

		DeathManager m = DeathControl.instance.getManager(ply.getName());
		if (m != null && m.expire(false))
			MessageHelper.sendMessage(ply, "Your items were dropped at your death location.");
		else
			throw new CommandException("You don't have any stored items to drop!");
	}

}
