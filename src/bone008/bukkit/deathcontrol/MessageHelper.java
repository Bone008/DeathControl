package bone008.bukkit.deathcontrol;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;

public final class MessageHelper {
	private MessageHelper() {
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

	private static String getPluginPrefix(boolean error) {
		return ChatColor.GRAY + "[" + DeathControl.instance.pdfFile.getName() + "] " + (error ? ChatColor.RED : ChatColor.WHITE);
	}

	/**
	 * Returns false if the sender is a player and not online, or true in all other cases.
	 */
	private static boolean isPlayerOnline(CommandSender sender) {
		return !(sender instanceof OfflinePlayer && !((OfflinePlayer) sender).isOnline());
	}

}
