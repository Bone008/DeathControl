package bone008.bukkit.deathcontrol;

import org.bukkit.entity.Entity;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

import bone008.bukkit.deathcontrol.util.Util;

public enum DeathCause {
	// Order of declaration does matter:
	// Causes defined later in the list are considered more generic.
	// If multiple causes apply to a death, the one with the lowest ordinal is the primary cause.
	// This is only relevant for overlapping causes.

	CONTACT("cactus"),
	DROWNING("drowning"),
	EXPLOSION("explosion"),
	FALL("fall"),
	FALLING_BLOCK("fallingblock"),
	FIRE("fire"),
	FIRE_TICK("firetick"),
	LAVA("lava"),
	LIGHTNING("lightning"),
	MAGIC("magic"),
	MOB("mob"),
	MONSTER("monster"),
	PLAYER("pvp"),
	POISON("poison"),
	STARVATION("starvation"),
	SUFFOCATION("suffocation"),
	SUICIDE("suicide"),
	THORNS("thorns"),
	VOID("void"),
	WITHER("wither"),

	UNKNOWN("unknown");

	private final String name;

	private DeathCause(String name) {
		this.name = name;
	}

	/**
	 * Checks whether this death cause is applicable for the given damage event.<br>
	 * Considers the {@link DamageCause} as well as other information associated with the event.
	 * 
	 * @param event an {@link EntityDamageEvent} to check
	 * 
	 * @return true if this cause matches the event, false otherwise
	 */
	public boolean appliesTo(EntityDamageEvent event) {
		DamageCause cause = event.getCause();

		switch (this) {
		case CONTACT:
		case DROWNING:
		case FALL:
		case FALLING_BLOCK:
		case FIRE_TICK:
		case LAVA:
		case LIGHTNING:
		case MAGIC:
		case POISON:
		case STARVATION:
		case SUFFOCATION:
		case SUICIDE:
		case THORNS:
		case VOID:
		case WITHER:
			// direct equivalent in the DamageCause enum
			return cause == DamageCause.valueOf(name());

			// special death causes with more complex logic here

		case EXPLOSION:
			return cause == DamageCause.ENTITY_EXPLOSION || cause == DamageCause.BLOCK_EXPLOSION;

		case FIRE:
			return cause == DamageCause.FIRE || cause == DamageCause.FIRE_TICK;

		case MOB: {
			Entity attacker = Util.getAttackerFromEvent(event);
			return attacker instanceof LivingEntity && !(attacker instanceof HumanEntity);
		}

		case MONSTER: {
			Entity attacker = Util.getAttackerFromEvent(event);
			return attacker instanceof Monster;
		}

		case PLAYER:
			return Util.getPlayerAttackerFromEvent(event) != null;

		case UNKNOWN:
			// check if anything else matches
			for (DeathCause dc : values()) {
				if (dc == UNKNOWN)
					continue;
				if (dc.appliesTo(event))
					return false;
			}
			return true;

		default:
			throw new Error("unimplemented death cause: " + this);
		}
	}

	public String toHumanString() {
		return name;
	}

	public String toMsgPath() {
		return "cause-reasons." + toHumanString();
	}

	/**
	 * Gets a DeathCause parsed from the given parameters.
	 * 
	 * @param name the main name of the cause
	 * 
	 * @return the parsed DeathCause, or null if it wasn't found
	 */
	public static DeathCause parseCause(String name) {
		for (DeathCause dc : values()) {
			if (dc.name.equalsIgnoreCase(name))
				return dc;
		}
		return null;
	}

}
