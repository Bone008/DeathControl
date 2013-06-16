package bone008.bukkit.deathcontrol.config;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;

import org.bukkit.configuration.ConfigurationSection;

import bone008.bukkit.deathcontrol.DeathContextImpl;
import bone008.bukkit.deathcontrol.DeathControl;
import bone008.bukkit.deathcontrol.util.ErrorObserver;
import bone008.bukkit.deathcontrol.util.ParserUtil;

public class HandlingDescriptor implements Comparable<HandlingDescriptor> {

	private final String name;
	private final int priority;
	private final boolean lastHandling;
	private final int timeoutOnDisconnect;

	private final List<ConditionDescriptor> conditions;
	private final List<Boolean> expectedConditionResults;
	private final List<ActionDescriptor> actions;

	public HandlingDescriptor(String name, ConfigurationSection config, ErrorObserver log) {
		this.name = name;
		this.priority = config.getInt("priority-order", 0);
		this.lastHandling = !config.getBoolean("allow-others", false);
		this.timeoutOnDisconnect = ParserUtil.parseTime(config.getString("timeout-on-disconnect"), 30);

		Iterator<String> it;
		int i;
		String current;

		List<String> rawConditions = config.getStringList("conditions");
		this.conditions = new ArrayList<ConditionDescriptor>(rawConditions.size());
		this.expectedConditionResults = new ArrayList<Boolean>(rawConditions.size());

		// iterate with i for display
		for (i = 1, it = rawConditions.iterator(); it.hasNext(); i++) {
			current = it.next().trim();

			if (current.isEmpty()) {
				log.addWarning("Condition %d is empty!", i);
				continue;
			}

			String opName = ParserUtil.parseOperationName(current);
			List<String> opArgs = ParserUtil.parseOperationArgs(current);

			boolean inverted = opName.startsWith("-");
			if (inverted)
				opName = opName.substring(1);

			ConditionDescriptor descriptor = ConditionDescriptor.createDescriptor(opName, opArgs, log);
			if (descriptor == null)
				continue;

			this.conditions.add(descriptor);
			this.expectedConditionResults.add(!inverted);
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

			String opName = ParserUtil.parseOperationName(current);
			List<String> opArgs = ParserUtil.parseOperationArgs(current);

			boolean required = opName.equalsIgnoreCase("require") || opName.equalsIgnoreCase("required");
			if (required) {
				// shift
				opName = opArgs.remove(0);
			}

			ActionDescriptor descriptor = ActionDescriptor.createDescriptor(opName, opArgs, log);
			if (descriptor == null)
				continue;

			descriptor.setRequired(required);

			this.actions.add(descriptor);
		}
	}

	public boolean areConditionsMet(DeathContext context) {
		DeathControl.instance.log(Level.FINEST, "@" + context.getVictim().getName() + ":  \"" + name + "\" is checking conditions ...");

		for (int i = 0; i < conditions.size(); i++) {
			ConditionDescriptor condition = conditions.get(i);

			try {
				boolean matched = condition.matches(context);
				if (matched != expectedConditionResults.get(i)) {
					DeathControl.instance.log(Level.FINEST, "    \"" + condition.getName() + "\" failed");
					return false;
				}
				else
					DeathControl.instance.log(Level.FINEST, "    \"" + condition.getName() + "\" matched");
			} catch (Throwable e) {
				DeathControl.instance.getLogger().log(Level.SEVERE, "Condition check \"" + condition.getName() + "\" threw an exception!", e);
				return false;
			}
		}

		return true;
	}

	public void assignAgents(DeathContextImpl context) {
		for (ActionDescriptor action : actions)
			context.assignAgent(action.createAgent(context));
	}

	public String getName() {
		return name;
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

	@Override
	public int compareTo(HandlingDescriptor other) {
		if (this.priority == other.priority)
			// to be consistent with equals, we need to give equal priorities an order instead of returning 0
			return -1;

		return this.priority - other.priority;
	}

}
