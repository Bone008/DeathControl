package bone008.bukkit.deathcontrol.config;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Set;

import bone008.bukkit.deathcontrol.DeathControl;
import bone008.bukkit.deathcontrol.config.lists.ListItem;
import bone008.bukkit.deathcontrol.config.lists.ListsParser;

public class DeathLists {

	private final Map<String, List<ListItem>> lists;

	public DeathLists(DeathControl plugin, File f) {
		this.lists = new ListsParser(f).parse();
	}

	/**
	 * Gets a list by its name.
	 * 
	 * @param name the name of the list
	 * @return a list of {@link ListItem}s
	 */
	public List<ListItem> getList(String name) {
		return lists.get(name);
	}

	public Set<String> getListNames() {
		return lists.keySet();
	}

	public int getListsAmount() {
		return lists.size();
	}

	/**
	 * parses lists.txt and writes the entries to "lists"-member
	 */
	//	private void parseFile(File f) {
	//		BufferedReader reader = null;
	//		try {
	//			reader = new BufferedReader(new FileReader(f));
	//
	//			List<ListItem> currentList = null;
	//			String line;
	//			
	//			// iterate over all lines
	//			while ((line = readValidLine(reader)) != null) {
	//				// new list start
	//				if (line.startsWith(PREFIX_LIST)) {
	//					String listName = line.substring(PREFIX_LIST.length()).toLowerCase().trim();
	//					if (listName.isEmpty())
	//						plugin.log(Level.WARNING, "lists.txt: found empty list name!", true);
	//					else {
	//						currentList = new ArrayList<ListItem>();
	//						lists.put(listName, currentList);
	//					}
	//
	//				}
	//
	//				// item declaration before any list started
	//				else if (currentList == null) {
	//					plugin.log(Level.WARNING, "lists.txt: found item declaration before any list was specified!", true);
	//				}
	//
	//				// regular list item
	//				else {
	//					List<String> chunks = Utilities.tokenize(line, ":", true);
	//					if (chunks.length > 2 || chunks.length < 1) {
	//						plugin.log(Level.WARNING, "lists.txt: invalid formatting of " + line, true);
	//						continue;
	//					}
	//
	//					Material mat = null;
	//					try {
	//						mat = Material.getMaterial(Integer.parseInt(chunks[0]));
	//					} catch (NumberFormatException e) {
	//						mat = Material.matchMaterial(chunks[0]);
	//					}
	//					if (mat == null)
	//						plugin.log(Level.WARNING, "lists.txt: could not find material " + chunks[0], true);
	//
	//					else {
	//
	//						Byte data = null;
	//						try {
	//							if (chunks.length == 2)
	//								data = Byte.parseByte(chunks[1]);
	//
	//							ListItem item = new ListItem(mat, data);
	//							currentList.add(item);
	//
	//						} catch (NumberFormatException e) {
	//							plugin.log(Level.WARNING, "lists.txt: data value '" + chunks[1] + "' must be a number!", true);
	//						}
	//
	//					}
	//				}
	//
	//			}
	//
	//			// must override logging-level because config is not yet loaded
	//			plugin.log(Level.CONFIG, "loaded " + lists.size() + " list" + (lists.size() == 1 ? "" : "s") + "!", true);
	//
	//		} catch (IOException e) {
	//			plugin.log(Level.WARNING, "Could not load lists.txt!", true);
	//			e.printStackTrace();
	//		} finally {
	//			if (reader != null)
	//				try {
	//					reader.close();
	//				} catch (IOException e) {
	//					e.printStackTrace();
	//				}
	//		}
	//	}

}
