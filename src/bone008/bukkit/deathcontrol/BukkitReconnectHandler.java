package bone008.bukkit.deathcontrol;

import java.util.HashMap;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class BukkitReconnectHandler implements Listener {
	
	private HashMap<String, Integer> logoffExpireTimers = new HashMap<String, Integer>();

	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerQuit(final PlayerQuitEvent event) {
		String plyName = event.getPlayer().getName();

		DeathManager m = DeathControl.instance.getManager(plyName);
		if (m != null) {
			QuitHandlerTask task = new QuitHandlerTask(m, plyName);
			int t = m.getTimeoutOnQuit();
			if (t > 0) {
				logoffExpireTimers.put(plyName, Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(DeathControl.instance, task, t * 20L));
				DeathControl.instance.log(Level.INFO, plyName + " left the game. Dropping saved items in " + t + " seconds ...");
			} else {
				task.run(); // manually execute the task to drop stuff instantly
			}
		}
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerJoin(final PlayerJoinEvent event) {
		String plyName = event.getPlayer().getName();
		if(logoffExpireTimers.containsKey(plyName)){
			Bukkit.getServer().getScheduler().cancelTask(logoffExpireTimers.get(plyName));
			logoffExpireTimers.remove(plyName);
			if(DeathControl.instance.getManager(plyName) != null)
				DeathControl.instance.log(Level.FINE, plyName + " rejoined. Expiration timer stopped.");
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
				DeathControl.instance.log(Level.INFO, "Saved items for disconnected player " + plyName + " were dropped!");
		}
	}

}
