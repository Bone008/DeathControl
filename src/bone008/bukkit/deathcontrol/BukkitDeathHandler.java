package bone008.bukkit.deathcontrol;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.Random;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;

import bone008.bukkit.deathcontrol.config.CauseData.HandlingMethod;
import bone008.bukkit.deathcontrol.config.CauseSettings;

public class BukkitDeathHandler implements Listener {

	@EventHandler(priority = EventPriority.HIGH)
	public void onRespawn(final PlayerRespawnEvent event) {
		final String playerName = event.getPlayer().getName();

		// delay this for the next tick to make sure the player fully respawned
		// to get the correct location
		// don't use getRespawnLocation(), because it might still be changed by
		// another plugin - this way is safer
		Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(DeathControl.instance, new Runnable() {
			@Override
			public void run() {
				DeathManager m = DeathControl.instance.getManager(playerName);
				if (m != null) {
					m.respawned();
				}
			}
		});
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void onDeath(final PlayerDeathEvent e) {
		assert (e.getEntity() instanceof Player);
		Player ply = (Player) e.getEntity();

		DeathControl.instance.expireManager(ply.getName());

		if (!DeathControl.instance.hasPermission(ply, DeathControl.PERMISSION_USE))
			return;

		EntityDamageEvent damageEvent = ply.getLastDamageCause();
		DeathCause deathCause = DeathCause.getDeathCause(damageEvent);

		StringBuilder log1 = new StringBuilder(), log2 = new StringBuilder();

		log1.append(ply.getName()).append(" died (cause: ").append(deathCause.toHumanString()).append(")");

		if (!DeathControl.instance.hasPermission(ply, DeathControl.PERMISSION_NOLIMITS) && !DeathControl.instance.config.isWorldAllowed(ply.getWorld().getName())) {
			DeathControl.instance.log(Level.FINE, log1.append("; Not in a valid world!").toString());
			return;
		}

		CauseSettings causeSettings = DeathControl.instance.config.getSettings(deathCause);
		if (causeSettings == null) {
			DeathControl.instance.log(Level.FINE, log1.append("; No handling configured!").toString());
			return;
		}

		List<ItemStack> drops = e.getDrops();
		final int totalExp = ExperienceUtils.getActualExp(ply);

		List<ItemStack> keptItems = null;
		int keptExp = 0;
		int droppedExp = 0;

		if (causeSettings.keepInventory()) {
			keptItems = calculateItems(drops, causeSettings);
			if (keptItems.isEmpty())
				keptItems = null;
		}

		if (causeSettings.keepExperience()) {
			keptExp = (int) Math.round(((100 - causeSettings.getLossExp()) / 100) * totalExp);
			droppedExp = e.getDroppedExp();
		}

		if (keptItems == null && keptExp <= 0)
			return;

		double cost = 0;
		if (!DeathControl.instance.hasPermission(ply, DeathControl.PERMISSION_FREE)) {
			cost = EconomyUtils.calcCost(ply, causeSettings);
			if (!EconomyUtils.canAfford(ply, cost)) {
				MessageHelper.sendMessage(ply, "You couldn't keep your items", true);
				MessageHelper.sendMessage(ply, "because you didn't have enough money!", true);
				return;
			}
		}

		if (keptItems != null) {
			ListIterator<ItemStack> it = keptItems.listIterator();
			while (it.hasNext()) {
				ItemStack is = it.next();
				drops.remove(is); // remove the item from the drops list
				it.set(is.clone()); // make sure we have an independent ItemStack
			}
		}

		if (causeSettings.keepExperience()) { // keep this down here so it stays after the money check
			e.setDroppedExp(0);
		}

		HandlingMethod method = causeSettings.getMethod();
		int timeout = causeSettings.getTimeout();

		final DeathManager dm = new DeathManager(ply, keptItems, keptExp, droppedExp, method, cost, causeSettings.getTimeoutOnQuit());
		DeathControl.instance.addManager(ply.getName(), dm);

		if (method == HandlingMethod.COMMAND && timeout > 0) {
			Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(DeathControl.instance, new Runnable() {
				@Override
				public void run() {
					dm.expire(true);
				}
			}, timeout * 20L);
		}

		log2.append("Handling death:\n");
		log2.append("| Player: ").append(ply.getName()).append('\n');
		log2.append("| Death cause: ").append(deathCause.toHumanString()).append('\n');
		log2.append("| Kept items: ");
		if (keptItems == null)
			log2.append("none");
		else if (drops.isEmpty())
			log2.append("all");
		else
			log2.append("some");
		log2.append('\n');
		if (keptExp > 0)
			log2.append("| Kept experience: ").append(keptExp).append(" of ").append(totalExp).append('\n');
		log2.append("| Method: ").append(method).append("\n");
		if (method == HandlingMethod.COMMAND)
			log2.append("| Expires in ").append(causeSettings.getTimeout()).append(" seconds!\n");

		if (DeathControl.instance.config.loggingLevel <= Level.FINEST.intValue())
			DeathControl.instance.log(Level.FINE, log2.toString().trim());
		else if (DeathControl.instance.config.loggingLevel <= Level.INFO.intValue())
			DeathControl.instance.log(Level.INFO, log1.toString().trim());

		MessageHelper.sendMessage(ply, ChatColor.YELLOW + "You keep " + ChatColor.WHITE + (drops.isEmpty() ? "all" : "some") + ChatColor.YELLOW + " of your items");
		MessageHelper.sendMessage(ply, ChatColor.YELLOW + "because you " + deathCause.toMsgString() + ".");
		if (method == HandlingMethod.COMMAND) {
			MessageHelper.sendMessage(ply, ChatColor.YELLOW + "You can get them back with " + ChatColor.GREEN + "/death back");
			if (causeSettings.getTimeout() > 0)
				MessageHelper.sendMessage(ply, ChatColor.RED + "This will expire in " + causeSettings.getTimeout() + " seconds!");
		}

		if (cost > 0)
			MessageHelper.sendMessage(ply, ChatColor.GOLD + "This " + (method == HandlingMethod.COMMAND ? "will cost" : "costs") + " you " + ChatColor.WHITE + EconomyUtils.formatMoney(cost) + ChatColor.GOLD + "!");
	}

	/**
	 * Calculates a list of {@link ItemStack}s that the player keeps. Considers lists and loss-percentage.
	 * 
	 * @param droppedItems
	 *            the original drops; not affected!
	 * @param settings
	 *            the cause settings associated with the death cause
	 * @return a list of ItemStacks that should be kept
	 */
	private List<ItemStack> calculateItems(List<ItemStack> droppedItems, CauseSettings settings) {
		List<ItemStack> ret = new ArrayList<ItemStack>();

		// save the items that may be kept due to whitelist/blacklist limits in
		// "temp"
		for (ItemStack item : droppedItems) {
			if (settings.isValidItem(item))
				ret.add(item);
		}

		// no need to do anything when there are no items
		if (ret.isEmpty())
			return ret;

		if (settings.getLoss() > 0) {
			// apply the loss-percentage
			double lossMultiplier = settings.getLoss() / 100;
			int lostStacks = Math.round((float) (ret.size() * lossMultiplier));
			Random rand = new Random();

			for (int i = 0; i < lostStacks; i++) {
				ret.remove(rand.nextInt(ret.size()));
			}
		}

		return ret;
	}

}
