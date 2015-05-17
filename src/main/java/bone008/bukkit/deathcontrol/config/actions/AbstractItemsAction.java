package bone008.bukkit.deathcontrol.config.actions;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.bukkit.inventory.ItemStack;

import bone008.bukkit.deathcontrol.DeathControl;
import bone008.bukkit.deathcontrol.StoredItemStack;
import bone008.bukkit.deathcontrol.config.ActionDescriptor;
import bone008.bukkit.deathcontrol.config.lists.ListItem;
import bone008.bukkit.deathcontrol.exceptions.DescriptorFormatException;
import bone008.bukkit.deathcontrol.util.ParserUtil;
import bone008.bukkit.deathcontrol.util.Util;

/**
 * Represents an action that is to be applied to a optionally filtered list of items.<br>
 * A percentage to further filter the affected items can also be applied in the descriptor.
 */
public abstract class AbstractItemsAction extends ActionDescriptor {

	private String filterName = null;
	private List<ListItem> itemsFilter = null;
	private boolean filterInverted = false;
	private double affectedPct = 1.0;
	private boolean percentageAllowed = false;

	protected void parseFilter(List<String> args, boolean allowPercentage) throws DescriptorFormatException {
		percentageAllowed = allowPercentage;
		String itemsInput = null;

		if (allowPercentage) {
			switch (args.size()) {
			case 2:
				if (tryParsePct(args.get(1)))
					itemsInput = args.get(0);
				else if (tryParsePct(args.get(0)))
					itemsInput = args.get(1);
				else
					throw new DescriptorFormatException("invalid percentage: " + args.get(1)); // official position is 2nd argument

				break;

			case 1:
				if (!tryParsePct(args.get(0))) { // not a percentage argument
					affectedPct = 1.0; // reset to 100%
					itemsInput = args.get(0); // treat it as a list
				}
				// otherwise we're done
				break;

			case 0:
				break;

			default:
				throw new DescriptorFormatException("too many arguments");
			}
		}
		else if (!args.isEmpty()) {
			itemsInput = args.get(0);
		}


		if (itemsInput != null) {
			if (itemsInput.startsWith("!")) {
				filterInverted = true;
				itemsInput = itemsInput.substring(1);
			}

			itemsFilter = DeathControl.instance.itemLists.getList(itemsInput);
			if (itemsFilter == null)
				throw new DescriptorFormatException("invalid item list: " + itemsInput);
			filterName = itemsInput;
		}
	}

	/**
	 * Attempts to parse an input string as a percentage and automatically sotres it in the {@link #affectedPct} field.<br>
	 * Automatically validates the value by either throwing an exception or returning false, which can lead to {@link #affectedPct} to stay in an inconsistent state.
	 * 
	 * @param input the input String to parse
	 * @return true if the percentage was successfully extracted and stored, false if it wasn't a parsable number
	 * @throws DescriptorFormatException if it could be parsed, but resulted in a value > 100%
	 */
	private boolean tryParsePct(String input) throws DescriptorFormatException {
		affectedPct = ParserUtil.parsePercentage(input);
		if (affectedPct == -1)
			return false;
		if (affectedPct > 1.0)
			throw new DescriptorFormatException("invalid percentage: " + input);

		return true;
	}

	/**
	 * Checks if the item stack matches the given filter. Considers inverted filters, ignores {@link #affectedPct}.
	 * 
	 * @param itemStack the stack to check
	 * @return true if it's valid according to the filter (or if there is no filter), false otherwise
	 */
	public boolean isValidItem(ItemStack itemStack) {
		if (itemsFilter == null)
			return true; // no filter

		boolean contained = false;
		for (ListItem item : itemsFilter) {
			if (item.matches(itemStack)) {
				contained = true;
				break;
			}
		}

		return (contained != filterInverted);
	}

	public int calculateAffectedAmount(int oldAmount) {
		return calculatePercentageAmount(oldAmount, affectedPct);
	}

	public int calculatePercentageAmount(int oldAmount, double percentage) {
		double newAmount = ((double) oldAmount) * percentage;
		int intAmount = (int) newAmount;

		// got a floating result --> apply random
		if (newAmount > intAmount && Util.getRandom().nextDouble() < newAmount - intAmount) {
			intAmount++;
		}

		return intAmount;
	}

	/**
	 * Utility method to apply this action on a collection of items. All the affected items are stored in a second collection.
	 * 
	 * @param all the set of items to use as an input
	 * @param affected all items that were removed from the input are placed here
	 */
	public void applyActionToStacks(Collection<StoredItemStack> all, Collection<StoredItemStack> affected) {
		Iterator<StoredItemStack> it = all.iterator();

		while (it.hasNext()) {
			StoredItemStack current = it.next();

			if (!isValidItem(current.itemStack))
				continue;

			int keptAmount = calculateAffectedAmount(current.itemStack.getAmount());
			if (keptAmount == current.itemStack.getAmount()) {
				affected.add(current);
				it.remove();
			}
			else if (keptAmount > 0) {
				StoredItemStack kept = current.clone();
				kept.itemStack.setAmount(keptAmount);
				affected.add(kept);

				current.itemStack.setAmount(current.itemStack.getAmount() - keptAmount);
			}
			// do nothing if kept amount is 0
		}
	}

	@Override
	public List<String> toParameters() {
		List<String> ret = new ArrayList<String>();
		if (filterName != null)
			ret.add(filterName);
		if (percentageAllowed)
			ret.add(String.format("%.0f%%", affectedPct * 100));

		return ret;
	}

}
