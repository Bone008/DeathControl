package bone008.bukkit.deathcontrol.commandhandler;

import java.util.Map;
import java.util.TreeMap;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import bone008.bukkit.deathcontrol.DeathPermission;
import bone008.bukkit.deathcontrol.MessageHelper;
import bone008.bukkit.deathcontrol.exceptions.CommandException;

public class CommandHandler implements CommandExecutor {

	public final Map<String, SubCommand> commandMap = new TreeMap<String, SubCommand>();
	public final Map<String, SubCommand> aliasesMap = new TreeMap<String, SubCommand>();
	private String msgOnUndefined = null;
	private DeathPermission basePermissionNode = null;

	public void addSubCommand(String name, SubCommand cmd, String... aliases) {
		if (name == null || name.trim().isEmpty())
			throw new IllegalArgumentException("invalid name");
		commandMap.put(name, cmd);
		for (String alias : aliases)
			aliasesMap.put(alias, cmd);
	}

	/**
	 * Sets the message that should be shown to the player when an undefined command was typed. This can be set to null in order to show the command usage (return false in the onCommand method)
	 */
	public void setMessageOnUndefinedCommand(String msg) {
		msg = (msg == null ? null : msg.trim());
		if (msg.isEmpty())
			msg = null;
		msgOnUndefined = msg;
	}

	/**
	 * Sets the base permission node that should be checked for each command. This is independent from the permissions that the commands themselves specify and is checked additionally.
	 */
	public void setBasePermissionNode(DeathPermission node) {
		basePermissionNode = node;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		boolean success = false;
		if (args.length > 0) {
			String[] subArgs = new String[args.length - 1];
			System.arraycopy(args, 1, subArgs, 0, subArgs.length);

			success = handleCommand(sender, cmd, label, args[0].toLowerCase(), subArgs);
		}

		if (!success) {
			if (msgOnUndefined == null)
				return false;
			sender.sendMessage(msgOnUndefined);
		}

		return true;
	}

	private boolean handleCommand(CommandSender sender, Command mainCmd, String mainLabel, String cmdName, String[] args) {
		SubCommand cmd = commandMap.get(cmdName);
		if (cmd == null)
			cmd = aliasesMap.get(cmdName);

		if (cmd == null) {
			return false;
		} else {
			try {
				cmd.checkPermission(sender, basePermissionNode);
				cmd.checkPermission(sender, cmd.getPermission());
				cmd.execute(new CommandContext(sender, mainCmd, mainLabel, this, args));
			} catch (CommandException e) {
				MessageHelper.sendMessage(sender, e.getMessage(), true);
			}
			return true;
		}
	}
}
