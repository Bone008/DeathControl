package bone008.bukkit.deathcontrol.commandhandler;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import bone008.bukkit.deathcontrol.exceptions.CommandException;

/**
 * The use of this class was inspired by WorldEdit's command handling. I didn't take any code of it, because most of its functionality isn't needed here.
 * 
 * @author Bone008
 */
public class CommandContext {

	private static final String MSG_PLAYER_CONTEXT = "You have to be a player to do this!";
	private static final String MSG_NOT_ENOUGH_ARGUMENTS = "You did not provide enough arguments!";
	private static final String MSG_NUMBER_EXPECTED = "A number was expected!";
	private static final String MSG_INVALID_PLAYER = "The given player is not online or does not exist!";

	/**
	 * The {@link CommandSender} associated with this command.
	 */
	public final CommandSender sender;
	/**
	 * The Command instance associated with the main command.
	 */
	public final Command mainCmd;
	/**
	 * The label/alias that was used for the main command.
	 */
	public final String mainLabel;
	/**
	 * The CommandHandle that executed this command.
	 */
	public final CommandHandler cmdHandler;

	private String[] args;

	public CommandContext(CommandSender sender, Command mainCmd, String mainLabel, CommandHandler cmdHandler, String[] args) {
		this.sender = sender;
		this.mainCmd = mainCmd;
		this.mainLabel = mainLabel;
		this.cmdHandler = cmdHandler;
		this.args = args;
	}

	/**
	 * Returns the CommandSender associated with this command as a Player.
	 * 
	 * @throws CommandException when the sender isn't a player
	 */
	public Player getPlayerSender() throws CommandException {
		if (sender instanceof Player)
			return (Player) sender;

		throw new CommandException(MSG_PLAYER_CONTEXT);
	}

	/**
	 * Returns the amount of arguments provided in this CommandContext.
	 */
	public int argsCount() {
		return args.length;
	}

	/**
	 * Makes sure at least {@code len} arguments are provided. Note: This does <b>NOT</b> have to be called before trying to retrieve args. It is meant to be used when the "length check" should happen before any "content checks".
	 * 
	 * @param len The length to ensure.
	 * @throws CommandException If the amount of provided arguments is less than the given length.
	 */
	public void ensureArgs(int len) throws CommandException {
		if (args.length < len)
			throw new CommandException(MSG_NOT_ENOUGH_ARGUMENTS);
	}

	/**
	 * Shifts this context by the given factor. The argument at position x will become the new 0 argument.
	 * 
	 * @param x The amount of arguments to shift this context.
	 */
	public void translate(int x) {
		if (x < 0)
			throw new IllegalArgumentException("cannot shift backwards");
		String[] newArgs = new String[args.length - x];
		System.arraycopy(args, x, newArgs, 0, newArgs.length);
		args = newArgs;
	}

	public String getStringArg(int pos) throws CommandException {
		if (args.length > pos)
			return args[pos];

		throw new CommandException(MSG_NOT_ENOUGH_ARGUMENTS);
	}

	/**
	 * Returns the argument at position pos if it exists, otherwise the given default value. Does not throw an exception if the argument was omitted!
	 * 
	 * @param pos The index of the argument
	 * @param def The default value to return
	 * @return The argument at position pos or def, if it doesn't exist.
	 */
	public String getStringArg(int pos, String def) {
		if (args.length > pos)
			return args[pos];
		else
			return def;
	}

	public int getIntArg(int pos) throws CommandException {
		try {
			return Integer.parseInt(getStringArg(pos));
		} catch (NumberFormatException ex) {
			throw new CommandException(MSG_NUMBER_EXPECTED);
		}
	}

	public int getIntArg(int pos, int def) throws CommandException {
		try {
			return Integer.parseInt(getStringArg(pos, Integer.toString(def)));
		} catch (NumberFormatException ex) {
			throw new CommandException(MSG_NUMBER_EXPECTED);
		}
	}

	public double getDoubleArg(int pos) throws CommandException {
		try {
			return Double.parseDouble(getStringArg(pos));
		} catch (NumberFormatException ex) {
			throw new CommandException(MSG_NUMBER_EXPECTED);
		}
	}

	public Player getPlayerArg(int pos) throws CommandException {
		Player ply = Bukkit.getPlayer(getStringArg(pos));
		if (ply == null)
			throw new CommandException(MSG_INVALID_PLAYER);
		return ply;
	}

	/**
	 * Returns all remaining args chained with a space (thus the way they were typed) as a String. This can be useful to rebuild multi-word messages from a command.
	 * 
	 * @param startPos The position from which to get the args from
	 * @param require Whether or not at least one word has to be given. If set to true, it will throw a {@link CommandException} on failure, otherwise it will return null.
	 * @throws CommandException If {@code require} is set to true and there is no argument at {@code startPos}
	 */
	public String getChainedArgs(int startPos, boolean require) throws CommandException {
		if (startPos >= args.length) {
			if (require)
				throw new CommandException(MSG_NOT_ENOUGH_ARGUMENTS);
			else
				return null;
		}

		StringBuilder sb = new StringBuilder();
		for (int i = startPos; i < args.length; i++) {
			if (i > startPos)
				sb.append(' ');
			sb.append(args[i]);
		}

		return sb.toString();
	}

}
