package bone008.bukkit.deathcontrol.config.actions;

import java.util.List;
import java.util.logging.Level;

import org.bukkit.Bukkit;
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

		asConsole = args.remove(0).equalsIgnoreCase("console");
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
				else
					sender = context.getVictim();

				try {
					boolean result = Bukkit.getServer().dispatchCommand(sender, commandString);
					return (result ? ActionResult.STANDARD : ActionResult.FAILED);
				} catch (org.bukkit.command.CommandException e) {
					// Bukkit throws this when the command handler threw an exception; this should not crash our action flow
					DeathControl.instance.getLogger().log(Level.SEVERE, "Executing the command \"" + commandString + "\" threw an exception!", e);
					return ActionResult.FAILED;
				}
			}

			@Override
			public void cancel() {
			}
		};
	}
}
