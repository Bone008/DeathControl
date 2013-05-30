package bone008.bukkit.deathcontrol.newconfig.actions;

import java.util.Iterator;
import java.util.List;

import bone008.bukkit.deathcontrol.exceptions.DescriptorFormatException;
import bone008.bukkit.deathcontrol.newconfig.ActionAgent;
import bone008.bukkit.deathcontrol.newconfig.ActionDescriptor;
import bone008.bukkit.deathcontrol.newconfig.DeathContext;
import bone008.bukkit.deathcontrol.util.ParserUtil;

public class ChargeAction extends ActionDescriptor {

	boolean isPercentage;
	double money;
	double capMin = 0;
	double capMax = Double.POSITIVE_INFINITY;

	public ChargeAction(List<String> args) throws DescriptorFormatException {
		Iterator<String> it = args.iterator();
		while (it.hasNext()) {
			String arg = it.next().toLowerCase();

			if (arg.startsWith("min=")) {
				capMin = ParserUtil.parseDouble(arg.substring(4));
				if (capMin < 0 || capMin < capMax)
					throw new DescriptorFormatException("invalid minimum cap: " + arg.substring(4));

				it.remove();
			}
			else if (arg.startsWith("max=")) {
				capMax = ParserUtil.parseDouble(arg.substring(4));
				if (capMax < 0 || capMax < capMin)
					throw new DescriptorFormatException("invalid maximum cap: " + arg.substring(4));

				it.remove();
			}
		}

		if (args.size() != 1)
			throw new DescriptorFormatException("no cost given!");

		double pctMoney = ParserUtil.parsePercentage(args.get(0));
		if (pctMoney != -1) {
			isPercentage = true;
			money = pctMoney;
		}
		else {
			isPercentage = false;
			money = ParserUtil.parseDouble(args.get(0));
			if (money < 0)
				throw new DescriptorFormatException("invalid cost: " + args.get(0));
		}
	}

	@Override
	public ActionAgent createAgent(DeathContext context) {
		return new ChargeActionAgent(context, this);
	};
}
