package bone008.bukkit.deathcontrol.newconfig.conditions;

import java.util.List;

import bone008.bukkit.deathcontrol.exceptions.DescriptorFormatException;
import bone008.bukkit.deathcontrol.newconfig.ConditionDescriptor;
import bone008.bukkit.deathcontrol.newconfig.DeathContext;

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

}
