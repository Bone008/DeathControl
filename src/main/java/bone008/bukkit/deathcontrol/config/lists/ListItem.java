package bone008.bukkit.deathcontrol.config.lists;

import java.util.Comparator;

import org.bukkit.inventory.ItemStack;

public abstract class ListItem {

	public abstract CharSequence toHumanString();

	public abstract boolean matches(ItemStack itemStack);



	public static Comparator<? super ListItem> getComparator() {
		return forwardingComparator;
	}

	private static Comparator<ListItem> forwardingComparator = new Comparator<ListItem>() {
		@Override
		public int compare(ListItem o1, ListItem o2) {
			boolean basic1 = (o1 instanceof BasicListItem);
			boolean basic2 = (o2 instanceof BasicListItem);

			if (basic1 && !basic2)
				return -1;
			if (!basic1 && basic2)
				return 1;

			if (basic1 && basic2)
				return BasicListItem.compare((BasicListItem) o1, (BasicListItem) o2);

			assert !basic1 && !basic2;
			assert o1 instanceof SpecialListItem && o2 instanceof SpecialListItem;

			return SpecialListItem.compare((SpecialListItem) o1, (SpecialListItem) o2);
		}
	};

}
