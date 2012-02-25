package bone008.bukkit.deathcontrol;

import java.util.HashMap;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

public class DeathControlPlayerListener implements Listener {

	private DeathControl plugin;
	private HashMap<String, Integer> logoffExpireTimers = new HashMap<String, Integer>();

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
			QuitHandlerTask task = new QuitHandlerTask(m, plyName);
			int t = m.getTimeoutOnQuit();
			if (t > 0) {
				logoffExpireTimers.put(plyName, plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, task, t * 20L));
				plugin.log(plyName + " left the game. Dropping saved items in " + t + " seconds ...");
			} else {
				task.run();
			}
		}
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerJoin(final PlayerJoinEvent event) {
		String plyName = event.getPlayer().getName();
		if(logoffExpireTimers.containsKey(plyName)){
			plugin.getServer().getScheduler().cancelTask(logoffExpireTimers.get(plyName));
			logoffExpireTimers.remove(plyName);
			if(plugin.getManager(plyName) != null)
				plugin.log(plyName + " rejoined. Expiration timer stopped.");
		}
	}

	private class QuitHandlerTask implements Runnable {
		private final DeathManager manager;
		private final String plyName;

		public QuitHandlerTask(final DeathManager m, final String pn) {
			manager = m;
			plyName = pn;
		}

		@Override
		public void run() {
			if(manager.expire(false))
				plugin.log("Saved items for disconnected player " + plyName + " were dropped!");
		}
	}

}
