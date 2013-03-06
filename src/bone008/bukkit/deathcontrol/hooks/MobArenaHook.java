package bone008.bukkit.deathcontrol.hooks;

import org.bukkit.entity.Player;

import com.garbagemule.MobArena.MobArenaHandler;

class MobArenaHook extends HooksManager.PluginHook {

	private static final String PLUGIN_NAME = "MobArena";

	private MobArenaHandler maHandler = null;

	@Override
	String getRequiredPlugin() {
		return PLUGIN_NAME;
	}

	@Override
	public boolean shouldCancelHandling(Player player) {
		if (maHandler == null)
			maHandler = new MobArenaHandler();

		return maHandler.isPlayerInArena(player);
	}

}
