package bone008.bukkit.deathcontrol.config.actions;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

import bone008.bukkit.deathcontrol.config.ActionAgent;
import bone008.bukkit.deathcontrol.config.ActionDescriptor;
import bone008.bukkit.deathcontrol.config.ActionResult;
import bone008.bukkit.deathcontrol.config.DeathContext;
import bone008.bukkit.deathcontrol.util.Util;

public class BroadcastAction extends ActionDescriptor {

	private final String message;

	public BroadcastAction(List<String> args) {
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
				Bukkit.broadcastMessage(context.replaceVariables(message));
				return null;
			}

			@Override
			public void cancel() {
			}
		};
	}
}
