package bone008.bukkit.deathcontrol.newconfig.actions;

import bone008.bukkit.deathcontrol.newconfig.ActionAgent;
import bone008.bukkit.deathcontrol.newconfig.ActionResult;
import bone008.bukkit.deathcontrol.newconfig.DeathContext;
import bone008.bukkit.deathcontrol.newconfig.PreprocessResult;
import bone008.bukkit.deathcontrol.util.ExperienceUtil;
import bone008.bukkit.deathcontrol.util.Util;

public class KeepExperienceActionAgent extends ActionAgent {

	private final double keepPct;
	private final boolean dropLeftovers;

	private int stored;
	private int preventedFromDropping;

	public KeepExperienceActionAgent(DeathContext context, double keepPct, boolean dropLeftovers) {
		super(context);
		this.keepPct = keepPct;
		this.dropLeftovers = dropLeftovers;
	}

	@Override
	public PreprocessResult preprocess() {
		stored = (int) Math.round(ExperienceUtil.getCurrentExp(context.getVictim()) * keepPct);

		int dropped = (int) Math.round(context.getDeathEvent().getDroppedExp() * (1 - keepPct));

		if (!dropLeftovers)
			dropped = 0;

		preventedFromDropping = context.getDeathEvent().getDroppedExp() - dropped;
		context.getDeathEvent().setDroppedExp(dropped);

		return null;
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
