package bone008.bukkit.deathcontrol.config;

import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import bone008.bukkit.deathcontrol.config.actions.*;
import bone008.bukkit.deathcontrol.exceptions.DescriptorFormatException;
import bone008.bukkit.deathcontrol.util.ErrorObserver;

public abstract class ActionDescriptor {

	private static final Map<String, Class<? extends ActionDescriptor>> registeredTypes = new HashMap<String, Class<? extends ActionDescriptor>>();

	public static void registerAction(String name, Class<? extends ActionDescriptor> clazz) {
		name = name.toLowerCase();
		if (registeredTypes.containsKey(name))
			throw new IllegalArgumentException("action " + name + " is already registered");

		registeredTypes.put(name, clazz);
	}

	public static Set<String> getDescriptorNames() {
		return registeredTypes.keySet();
	}

	public static ActionDescriptor createDescriptor(String name, List<String> args, ErrorObserver log) {
		name = name.toLowerCase();
		if (!registeredTypes.containsKey(name)) {
			log.addWarning("Action \"%s\" not found!", name);
			return null;
		}

		try {
			ActionDescriptor action = registeredTypes.get(name.toLowerCase()).getConstructor(List.class).newInstance(args);
			action.name = name;
			return action;
		} catch (InvocationTargetException e) {
			if (e.getCause() instanceof DescriptorFormatException) {
				log.addWarning("Action \"%s\": %s", name, e.getCause().getMessage());
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
		registerAction("keep-items", KeepItemsAction.class);
		registerAction("keep-experience", KeepExperienceAction.class);
		registerAction("keep-hunger", KeepHungerAction.class);
		registerAction("charge", ChargeAction.class);
		registerAction("destroy-item", DestroyItemAction.class);
		registerAction("destroy-items", DestroyItemsAction.class);
		registerAction("damage-items", DamageItemsAction.class);
		registerAction("message", MessageAction.class);
		registerAction("broadcast", BroadcastAction.class);
		registerAction("command", CommandAction.class);
		registerAction("wait", WaitAction.class);
		registerAction("clear-death-message", ClearDeathMessageAction.class);
	}

	private String name = "";
	private boolean required = false;

	public final void setRequired(boolean required) {
		this.required = required;
	}

	public final boolean isRequired() {
		return required;
	}

	public final String getName() {
		return name;
	}

	public abstract ActionAgent createAgent(DeathContext context);

	public List<String> toParameters() {
		return Collections.emptyList();
	}

}
