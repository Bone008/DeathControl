package bone008.bukkit.deathcontrol;

import org.bukkit.entity.Creeper;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Wolf;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

import bone008.bukkit.deathcontrol.config.DeathConfiguration;

public enum DeathCause {
	CONTACT("cactus", "died of a cactus"),
	SUFFOCATION("suffocation", "suffocated"),
	FALL("fall", "died of falling"),
	VOID("void", "died in the void"),
	SUICIDE("suicide", "committed suicide"),
	STARVATION("starvation", "starved"),
	LIGHTNING("lightning", "were struck by lightning"),
	MAGIC("magic", "were killed by magic"),
	POISON("poison", "were killed by poison"),
	DROWNING("drowning", "drowned"),
	LAVA("lava", "died of lava"),
	FIRE("fire", "died of fire"),
	FIRE_TICK(FIRE, "tick", null),
	EXPLOSION("explosion", "exploded"),
	MOB("mob", "were killed by a mob"),
	MOB_CREEPER(MOB, "creeper", "fell victim to a creeper"),
	MOB_WOLF(MOB, "wolf", "were killed by a wolf"),
	PLAYER("player", "were killed by a player"),
	UNKNOWN("unknown", "don't know why you died");
	
	private final String name;
	private final String meta;
	private final String msgString;
	public final DeathCause parent;
	
	private DeathCause(String name, String msgString){
		this.name = name;
		this.meta = null;
		this.msgString = msgString;
		this.parent = null;
	}
	private DeathCause(DeathCause parent, String meta, String msgString){
		if(parent == null)
			throw new IllegalArgumentException();
		this.name = parent.name;
		this.meta = meta;
		this.msgString = (msgString==null ? parent.msgString : msgString);
		this.parent = parent;
	}
	
	
	
	public String toMsgString(){
		return this.msgString;
	}
	public String toHumanString(){
		String str;
		if(meta == null)
			str = name;
		else
			str = name+DeathConfiguration.SEPARATOR+meta;
		return str.toUpperCase();
	}
	
	
	
	/**
	 * Gets a DeathCause parsed from the given parameters.
	 * @param n the main name of the cause
	 * @param m the meta for the cause, or null if there is none
	 * @return the parsed DeathCause, or null if it wasn't found
	 */
	public static DeathCause parseCause(String n, String m){
		String m_ = (m==null ? "" : m);
		
		for(DeathCause c: values()){
			String c_m = (c.meta==null ? "" : c.meta); 
			if(c.name.equalsIgnoreCase(n) && m_.equalsIgnoreCase(c_m))
				return c;
		}
		return null;
	}
	
	/**
	 * Retrieves a DeathCause from an EntityDamageEvent.
	 * @param event the event to use
	 * @return the DeathCause that fits the DamageCause of the event
	 */
	public static DeathCause getDeathCause(EntityDamageEvent event){
		if(event != null){
			DamageCause cause = event.getCause();
			
			if(cause == DamageCause.BLOCK_EXPLOSION || cause == DamageCause.ENTITY_EXPLOSION){
				if(event instanceof EntityDamageByEntityEvent && ((EntityDamageByEntityEvent)event).getDamager() instanceof Creeper)
					return DeathCause.MOB_CREEPER;
				return DeathCause.EXPLOSION;
			}
			if((cause == DamageCause.ENTITY_ATTACK || cause == DamageCause.PROJECTILE) && (event instanceof EntityDamageByEntityEvent)){
				EntityDamageByEntityEvent eEvent = (EntityDamageByEntityEvent)event;
				Entity damager = eEvent.getDamager();
				if(damager instanceof Projectile)
					damager = ((Projectile)damager).getShooter();
				
				if(damager instanceof Player)
					return DeathCause.PLAYER;
				if(damager instanceof Wolf)
					return DeathCause.MOB_WOLF;
				if(damager instanceof LivingEntity)
					return DeathCause.MOB;
				return DeathCause.UNKNOWN;
			}
			
			
			// if no special case matched, check for a direct match
			try{
				DeathCause directMatch = DeathCause.valueOf(cause.toString());
				return directMatch;
			}
			// exception thrown when not found => ignore
			catch(IllegalArgumentException e){}
		}
		
		// if no valid cause was detected
		return DeathCause.UNKNOWN;
	}
	
	
}
