package bone008.bukkit.deathcontrol;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.Random;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;

import bone008.bukkit.deathcontrol.config.CauseData.HandlingMethod;
import bone008.bukkit.deathcontrol.config.CauseSettings;

public class DeathControlEntityListener implements Listener {

	private DeathControl plugin;

	public DeathControlEntityListener(DeathControl plugin) {
		this.plugin = plugin;
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void onEntityDeath(final EntityDeathEvent event) {
		if (!(event instanceof PlayerDeathEvent)) {
			return;
		}

		PlayerDeathEvent e = (PlayerDeathEvent) event;
		assert (e.getEntity() instanceof Player);
		Player ply = (Player) e.getEntity();

		plugin.expireManager(ply.getName());

		if (!plugin.hasPermission(ply, DeathControl.PERMISSION_USE)) {
			return;
		}

		EntityDamageEvent damageEvent = ply.getLastDamageCause();
		DeathCause deathCause = DeathCause.getDeathCause(damageEvent);

		CauseSettings causeSettings = plugin.config.getSettings(deathCause);
		if (causeSettings == null)
			return;

		List<ItemStack> drops = e.getDrops();
		List<ItemStack> keptItems = null;
		int keptExp = 0;

		if (causeSettings.keepInventory()) {
			keptItems = calculateItems(drops, causeSettings);
			if (keptItems.isEmpty())
				keptItems = null;
		}
		// TODO: do the same for experience

		if (keptItems == null && keptExp <= 0)
			return;

		double cost = 0;
		if (!plugin.hasPermission(ply, DeathControl.PERMISSION_FREE)) {
			cost = EconomyUtils.calcCost(ply, causeSettings);
			if (!EconomyUtils.canAfford(ply, cost)) {
				plugin.display(ply, ChatColor.RED + "You couldn't keep your items");
				plugin.display(ply, ChatColor.RED + "because you didn't have enough money!");
				return;
			}
		}

		if (keptItems != null) {
			ListIterator<ItemStack> it = keptItems.listIterator();
			while (it.hasNext()) {
				ItemStack is = it.next();
				drops.remove(is); // remove the item from the drops list
				it.set(is.clone()); // make sure we have an independent
									// ItemStack
			}
		}
		if (keptExp > 0) {
			// TODO: experience handling
		}

		HandlingMethod method = causeSettings.getMethod();
		int timeout = causeSettings.getTimeout();

		final DeathManager dm = new DeathManager(plugin, ply, keptItems, method, cost);
		plugin.addManager(ply.getName(), dm);

		if (method == HandlingMethod.COMMAND && timeout > 0) {
			plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
				@Override
				public void run() {
					dm.expire(true);
				}
			}, timeout * 20L);
		}

		StringBuilder log1 = new StringBuilder(), log2 = new StringBuilder();

		log1.append(ply.getName()).append(" died (cause: ").append(deathCause.toHumanString()).append(")");
		log2.append("Handling death:\n").append("| Player: ").append(ply.getName()).append('\n').append("| Death cause: ").append(deathCause.toHumanString()).append('\n').append("| Kept items: ");
		if (keptItems == null)
			log2.append("none");
		else if (drops.isEmpty())
			log2.append("all");
		else
			log2.append("some");
		log2.append('\n').append("| Method: ").append(method).append("\n");
		if (method == HandlingMethod.COMMAND)
			log2.append("| Expires in ").append(causeSettings.getTimeout()).append(" seconds!\n");

		if (plugin.config.loggingLevel == 1)
			plugin.log(log1.toString().trim());
		else if (plugin.config.loggingLevel == 2)
			plugin.log(log2.toString().trim());
		// else do nothing -> no logging

		plugin.display(ply, ChatColor.YELLOW + "You keep " + ChatColor.WHITE + (drops.isEmpty() ? "all" : "some") + ChatColor.YELLOW + " of your items");
		plugin.display(ply, ChatColor.YELLOW + "because you " + deathCause.toMsgString() + ".");
		if (method == HandlingMethod.COMMAND) {
			plugin.display(ply, ChatColor.YELLOW + "You can get them back with " + ChatColor.GREEN + "/death back");
			if (causeSettings.getTimeout() > 0)
				plugin.display(ply, ChatColor.RED + "This will expire in " + causeSettings.getTimeout() + " seconds!");
		}

