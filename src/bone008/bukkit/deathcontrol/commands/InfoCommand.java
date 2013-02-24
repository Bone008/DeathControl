package bone008.bukkit.deathcontrol.commands;

import static org.bukkit.ChatColor.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.bukkit.util.StringUtil;

import bone008.bukkit.deathcontrol.DeathCause;
import bone008.bukkit.deathcontrol.DeathControl;
import bone008.bukkit.deathcontrol.MessageHelper;
import bone008.bukkit.deathcontrol.Utilities;
import bone008.bukkit.deathcontrol.commandhandler.CommandContext;
import bone008.bukkit.deathcontrol.commandhandler.SubCommand;
import bone008.bukkit.deathcontrol.config.CauseData.HandlingMethod;
import bone008.bukkit.deathcontrol.config.CauseSettings;
import bone008.bukkit.deathcontrol.config.DeathConfiguration;
import bone008.bukkit.deathcontrol.config.lists.ListItem;
import bone008.bukkit.deathcontrol.exceptions.CommandException;

public class InfoCommand extends SubCommand {

	private static final List<String> COMPLETE_OPTIONS = Arrays.asList("cause", "causes", "list", "lists");

	public InfoCommand() {
		this.description = "Displays various information about the config.";
		this.usage = "info [causes|lists]\ninfo cause <cause-name>\ninfo list <list-name>";
		this.permission = DeathControl.PERMISSION_INFO;
	}

	@Override
	public List<String> tabComplete(CommandContext context) throws CommandException {
		List<String> matches = new ArrayList<String>();

		if (context.argsCount() < 1)
			return matches;

		String option = context.getStringArg(0);

		if (context.argsCount() == 1) {
			return StringUtil.copyPartialMatches(context.getStringArg(0), COMPLETE_OPTIONS, matches);
		}

		else if (option.equalsIgnoreCase("cause")) {
			String userCause = context.getStringArg(1);

			for (DeathCause dc : DeathControl.instance.config.handlings.keySet()) {
				if (StringUtil.startsWithIgnoreCase(dc.toHumanString(), userCause))
					matches.add(dc.toHumanString());
			}

			return matches;
		}

		else if (option.equalsIgnoreCase("list")) {
			String userList = context.getStringArg(1);

			return StringUtil.copyPartialMatches(userList, DeathControl.instance.deathLists.getListNames(), matches);
		}

		return matches; // empty list
	}

