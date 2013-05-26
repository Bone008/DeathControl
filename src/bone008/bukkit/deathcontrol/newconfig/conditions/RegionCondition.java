package bone008.bukkit.deathcontrol.newconfig.conditions;

import java.util.List;

import bone008.bukkit.deathcontrol.newconfig.ConditionDescriptor;
import bone008.bukkit.deathcontrol.newconfig.DeathContext;

// test class for conditions
public class RegionCondition extends ConditionDescriptor {

	private String regionName;

	public RegionCondition(List<String> args) {
		if (args.isEmpty())
			throw new RuntimeException("no region given");

		regionName = args.get(0);
	}

	@Override
	public boolean matches(DeathContext context) {
		return true;
	}

}
