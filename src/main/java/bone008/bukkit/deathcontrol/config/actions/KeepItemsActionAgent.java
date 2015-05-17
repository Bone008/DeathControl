package bone008.bukkit.deathcontrol.config.actions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import bone008.bukkit.deathcontrol.StoredItemStack;
import bone008.bukkit.deathcontrol.config.ActionAgent;
import bone008.bukkit.deathcontrol.config.ActionResult;
import bone008.bukkit.deathcontrol.config.DeathContext;
import bone008.bukkit.deathcontrol.util.Util;

public class KeepItemsActionAgent extends ActionAgent {

	private final KeepItemsAction action;

	private List<StoredItemStack> keptItems = new ArrayList<StoredItemStack>();

	public KeepItemsActionAgent(DeathContext context, KeepItemsAction action) {
		super(context, action);
		this.action = action;
	}

	@Override
	public void preprocess() {
		// do the actual calculations from the action
		action.applyActionToStacks(context.getItemDrops(), keptItems);

		// count the items
		int keptAmount = 0, droppedAmount = 0;
		for (StoredItemStack kept : keptItems)
			keptAmount += kept.itemStack.getAmount();
		for (StoredItemStack dropped : context.getItemDrops())
			droppedAmount += dropped.itemStack.getAmount();
		int totalAmount = keptAmount + droppedAmount;

		// set context variables
		if (totalAmount == 0) {
			// avoid division by 0
			context.setVariable("items-kept-percent", "0%");
			context.setVariable("items-dropped-percent", "0%");
		}
		else {
			context.setVariable("items-kept-percent", String.format("%.0f%%", keptAmount * 100.0 / totalAmount));
			context.setVariable("items-dropped-percent", String.format("%.0f%%", droppedAmount * 100.0 / totalAmount));
		}
	}

	@Override
	public ActionResult execute() {
		// here we do the restoring part

		if (keptItems.isEmpty()) // nothing to keep
			return ActionResult.FAILED;

		Player victimPlayer = context.getVictim().getPlayer();

		if (victimPlayer == null) {
			cancel();
			return ActionResult.PLAYER_OFFLINE;
		}

		PlayerInventory inv = victimPlayer.getInventory();

		for (StoredItemStack stored : keptItems) {
			if (inv.getItem(stored.slot) == null) // slot is empty
				inv.setItem(stored.slot, stored.itemStack);

			else { // slot is occupied --> add it regularly and drop if necessary
				HashMap<Integer, ItemStack> leftovers = inv.addItem(stored.itemStack);
				if (leftovers.size() > 0)
					Util.dropItems(victimPlayer.getLocation(), leftovers, false);
			}
		}

		return null;
	}

	@Override
	public void cancel() {
		for (StoredItemStack stored : keptItems)
			Util.dropItem(context.getDeathLocation(), stored.itemStack, true);
	}

}
