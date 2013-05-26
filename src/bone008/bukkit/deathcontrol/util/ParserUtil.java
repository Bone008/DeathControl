package bone008.bukkit.deathcontrol.util;

import java.util.List;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import bone008.bukkit.deathcontrol.Utilities;

public class ParserUtil {

	private static final Pattern REGEX_TIME_MINUTES = Pattern.compile("\\s*(\\d+)\\s*m\\s*", Pattern.CASE_INSENSITIVE);
	private static final Pattern REGEX_TIME_SECONDS = Pattern.compile("\\s*(\\d+)\\s*s\\s*", Pattern.CASE_INSENSITIVE);

	/**
	 * Parses a user-friendly name of a logging level into its corresponding level number.
	 * 
	 * @param name the name to parse
	 * 
	 * @return the intValue of the level, or -1 if none was matched
	 * 
	 * @see Level#intValue()
	 */
	public static int parseLoggingLevel(String name) {
		if (name.equalsIgnoreCase("errors") || name.equalsIgnoreCase("error"))
			return Level.SEVERE.intValue();
		if (name.equalsIgnoreCase("warnings") || name.equalsIgnoreCase("warning"))
			return Level.WARNING.intValue();
		if (name.equalsIgnoreCase("standard") || name.equalsIgnoreCase("info") || name.equalsIgnoreCase("standart")) // for the common typo ;)
			return Level.INFO.intValue();
		if (name.equalsIgnoreCase("detailed") || name.equalsIgnoreCase("detail") || name.equalsIgnoreCase("debug"))
			return Level.FINEST.intValue();
		return -1;
	}

	/**
	 * Parses a string as a given time interval. Accepted formats are "20m", "20s" and a plain number.
	 * 
	 * @param input the input to parse; trimmed automatically
	 * @param def the default to return when no match could be found
	 * @return the parsed time in <b>seconds</b>, or <code>def</code> if none could be parsed
	 */
	public static int parseTime(String input, int def) {
		if (input == null || input.isEmpty())
			return def;

		// 1m
		Matcher matcher = REGEX_TIME_MINUTES.matcher(input);
		if (matcher.matches())
			return Integer.parseInt(matcher.group(1)) * 60;

		// 1s
		matcher = REGEX_TIME_SECONDS.matcher(input);
		if (matcher.matches())
			return Integer.parseInt(matcher.group(1));

		// raw number
		try {
			return Integer.parseInt(input.trim());
		} catch (NumberFormatException e) {
			return def;
		}
	}

	/**
	 * Extracts the name of an operation in space-separated format.
	 * 
	 * @param input the complete operation, may have trailing spaces
	 * @return the name (first) token of the operation, or null if <code>input</code> was empty
	 */
	public static String parseOperationName(String input) {
		List<String> tokens = Utilities.tokenize(input, " ", false);

		if (tokens.isEmpty())
			return null;

		return tokens.get(0);
	}

	/**
	 * Extracts a list of parameters from an operation in space-separated format.
	 * 
	 * @param input the complete operation, may have trailing spaces
	 * @return a list of all tokens but the first one (which is the name of the operation); may be empty, but never null
	 */
	public static List<String> parseOperationArgs(String input) {
		List<String> tokens = Utilities.tokenize(input, " ", false);
		if (!tokens.isEmpty())
			tokens.remove(0);
		return tokens;
	}
}
