package bone008.bukkit.deathcontrol.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;

public final class Util {
	private Util() {
	}

	private static final Random rand = new Random();

	/**
	 * Returns the player attacker from a damage event, or null if there was none.
	 * Considers projectile damages and tries to pull the shooter out of the projectile.
	 * 
	 * @param event an EntityDamageEvent, may be null
	 * @return the player who caused the damage, or null if the damage wasn't caused by one
	 */
	public static Player getPlayerAttackerFromEvent(EntityDamageEvent event) {
		Entity attacker = getAttackerFromEvent(event);

		if (attacker instanceof Player)
			return (Player) attacker;

		return null;
	}

	/**
	 * Returns the entity attacker from a damage event, or null if there was none.
	 * Considers projectile damages and tries to pull the shooter out of the projectile.
	 * 
	 * @param event an EntityDamageEvent, may be null
	 * @return the entity which caused the damage, or null if the damage wasn't caused by one
	 */
	public static Entity getAttackerFromEvent(EntityDamageEvent event) {
		if (!(event instanceof EntityDamageByEntityEvent)) // implicit null-check
			return null;

		Entity damager = ((EntityDamageByEntityEvent) event).getDamager();

		if (damager instanceof Projectile)
			damager = ((Projectile) damager).getShooter();

		return damager;
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

	public static void dropExp(Location l, int amount) {
		// the native way of dropping experience is no longer supported to stop depending on CraftBukkit

		// This spawns a single orb containing all the experience.
		// As of 1.1 (still present in 1.4.x), there is a bug that changing the experience won't send a notification packet to the client
		// so the orb size will always be displayed very small.
		ExperienceOrb orb = l.getWorld().spawn(l, ExperienceOrb.class);
		orb.setExperience(amount);
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
		if (obj == null)
			return (search == null ? repl : null);
		if (obj.equals(search))
			return repl;
		return obj;
	}

	/**
	 * Splits a given input at a delimiter and returns the processed result as a {@link List}.
	 * <p/>
	 * The returned list may be empty if he given input is empty, regardless of the setting of <code>allowEmpty</code>!
	 * 
	 * @param input The input string to split.
	 * @param delimiterRegex The delimiter to split the input at.
	 * @param allowEmpty Whether to delete empty results.
	 * @return A {@link List} containing all the filtered results. Never null, but possibly empty.
	 */
	public static List<String> tokenize(String input, String delimiterRegex, boolean allowEmpty) {
		String[] rawTokens = input.trim().split(delimiterRegex);
		List<String> tokens = new ArrayList<String>(rawTokens.length);

		for (String token : rawTokens) {
			token = token.trim();
			if (!allowEmpty && token.isEmpty())
				continue;

			tokens.add(token);
		}

		return tokens;
	}


	/**
	 * Utility method to build a word's plural only if necessary. It returns the passed number plus the word as it is if num equals 1, otherwise it appends an s.
	 * 
	 * @param num The number to check
	 * @param word The singular word
	 * @return the number + {@code word} + possibly an 's', depending on {@code num}
	 */
	public static String pluralNum(int num, String word) {
		return num + " " + word + (num == 1 ? "" : "s");
	}

	/**
	 * Utility method to build a word's plural only if necessary. It returns the passed number plus the normal word if num equals 1, otherwise the plural word.
	 * 
	 * @param num The number to check
	 * @param word The singular word
	 * @param wordPlural The plural word
	 * @return the number + either {@code word} or {@code wordPlural}, depending on {@code num}, separated with a space
	 */
	public static String pluralNum(int num, String word, String wordPlural) {
		return num + " " + (num == 1 ? word : wordPlural);
	}

	/**
	 * Returns a cached {@link Random} instance.
	 */
	public static Random getRandom() {
		return rand;
	}

}