		if (cost > 0)
			plugin.display(ply, ChatColor.GOLD + "This " + (method == HandlingMethod.COMMAND ? "will cost" : "costs") + " you " + ChatColor.WHITE + EconomyUtils.formatMoney(cost) + ChatColor.GOLD + "!");
		/*
		 * DeathManager manager = new DeathManager(plugin, ply, deathCause, e);
		 * Response ret = manager.handle();
		 * 
		 * 
		 * 
		 * if(ret.didSomething){ // build the logs to the console StringBuilder
		 * log1Builder = new StringBuilder(), log2Builder = new StringBuilder();
		 * 
		 * log1Builder.append(ply.getName()).append(" died (cause: ").append(
		 * deathCause.toHumanString()).append(")");
		 * 
		 * log2Builder .append("Handling death:\n")
		 * .append("| Player: ").append(ply.getName()).append('\n')
		 * .append("| Death cause: "
		 * ).append(deathCause.toHumanString()).append('\n')
		 * .append("| Kept items: "); switch(ret.keptItems){ case
		 * Response.KEPT_NONE: log2Builder.append("none"); break; case
		 * Response.KEPT_SOME: log2Builder.append("some"); break; case
		 * Response.KEPT_ALL: log2Builder.append("all"); break; }
		 * log2Builder.append("\n") .append("| Method: ").append(ret.isCommand ?
		 * "command" : "auto").append("\n");
		 * 
		 * 
		 * if(ret.success){ if(ret.keptItems != Response.KEPT_NONE){
		 * plugin.display(ply, ChatColor.YELLOW+"You keep "+ ChatColor.WHITE +
		 * (ret.keptItems==Response.KEPT_ALL ? "all":"some") +
		 * ChatColor.YELLOW+" of your items"); plugin.display(ply,
		 * ChatColor.YELLOW+"because you "+ deathCause.toMsgString()+".");
		 * if(ret.isCommand){ plugin.display(ply,
		 * ChatColor.YELLOW+"You can get them back with "
		 * +ChatColor.GREEN+"/death back"); if(manager.getTimeout() > 0){
		 * log2Builder
		 * .append("| Expires in ").append(manager.getTimeout()).append
		 * (" seconds!\n"); plugin.display(ply,
		 * ChatColor.RED+"This will expire in "
		 * +manager.getTimeout()+" seconds!"); } } } if(ret.money > 0 &&
		 * plugin.getRegisterMethod()!=null){ String moneyStr =
		 * plugin.getRegisterMethod().format(ret.money);
		 * log1Builder.append("; paid ").append(moneyStr);
		 * log2Builder.append("| Paid money: ").append(moneyStr).append("\n");
		 * plugin.display(ply, ChatColor.GOLD+"This "+(ret.isCommand ?
		 * "will cost" :
		 * "costs")+" you "+ChatColor.WHITE+moneyStr+ChatColor.GOLD+"!"); } }
		 * else if(ret.money == null){ log1Builder.append("; not enough money");
		 * log2Builder.append("| Not enough money!\n"); plugin.display(ply,
		 * ChatColor.RED+"You couldn't keep your items"); plugin.display(ply,
		 * ChatColor.RED+"because you didn't have enough money!"); } else{
		 * plugin.display(ply, ChatColor.RED+"A disruption in space-time!");
		 * plugin.display(ply,
		 * ChatColor.RED+"In other words: A bug in this plugin!");
		 * plugin.display(ply,
		 * ChatColor.RED+"This was not supposed to happen.");
		 * plugin.log(Level.SEVERE,
		 * "The manager returned an invalid response! Please report this bug!");
		 * }
		 * 
		 * if(plugin.config.loggingLevel == 1)
		 * plugin.log(log1Builder.toString().trim()); else
		 * if(plugin.config.loggingLevel == 2)
		 * plugin.log(log2Builder.toString().trim()); // else do nothing -> no
		 * logging }
		 */
	}

	/**
	 * Calculates a list of {@link ItemStack}s that the player keeps. Considers
	 * lists and loss-percentage.
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
