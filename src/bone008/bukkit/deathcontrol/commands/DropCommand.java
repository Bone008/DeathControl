package bone008.bukkit.deathcontrol.commands;

import org.bukkit.entity.Player;

import bone008.bukkit.deathcontrol.DeathCause;
import bone008.bukkit.deathcontrol.DeathControl;
import bone008.bukkit.deathcontrol.DeathManager;
import bone008.bukkit.deathcontrol.Message;
import bone008.bukkit.deathcontrol.MessageHelper;
import bone008.bukkit.deathcontrol.commandhandler.CommandContext;
import bone008.bukkit.deathcontrol.commandhandler.SubCommand;
import bone008.bukkit.deathcontrol.config.CauseSettings;
import bone008.bukkit.deathcontrol.exceptions.CommandException;

public class DropCommand extends SubCommand {

	public DropCommand() {
		this.description = "Drops saved items to the ground.";
	}

	@Override
	public void execute(CommandContext context) throws CommandException {
		Player ply = context.getPlayerSender();

		CauseSettings settings = DeathControl.instance.config.getSettings(DeathCause.valueOf(context.getStringArg(0)));
		ply.sendMessage("valid: " + settings.isValidItem(ply.getItemInHand()));

		if (Boolean.TRUE)
			return;

		DeathManager m = DeathControl.instance.getManager(ply.getName());
		if (m != null && m.expire(false))
			MessageHelper.sendMessage(ply, Message.CMD_ITEMS_WERE_DROPPED);
		else
			throw new CommandException(Message.CMD_NO_DROPPABLE_ITEMS);
	}

}
