package bone008.bukkit.deathcontrol.config.lists;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import org.bukkit.Material;

import bone008.bukkit.deathcontrol.DeathControl;
import bone008.bukkit.deathcontrol.exceptions.ConditionFormatException;
import bone008.bukkit.deathcontrol.util.Util;

public class ListsParser {

	private static final String PREFIX_LIST = "$list ";

	private final File file;

	private BufferedReader reader = null;
	private List<ListItem> currentList = null;
	private String currentLine = null;
	private int currentNumLine = 0;

	private Map<String, List<ListItem>> parsedLists = new HashMap<String, List<ListItem>>();

	public ListsParser(File file) {
		this.file = file;
	}


	public Map<String, List<ListItem>> parse() {
		try {
			reader = new BufferedReader(new FileReader(file));

			// iterate over all lines
			while (readValidLine()) {
				if (currentLine.startsWith(PREFIX_LIST)) {
					// new list start
					parseListStart();
				}
				else {
					// treat as a list item
					parseListItem();
				}
			}

			// must override logging-level because config is not yet loaded
			DeathControl.instance.log(Level.CONFIG, "loaded " + parsedLists.size() + " list" + (parsedLists.size() == 1 ? "" : "s") + "!", true);
		} catch (IOException e) {
			DeathControl.instance.log(Level.WARNING, "Could not load lists.txt!", true);
			e.printStackTrace();
		} finally {
			if (reader != null)
				try {
					reader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
		}

		return parsedLists;
	}


	private void parseListStart() {
		String listName = currentLine.substring(PREFIX_LIST.length()).toLowerCase().trim();
		if (listName.isEmpty())
			logLineWarning("found empty list name!");
		else {
			currentList = new ArrayList<ListItem>();
			parsedLists.put(listName, currentList);
		}
	}


	private void parseListItem() throws IOException {
		if (currentList == null) {
			logLineWarning("found item declaration before any list was specified!");
			return;
		}

		if (currentLine.startsWith("{")) {
			parseSpecialItem();
			return;
		}

		// regular list item

		List<String> chunks = Util.tokenize(currentLine, ":", true);
		if (chunks.size() > 2 || chunks.size() < 1) {
			logLineWarning("invalid formatting of item '" + currentLine + "'");
			return;
		}

		Material mat = null;
		try {
			mat = Material.getMaterial(Integer.parseInt(chunks.get(0)));
		} catch (NumberFormatException e) {
			mat = Material.matchMaterial(chunks.get(0));
		}
		if (mat == null)
			logLineWarning("could not find material '" + chunks.get(0) + "'");

		else {

			Byte data = null;
			try {
				if (chunks.size() == 2)
					data = Byte.parseByte(chunks.get(1));

				BasicListItem item = new BasicListItem(mat, data);
				currentList.add(item);

			} catch (NumberFormatException e) {
				logLineWarning("data value '" + chunks.get(1) + "' must be a number!");
			}

		}
	}


	private void parseSpecialItem() throws IOException {
		StringBuilder specialToken = new StringBuilder();
		specialToken.append(currentLine);

		int startLineNum = currentNumLine;

		while (specialToken.indexOf("}") == -1) {
			if (!readValidLine()) {
				logLineWarning("unterminated special item block starting at line " + startLineNum);
				return;
			}

			specialToken.append(currentLine);
		}

		int braceStart = specialToken.indexOf("{");
		int braceEnd = specialToken.indexOf("}");

		String specialItemArgs = specialToken.substring(braceStart + 1, braceEnd);

		SpecialListItem listItem = new SpecialListItem();

		try {
			for (String argToken : Util.tokenize(specialItemArgs, ",", false)) {
				listItem.parseCondition(argToken);
			}
		} catch (ConditionFormatException e) {
			logLineWarning("invalid condition: " + e.getMessage());
			return;
		}

		currentList.add(listItem);

	}


	private boolean readValidLine() throws IOException {
		currentLine = reader.readLine();

		// EOF
		if (currentLine == null)
			return false;

		currentNumLine++;

		// strip comments
		int commentIndex = currentLine.indexOf('#');
		if (commentIndex > -1)
			currentLine = currentLine.substring(0, commentIndex);

		// strip trailing whitespaces
		currentLine = currentLine.trim();

		// skip empty lines
		if (currentLine.isEmpty())
			return readValidLine();

		return true;
	}

	private void logLineWarning(String msg) {
		DeathControl.instance.log(Level.WARNING, String.format("lists.txt[%d]: %s", currentNumLine, msg), true);
	}

}
