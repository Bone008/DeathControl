package bone008.bukkit.deathcontrol.config.actions;

import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import bone008.bukkit.deathcontrol.config.ActionAgent;
import bone008.bukkit.deathcontrol.config.ActionDescriptor;
import bone008.bukkit.deathcontrol.config.ActionResult;
import bone008.bukkit.deathcontrol.config.DeathContext;
import bone008.bukkit.deathcontrol.util.Util;

public class MessageAction extends ActionDescriptor {

	private final String message;

	public MessageAction(List<String> args) {
		message = ChatColor.translateAlternateColorCodes('&', Util.joinCollection(" ", args));
	}

	@Override
	public ActionAgent createAgent(DeathContext context) {
		return new ActionAgent(context, this) {
			@Override
			public void preprocess() {
			}

			@Override
			public ActionResult execute() {
				Player victimPlayer = context.getVictim().getPlayer();
				if (victimPlayer == null)
					return ActionResult.PLAYER_OFFLINE;

				context.getVictim().getPlayer().sendMessage(context.replaceVariables(message));
				return null;
			}

			@Override
			public void cancel() {
			}
		};
	}
}
