package bone008.bukkit.deathcontrol;

import java.util.ArrayList;
import java.util.List;
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
import org.bukkit.inventory.PlayerInventory;

import bone008.bukkit.deathcontrol.config.CauseData.HandlingMethod;
import bone008.bukkit.deathcontrol.config.CauseSettings;

public class BukkitDeathHandler implements Listener {

	private final Random rand = new Random();

	@EventHandler(priority = EventPriority.HIGH)
	public void onRespawn(final PlayerRespawnEvent event) {
		final String playerName = event.getPlayer().getName();

		// delay this for the next tick to make sure the player fully respawned to get the correct location
		// don't use getRespawnLocation(), because it might still be changed by another plugin - this way is safer
		// this also allows the plugin to correctly view and handle other plugins actions on the player (e.g. Essentials giving back exp automatically)
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

	@EventHandler(priority = EventPriority.HIGH)
	// Note: Essentials listens on LOW
	public void onDeath(final PlayerDeathEvent event) {
		assert (event.getEntity() instanceof Player);
		Player ply = (Player) event.getEntity();

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

		final int totalExp = ExperienceUtils.getCurrentExp(ply);

		List<ItemStack> desiredDrops = new ArrayList<ItemStack>();
		List<StoredItemStack> keptItems = null;
		int keptExp = 0;
		int droppedExp = 0;

		if (causeSettings.keepInventory()) {
			keptItems = calculateItems(ply.getInventory(), causeSettings, desiredDrops);
			if (keptItems.isEmpty())
				keptItems = null;
		}

		if (causeSettings.keepExperience()) {
			keptExp = (int) Math.round(((100 - causeSettings.getLossExp()) / 100) * totalExp);
			droppedExp = event.getDroppedExp();

			// fix for Essentials: we take control over respawned exp ...
			event.setKeepLevel(false);
			event.setNewExp(0);
			event.setNewLevel(0);
			event.setNewTotalExp(0);
		}

		if (keptItems == null && keptExp <= 0)
			return;

		double cost = 0;
		if (!DeathControl.instance.hasPermission(ply, DeathControl.PERMISSION_FREE)) {
			cost = EconomyUtils.calcCost(ply, causeSettings);
			if (!EconomyUtils.canAfford(ply, cost)) {
				MessageHelper.sendMessage(ply, "You couldn't keep your items", true);
				MessageHelper.sendMessage(ply, "because you didn't have enough money!", true);
				DeathControl.instance.log(Level.FINE, log1.append("; Not enough money!").toString());
				return;
			}
		}

		if (keptItems != null) {
			// replace the natural drops with our filtered ones
			event.getDrops().clear();
			event.getDrops().addAll(desiredDrops);
		}

		if (causeSettings.keepExperience()) { // keep this down here so it stays after the money check
			event.setDroppedExp(0);
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
		else if (event.getDrops().isEmpty())
			log2.append("all");
		else
			log2.append("some");
		log2.append('\n');
		if (keptExp > 0)
			log2.append("| Kept experience: ").append(keptExp).append(" of ").append(totalExp).append('\n');
		log2.append("| Method: ").append(method).append("\n");
		if (method == HandlingMethod.COMMAND)
			log2.append("| Expires in ").append(causeSettings.getTimeout()).append(" seconds!\n");

		// message the console
		if (DeathControl.instance.config.loggingLevel <= Level.FINEST.intValue())
			DeathControl.instance.log(Level.FINE, log2.toString().trim());
		else if (DeathControl.instance.config.loggingLevel <= Level.INFO.intValue())
			DeathControl.instance.log(Level.INFO, log1.toString().trim());

		// message the player
		if (DeathControl.instance.config.showMessages) {
			MessageHelper.sendMessage(ply, ChatColor.YELLOW + "You keep " + ChatColor.WHITE + (event.getDrops().isEmpty() ? "all" : "some") + ChatColor.YELLOW + " of your items");
			MessageHelper.sendMessage(ply, ChatColor.YELLOW + "because you " + deathCause.toMsgString() + ".");
			if (method == HandlingMethod.COMMAND) {
				MessageHelper.sendMessage(ply, ChatColor.YELLOW + "You can get them back with " + ChatColor.GREEN + "/death back");
				if (causeSettings.getTimeout() > 0)
					MessageHelper.sendMessage(ply, ChatColor.RED + "This will expire in " + causeSettings.getTimeout() + " seconds!");
			}

			if (cost > 0)
				MessageHelper.sendMessage(ply, ChatColor.GOLD + "This " + (method == HandlingMethod.COMMAND ? "will cost" : "costs") + " you " + ChatColor.WHITE + EconomyUtils.formatMoney(cost) + ChatColor.GOLD + "!");
		}
	}

	/**
	 * Calculates a list of {@link ItemStack}s that the player keeps. Considers lists and loss-percentage.
	 * 
	 * @param playerInv the inventory of the dying player
	 * @param settings the cause settings associated with the death cause
	 * @param desiredDrops ItemStacks that should be lost are added to this list
	 * @return a list of ItemStacks that should be kept
	 */
	private List<StoredItemStack> calculateItems(PlayerInventory playerInv, CauseSettings settings, List<ItemStack> desiredDrops) {
		// the actual size of the inventory including armor
		final int invSize = playerInv.getSize() + playerInv.getArmorContents().length;

		final double loss = settings.getLoss() / 100;

		List<StoredItemStack> ret = new ArrayList<StoredItemStack>(invSize);

		// save the items that may be kept due to whitelist/blacklist limits in "temp"
		for (int slot = 0; slot < invSize; slot++) {
			ItemStack item = playerInv.getItem(slot);

			if (item == null) // skip empty slots
				continue;

			if (!settings.isValidItem(item)) {
				desiredDrops.add(item.clone());
				continue;
			}

			ItemStack keptItem = item.clone();
			applyLoss(keptItem, loss);

			if (keptItem.getAmount() > 0)
				ret.add(new StoredItemStack(slot, keptItem));

			if (keptItem.getAmount() < item.getAmount()) {
				ItemStack droppedItem = item.clone();
				droppedItem.setAmount(item.getAmount() - keptItem.getAmount());
				desiredDrops.add(droppedItem);
			}
		}

		return ret;
	}

	/**
	 * Applies a given loss-percentage to an ItemStack.
	 * 
	 * @param item The ItemStack to modify.
	 * @param loss The loss-percentage (between 0.0 and 1.0) to apply.
	 */
	private void applyLoss(ItemStack item, double loss) {
		if (loss <= 0.0)
			return;

		double newAmount = ((double) item.getAmount()) * (1.0 - loss);
		int intAmount = (int) newAmount;

		// got a floating result --> apply random
		if (newAmount > intAmount && rand.nextDouble() < newAmount - intAmount) {
			intAmount++;
		}

		item.setAmount(intAmount);
	}

}
