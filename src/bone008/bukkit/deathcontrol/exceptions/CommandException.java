package bone008.bukkit.deathcontrol.exceptions;

import bone008.bukkit.deathcontrol.Message;

public class CommandException extends Exception {

	private static final long serialVersionUID = 1L;

	private final Message msg;

	public CommandException(String string) {
		super(string);
		this.msg = null;
	}

	public CommandException(Message externalizedMsg) {
		super();
		this.msg = externalizedMsg;
	}

	public Message getTranslatableMessage() {
		return this.msg;
	}

}
