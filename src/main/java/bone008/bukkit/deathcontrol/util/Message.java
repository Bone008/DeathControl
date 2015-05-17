package bone008.bukkit.deathcontrol.util;

import bone008.bukkit.deathcontrol.DeathControl;

public enum Message {
	NOTIF_NOCROSSWORLD("notification.no-cross-world"),
	CMD_NOTHING_STORED("command.nothing-stored"),
	CMD_CANCELLED("command.cancelled"),
	CMDCONTEXT_NO_PERMISSION("command.context.no-permission"),
	CMDCONTEXT_PLAYER_CONTEXT("command.context.player-context-required"),
	CMDCONTEXT_ARGUMENT_MISSING("command.context.not-enough-arguments"),
	CMDCONTEXT_NUMBER_EXPECTED("command.context.number-expected"),
	CMDCONTEXT_INVALID_PLAYER("command.context.invalid-player-arg");

	private final String path;

	private Message(String path) {
		this.path = path;
	}

	public String getPath() {
		return this.path;
	}

	public String getTranslation() {
		return translatePath(this.path);
	}

	public static String translatePath(String path) {
		return DeathControl.instance.messagesData.getString(path, path);
	}

}
