package bone008.bukkit.deathcontrol;

import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import bone008.bukkit.deathcontrol.config.CauseData.HandlingMethod;

public class DeathManager {

	private boolean valid = true;

	private final DeathControl plugin;
	private final String plyName;
	private final Location deathLocation;
	private final List<ItemStack> keptItems;
	private final int keptExp;
	private final int droppedExp;
	private final HandlingMethod method;
	private final double cost;
	private final int timeoutOnQuit;

	public DeathManager(DeathControl plugin, Player ply, List<ItemStack> keptItems, int keptExp, int droppedExp, HandlingMethod method, double cost, int timeoutOnQuit) {
		this.plugin = plugin;
		this.plyName = ply.getName();
		this.deathLocation = ply.getLocation();
		this.keptItems = keptItems;
		this.keptExp = keptExp;
		this.droppedExp = keptExp;
		this.method = method;
		this.cost = cost;
		this.timeoutOnQuit = timeoutOnQuit;
	}

	public boolean expire(boolean showMessage) {
		if (!valid)
			return false;

		// drops items
		Utilities.dropItems(deathLocation, keptItems, true);
		// drops experience orbs
		Utilities.dropExp(deathLocation, droppedExp, true);

		// sends notification to the player
		if (showMessage) {
			Player ply = Bukkit.getPlayer(plyName);
			plugin.display(ply, ChatColor.DARK_RED + "Time is up.");
			plugin.display(ply, ChatColor.DARK_RED + "Your items are dropped at your death location.");
			// logs to console
			plugin.log(Level.FINE, "Timer for " + plyName + " expired! Items dropped.");
		}

		unregister();
		return true;
	}

	public void respawned() {
		if (!valid)
			return;
		if (method == HandlingMethod.AUTO) {
			if (restore())
				plugin.log(Level.FINE, plyName + " respawned and got back their items.");
			unregister();
		}
	}

	public boolean commandIssued() {
		if (method == HandlingMethod.COMMAND && this.valid) {
			Player ply = Bukkit.getPlayer(plyName);
			if (restore()) {
				plugin.display(ply, "You got your items back!");
				plugin.log(Level.FINE, ply.getName() + " got back their items via command.");
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
		
		Player ply = Bukkit.getPlayer(plyName);
		if (EconomyUtils.payCost(ply, cost)) {
			if (keptItems != null) {
				HashMap<Integer, ItemStack> leftovers = ply.getInventory().addItem(keptItems.toArray(new ItemStack[keptItems.size()]));
				if (leftovers.size() > 0) {
					Utilities.dropItems(ply.getLocation(), leftovers, false);
				}
				success = true;
			}

			if (keptExp > 0) {
				ExperienceUtils.changeExp(ply, keptExp);
				success = true;
			}
		}

		return success;
	}

	private void unregister() {
		if (!valid)
			return;
		plugin.removeManager(plyName);
		valid = false;
	}

	public int getTimeoutOnQuit() {
		return timeoutOnQuit;
	}

}
