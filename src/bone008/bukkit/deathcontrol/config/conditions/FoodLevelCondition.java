package bone008.bukkit.deathcontrol.config.conditions;

import java.util.List;

import bone008.bukkit.deathcontrol.Operator;
import bone008.bukkit.deathcontrol.config.ConditionDescriptor;
import bone008.bukkit.deathcontrol.config.DeathContext;
import bone008.bukkit.deathcontrol.exceptions.DescriptorFormatException;
import bone008.bukkit.deathcontrol.util.ParserUtil;

public class FoodLevelCondition extends ConditionDescriptor {

	private final Operator operator;
	private final int number;

	public FoodLevelCondition(List<String> args) throws DescriptorFormatException {
		if (args.size() != 2)
			throw new DescriptorFormatException("exactly 2 arguments needed");

		operator = Operator.parse(args.get(0));
		number = ParserUtil.parseInt(args.get(1));

		if (operator == null)
			throw new DescriptorFormatException("invalid operator \"" + args.get(0) + "\"!");
		if (number < 0 || number > 20)
			throw new DescriptorFormatException("invalid food level \"" + args.get(1) + "\": only numbers between 0 and 20 are allowed!");
	}

	@Override
	public boolean matches(DeathContext context) {
		return operator.invokeInt(context.getVictim().getPlayer().getFoodLevel(), number);
	}

}
