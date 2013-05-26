package bone008.bukkit.deathcontrol.newconfig.conditions;

import java.util.ArrayList;
import java.util.List;

import bone008.bukkit.deathcontrol.DeathCause;
import bone008.bukkit.deathcontrol.newconfig.ConditionDescriptor;
import bone008.bukkit.deathcontrol.newconfig.DeathContext;

public class CauseCondition extends ConditionDescriptor {

	private List<DeathCause> causes;

	public CauseCondition(List<String> args) {
		this.causes = new ArrayList<DeathCause>();
		for (String arg : args) {
			DeathCause cause = DeathCause.parseCause(arg, null);
			if (cause == null)
				throw new RuntimeException("invalid death cause: " + arg);

			causes.add(cause);
		}

	}

	@Override
	public boolean matches(DeathContext context) {
		for (DeathCause cause : causes) {
			if (context.getDeathCause() == cause)
				return true;
		}

		return false;
	}

}
