package bone008.bukkit.deathcontrol.commands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.bukkit.ChatColor;
import org.bukkit.util.StringUtil;

import bone008.bukkit.deathcontrol.DeathControl;
import bone008.bukkit.deathcontrol.commandhandler.CommandContext;
import bone008.bukkit.deathcontrol.commandhandler.SubCommand;
import bone008.bukkit.deathcontrol.config.ActionDescriptor;
import bone008.bukkit.deathcontrol.config.ConditionDescriptor;
import bone008.bukkit.deathcontrol.config.HandlingDescriptor;
import bone008.bukkit.deathcontrol.config.lists.ListItem;
import bone008.bukkit.deathcontrol.exceptions.CommandException;
import bone008.bukkit.deathcontrol.util.MessageUtil;
import bone008.bukkit.deathcontrol.util.Util;

public class ConfigCommand extends SubCommand {

	private static final List<String> PARAM_OPTIONS = Arrays.asList("handling", "list", "conditions", "actions");

	public ConfigCommand() {
		this.description = "Displays various information about the config.";
		this.usage = "config [handling|list] [name]";
		this.permission = DeathControl.PERMISSION_INFO;
	}

	@Override
	public List<String> tabComplete(CommandContext context) throws CommandException {
		switch (context.argsCount()) {
		case 1:
			return StringUtil.copyPartialMatches(context.getStringArg(0), PARAM_OPTIONS, new ArrayList<String>());
		case 2:
			String arg0 = context.getStringArg(0);
			if (arg0.equalsIgnoreCase("handling"))
				return DeathControl.instance.config.getPartialHandlingNames(context.getStringArg(1), new ArrayList<String>());
			if (arg0.equalsIgnoreCase("list"))
				return StringUtil.copyPartialMatches(context.getStringArg(0), DeathControl.instance.itemLists.getListNames(), new ArrayList<String>());

			// no break, fall-through
		default:
			// nothing valid to auto-complete
			return super.tabComplete(context);
		}
	}

