package bone008.bukkit.deathcontrol.config.actions;

import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import bone008.bukkit.deathcontrol.DeathControl;
import bone008.bukkit.deathcontrol.config.ActionAgent;
import bone008.bukkit.deathcontrol.config.ActionDescriptor;
import bone008.bukkit.deathcontrol.config.ActionResult;
import bone008.bukkit.deathcontrol.config.DeathContext;
import bone008.bukkit.deathcontrol.exceptions.DescriptorFormatException;
import bone008.bukkit.deathcontrol.util.Util;

public class CommandAction extends ActionDescriptor {

	private final boolean asConsole;
	private final String commandString;

	public CommandAction(List<String> args) throws DescriptorFormatException {
		if (args.size() < 2)
			throw new DescriptorFormatException("not enough arguments");

		String senderStr = args.remove(0);
		if (senderStr.equalsIgnoreCase("console"))
			asConsole = true;
		else if (senderStr.equalsIgnoreCase("victim"))
			asConsole = false;
		else
			throw new DescriptorFormatException("invalid command sender: only \"victim\" or \"console\" is allowed!");

		commandString = Util.joinCollection(" ", args);
	}

	@Override
	public ActionAgent createAgent(DeathContext context) {
		return new ActionAgent(context, this) {
			@Override
			public void preprocess() {
			}

			@Override
			public ActionResult execute() {
				CommandSender sender;
				if (asConsole)
					sender = Bukkit.getConsoleSender();
				else if (context.getVictim().isOnline())
					sender = context.getVictim().getPlayer();
				else
					return ActionResult.PLAYER_OFFLINE;

				String cmd = context.replaceVariables(commandString);
				context.setVariable("last-command", cmd);

				try {
					boolean result = Bukkit.getServer().dispatchCommand(sender, cmd);
					return (result ? ActionResult.STANDARD : ActionResult.FAILED);
				} catch (org.bukkit.command.CommandException e) {
					// Bukkit throws this when the command handler threw an exception; this should not crash our action flow
					DeathControl.instance.getLogger().log(Level.SEVERE, "Executing the command \"" + cmd + "\" threw an exception!", e);
					return ActionResult.FAILED;
				}
			}

			@Override
			public void cancel() {
			}
		};
	}

	@Override
	public List<String> toParameters() {
		return Arrays.asList(ChatColor.ITALIC + (asConsole ? "console" : "victim") + ChatColor.RESET, commandString);
	}
}
