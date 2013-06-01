package bone008.bukkit.deathcontrol.newconfig.actions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.inventory.ItemStack;

import bone008.bukkit.deathcontrol.StoredItemStack;
import bone008.bukkit.deathcontrol.newconfig.ActionAgent;
import bone008.bukkit.deathcontrol.newconfig.ActionResult;
import bone008.bukkit.deathcontrol.newconfig.DeathContext;
import bone008.bukkit.deathcontrol.util.Util;

public class ChargeItemActionAgent extends ActionAgent {

	private final ChargeItemAction action;

	private ActionResult result = null;
	private List<ItemStack> destroyedStacks = new ArrayList<ItemStack>();

	public ChargeItemActionAgent(DeathContext context, ChargeItemAction action) {
		super(context, action);
		this.action = action;
	}

	@Override
	public void preprocess() {
		// Cache the stacks to alter first.
		// This way, when it fails subtracting the whole amount from mulitple stacks, nothing will be changed
		// and the items are not left in an inconsistent state.
		Map<StoredItemStack, Integer> subtractStacks = new HashMap<StoredItemStack, Integer>();

		int amountLeft = action.amount;

		for (StoredItemStack drop : context.getItemDrops()) {
			if (action.item.matches(drop.itemStack)) {
				int subtracted = Math.min(amountLeft, drop.itemStack.getAmount());
				subtractStacks.put(drop, subtracted);

				amountLeft -= subtracted;
			}

			if (amountLeft <= 0)
				break;
		}

		// not enough items available to pay everything
		if (amountLeft > 0) {
			result = ActionResult.FAILED;
			return;
		}

		for (Entry<StoredItemStack, Integer> e : subtractStacks.entrySet()) {
			StoredItemStack stack = e.getKey();
			int amount = e.getValue();

			if (amount >= stack.itemStack.getAmount())
				context.getItemDrops().remove(stack);
			else
				stack.itemStack.setAmount(stack.itemStack.getAmount() - amount);

			// keep track of the subtracted one in case we are cancelled later
			ItemStack destroyed = stack.itemStack.clone();
			destroyed.setAmount(amount);
			destroyedStacks.add(destroyed);
		}
	}

	@Override
	public ActionResult execute() {
		return result; // our real work is done when preprocessing
	}

	@Override
	public void cancel() {
		// drop the stacks which we previously prevented from dropping
		Util.dropItems(context.getDeathLocation(), destroyedStacks, true);
	}

}
