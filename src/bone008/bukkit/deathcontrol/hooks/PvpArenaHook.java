package bone008.bukkit.deathcontrol.hooks;

import net.slipcor.pvparena.api.PVPArenaAPI;

import org.bukkit.entity.Player;

class PvpArenaHook extends HooksManager.PluginHook {

	private static final String PLUGIN_NAME = "pvparena";

	@Override
	String getRequiredPlugin() {
		return PLUGIN_NAME;
	}

	@Override
	public boolean shouldCancelHandling(Player player) {
		return PVPArenaAPI.getArenaTeam(player) != null;
	}

}
