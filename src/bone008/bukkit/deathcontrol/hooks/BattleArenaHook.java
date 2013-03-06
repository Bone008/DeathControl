package bone008.bukkit.deathcontrol.hooks;

import mc.alk.arena.BattleArena;

import org.bukkit.entity.Player;

class BattleArenaHook extends HooksManager.PluginHook {

	private static final String PLUGIN_NAME = "BattleArena";

	@Override
	String getRequiredPlugin() {
		return PLUGIN_NAME;
	}

	@Override
	public boolean shouldCancelHandling(Player player) {
		return BattleArena.inArena(player);
	}

}
