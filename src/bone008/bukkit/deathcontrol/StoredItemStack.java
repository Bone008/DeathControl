package bone008.bukkit.deathcontrol;

import org.bukkit.inventory.ItemStack;

public class StoredItemStack implements Cloneable {
	public final int slot;
	public final ItemStack itemStack;

	public StoredItemStack(int slot, ItemStack itemStack) {
		this.slot = slot;
		this.itemStack = itemStack;
	}

	/**
	 * Performs a deep clone of this StoredItemStack.
	 */
	@Override
	public StoredItemStack clone() {
		return new StoredItemStack(slot, (itemStack == null ? null : itemStack.clone()));
	}
}
