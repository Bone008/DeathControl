package bone008.bukkit.deathcontrol.newconfig.conditions;

import java.util.List;

import bone008.bukkit.deathcontrol.newconfig.ConditionDescriptor;
import bone008.bukkit.deathcontrol.newconfig.DeathContext;
import bone008.bukkit.deathcontrol.util.ErrorObserver;

// test class for conditions
public class CauseCondition extends ConditionDescriptor {

	private List<String> causes;

	public CauseCondition(List<String> args) {
		this.causes = args;

	}

	@Override
	public boolean matches(DeathContext context) {
		return true;
	}

}
