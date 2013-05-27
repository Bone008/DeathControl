package bone008.bukkit.deathcontrol.newconfig.actions;

import bone008.bukkit.deathcontrol.newconfig.ActionAgent;
import bone008.bukkit.deathcontrol.newconfig.ActionResult;
import bone008.bukkit.deathcontrol.newconfig.DeathContext;
import bone008.bukkit.deathcontrol.util.ExperienceUtil;
import bone008.bukkit.deathcontrol.util.Util;

public class KeepExperienceActionAgent extends ActionAgent {

	private final KeepExperienceAction action;

	private int stored;
	private int preventedFromDropping;

	public KeepExperienceActionAgent(DeathContext context, KeepExperienceAction action) {
		super(context, action);
		this.action = action;
	}

	@Override
	public void preprocess() {
		stored = (int) Math.round(ExperienceUtil.getCurrentExp(context.getVictim()) * action.keepPct);

		int dropped = (int) Math.round(context.getDeathEvent().getDroppedExp() * (1 - action.keepPct));

		if (!action.dropLeftovers)
			dropped = 0;

		preventedFromDropping = context.getDeathEvent().getDroppedExp() - dropped;
		context.getDeathEvent().setDroppedExp(dropped);
	}

	@Override
	public ActionResult execute() {
		ExperienceUtil.changeExp(context.getVictim(), stored);
		return null;
	}

	@Override
	public void cancel() {
		if (preventedFromDropping > 0)
			Util.dropExp(context.getDeathLocation(), preventedFromDropping);
	}
}