	@Override
	public void execute(CommandContext context) throws CommandException {
		final String prefix = ChatColor.GRAY + "> " + ChatColor.RESET;

		if (context.argsCount() == 2) {
			String rawName = context.getStringArg(1);

			if (context.getStringArg(0).equalsIgnoreCase("handling")) {
				HandlingDescriptor handling = DeathControl.instance.config.getHandling(rawName);
				if (handling == null)
					throw new CommandException("The handling \"" + rawName + "\" was not found!");

				List<ConditionDescriptor> conditions = handling.getConditions();
				List<ActionDescriptor> actions = handling.getActions();

				MessageUtil.sendMessage(context.sender, "=== Handling info: " + ChatColor.YELLOW + handling.getName() + ChatColor.RESET + " ===");

				MessageUtil.sendMessage(context.sender, "Priority: " + ChatColor.YELLOW + handling.getPriority(), prefix);
				MessageUtil.sendMessage(context.sender, "Allows other handlings: " + ChatColor.YELLOW + !handling.isLastHandling(), prefix);
				MessageUtil.sendMessage(context.sender, "Cancel message: \"" + Util.replaceValue(handling.getCancelMessage(), null, "none") + ChatColor.RESET + "\"", prefix);
				MessageUtil.sendMessage(context.sender, "Conditions (" + conditions.size() + "):", prefix);
				int i = 0;
				for (Iterator<ConditionDescriptor> cit = conditions.iterator(); cit.hasNext(); i++) {
					ConditionDescriptor c = cit.next();
					MessageUtil.sendMessage(context.sender, (handling.isInverted(i) ? "not " : "") + ChatColor.YELLOW + c.getName() + ChatColor.RESET + " " + Util.joinCollection(" ", c.toParameters()), prefix + "  - ");
				}
				if (conditions.isEmpty())
					MessageUtil.sendMessage(context.sender, ChatColor.ITALIC + "none", prefix + "  ");
				MessageUtil.sendMessage(context.sender, "Actions (" + actions.size() + "):", prefix);
				for (ActionDescriptor a : actions)
					MessageUtil.sendMessage(context.sender, (a.isRequired() ? "required " : "") + ChatColor.YELLOW + a.getName() + ChatColor.RESET + " " + Util.joinCollection(" ", a.toParameters()), prefix + "  - ");
				if (actions.isEmpty())
					MessageUtil.sendMessage(context.sender, ChatColor.ITALIC + "none", prefix + "  ");

				MessageUtil.sendMessage(context.sender, "======================");
				return;
			}



			else if (context.getStringArg(0).equalsIgnoreCase("list")) {
				List<ListItem> list = DeathControl.instance.itemLists.getList(rawName);
				if (list == null)
					throw new CommandException("The list \"" + rawName + "\" was not found!");

				list = new ArrayList<ListItem>(list);
				Collections.sort(list, ListItem.getComparator());

				StringBuilder sb = new StringBuilder();
				for (ListItem item : list) {
					CharSequence str = item.toHumanString();

					if (sb.length() > 0) {
						sb.append(", ");
					}

					sb.append(str).append(ChatColor.RESET);
				}

				MessageUtil.sendMessage(context.sender, "=== List info: " + ChatColor.YELLOW + rawName + ChatColor.RESET + ChatColor.ITALIC + " (" + Util.pluralNum(list.size(), "entry", "entries") + ") " + ChatColor.RESET + "===");
				MessageUtil.sendMessage(context.sender, sb, prefix);
				MessageUtil.sendMessage(context.sender, "======================");
				return;
			}


		}



		else if (context.argsCount() == 1) {
			if (context.getStringArg(0).equals("conditions")) {
				MessageUtil.sendMessage(context.sender, "===== Available conditions =====");
				MessageUtil.sendMessage(context.sender, Util.joinCollection(", ", ConditionDescriptor.getDescriptorNames()), prefix);
				MessageUtil.sendMessage(context.sender, "======================");
				return;
			}

			else if (context.getStringArg(0).equals("actions")) {
				MessageUtil.sendMessage(context.sender, "===== Available actions =====");
				MessageUtil.sendMessage(context.sender, Util.joinCollection(", ", ActionDescriptor.getDescriptorNames()), prefix);
				MessageUtil.sendMessage(context.sender, "======================");
				return;
			}
		}

		MessageUtil.sendMessage(context.sender, "===== Configuration summary =====");

		Set<HandlingDescriptor> handlings = DeathControl.instance.config.getHandlings();
		Set<String> listNames = DeathControl.instance.itemLists.getListNames();

		MessageUtil.sendMessage(context.sender, "Permissions are " + ChatColor.YELLOW + (DeathControl.instance.config.usesBukkitPerms() ? "enabled" : "disabled") + ChatColor.RESET + ".", prefix);
		MessageUtil.sendMessage(context.sender, "Cross-world respawns are " + ChatColor.YELLOW + (DeathControl.instance.config.allowsCrossworld() ? "allowed" : "not allowed") + ChatColor.RESET + ".", prefix);
		if (DeathControl.instance.config.getBlacklistedWorlds().isEmpty())
			MessageUtil.sendMessage(context.sender, "No worlds are blacklisted.", prefix);
		else
			MessageUtil.sendMessage(context.sender, "Blacklisted worlds: " + ChatColor.YELLOW + Util.joinCollection(", ", DeathControl.instance.config.getBlacklistedWorlds()) + ChatColor.RESET + ".", prefix);

		MessageUtil.sendMessage(context.sender, ChatColor.GRAY + "-------------------", prefix);
		MessageUtil.sendMessage(context.sender, ChatColor.UNDERLINE + "Loaded handlings (" + handlings.size() + ")" + ChatColor.RESET + ": " + ChatColor.YELLOW + Util.joinCollection(", ", handlings), prefix);
		MessageUtil.sendMessage(context.sender, ChatColor.UNDERLINE + "Loaded item lists (" + listNames.size() + ")" + ChatColor.RESET + ": " + ChatColor.YELLOW + Util.joinCollection(", ", listNames), prefix);

		MessageUtil.sendMessage(context.sender, ChatColor.GRAY + "-------------------", prefix);
		MessageUtil.sendMessage(context.sender, ChatColor.BLUE + "/dc config handling <name>" + ChatColor.RESET + ChatColor.ITALIC + " for information on a handling!", prefix);
		MessageUtil.sendMessage(context.sender, ChatColor.BLUE + "/dc config list <name>" + ChatColor.RESET + ChatColor.ITALIC + " for information on an item list!", prefix);
		MessageUtil.sendMessage(context.sender, ChatColor.BLUE + "/dc config conditions" + ChatColor.RESET + ChatColor.ITALIC + " for a list of all conditions!", prefix);
		MessageUtil.sendMessage(context.sender, ChatColor.BLUE + "/dc config actions" + ChatColor.RESET + ChatColor.ITALIC + " for a list of all actions!", prefix);
		MessageUtil.sendMessage(context.sender, "======================");
	}
}
