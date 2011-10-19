package bone008.bukkit.deathcontrol;

import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

public class DeathControlPlayerListener extends PlayerListener {

	private DeathControl plugin;
	
	public DeathControlPlayerListener(DeathControl plugin){
		this.plugin = plugin;
	}
	
	@Override
	public void onPlayerRespawn(PlayerRespawnEvent event){
		Player ply = event.getPlayer();
		
		DeathManager m = plugin.managers.get(ply);
		if(m != null){
			m.respawned();
		}
	}
	
	
	@Override
	public void onPlayerQuit(PlayerQuitEvent event){
		Player ply = event.getPlayer();
		
		DeathManager m = plugin.managers.get(ply);
		if(m != null){
			m.expire(false);
			plugin.log("Dropping saved items for "+ply.getName());
			plugin.log("because they left the game!");
		}
	}
	
}
