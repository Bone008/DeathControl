package bone008.bukkit.deathcontrol;

import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import bone008.bukkit.deathcontrol.config.CauseData.HandlingMethod;

public class DeathManager {

	private boolean valid = true;

	private final String plyName;
	private final Location deathLocation;
	private final List<StoredItemStack> keptItems;
	private final int keptExp;
	private final int droppedExp;
	private final HandlingMethod method;
	private final double cost;
	private final int timeoutOnQuit;

	public DeathManager(Player ply, List<StoredItemStack> keptItems, int keptExp, int droppedExp, HandlingMethod method, double cost, int timeoutOnQuit) {
		this.plyName = ply.getName();
		this.deathLocation = ply.getLocation();
		this.keptItems = keptItems;
		this.keptExp = keptExp;
		this.droppedExp = droppedExp;
		this.method = method;
		this.cost = cost;
		this.timeoutOnQuit = timeoutOnQuit;
	}

	public boolean expire(boolean showMessage) {
		if (!valid)
			return false;

		// drops items
		if (keptItems != null)
			for (StoredItemStack storedStack : keptItems)
				Utilities.dropItem(deathLocation, storedStack.itemStack, true);

		// drops experience orbs
		Utilities.dropExp(deathLocation, droppedExp);

		// sends notification to the player
		if (showMessage) {
			Player ply = Bukkit.getPlayerExact(plyName);
			MessageHelper.sendMessage(ply, Message.NOTIF_EXPIRATION);
			// logs to console
			DeathControl.instance.log(Level.FINE, "Timer for " + plyName + " expired! Items dropped.");
		}

		unregister();
		return true;
	}

	public void respawned() {
		if (!valid)
			return;
		if (method == HandlingMethod.AUTO) {
			if (restore(true))
				DeathControl.instance.log(Level.FINE, plyName + " respawned and got back their items.");
			unregister();
		}
	}

	public boolean commandIssued() {
		if (method == HandlingMethod.COMMAND && this.valid) {
			Player ply = Bukkit.getPlayerExact(plyName);
			if (restore(false)) {
				MessageHelper.sendMessage(ply, Message.NOTIF_RESTORATION);
				DeathControl.instance.log(Level.FINE, ply.getName() + " got back their items via command.");
				unregister();
			}
			else {

			}
			return true;
		}
		return false;
	}

	private boolean restore(boolean isRespawn) {
		if (!valid)
			return false;

		final Player ply = Bukkit.getPlayerExact(plyName);

		if (!DeathControl.instance.config.allowCrossworld && !DeathControl.instance.hasPermission(ply, DeathControl.PERMISSION_CROSSWORLD) && !ply.getWorld().equals(deathLocation.getWorld())) {
			MessageHelper.sendMessage(ply, Message.NOTIF_NOCROSSWORLD);
			expire(false);
			return false;
		}

		boolean success = false;

		if (EconomyUtils.payCost(ply, cost)) {
			if (keptItems != null) {
				PlayerInventory inv = ply.getInventory();

				for (StoredItemStack storedStack : keptItems) {

					if (inv.getItem(storedStack.slot) == null) // slot is empty
						inv.setItem(storedStack.slot, storedStack.itemStack);

					else { // slot is occupied --> add it regularly and drop if necessary
						HashMap<Integer, ItemStack> leftovers = inv.addItem(storedStack.itemStack);
						if (leftovers.size() > 0)
							Utilities.dropItems(ply.getLocation(), leftovers, false);
					}

				}

				success = true;
			}

			if (keptExp > 0) {
				if (isRespawn) {
					// check for modifications
					if (ExperienceUtils.getCurrentExp(ply) > 0)
						DeathControl.instance.log(Level.FINE, "Another plugin set the player's experience after respawning. These changes have been overridden.");

					// reset always just in case
					ply.setExp(0);
					ply.setLevel(0);
					ply.setTotalExperience(0);
				}

				// give back the exp
				ExperienceUtils.changeExp(ply, keptExp);

				success = true;
			}
		}
		else {
			MessageHelper.sendMessage(ply, Message.NOTIF_NOMONEY);
		}

		return success;
	}

	private void unregister() {
		if (!valid)
			return;
		DeathControl.instance.removeManager(plyName);
		valid = false;
	}

	public int getTimeoutOnQuit() {
		return timeoutOnQuit;
	}

}
