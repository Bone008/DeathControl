package bone008.bukkit.deathcontrol.commands;

import org.bukkit.entity.Player;

import bone008.bukkit.deathcontrol.DeathControl;
import bone008.bukkit.deathcontrol.DeathManager;
import bone008.bukkit.deathcontrol.commandhandler.CommandContext;
import bone008.bukkit.deathcontrol.commandhandler.SubCommand;
import bone008.bukkit.deathcontrol.exceptions.CommandException;
import bone008.bukkit.deathcontrol.util.Message;
import bone008.bukkit.deathcontrol.util.MessageHelper;

public class DropCommand extends SubCommand {

	public DropCommand() {
		this.description = "Drops saved items to the ground.";
	}

	@Override
	public void execute(CommandContext context) throws CommandException {
		Player ply = context.getPlayerSender();

		DeathManager m = DeathControl.instance.getManager(ply.getName());
		if (m != null && m.expire(false))
			MessageHelper.sendMessage(ply, Message.CMD_ITEMS_WERE_DROPPED);
		else
			throw new CommandException(Message.CMD_NO_DROPPABLE_ITEMS);
	}

}
