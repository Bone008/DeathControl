package bone008.bukkit.deathcontrol.newconfig;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.bukkit.configuration.ConfigurationSection;

import bone008.bukkit.deathcontrol.util.ErrorObserver;
import bone008.bukkit.deathcontrol.util.ParserUtil;

public class HandlingDescriptor {

	private final int priority;
	private final boolean lastHandling;
	private final int timeoutOnDisconnect;

	private final List<ConditionDescriptor> conditions;
	private final List<Boolean> invertedConditions;
	private final List<ActionDescriptor> actions;

	public HandlingDescriptor(ConfigurationSection config, ErrorObserver log) {
		this.priority = config.getInt("priority", 0);
		this.lastHandling = config.getBoolean("last-handling", false);
		this.timeoutOnDisconnect = ParserUtil.parseTime(config.getString("timeout-on-disconnect"), 30);

		Iterator<String> it;
		int i;
		String current;

		List<String> rawConditions = config.getStringList("conditions");
		this.conditions = new ArrayList<ConditionDescriptor>(rawConditions.size());
		this.invertedConditions = new ArrayList<Boolean>(rawConditions.size());

		// iterate with i for display
		for (i = 1, it = rawConditions.iterator(); it.hasNext(); i++) {
			current = it.next().trim();

			if (current.isEmpty()) {
				log.addWarning("Condition %d is empty!", i);
				continue;
			}

			String name = ParserUtil.parseOperationName(current);
			List<String> args = ParserUtil.parseOperationArgs(current);

			boolean inverted = name.startsWith("-");
			if (inverted)
				name = name.substring(1);

			ConditionDescriptor descriptor = ConditionDescriptor.createDescriptor(name, args, log);
			if (descriptor == null) {
				log.addWarning("Condition %d: condition \"%s\" not found!", i, name);
				continue;
			}

			this.conditions.add(descriptor);
			this.invertedConditions.add(inverted);
		}


		List<String> rawActions = config.getStringList("actions");
		this.actions = new ArrayList<ActionDescriptor>(rawActions.size());

		// iterate with i for display
		for (i = 1, it = rawActions.iterator(); it.hasNext(); i++) {
			current = it.next().trim();

			if (current.isEmpty()) {
				log.addWarning("Action %d is empty!", i);
				continue;
			}

			String name = ParserUtil.parseOperationName(current);
			List<String> args = ParserUtil.parseOperationArgs(current);

			ActionDescriptor descriptor = ActionDescriptor.createDescriptor(name, args, log);
			if (descriptor == null) {
				log.addWarning("Action %d: action \"%s\" not found!", i, name);
				continue;
			}

			this.actions.add(descriptor);
		}
	}

	public int getPriority() {
		return priority;
	}

	public boolean isLastHandling() {
		return lastHandling;
	}

	public int getTimeoutOnDisconnect() {
		return timeoutOnDisconnect;
	}

}
