package bone008.bukkit.deathcontrol;

import bone008.bukkit.deathcontrol.DeathManager.Response;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityListener;
import org.bukkit.event.entity.PlayerDeathEvent;

import java.util.logging.Level;

public class DeathControlEntityListener extends EntityListener{
	
	private DeathControl plugin;
	
	public DeathControlEntityListener(DeathControl plugin){
		this.plugin = plugin;
	}
	
	@Override
	public void onEntityDeath(EntityDeathEvent event){
		if(!(event instanceof PlayerDeathEvent)){
			return;
		}
		
		PlayerDeathEvent e = (PlayerDeathEvent)event;
		assert (e.getEntity() instanceof Player);
		Player ply = (Player)e.getEntity();
		
		
		EntityDamageEvent damageEvent = ply.getLastDamageCause();
		DeathCause deathCause = DeathCause.getDeathCause(damageEvent);
		
		DeathManager manager = new DeathManager(plugin, ply, deathCause, event.getDrops());
		Response ret = manager.handle();
		
		
		
		if(ret.didSomething){
			// build the logs to the console
			StringBuilder	log1Builder = new StringBuilder(),
							log2Builder = new StringBuilder();
			
			log1Builder.append(ply.getName()).append(" died (cause: ").append(deathCause.toMsgString()).append(")");
			
			log2Builder
			.append("Handling death:\n")
			.append("| Player: ").append(ply.getName()).append('\n')
			.append("| Death cause: ").append(deathCause.toHumanString()).append('\n')
			.append("| Kept items: ");
			switch(ret.keptItems){
			case Response.KEPT_NONE: log2Builder.append("none"); break;
			case Response.KEPT_SOME: log2Builder.append("some"); break;
			case Response.KEPT_ALL:  log2Builder.append("all"); break;
			}
			log2Builder.append("\n")
			.append("| Method: ").append(ret.isCommand ? "command" : "auto").append("\n");
			
			
			if(ret.success){
				if(ret.keptItems != Response.KEPT_NONE){
					plugin.display(ply, ChatColor.YELLOW+"You keep "+ ChatColor.WHITE + (ret.keptItems==Response.KEPT_ALL ? "all":"some") + ChatColor.YELLOW+" of your items");
					plugin.display(ply, ChatColor.YELLOW+"because you "+ deathCause.toMsgString()+".");
					if(ret.isCommand){
						plugin.display(ply, ChatColor.YELLOW+"You can get them back with "+ChatColor.GREEN+"/death back");
						if(manager.getTimeout() > 0){
							log2Builder.append("| Expires in ").append(manager.getTimeout()).append(" seconds!\n");
							plugin.display(ply, ChatColor.RED+"This will expire in "+manager.getTimeout()+" seconds!");
						}
					}
				}
				if(ret.money > 0 && plugin.getRegisterMethod()!=null){
					String moneyStr = plugin.getRegisterMethod().format(ret.money);
					log1Builder.append("; paid ").append(moneyStr);
					log2Builder.append("| Paid money: ").append(moneyStr).append("\n");
					plugin.display(ply, ChatColor.GOLD+"This "+(ret.isCommand ? "will cost" : "costs")+" you "+ChatColor.WHITE+moneyStr+ChatColor.GOLD+"!");
				}
			} else if(ret.money == null){
				log1Builder.append("; not enough money");
				log2Builder.append("| Not enough money!\n");
				plugin.display(ply, ChatColor.RED+"You couldn't keep your items");
				plugin.display(ply, ChatColor.RED+"because you didn't have enough money!");
			} else{
				plugin.display(ply, ChatColor.RED+"A disruption in space-time!");
				plugin.display(ply, ChatColor.RED+"In other words: A bug in this plugin!");
				plugin.display(ply, ChatColor.RED+"This was not supposed to happen.");
				plugin.log(Level.SEVERE, "The manager returned an invalid response! Please report this bug!");
			}
			
			if(plugin.config.loggingLevel == 1)
				plugin.log(log1Builder.toString().trim());
			else if(plugin.config.loggingLevel == 2)
				plugin.log(log2Builder.toString().trim());
			// else do nothing -> no logging
		}
	}
	
}
