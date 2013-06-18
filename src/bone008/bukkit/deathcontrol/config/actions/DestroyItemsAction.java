package bone008.bukkit.deathcontrol.config.actions;

import java.util.List;

import bone008.bukkit.deathcontrol.config.ActionAgent;
import bone008.bukkit.deathcontrol.config.DeathContext;
import bone008.bukkit.deathcontrol.exceptions.DescriptorFormatException;

public class DestroyItemsAction extends AbstractItemsAction {

	public DestroyItemsAction(List<String> args) throws DescriptorFormatException {
		parseFilter(args, true);
	}

	@Override
	public ActionAgent createAgent(DeathContext context) {
		return new DestroyItemsActionAgent(context, this);
	}

}
