package bone008.bukkit.deathcontrol.commandhandler;

import java.util.Collections;
import java.util.List;

import org.bukkit.command.CommandSender;

import bone008.bukkit.deathcontrol.DeathControl;
import bone008.bukkit.deathcontrol.exceptions.CommandException;
import bone008.bukkit.deathcontrol.util.DPermission;
import bone008.bukkit.deathcontrol.util.Message;

public abstract class SubCommand {

	/**
	 * A brief description of the command.
	 */
	protected String description = null;

	/**
	 * The usage syntax for the command, including its own name.
	 * <p />
	 * Example:
	 * 
	 * <pre>
	 * expand &lt;dominion-name&gt; &lt;distance&gt; [direction]
	 * ----
	 * &lt;parameter&gt; --&gt; mandatory argument
	 * [parameter] --&gt; optional argument
	 * </pre>
	 */
	protected String usage = null;

	/**
	 * The permission node that is required for the command to execute. This is checked automatically.
	 */
	protected DPermission permission = null;

	protected SubCommand() {
	}

	/**
	 * Called when the sub-command is executed and the required permissions are there.
	 * 
	 * @param context The CommandContext associated with the command
	 * @throws CommandException if an error occurred that should be automatically shown to the player
	 */
	public abstract void execute(CommandContext context) throws CommandException;

	/**
	 * Called when any argument of the sub-command needs to be auto-completed. The default method in SubCommand returns an empty List.
	 * 
	 * @param context The current CommandContext of this operation.
	 * 
	 * @return A List of possible options to return, or null if the default handling should be passed.
	 * @throws CommandException if an error occurred that should be automatically shown to the player
	 */
	public List<String> tabComplete(CommandContext context) throws CommandException {
		return Collections.emptyList();
	}

	/**
	 * Checks if the given CommandSender has the given permission node and throws a CommandException if not. <b>Note:</b> This is automatically done before each sub-command is executed.
	 * 
	 * @param sender The CommandSender to check the permission for
	 * @param perm The permission node to check
	 * @throws CommandException if {@code sender} does not have {@code perm}
	 */
	public final void checkPermission(CommandSender sender, DPermission perm) throws CommandException {
		if (perm == null)
			return;
		if (!DeathControl.instance.hasPermission(sender, perm))
			throw new CommandException(Message.CMDCONTEXT_NO_PERMISSION);
	}

	public final String getUsage() {
		return usage;
	}

	public final String getDescription() {
		return description;
	}

	public final DPermission getPermission() {
		return permission;
	}
}
