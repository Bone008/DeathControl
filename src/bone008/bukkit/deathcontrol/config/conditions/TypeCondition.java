package bone008.bukkit.deathcontrol.config.conditions;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Wolf;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;

import bone008.bukkit.deathcontrol.config.ConditionDescriptor;
import bone008.bukkit.deathcontrol.config.DeathContext;
import bone008.bukkit.deathcontrol.exceptions.DescriptorFormatException;
import bone008.bukkit.deathcontrol.util.Util;

public class TypeCondition extends ConditionDescriptor {

	private static enum SpecialType {
		MONSTER,
		PROJECTILE,
		TAMED_WOLF;
	}

	private Set<EntityType> basicTypes = EnumSet.noneOf(EntityType.class);
	private Set<SpecialType> specialTypes = EnumSet.noneOf(SpecialType.class);

	public TypeCondition(List<String> args) throws DescriptorFormatException {
		if (args.isEmpty())
			throw new DescriptorFormatException("no type given");

		for (String input : args) {
			String inputEnum = input.toUpperCase().replace('-', '_');

			try {
				// try to match a special type
				specialTypes.add(SpecialType.valueOf(inputEnum));
			} catch (IllegalArgumentException e) {
				// fall back to looking for a match in the EntityType enum
				EntityType basicType = EntityType.fromName(input);

				if (basicType == null) {
					try {
						// try to match by the enum constant name
						basicType = EntityType.valueOf(inputEnum);
					} catch (IllegalArgumentException e2) {
						// nope, actually invalid
						throw new DescriptorFormatException("invalid type: " + input);
					}
				}

				basicTypes.add(basicType);
			}
		}
	}

	@Override
	public boolean matches(DeathContext context) {
		EntityDamageEvent dmgEvent = context.getVictim().getLastDamageCause();
		EntityDamageByEntityEvent dmgBEEvent = (dmgEvent instanceof EntityDamageByEntityEvent ? (EntityDamageByEntityEvent) dmgEvent : null);


		// check for any EntityType matches
		if (dmgBEEvent != null) {
			Entity damager = dmgBEEvent.getDamager();

			for (EntityType etype : basicTypes) {
				// check direct attacker
				if (etype == damager.getType())
					return true;

				// check projectile shooter
				if (damager instanceof Projectile) {
					damager = ((Projectile) damager).getShooter();
					if (damager != null && etype == damager.getType())
						return true;
				}
			}
		}

		// check for any SpecialType matches
		for (SpecialType special : specialTypes) {
			boolean match = false;

			switch (special) {
			case MONSTER:
				match = Util.getAttackerFromEvent(dmgBEEvent) instanceof Monster; // assume that a potential projectile itself can't be a monster
				break;
			case PROJECTILE:
				match = dmgBEEvent != null && dmgBEEvent.getDamager() instanceof Projectile; // check the real damager, not the shooter
				break;
			case TAMED_WOLF:
				match = dmgBEEvent != null && dmgBEEvent.getDamager() instanceof Wolf && ((Wolf) dmgBEEvent.getDamager()).isTamed(); // wolves don't shoot projectiles
				break;
			default:
				throw new IllegalStateException("unknown special enum member: " + special);
			}

			if (match)
				return true;
		}

		// if nothing at all matched, we don't match
		return false;
	}
}
