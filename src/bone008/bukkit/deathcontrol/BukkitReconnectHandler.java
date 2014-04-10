package bone008.bukkit.deathcontrol;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;

public class BukkitReconnectHandler implements Listener {

	private Map<UUID, QuitHandlerTask> logoffExpireTimers = new HashMap<UUID, QuitHandlerTask>();

	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerQuit(final PlayerQuitEvent event) {
		Player player = event.getPlayer();

		DeathContextImpl context = DeathControl.instance.getActiveDeath(player.getUniqueId());
		if (context != null) {
			QuitHandlerTask task = new QuitHandlerTask(context, player.getUniqueId());

			int t = context.getDisconnectTimeout();
			if (t > 0) {
				task.runTaskLater(DeathControl.instance, 20L * t);
				logoffExpireTimers.put(player.getUniqueId(), task);
				DeathControl.instance.log(Level.INFO, player.getName() + " left the game. Cancelling in " + t + " seconds ...");
			}
			else if (t == 0) {
				task.run(); // manually execute the task to drop stuff instantly
				DeathControl.instance.log(Level.INFO, player.getName() + " left the game. Cancelling now ...");
			}
			// don't do anything if infinite timeout
		}
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerJoin(final PlayerJoinEvent event) {
		Player player = event.getPlayer();

		if (logoffExpireTimers.containsKey(player.getUniqueId())) {
			logoffExpireTimers.remove(player.getUniqueId()).cancel();

			// if player still has an active death
			if (DeathControl.instance.getActiveDeath(player.getUniqueId()) != null)
				DeathControl.instance.log(Level.FINE, player.getName() + " rejoined. Expiration timer stopped.");
		}
	}

	private class QuitHandlerTask extends BukkitRunnable {
		private final DeathContextImpl context;
		private final UUID victimUniqueId;

		public QuitHandlerTask(DeathContextImpl context, UUID victimUniqueId) {
			this.context = context;
			this.victimUniqueId = victimUniqueId;
		}

		@Override
		public void run() {
			if (!context.isCancelled()) {
				context.cancel();
				DeathControl.instance.log(Level.INFO, "Death handling for disconnected player " + context.getVictim().getName() + " was cancelled!");
			}

			logoffExpireTimers.remove(victimUniqueId);
		}
	}

}
