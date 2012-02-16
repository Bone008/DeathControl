package bone008.bukkit.deathcontrol;

import java.util.HashMap;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import bone008.bukkit.deathcontrol.config.CauseData.HandlingMethod;

public class DeathManager {

	private boolean valid = true;

	private DeathControl plugin;
	private Player ply;
	private Location deathLocation;
	private List<ItemStack> keptItems;
	private HandlingMethod method;
	private double cost;

	public DeathManager(DeathControl plugin, Player ply, List<ItemStack> keptItems, HandlingMethod method, double cost) {
		this.plugin = plugin;
		this.ply = ply;
		this.deathLocation = ply.getLocation();
		this.keptItems = keptItems;
		this.method = method;
		this.cost = cost;
	}

	public void expire(boolean showMessage) {
		if (!valid)
			return;

		// drops items
		Utilities.dropItems(deathLocation, keptItems, true);

		// sends notification to the player
		if (showMessage) {
			plugin.display(ply, ChatColor.DARK_RED + "Time is up.");
			plugin.display(ply, ChatColor.DARK_RED + "Your items are dropped at your death location.");
		}

		// logs to console
		plugin.log("Timer for " + ply.getName() + " expired! Items dropped.");

		unregister();
	}

	public void respawned() {
		if (!valid)
			return;
		if (method == HandlingMethod.AUTO) {
			if (restore())
				plugin.log(ply.getName() + " respawned and got back their items.");
			unregister();
		}
	}

	public boolean commandIssued() {
		if (method == HandlingMethod.COMMAND && this.valid) {
			if (restore()) {
				plugin.display(ply, "You got your items back!");
				plugin.log(ply.getName() + " got back their items via command.");
				unregister();
			} else {
				plugin.display(ply, ChatColor.RED + "You don't have enough money for that!");
			}
			return true;
		}
		return false;
	}

	private boolean restore() {
		if (!valid)
			return false;

		boolean success = false;

		if (EconomyUtils.payCost(ply, cost)) {
			if (keptItems != null) {
				HashMap<Integer, ItemStack> leftovers = ply.getInventory().addItem(keptItems.toArray(new ItemStack[keptItems.size()]));
				if (leftovers.size() > 0) {
					Utilities.dropItems(ply.getLocation(), leftovers, false);
				}
				success = true;
			}
		}

		return success;
	}

	private void unregister() {
		if (!valid)
			return;
		plugin.removeManager(ply.getName());
		valid = false;
	}

}
