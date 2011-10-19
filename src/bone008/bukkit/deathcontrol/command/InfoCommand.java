package bone008.bukkit.deathcontrol.command;

import java.util.Iterator;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import bone008.bukkit.deathcontrol.DeathCause;
import bone008.bukkit.deathcontrol.DeathControl;
import bone008.bukkit.deathcontrol.DeathPermission;
import bone008.bukkit.deathcontrol.Utilities;
import bone008.bukkit.deathcontrol.config.CauseData.HandlingMethod;
import bone008.bukkit.deathcontrol.config.CauseSettings;
import bone008.bukkit.deathcontrol.config.DeathConfiguration;
import bone008.bukkit.deathcontrol.exceptions.CommandException;

public class InfoCommand extends SubCommand {

	public InfoCommand(CommandManager manager) {
		super(manager);
	}

	@Override
	public void execute(CommandSender sender, Command mainCmd, String mainLabel, String[] args) throws CommandException {

		String pre = ChatColor.GRAY + "| ";

		if (args.length >= 1) {
			if (args[0].equalsIgnoreCase("causes")) {
				sender.sendMessage(ChatColor.GRAY + "Registered death causes (" + ChatColor.YELLOW + manager.plugin.config.handlings.size() + ChatColor.GRAY + "):");
				Iterator<DeathCause> it = manager.plugin.config.handlings.keySet().iterator();
				while (it.hasNext()) {
					sender.sendMessage(pre + ChatColor.WHITE + it.next().toHumanString());
				}
				return;
			}

			else if (args[0].equalsIgnoreCase("cause") && args.length >= 2) {
				DeathCause dc = DeathCause.parseCause(DeathConfiguration.getCauseNameFromValue(args[1]), DeathConfiguration.getCauseMetaFromValue(args[1]));
				CauseSettings settings = manager.plugin.config.getSettings(dc);
				if (settings == null) {
					sender.sendMessage(ChatColor.RED + "There is no registered death cause called " + ChatColor.DARK_RED + args[1]);
					sender.sendMessage(ChatColor.RED + "Use " + ChatColor.BLUE + "/" + mainLabel + " info causes" + ChatColor.RED + " for a list of all causes.");
					return;
				}
				sender.sendMessage(ChatColor.GRAY + "Current settings for "+ChatColor.YELLOW+dc.toHumanString()+ChatColor.GRAY+":");
				sender.sendMessage(pre + ChatColor.WHITE + "keep-inventory: " + ChatColor.YELLOW + settings.keepInventory());
				sender.sendMessage(pre + ChatColor.WHITE + "cost: " + ChatColor.YELLOW + settings.getRawCost());
				sender.sendMessage(pre + ChatColor.WHITE + "method: " + ChatColor.YELLOW + settings.getMethod().toString());
				if(settings.getMethod() == HandlingMethod.COMMAND)
					sender.sendMessage(pre + ChatColor.WHITE + "timeout: " + ChatColor.YELLOW + settings.getTimeout());
				sender.sendMessage(pre + ChatColor.WHITE + "loss-percentage: " + ChatColor.YELLOW + settings.getLoss()+"%");
				sender.sendMessage(pre + ChatColor.WHITE + "whitelist: " + ChatColor.YELLOW + Utilities.joinCollection(", ", settings.getWhitelist()));
				sender.sendMessage(pre + ChatColor.WHITE + "blacklist: " + ChatColor.YELLOW + Utilities.joinCollection(", ", settings.getBlacklist()));
				return;
			}

			else if (args[0].equalsIgnoreCase("lists")) {
				sender.sendMessage(ChatColor.GRAY + "Registered lists (" + ChatColor.YELLOW + manager.plugin.deathLists.getListsAmount() + ChatColor.GRAY + "):");
				Iterator<String> it = manager.plugin.deathLists.getListNames().iterator();
				while (it.hasNext()) {
					sender.sendMessage(pre + ChatColor.WHITE + it.next());
				}
				return;
			}
		}

		// get here when no valid command was captured above
		sender.sendMessage(ChatColor.GRAY + "==== DeathControl configuration ====");
		sender.sendMessage(pre + ChatColor.WHITE + "registered valid death causes: " + ChatColor.YELLOW + manager.plugin.config.handlings.size());
		sender.sendMessage(pre + ChatColor.WHITE + "registered valid lists: " + ChatColor.YELLOW + manager.plugin.deathLists.getListsAmount());

		if (!manager.plugin.config.errors.isEmpty()) {
			String errorsStr;
			if (manager.plugin.config.errors.size() == 1)
				errorsStr = "There is " + ChatColor.YELLOW + 1 + ChatColor.DARK_RED + " error";
			else
				errorsStr = "There are " + ChatColor.YELLOW + manager.plugin.config.errors.size() + ChatColor.DARK_RED + " errors";

			sender.sendMessage(pre + ChatColor.DARK_RED + errorsStr + " in the config:");
			for (String err : manager.plugin.config.errors) {
				sender.sendMessage(pre + pre + ChatColor.RED + err);
			}
		}

		sender.sendMessage(pre + "----------");
		sender.sendMessage(pre + ChatColor.WHITE + "Use " + ChatColor.BLUE + "/" + mainLabel + " info causes" + ChatColor.WHITE + " for details about causes");
		sender.sendMessage(pre + ChatColor.WHITE + "Use " + ChatColor.BLUE + "/" + mainLabel + " info cause <causename>" + ChatColor.WHITE + " for a specific cause");
		sender.sendMessage(pre + ChatColor.WHITE + "Use " + ChatColor.BLUE + "/" + mainLabel + " info lists" + ChatColor.WHITE + " for details about lists");
		sender.sendMessage(pre + ChatColor.WHITE + "Use " + ChatColor.BLUE + "/" + mainLabel + " info list <listname>" + ChatColor.WHITE + " for a specific cause");

	}

	@Override
	public String getDescription() {
		return "Displays information about the config.";
	}

	@Override
	public DeathPermission getPermission() {
		return DeathControl.PERMISSION_ADMIN;
	}

}
