package bone008.bukkit.deathcontrol.config.actions;

import org.bukkit.entity.Player;

import bone008.bukkit.deathcontrol.config.ActionAgent;
import bone008.bukkit.deathcontrol.config.ActionResult;
import bone008.bukkit.deathcontrol.config.DeathContext;

public class KeepHungerActionAgent extends ActionAgent {

	// currently can't be retrieved through the Bukkit API, so we have to assume that it's 20
	private static final int MAX_FOOD_LEVEL = 20;

	private final KeepHungerAction action;

	private int foodLevel;
	private float saturation;
	private float exhaustion;

	public KeepHungerActionAgent(DeathContext context, KeepHungerAction action) {
		super(context, action);
		this.action = action;
	}

	@Override
	public void preprocess() {
		Player p = context.getVictim().getPlayer();
		foodLevel = p.getFoodLevel();
		saturation = p.getSaturation();
		exhaustion = p.getExhaustion();

		// apply percentage setting
		foodLevel = MAX_FOOD_LEVEL - (int) Math.round((MAX_FOOD_LEVEL - foodLevel) * action.keepPct);
	}

	@Override
	public ActionResult execute() {
		if (!context.getVictim().isOnline()) {
			return ActionResult.PLAYER_OFFLINE;
		}

		Player p = context.getVictim().getPlayer();
		p.setFoodLevel(foodLevel);
		p.setSaturation(saturation);
		p.setExhaustion(exhaustion);

		return null;
	}

	@Override
	public void cancel() {
	}
}
