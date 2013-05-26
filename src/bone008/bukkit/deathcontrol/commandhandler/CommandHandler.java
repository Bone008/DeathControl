package bone008.bukkit.deathcontrol.commandhandler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.util.StringUtil;

import bone008.bukkit.deathcontrol.DeathControl;
import bone008.bukkit.deathcontrol.exceptions.CommandException;
import bone008.bukkit.deathcontrol.util.DPermission;
import bone008.bukkit.deathcontrol.util.MessageUtil;

public class CommandHandler implements TabExecutor {

	public final Map<String, SubCommand> commandMap = new TreeMap<String, SubCommand>();
	public final Map<String, SubCommand> aliasesMap = new TreeMap<String, SubCommand>();
	private String msgOnUndefined = null;
	private DPermission basePermissionNode = null;

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
	public void setBasePermissionNode(DPermission node) {
		basePermissionNode = node;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		if (args.length == 1) {
			List<String> matches = new ArrayList<String>();
			String token = args[0].toLowerCase();

			for (Entry<String, SubCommand> cmdEntry : commandMap.entrySet()) {
				if (StringUtil.startsWithIgnoreCase(cmdEntry.getKey(), token) && (cmd.getPermission() == null || DeathControl.instance.hasPermission(sender, cmdEntry.getValue().getPermission()))) {
					matches.add(cmdEntry.getKey());
				}
			}
			for (Entry<String, SubCommand> cmdEntry : aliasesMap.entrySet()) {
				if (StringUtil.startsWithIgnoreCase(cmdEntry.getKey(), token) && (cmd.getPermission() == null || DeathControl.instance.hasPermission(sender, cmdEntry.getValue().getPermission()))) {
					matches.add(cmdEntry.getKey());
				}
			}

			return matches;
		}
		else if (args.length > 1) {
			SubCommand subCmd = getCmdByName(args[0]);
			if (subCmd != null) {
				try {
					return subCmd.tabComplete(new CommandContext(sender, cmd, label, this, getSubArgs(args)));
				} catch (CommandException e) {
					MessageUtil.sendMessage(sender, "Error on tab-completion: " + e.getMessage(), true);
				}
			}
		}

		return Collections.emptyList();
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		boolean success = false;
		if (args.length > 0) {
			success = handleCommand(sender, cmd, label, args[0], getSubArgs(args));
		}

		if (!success) {
			if (msgOnUndefined == null)
				return false;
			sender.sendMessage(msgOnUndefined);
		}

		return true;
	}

	private boolean handleCommand(CommandSender sender, Command mainCmd, String mainLabel, String cmdName, String[] args) {
		SubCommand cmd = getCmdByName(cmdName);

		if (cmd == null) {
			return false;
		}
		else {
			try {
				cmd.checkPermission(sender, basePermissionNode);
				cmd.checkPermission(sender, cmd.getPermission());
				cmd.execute(new CommandContext(sender, mainCmd, mainLabel, this, args));
			} catch (CommandException e) {
				if (e.getTranslatableMessage() == null)
					MessageUtil.sendMessage(sender, e.getMessage());
				else
					MessageUtil.sendMessage(sender, e.getTranslatableMessage());
			}
			return true;
		}
	}

	private SubCommand getCmdByName(String cmdName) {
		SubCommand cmd = commandMap.get(cmdName.toLowerCase());
		if (cmd == null)
			cmd = aliasesMap.get(cmdName);

		return cmd;
	}

	private String[] getSubArgs(String[] args) {
		String[] subArgs = new String[args.length - 1];
		System.arraycopy(args, 1, subArgs, 0, subArgs.length);
		return subArgs;
	}
}
