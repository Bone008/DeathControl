package bone008.bukkit.deathcontrol.newconfig.actions;

import bone008.bukkit.deathcontrol.newconfig.ActionAgent;
import bone008.bukkit.deathcontrol.newconfig.ActionResult;
import bone008.bukkit.deathcontrol.newconfig.DeathContext;
import bone008.bukkit.deathcontrol.util.EconomyUtil;

public class ChargeActionAgent extends ActionAgent {

	private final ChargeAction action;

	public ChargeActionAgent(DeathContext context, ChargeAction action) {
		super(context, action);
		this.action = action;
	}

	@Override
	public void preprocess() {
	}

	@Override
	public ActionResult execute() {
		double cost;
		if (action.isPercentage) {
			cost = EconomyUtil.calcCost(context.getVictim(), action.money);
			if (cost > action.capMax)
				cost = action.capMax;
			if (cost < action.capMin)
				cost = action.capMin;
		}
		else {
			cost = action.money;
		}

		return (EconomyUtil.payCost(context.getVictim(), cost) ? ActionResult.STANDARD : ActionResult.FAILED);
	}

	@Override
	public void cancel() {
	}

}
