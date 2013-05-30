package bone008.bukkit.deathcontrol.newconfig.conditions;

import java.util.List;

import bone008.bukkit.deathcontrol.exceptions.DescriptorFormatException;
import bone008.bukkit.deathcontrol.newconfig.ConditionDescriptor;
import bone008.bukkit.deathcontrol.newconfig.DeathContext;

// test class for conditions
public class RegionCondition extends ConditionDescriptor {

	private String regionName;

	public RegionCondition(List<String> args) throws DescriptorFormatException {
		if (args.isEmpty())
			throw new DescriptorFormatException("no region given");

		regionName = args.get(0);
	}

	@Override
	public boolean matches(DeathContext context) {
		return false;
	}

}
