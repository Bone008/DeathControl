package bone008.bukkit.deathcontrol.config.actions;

import java.util.List;

import bone008.bukkit.deathcontrol.config.ActionAgent;
import bone008.bukkit.deathcontrol.config.ActionDescriptor;
import bone008.bukkit.deathcontrol.config.DeathContext;
import bone008.bukkit.deathcontrol.config.lists.BasicListItem;
import bone008.bukkit.deathcontrol.exceptions.DescriptorFormatException;
import bone008.bukkit.deathcontrol.exceptions.FormatException;
import bone008.bukkit.deathcontrol.util.ParserUtil;

public class ChargeItemAction extends ActionDescriptor {

	BasicListItem item;
	int amount = 1;

	public ChargeItemAction(List<String> args) throws DescriptorFormatException {
		if (args.size() > 2)
			throw new DescriptorFormatException("too many arguments");
		if (args.size() == 0)
			throw new DescriptorFormatException("no item given");

		if (args.size() == 2) {
			amount = ParserUtil.parseInt(args.get(1));
			if (amount <= 0)
				throw new DescriptorFormatException("invalid amount: " + args.get(1));
		}

		try {
			item = BasicListItem.parse(args.get(0));
		} catch (FormatException e) {
			throw new DescriptorFormatException(e.getMessage());
		}
	}

	@Override
	public ActionAgent createAgent(DeathContext context) {
		return new ChargeItemActionAgent(context, this);
	};
}
