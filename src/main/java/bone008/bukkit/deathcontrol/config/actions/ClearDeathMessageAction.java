package bone008.bukkit.deathcontrol.config.actions;

import java.util.List;

import bone008.bukkit.deathcontrol.config.ActionAgent;
import bone008.bukkit.deathcontrol.config.ActionDescriptor;
import bone008.bukkit.deathcontrol.config.ActionResult;
import bone008.bukkit.deathcontrol.config.DeathContext;
import bone008.bukkit.deathcontrol.exceptions.DescriptorFormatException;

public class ClearDeathMessageAction extends ActionDescriptor {

	public ClearDeathMessageAction(List<String> args) throws DescriptorFormatException {
		if (args.size() > 0)
			throw new DescriptorFormatException("action does not take any arguments");
	}

	@Override
	public ActionAgent createAgent(DeathContext context) {
		return new ActionAgent(context, this) {
			@Override
			public void preprocess() {
				context.getDeathEvent().setDeathMessage(null);
			}

			@Override
			public ActionResult execute() {
				return null;
			}

			@Override
			public void cancel() {
			}
		};
	}

}
