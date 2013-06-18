package bone008.bukkit.deathcontrol.config.actions;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.bukkit.inventory.ItemStack;

import bone008.bukkit.deathcontrol.StoredItemStack;
import bone008.bukkit.deathcontrol.config.ActionAgent;
import bone008.bukkit.deathcontrol.config.ActionResult;
import bone008.bukkit.deathcontrol.config.DeathContext;
import bone008.bukkit.deathcontrol.util.Util;

public class DamageItemsActionAgent extends ActionAgent {

	private static class ItemStackEntry {
		final ItemStack stack;
		final short durabilityChanged;

		public ItemStackEntry(ItemStack stack, short durabilityChanged) {
			this.stack = stack;
			this.durabilityChanged = durabilityChanged;
		}
	}

	private final DamageItemsAction action;

	// contains stacks that had durability subtracted
	private List<ItemStackEntry> damagedStacks = new ArrayList<ItemStackEntry>();
	// contains stacks that were set to max durability (and thus effectively removed); durability wasn't actually changed here
	private List<ItemStack> removedStacks = new ArrayList<ItemStack>();

	public DamageItemsActionAgent(DeathContext context, DamageItemsAction action) {
		super(context, action);
		this.action = action;
	}

	@Override
	public void preprocess() {
		for (Iterator<StoredItemStack> it = context.getItemDrops().iterator(); it.hasNext();) {
			StoredItemStack dropped = it.next();

			int maxDurability = dropped.itemStack.getType().getMaxDurability();
			if (maxDurability > 0 && action.isValidItem(dropped.itemStack)) {
				int usesLeft = maxDurability - dropped.itemStack.getDurability();
				int newUsesLeft = action.calculatePercentageAmount(usesLeft, 1 - action.damagePct);

				if (newUsesLeft > 0) {
					dropped.itemStack.setDurability((short) (maxDurability - newUsesLeft));
					damagedStacks.add(new ItemStackEntry(dropped.itemStack, (short) (usesLeft - newUsesLeft)));
				}
				else {
					it.remove();
					removedStacks.add(dropped.itemStack);
				}
			}
		}

		// set context variable
		context.setVariable("items-damaged", (Integer) context.getVariable("items-damaged") + damagedStacks.size() + removedStacks.size());
	}

	@Override
	public ActionResult execute() {
		// everything important happened while preprocessing

		return (damagedStacks.isEmpty() && removedStacks.isEmpty() ? ActionResult.FAILED : ActionResult.STANDARD);
	}

	@Override
	public void cancel() {
		// add the uses back to the damaged stacks
		for (ItemStackEntry e : damagedStacks) {
			e.stack.setDurability((short) (e.stack.getDurability() - e.durabilityChanged));
		}

		// drop the stacks which we previously removed completely
		Util.dropItems(context.getDeathLocation(), removedStacks, true);
	}

}