	@Override
	public void execute(CommandContext context) throws CommandException {
		final String pre = GRAY + "| ";

		if (context.argsCount() >= 1) {
			String option = context.getStringArg(0);
			if (option.equalsIgnoreCase("causes")) {
				context.sender.sendMessage(GRAY + "Registered death causes (" + YELLOW + DeathControl.instance.config.handlings.size() + GRAY + "):");

				Iterator<DeathCause> it = DeathControl.instance.config.handlings.keySet().iterator();
				while (it.hasNext()) {
					context.sender.sendMessage(pre + RESET + it.next().toHumanString());
				}
				return;
			}

			else if (option.equalsIgnoreCase("cause")) {
				String causeName = context.getStringArg(1);

				DeathCause dc = DeathCause.parseCause(DeathConfiguration.getCauseNameFromValue(causeName), DeathConfiguration.getCauseMetaFromValue(causeName));

				CauseSettings settings = DeathControl.instance.config.getSettings(dc);
				if (settings == null) {
					// it's prettier not to throw a CommandException here
					MessageHelper.sendMessage(context.sender, "There is no registered death cause called " + ITALIC + causeName, true);
					MessageHelper.sendMessage(context.sender, BLUE + "/" + context.mainLabel + " info causes" + RED + " lists all registered causes.", true);
					return;
				}
				context.sender.sendMessage(GRAY + "Current settings for " + YELLOW + dc.toHumanString() + GRAY + ":");
				context.sender.sendMessage(pre + RESET + "keep-inventory: " + YELLOW + settings.keepInventory());
				context.sender.sendMessage(pre + RESET + "keep-experience: " + YELLOW + settings.keepExperience());
				context.sender.sendMessage(pre + RESET + "cost: " + YELLOW + settings.getRawCost());
				context.sender.sendMessage(pre + RESET + "method: " + YELLOW + settings.getMethod().toString());
				if (settings.getMethod() == HandlingMethod.COMMAND)
					context.sender.sendMessage(pre + RESET + "timeout: " + YELLOW + settings.getTimeout());
				context.sender.sendMessage(pre + RESET + "timeout-on-quit: " + YELLOW + settings.getTimeoutOnQuit());
				context.sender.sendMessage(pre + RESET + "loss-percentage: " + YELLOW + settings.getLoss() + "%");
				context.sender.sendMessage(pre + RESET + "loss-percentage-experience: " + YELLOW + settings.getLossExp() + "%");
				context.sender.sendMessage(pre + RESET + "whitelist: " + YELLOW + Utilities.replaceValue(Utilities.joinCollection(", ", settings.getRawWhitelist()), "", "none"));
				context.sender.sendMessage(pre + RESET + "blacklist: " + YELLOW + Utilities.replaceValue(Utilities.joinCollection(", ", settings.getRawBlacklist()), "", "none"));
				return;
			}

			else if (option.equalsIgnoreCase("lists")) {
				context.sender.sendMessage(GRAY + "Registered lists (" + YELLOW + DeathControl.instance.deathLists.getListsAmount() + GRAY + "):");
				Iterator<String> it = DeathControl.instance.deathLists.getListNames().iterator();
				while (it.hasNext()) {
					context.sender.sendMessage(pre + RESET + it.next());
				}
				return;
			}

			else if (option.equalsIgnoreCase("list")) {
				String listName = context.getStringArg(1).toLowerCase();

				List<ListItem> list = DeathControl.instance.deathLists.getList(listName);
				if (list == null) {
					// it's prettier not to throw a CommandException here
					MessageHelper.sendMessage(context.sender, "There is no list called " + ITALIC + listName, true);
					MessageHelper.sendMessage(context.sender, BLUE + "/" + context.mainLabel + " info lists" + RED + " lists all registered lists.", true);
					return;
				}

				Collections.sort(list, ListItem.getComparator());

				context.sender.sendMessage(GRAY + "Current entries for list " + YELLOW + listName + GRAY + ":");
				StringBuilder sb = new StringBuilder();
				for (ListItem item : list) {
					if (sb.length() > 0) {
						sb.append(", ");
					}
					sb.append(item.toHumanString()).append(RESET);
				}

				MessageHelper.sendMessage(context.sender, sb, pre + RESET);
				context.sender.sendMessage(GRAY + "==========================");
				return;
			}
		}

		// get here when no valid command was captured above
		context.sender.sendMessage(GRAY + "==== DeathControl configuration ====");
		context.sender.sendMessage(pre + RESET + "registered valid death causes: " + YELLOW + DeathControl.instance.config.handlings.size());
		context.sender.sendMessage(pre + RESET + "registered valid lists: " + YELLOW + DeathControl.instance.deathLists.getListsAmount());

		List<String> cfgErrors = DeathControl.instance.config.errors;

		if (!cfgErrors.isEmpty()) {
			String errorsStr;
			if (cfgErrors.size() == 1)
				errorsStr = "There is " + YELLOW + 1 + DARK_RED + " error";
			else
				errorsStr = "There are " + YELLOW + cfgErrors.size() + DARK_RED + " errors";

			context.sender.sendMessage(pre + DARK_RED + errorsStr + " in the config:");
			for (String err : cfgErrors) {
				context.sender.sendMessage(pre + pre + RED + err);
			}
		}

		context.sender.sendMessage(pre + "----------");
		context.sender.sendMessage(pre + RESET + "Use " + BLUE + "/" + context.mainLabel + " info causes" + RESET + " for details about causes");
		context.sender.sendMessage(pre + RESET + "Use " + BLUE + "/" + context.mainLabel + " info cause <causename>" + RESET + " for a specific cause");
		context.sender.sendMessage(pre + RESET + "Use " + BLUE + "/" + context.mainLabel + " info lists" + RESET + " for details about lists");
		context.sender.sendMessage(pre + RESET + "Use " + BLUE + "/" + context.mainLabel + " info list <listname>" + RESET + " for a specific list");

	}

}
