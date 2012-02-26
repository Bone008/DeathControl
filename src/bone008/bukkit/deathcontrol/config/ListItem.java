package bone008.bukkit.deathcontrol.config;

import java.util.Comparator;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class ListItem {

	private int id = 0;
	private byte data = 0;
	private boolean hasData = false;

	public ListItem(Material mat, Byte data) {
		this.id = mat.getId();
		if (data != null) {
			this.data = data;
			hasData = true;
		}
	}

	public boolean matches(ItemStack itemStack) {
		if (itemStack.getTypeId() == this.id) {
			if (this.hasData) {
				return (this.data == itemStack.getDurability());
			} else {
				return true;
			}
		}
		return false;
	}

	@Override
	public String toString() {
		return "ListItem@" + id + ":" + data + "/" + hasData;
	}

	public String toHumanString() {
		return id + (hasData ? ":" + data : "");
	}

	public static Comparator<ListItem> getComparator() {
		return new Comparator<ListItem>() {
			@Override
			public int compare(ListItem o1, ListItem o2) {
				if (o1.id == o2.id) {
					if (o1.hasData) {
						if (o2.hasData) {
							return compInt(o1.data, o2.data);
						} else {
							return 1;
						}
					} else if (o2.hasData) {
						if (o1.hasData) {
							return compInt(o1.data, o2.data);
						} else {
							return -1;
						}
					} else {
						return 0;
					}
				} else if (o1.id > o2.id) {
					return 1;
				} else {
					assert o1.id < o2.id;
					return -1;
				}

			}

			private int compInt(int i1, int i2) {
				if (i1 < i2)
					return -1;
				else if (i1 == i2)
					return 0;
				else {
					assert i1 > i2;
					return 1;
				}
			}
		};
	}

}
