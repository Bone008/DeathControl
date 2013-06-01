package bone008.bukkit.deathcontrol.newconfig.actions;

import java.util.List;

import org.bukkit.ChatColor;

import bone008.bukkit.deathcontrol.newconfig.ActionAgent;
import bone008.bukkit.deathcontrol.newconfig.ActionDescriptor;
import bone008.bukkit.deathcontrol.newconfig.ActionResult;
import bone008.bukkit.deathcontrol.newconfig.DeathContext;
import bone008.bukkit.deathcontrol.util.MessageUtil;
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
				MessageUtil.sendMessage(context.getVictim(), message);
				return null;
			}

			@Override
			public void cancel() {
			}
		};
	}
}
