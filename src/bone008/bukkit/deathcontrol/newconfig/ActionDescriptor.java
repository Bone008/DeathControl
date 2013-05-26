package bone008.bukkit.deathcontrol.newconfig;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import bone008.bukkit.deathcontrol.util.ErrorObserver;

public abstract class ActionDescriptor {

	private static final Map<String, Class<? extends ActionDescriptor>> registeredTypes = new HashMap<String, Class<? extends ActionDescriptor>>();

	public static void registerAction(String name, Class<? extends ActionDescriptor> clazz) {
		name = name.toLowerCase();
		if (registeredTypes.containsKey(name))
			throw new IllegalArgumentException("action " + name + " is already registered");

		registeredTypes.put(name, clazz);
	}

	public static ActionDescriptor createDescriptor(String name, List<String> args, ErrorObserver log) {
		try {
			return registeredTypes.get(name.toLowerCase()).getConstructor(List.class, ErrorObserver.class).newInstance(args, log);
		} catch (Exception e) {
			return null;
		}
	}

	public abstract ActionAgent createAgent(DeathContext context);

}
