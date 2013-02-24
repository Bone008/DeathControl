package bone008.bukkit.deathcontrol.config.lists;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;


public class BasicListItem extends ListItem {

	private int id = 0;
	private byte data = 0;
	private boolean hasData = false;

	public BasicListItem(Material mat, Byte data) {
		this.id = mat.getId();
		if (data != null) {
			this.data = data;
			hasData = true;
		}
	}

	@Override
	public boolean matches(ItemStack itemStack) {
		if (itemStack.getTypeId() == this.id) {
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
		return "ListItem@" + id + ":" + data + "/" + hasData;
	}

	@Override
	public CharSequence toHumanString() {
		return Material.getMaterial(id).toString() + ChatColor.ITALIC + '#' + id + (hasData ? ":" + data : "");
	}


	public static int compare(BasicListItem o1, BasicListItem o2) {
		if (o1.id == o2.id) {
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
		else if (o1.id > o2.id) {
			return 1;
		}
		else {
			assert o1.id < o2.id;
			return -1;
		}

	}

}
