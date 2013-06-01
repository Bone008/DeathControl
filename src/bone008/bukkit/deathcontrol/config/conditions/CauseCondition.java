package bone008.bukkit.deathcontrol.config.conditions;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.event.entity.EntityDamageEvent;

import bone008.bukkit.deathcontrol.DeathCause;
import bone008.bukkit.deathcontrol.config.ConditionDescriptor;
import bone008.bukkit.deathcontrol.config.DeathContext;
import bone008.bukkit.deathcontrol.exceptions.DescriptorFormatException;

public class CauseCondition extends ConditionDescriptor {

	private List<DeathCause> causes = new ArrayList<DeathCause>();

	public CauseCondition(List<String> args) throws DescriptorFormatException {
		if (args.isEmpty())
			throw new DescriptorFormatException("no causes given");

		for (String arg : args) {
			DeathCause cause = DeathCause.parseCause(arg);
			if (cause == null)
				throw new DescriptorFormatException("invalid death cause: " + arg);

			causes.add(cause);
		}

	}

	@Override
	public boolean matches(DeathContext context) {
		EntityDamageEvent lastDamage = context.getVictim().getLastDamageCause();

		for (DeathCause cause : causes) {
			if (cause.appliesTo(lastDamage))
				return true;
		}

		return false;
	}
}
