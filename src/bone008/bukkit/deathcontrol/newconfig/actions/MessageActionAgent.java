package bone008.bukkit.deathcontrol.newconfig.actions;

import bone008.bukkit.deathcontrol.newconfig.ActionAgent;
import bone008.bukkit.deathcontrol.newconfig.ActionResult;
import bone008.bukkit.deathcontrol.newconfig.DeathContext;
import bone008.bukkit.deathcontrol.newconfig.PreprocessResult;
import bone008.bukkit.deathcontrol.util.MessageUtil;

public class MessageActionAgent extends ActionAgent {

	private final String message;

	public MessageActionAgent(DeathContext context, String message) {
		super(context);
		this.message = message;
	}

	@Override
	public PreprocessResult preprocess() {
		return null;
	}

	@Override
	public ActionResult execute() {
		MessageUtil.sendMessage(context.getVictim(), message);
		return null;
	}

	@Override
	public void cancel() {
	}

}
