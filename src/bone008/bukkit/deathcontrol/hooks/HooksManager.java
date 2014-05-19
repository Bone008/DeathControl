package bone008.bukkit.deathcontrol.hooks;

import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import bone008.bukkit.deathcontrol.DeathControl;

public final class HooksManager {

	private HooksManager() {
	}

	// @formatter:off
	private final static PluginHook[] hooks = {
		new MobArenaHook(),
		new BattleArenaHook(),
		new PvpArenaHook()
	};
	// @formatter:on

	public static boolean shouldCancelDeathHandling(Player player) {
		for (PluginHook hook : hooks) {
			if (isPluginEnabled(hook.getRequiredPlugin())) {
				try {
					if (hook.shouldCancelHandling(player))
						return true;
				} catch (Throwable e) {
					DeathControl.instance.log(Level.SEVERE, "An error occurred while checking integrity with plugin \"" + hook.getRequiredPlugin() + "\"!");
					e.printStackTrace();
					// now ignore the hook
				}
			}
		}

		return false;
	}

	// hook utils
	static boolean isPluginEnabled(String name) {
		return Bukkit.getPluginManager().isPluginEnabled(name);
	}

	static Plugin getServerPlugin(String name) {
		return Bukkit.getPluginManager().getPlugin(name);
	}

	static abstract class PluginHook {
		/**
		 * Returns the name of the plugin that this hook requires to be executed.
		 * 
		 * @return the name, or null if it should <b>always</b> be called
		 */
		abstract String getRequiredPlugin();

		/**
		 * Checks if the death handling of the given player should be cancelled.
		 * This is only called if the plugin returned by {@link #getRequiredPlugin()} is enabled.
		 * 
		 * @param player the player to check
		 * 
		 * @return true if DeathControl should cancel the handling, false otherwise
		 */
		abstract boolean shouldCancelHandling(Player player);
	}

}
