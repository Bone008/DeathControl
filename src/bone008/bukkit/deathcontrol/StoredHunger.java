package bone008.bukkit.deathcontrol;

import org.bukkit.entity.Player;

/**
 * Represents a hunger state including food level, saturation and exhaustion.
 * 
 * To be treated like a struct.
 */
public class StoredHunger {

	public final int foodLevel;
	public final float saturation;
	public final float exhaustion;

	public StoredHunger(int foodLevel, float saturation, float exhaustion) {
		this.foodLevel = foodLevel;
		this.saturation = saturation;
		this.exhaustion = exhaustion;
	}

	public StoredHunger(Player source) {
		this(source.getFoodLevel(), source.getSaturation(), source.getExhaustion());
	}

}
