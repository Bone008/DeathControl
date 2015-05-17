package bone008.bukkit.deathcontrol.config.lists;

import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import bone008.bukkit.deathcontrol.exceptions.FormatException;
import bone008.bukkit.deathcontrol.util.Util;


public class BasicListItem extends ListItem {

	private Material type;
	private int data = 0;
	private boolean hasData = false;

	public BasicListItem(Material mat, Integer data) {
		this.type = mat;
		if (data != null) {
			this.data = data;
			hasData = true;
		}
	}

	@Override
	public boolean matches(ItemStack itemStack) {
		if (itemStack.getType() == this.type) {
			if (this.hasData) {
				return (this.data == itemStack.getDurability());
			}
			else {
				return true;
			}
		}
		return false;
	}

	@Override
	public String toString() {
		return "BasicListItem@" + type + ":" + data + "/" + hasData;
	}

	@Override
	public CharSequence toHumanString() {
		return type.toString() + ChatColor.ITALIC + '#' + type.getId() + (hasData ? ":" + data : "");
	}


	public static BasicListItem parse(String input) throws FormatException {
		List<String> tokens = Util.tokenize(input, ":", true);
		if (tokens.size() > 2 || tokens.size() < 1)
			throw new FormatException("invalid formatting of item '" + input + "'");

		Material mat = null;
		try {
			mat = Material.getMaterial(Integer.parseInt(tokens.get(0)));
		} catch (NumberFormatException e) {
			mat = Material.matchMaterial(tokens.get(0).replace('-', '_'));
		}
		if (mat == null)
			throw new FormatException("could not find material '" + tokens.get(0) + "'");


		Integer data = null;
		try {
			if (tokens.size() == 2)
				data = Integer.parseInt(tokens.get(1));

			BasicListItem item = new BasicListItem(mat, data);
			return item;
		} catch (NumberFormatException e) {
			throw new FormatException("data value '" + tokens.get(1) + "' must be a number!");
		}
	}

	public static int compare(BasicListItem o1, BasicListItem o2) {
		if (o1.type == o2.type) {
			if (o1.hasData) {
				if (o2.hasData) {
					return o1.data - o2.data;
				}
				else {
					return 1;
				}
			}
			else if (o2.hasData) {
				if (o1.hasData) {
					return o1.data - o2.data;
				}
				else {
					return -1;
				}
			}
			else {
				return 0;
			}
		}
		else {
			return o1.type.compareTo(o2.type);
		}

	}

}
