package bone008.bukkit.deathcontrol.config.conditions;

import java.util.Arrays;
import java.util.List;

import bone008.bukkit.deathcontrol.config.ConditionDescriptor;
import bone008.bukkit.deathcontrol.config.DeathContext;
import bone008.bukkit.deathcontrol.exceptions.DescriptorFormatException;

public class WorldCondition extends ConditionDescriptor {

	private String worldName;

	public WorldCondition(List<String> args) throws DescriptorFormatException {
		if (args.isEmpty())
			throw new DescriptorFormatException("no world given");

		worldName = args.get(0);
	}

	@Override
	public boolean matches(DeathContext context) {
		return context.getDeathLocation().getWorld().getName().equalsIgnoreCase(worldName);
	}

	@Override
	public List<String> toParameters() {
		return Arrays.asList(worldName);
	}

}
