package bone008.bukkit.deathcontrol.util;

import bone008.bukkit.deathcontrol.DeathControl;

public enum Message {
	DEATH_NO_MONEY("death.not-enough-money"),
	DEATH_KEPT("death.items-kept"),
	DEATH_COMMAND_INDICATOR("death.command-indicator"),
	DEATH_TIMEOUT_INDICATOR("death.timeout-indicator"),
	DEATH_COST_INDICATOR_DIRECT("death.cost-indicator-direct"),
	DEATH_COST_INDICATOR_COMMAND("death.cost-indicator-command"),
	NOTIF_EXPIRATION("notification.expiration"),
	NOTIF_RESTORATION("notification.restoration"),
	NOTIF_NOCROSSWORLD("notification.no-cross-world"),
	NOTIF_NOMONEY("notification.no-money"),
	CMD_NO_RESTORABLE_ITEMS("command.no-restorable-items"),
	CMD_ITEMS_WERE_DROPPED("command.items-were-dropped"),
	CMD_NO_DROPPABLE_ITEMS("command.no-droppable-items"),
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
		return DeathControl.instance.messagesData.getString(path);
	}

}
