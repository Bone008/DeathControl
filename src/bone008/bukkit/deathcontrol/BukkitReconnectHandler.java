package bone008.bukkit.deathcontrol;

import java.util.HashMap;
import java.util.logging.Level;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;

public class BukkitReconnectHandler implements Listener {

	private HashMap<String, QuitHandlerTask> logoffExpireTimers = new HashMap<String, QuitHandlerTask>();

	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerQuit(final PlayerQuitEvent event) {
		Player player = event.getPlayer();

		DeathContextImpl context = DeathControl.instance.getActiveDeath(player);
		if (context != null) {
			QuitHandlerTask task = new QuitHandlerTask(context);

			int t = context.getDisconnectTimeout();
			if (t > 0) {
				task.runTaskLater(DeathControl.instance, 20L * t);
				logoffExpireTimers.put(player.getName(), task);
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

		if (logoffExpireTimers.containsKey(player.getName())) {
			logoffExpireTimers.remove(player.getName()).cancel();

			// if player still has an active death
			if (DeathControl.instance.getActiveDeath(player) != null)
				DeathControl.instance.log(Level.FINE, player.getName() + " rejoined. Expiration timer stopped.");
		}
	}

	private class QuitHandlerTask extends BukkitRunnable {
		private final DeathContextImpl context;

		public QuitHandlerTask(DeathContextImpl context) {
			this.context = context;
		}

		@Override
		public void run() {
			if (!context.isCancelled()) {
				context.cancel();
				DeathControl.instance.log(Level.INFO, "Death handling for disconnected player " + context.getVictim().getName() + " was cancelled!");
			}

			logoffExpireTimers.remove(context.getVictim().getName());
		}
	}

}
