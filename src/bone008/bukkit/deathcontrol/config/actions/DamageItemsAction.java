package bone008.bukkit.deathcontrol.config.actions;

import java.util.List;

import bone008.bukkit.deathcontrol.config.ActionAgent;
import bone008.bukkit.deathcontrol.config.DeathContext;
import bone008.bukkit.deathcontrol.exceptions.DescriptorFormatException;
import bone008.bukkit.deathcontrol.util.ParserUtil;

public class DamageItemsAction extends AbstractItemsAction {

	double damagePct;

	public DamageItemsAction(List<String> args) throws DescriptorFormatException {
		if (args.isEmpty())
			throw new DescriptorFormatException("no damage percentage given");

		damagePct = ParserUtil.parsePercentage(args.remove(0));

		if (damagePct == -1 || damagePct > 1)
			throw new DescriptorFormatException("invalid damage percentage!");

		parseFilter(args, false);
	}


	@Override
	public ActionAgent createAgent(DeathContext context) {
		return new DamageItemsActionAgent(context, this);
	}

}
