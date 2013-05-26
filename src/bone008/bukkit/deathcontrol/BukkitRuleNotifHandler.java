package bone008.bukkit.deathcontrol;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scheduler.BukkitRunnable;

import bone008.bukkit.deathcontrol.util.MessageHelper;

public class BukkitRuleNotifHandler implements Listener {

	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerJoin(PlayerJoinEvent event) {
		Player player = event.getPlayer();

		if (player.isOp() || DeathControl.instance.hasPermission(player, DeathControl.PERMISSION_ADMIN)) {
			for (World w : Bukkit.getWorlds()) {
				if (isProblematicRuleEnabled(w))
					warn(player, w.getName());
			}
		}
	}

	public static void warnAll() {
		new BukkitRunnable() {
			@Override
			public void run() {
				for (World w : Bukkit.getWorlds()) {
					if (isProblematicRuleEnabled(w)) {
						warn(Bukkit.getConsoleSender(), w.getName());

						for (Player ply : Bukkit.getOnlinePlayers())
							if (ply.isOp() || DeathControl.instance.hasPermission(ply, DeathControl.PERMISSION_ADMIN))
								warn(ply, w.getName());
					}
				}
			}
		}.runTask(DeathControl.instance);
	}

	/**
	 * Warns someone that the "keepInventory" gamerule is enabled.
	 */
	private static void warn(CommandSender who, String worldName) {
		final String prefix = ChatColor.BOLD.toString() + ChatColor.RED + "> " + ChatColor.RESET;
		MessageHelper.sendMessage(who, "====== WARNING ======", true);
		MessageHelper.sendMessage(who, "The gamerule \"keepInventory\" is enabled in world \"" + worldName + "\"!", prefix);
		MessageHelper.sendMessage(who, "This breaks DeathControl by overwriting its functionality.", prefix);
		MessageHelper.sendMessage(who, "Please disable the rule with the following command:\n    " + ChatColor.AQUA + "/gamerule keepInventory false", prefix);
		MessageHelper.sendMessage(who, "=====================", true);
	}

	/**
	 * Checks if the problematic "keepInventory" gamerule is enabled in a given world.
	 * 
	 * @return true or false
	 */
	public static boolean isProblematicRuleEnabled(World world) {
		return Boolean.parseBoolean(world.getGameRuleValue("keepInventory"));
	}

}
