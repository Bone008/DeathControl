package bone008.bukkit.deathcontrol.newconfig.actions;

import java.util.List;

import bone008.bukkit.deathcontrol.newconfig.ActionAgent;
import bone008.bukkit.deathcontrol.newconfig.ActionDescriptor;
import bone008.bukkit.deathcontrol.newconfig.DeathContext;
import bone008.bukkit.deathcontrol.util.Util;

public class MessageAction extends ActionDescriptor {

	private String message;

	public MessageAction(List<String> args) {
		message = Util.joinCollection(" ", args);
	}

	@Override
	public ActionAgent createAgent(DeathContext context) {
		return new MessageActionAgent(context, message);
	}
}
