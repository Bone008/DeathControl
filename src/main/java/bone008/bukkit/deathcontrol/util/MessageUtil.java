package bone008.bukkit.deathcontrol.util;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.ConversationPrefix;

import bone008.bukkit.deathcontrol.DeathControl;

public final class MessageUtil {
	private MessageUtil() {
	}

	public static void sendMessage(CommandSender who, Message msg, String... replaces) {
		if (replaces.length % 2 > 0)
			throw new IllegalArgumentException("replace patterns must consist of two elements");

		String translatedMessage = msg.getTranslation();

		if (translatedMessage == null || translatedMessage.isEmpty())
			return;

		for (int i = 0; i < replaces.length - 1; i += 2) {
			if (replaces[i] != null && replaces[i + 1] != null)
				translatedMessage = translatedMessage.replace(replaces[i], replaces[i + 1]);
		}

		sendMessage(who, ChatColor.translateAlternateColorCodes('&', translatedMessage), false);
	}

	public static void sendMessage(CommandSender who, CharSequence msg) {
		sendMessage(who, msg, false);
	}

	public static void sendMessage(CommandSender who, CharSequence msg, boolean error) {
		sendMessage(who, msg, getPluginPrefix(error));
	}

	public static void sendMessage(CommandSender who, CharSequence msg, String prefix) {
		if (msg != null && who != null && isPlayerOnline(who)) {
			if (prefix == null)
				prefix = "";

			String[] splitMsg = msg.toString().split("\n");

			for (int i = 0; i < splitMsg.length; i++) {
				if (splitMsg[i] == null)
					splitMsg[i] = ""; // send an empty line
				who.sendMessage(prefix + splitMsg[i]);
			}
		}
	}

	/**
	 * Broadcasts a plugin prefixed message on the server.
	 * 
	 * @param msg The message to broadcast
	 */
	public static void broadcast(CharSequence msg) {
		Bukkit.broadcastMessage(getPluginPrefix(false) + msg);
	}

	public static String getPluginPrefix(boolean error) {
		return ChatColor.GRAY + "[" + DeathControl.instance.pdfFile.getName() + "] " + (error ? ChatColor.RED : ChatColor.RESET);
	}

	public static ConversationPrefix getPluginConversationPrefix(final boolean error) {
		return new ConversationPrefix() {
			@Override
			public String getPrefix(ConversationContext context) {
				return getPluginPrefix(error);
			}
		};
	}

	/**
	 * Returns false if the sender is a player and not online, or true in all other cases.
	 */
	private static boolean isPlayerOnline(CommandSender sender) {
		return !(sender instanceof OfflinePlayer && !((OfflinePlayer) sender).isOnline());
	}

}
