package bone008.bukkit.deathcontrol.config.actions;

import java.util.List;

import org.bukkit.inventory.ItemStack;

import bone008.bukkit.deathcontrol.DeathControl;
import bone008.bukkit.deathcontrol.config.ActionAgent;
import bone008.bukkit.deathcontrol.config.ActionDescriptor;
import bone008.bukkit.deathcontrol.config.DeathContext;
import bone008.bukkit.deathcontrol.config.lists.ListItem;
import bone008.bukkit.deathcontrol.exceptions.DescriptorFormatException;
import bone008.bukkit.deathcontrol.util.ParserUtil;

public class KeepItemsAction extends ActionDescriptor {

	private List<ListItem> items = null;
	private boolean inverted = false;
	double keepPct = 1.0;

	public KeepItemsAction(List<String> args) throws DescriptorFormatException {
		String itemsInput = null;

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
				keepPct = 1.0; // reset to 100%
				itemsInput = args.get(0); // treat it as a list
			}
			// otherwise we're done
			break;

		case 0:
			break;

		default:
			throw new DescriptorFormatException("too many arguments");
		}

		if (itemsInput != null) {
			if (itemsInput.startsWith("!")) {
				inverted = true;
				itemsInput = itemsInput.substring(1);
			}

			items = DeathControl.instance.itemLists.getList(itemsInput);
			if (items == null)
				throw new DescriptorFormatException("invalid item list: " + itemsInput);
		}
	}

	private boolean tryParsePct(String input) throws DescriptorFormatException {
		keepPct = ParserUtil.parsePercentage(input);
		if (keepPct == -1)
			return false;
		if (keepPct > 1.0)
			throw new DescriptorFormatException("invalid percentage: " + input);

		return true;
	}

	public boolean isValidItem(ItemStack itemStack) {
		if (items == null)
			return true; // no filter

		boolean contained = false;
		for (ListItem item : items) {
			if (item.matches(itemStack)) {
				contained = true;
				break;
			}
		}

		return (contained != inverted);
	}

	@Override
	public ActionAgent createAgent(DeathContext context) {
		return new KeepItemsActionAgent(context, this);
	};
}
