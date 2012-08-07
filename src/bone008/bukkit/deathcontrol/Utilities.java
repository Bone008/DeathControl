package bone008.bukkit.deathcontrol;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import net.minecraft.server.EntityExperienceOrb;
import net.minecraft.server.Packet43SetExperience;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public final class Utilities {
	private Utilities() {
	}

	public static void dropItem(Location l, ItemStack i, boolean naturally) {
		if (l == null || i == null || i.getTypeId() < 1 || i.getAmount() < 1)
			return;

		World w = l.getWorld();
		if (!w.isChunkLoaded(l.getChunk())) {
			w.loadChunk(l.getChunk());
		}

		if (naturally)
			l.getWorld().dropItemNaturally(l, i);
		else
			l.getWorld().dropItem(l, i);
	}

	public static void dropItems(Location l, Iterable<ItemStack> items, boolean naturally) {
		if (items == null)
			return;
		for (ItemStack i : items)
			dropItem(l, i, naturally);
	}

	public static void dropItems(Location l, Map<?, ItemStack> items, boolean naturally) {
		if (items == null)
			return;
		dropItems(l, items.values(), naturally);
	}

	public static void dropExp(Location l, int amount, boolean native_) {
		if (native_) {
			// This splits up the experience into multiple orbs, just like it is natively done upon death.
			net.minecraft.server.World w = ((CraftWorld) l.getWorld()).getHandle();
			while (amount > 0) {
				int orbSize = EntityExperienceOrb.getOrbValue(amount);
				amount -= orbSize;
				w.addEntity(new EntityExperienceOrb(w, l.getBlockX(), l.getBlockY(), l.getBlockZ(), orbSize));
			}
		} else {
			// This spawns a single orb containing all the experience.
			// As of 1.1, there is a bug that changing the experience won't send a notification packet to the client
			// so the orb size will always be displayed very small.
			ExperienceOrb orb = l.getWorld().spawn(l, ExperienceOrb.class);
			orb.setExperience(amount);
		}
	}

	public static void updateExperience(Player ply) {
		((CraftPlayer) ply).getHandle().netServerHandler.sendPacket(new Packet43SetExperience(ply.getExp(), ply.getTotalExperience(), ply.getLevel()));
	}

	/**
	 * Prefixes the given String at each new line.
	 * 
	 * @param str The string to prefix.
	 * @param prefix The prefix to put in front of the lines.
	 * @return The wrapped string as a CharSequence
	 */
	public static CharSequence wrapPrefixed(String str, String prefix) {
		String[] lines = str.split("\n");
		StringBuilder output = new StringBuilder();
		for (int i = 0; i < lines.length; i++) {
			if (output.length() > 0)
				output.append('\n');
			output.append(prefix).append(lines[i]);
		}
		return output;
	}

	/**
	 * Gets a list from the given {@code ConfigurationSection} with a special type given through the formal type parameter.
	 * 
	 * @param <T> The type which the list is expected to be. <u>Note: Must be explicitly declared in the method call!</u>
	 * @param sec The ConfigurationSection to use
	 * @param node The node to use
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static <T> List<T> getConfigList(ConfigurationSection sec, String node) {
		return (List<T>) sec.getList(node);
	}

	public static int getConfigInt(ConfigurationSection sec, String node, int def) {
		Object o = sec.get(node, def);
		if (o == null)
			return def;
		try {
			return Integer.valueOf(o.toString());
		} catch (NumberFormatException ex) {
			return 0;
		}
	}

	public static double getConfigDouble(ConfigurationSection sec, String node, double def) {
		Object o = sec.get(node, def);
		if (o == null)
			return def;
		try {
			return Double.valueOf(o.toString());
		} catch (NumberFormatException ex) {
			return 0;
		}
	}

	public static String getConfigString(ConfigurationSection sec, String node, String def) {
		Object o = sec.get(node, def);
		if (o == null)
			return def;
		return o.toString();
	}

	/**
	 * Joins the elements of a given Collection with a delimiter.
	 * 
	 * @param delimiter The delimiter to separate the elements.
	 * @param collection The Collection to take the elements from.
	 * @return A String with the joined elements, or null if the Collection was null. Returns an empty String if the Collection was empty.
	 */
	public static String joinCollection(String delimiter, Collection<?> collection) {
		if (collection == null)
			return null;
		StringBuilder ret = new StringBuilder();
		Iterator<?> it = collection.iterator();
		while (it.hasNext()) {
			ret.append(it.next());
			if (it.hasNext())
				ret.append(delimiter);
		}
		return ret.toString();
	}

	/**
	 * If {@code obj} equals {@code search}, {@code repl} is returned. Otherwise obj will be returned unchanged.
	 * 
	 * @param obj The object to validate.
	 * @param search The condition to replace {@code obj}
	 * @param repl The value to replace {@code obj} with if {@code search} matched
	 */
	public static <T> T replaceValue(T obj, T search, T repl) {
		if (obj.equals(search))
			return repl;
		return obj;
	}

}
