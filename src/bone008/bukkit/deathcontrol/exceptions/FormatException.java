package bone008.bukkit.deathcontrol.exceptions;

/**
 * An exception indicating a general failure caused by wrong formatting of something.
 */
public class FormatException extends Exception {
	private static final long serialVersionUID = 1L;

	public FormatException(String msg) {
		super(msg);
	}
}
