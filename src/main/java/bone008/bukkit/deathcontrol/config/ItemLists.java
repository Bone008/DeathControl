package bone008.bukkit.deathcontrol.config;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Set;

import bone008.bukkit.deathcontrol.DeathControl;
import bone008.bukkit.deathcontrol.config.lists.ListItem;
import bone008.bukkit.deathcontrol.config.lists.ListsParser;

public class ItemLists {

	private final Map<String, List<ListItem>> lists;

	public ItemLists(DeathControl plugin, File f) {
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

}
