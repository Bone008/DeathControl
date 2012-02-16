package bone008.bukkit.deathcontrol;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

public class DeathControlPlayerListener implements Listener {

	private DeathControl plugin;

	public DeathControlPlayerListener(DeathControl plugin) {
		this.plugin = plugin;
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void onPlayerRespawn(final PlayerRespawnEvent event) {
		final String playerName = event.getPlayer().getName();

		// delay this for the next tick to make sure the player fully respawned
		// to get the correct location
		// don't use getRespawnLocation(), because it might still be changed by
		// another plugin - this way is safer
		plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
			@Override
			public void run() {
				DeathManager m = plugin.getManager(playerName);
				if (m != null) {
					m.respawned();
				}
			}
		});
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerQuit(final PlayerQuitEvent event) {
		String plyName = event.getPlayer().getName();

		DeathManager m = plugin.getManager(plyName);
		if (m != null) {
			m.expire(false);
			plugin.log("Dropping saved items for " + plyName + " because they left the game!");
		}
	}

}
