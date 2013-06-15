package bone008.bukkit.deathcontrol.config;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import bone008.bukkit.deathcontrol.config.conditions.CauseCondition;
import bone008.bukkit.deathcontrol.config.conditions.FoodLevelCondition;
import bone008.bukkit.deathcontrol.config.conditions.PermissionCondition;
import bone008.bukkit.deathcontrol.config.conditions.RegionCondition;
import bone008.bukkit.deathcontrol.config.conditions.TypeCondition;
import bone008.bukkit.deathcontrol.config.conditions.WorldCondition;
import bone008.bukkit.deathcontrol.exceptions.DescriptorFormatException;
import bone008.bukkit.deathcontrol.util.ErrorObserver;

public abstract class ConditionDescriptor {

	private static final Map<String, Class<? extends ConditionDescriptor>> registeredTypes = new HashMap<String, Class<? extends ConditionDescriptor>>();

	public static void registerCondition(String name, Class<? extends ConditionDescriptor> clazz) {
		name = name.toLowerCase();
		if (registeredTypes.containsKey(name))
			throw new IllegalArgumentException("condition " + name + " is already registered");

		registeredTypes.put(name, clazz);
	}

	public static ConditionDescriptor createDescriptor(String name, List<String> args, ErrorObserver log) {
		name = name.toLowerCase();
		if (!registeredTypes.containsKey(name)) {
			log.addWarning("Condition \"%s\" not found!", name);
			return null;
		}

		try {
			ConditionDescriptor condition = registeredTypes.get(name.toLowerCase()).getConstructor(List.class).newInstance(args);
			condition.name = name;
			return condition;
		} catch (InvocationTargetException e) {
			if (e.getCause() instanceof DescriptorFormatException) {
				log.addWarning("Condition \"%s\": %s", name, e.getCause().getMessage());
			}
			else {
				e.printStackTrace();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	static {
		registerCondition("cause", CauseCondition.class);
		registerCondition("world", WorldCondition.class);
		registerCondition("permission", PermissionCondition.class);
		registerCondition("food-level", FoodLevelCondition.class);
		registerCondition("killer-type", TypeCondition.class);
		registerCondition("region", RegionCondition.class);
	}

	private String name = "";

	public abstract boolean matches(DeathContext context);

	public final String getName() {
		return name;
	}

}
