package bone008.bukkit.deathcontrol.config.actions;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.inventory.ItemStack;

import bone008.bukkit.deathcontrol.StoredItemStack;
import bone008.bukkit.deathcontrol.config.ActionAgent;
import bone008.bukkit.deathcontrol.config.ActionResult;
import bone008.bukkit.deathcontrol.config.DeathContext;
import bone008.bukkit.deathcontrol.util.Util;

public class DestroyItemsActionAgent extends ActionAgent {

	private final DestroyItemsAction action;

	private List<ItemStack> destroyedStacks = new ArrayList<ItemStack>();

	public DestroyItemsActionAgent(DeathContext context, DestroyItemsAction action) {
		super(context, action);
		this.action = action;
	}

	@Override
	public void preprocess() {
		List<StoredItemStack> destroyed = new ArrayList<StoredItemStack>();
		action.applyActionToStacks(context.getItemDrops(), destroyed);

		// we don't need to store the slot
		for (StoredItemStack stored : destroyed)
			destroyedStacks.add(stored.itemStack);

		// TODO set context variables for destroyed items
	}

	@Override
	public ActionResult execute() {
		// since we destroyed the items while preprocessing and this action does no restoring, there is nothing left to do here

		return (destroyedStacks.isEmpty() ? ActionResult.FAILED : ActionResult.STANDARD);
	}

	@Override
	public void cancel() {
		// drop the stacks which we previously prevented from dropping
		Util.dropItems(context.getDeathLocation(), destroyedStacks, true);
	}

}
