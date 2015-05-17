package bone008.bukkit.deathcontrol.util;

import java.util.Arrays;

import org.bukkit.entity.Player;

/**
 * Adapted from ExperienceUtils code originally in ScrollingMenuSign.
 * 
 * Credit to nisovin (http://forums.bukkit.org/threads/experienceutils-make-giving-taking-exp-a-bit-more-intuitive.54450/#post-1067480) for an implementation that avoids the problems of getTotalExperience(), which doesn't work properly after a player has enchanted something.
 * 
 * @author desht
 * @author modified by Bone008
 */
public final class ExperienceUtil {

	private ExperienceUtil() {
	}

	// this is to stop the lookup tables growing without control
	private static final int hardMaxLevel = 100000;

	private static int xpRequiredForNextLevel[];
	private static int xpTotalToReachLevel[];

	static {
		// 25 is an arbitrary value for the initial table size - the actual value isn't critically
		// important since the tables are resized as needed.
		initLookupTables(25);
	}

	/**
	 * Initialise the XP lookup tables. Basing this on observations noted in https://bukkit.atlassian.net/browse/BUKKIT-47
	 * 
	 * 7 xp to get to level 1, 17 to level 2, 31 to level 3... At each level, the increment to get to the next level increases alternately by 3 and 4
	 * 
	 * @param maxLevel The highest level handled by the lookup tables
	 */
	private static void initLookupTables(int maxLevel) {
		xpRequiredForNextLevel = new int[maxLevel];
		xpTotalToReachLevel = new int[maxLevel];

		xpTotalToReachLevel[0] = 0;
		// 		Code valid for MC 1.2 and earlier
		//		int incr = 7;
		//		for (int i = 1; i < xpTotalToReachLevel.length; i++) {
		//			xpRequiredForNextLevel[i - 1] = incr;
		//			xpTotalToReachLevel[i] = xpTotalToReachLevel[i - 1] + incr;
		//			incr += (i % 2 == 0) ? 4 : 3;
		//		}

		// Valid for MC 1.3 and later
		int incr = 17;
		for (int i = 1; i < xpTotalToReachLevel.length; i++) {
			xpRequiredForNextLevel[i - 1] = incr;
			xpTotalToReachLevel[i] = xpTotalToReachLevel[i - 1] + incr;
			if (i >= 30) {
				incr += 7;
			}
			else if (i >= 16) {
				incr += 3;
			}
		}
		xpRequiredForNextLevel[xpRequiredForNextLevel.length - 1] = incr;
	}

	/**
	 * Calculate the level that the given XP quantity corresponds to, without using the lookup tables. This is needed if getLevelForExp() is called with an XP quantity beyond the range of the existing lookup tables.
	 * 
	 * @param exp
	 * @return
	 */
	private static int calculateLevelForExp(int exp) {
		int level = 0;
		int curExp = 7; // level 1
		int incr = 10;
		while (curExp <= exp) {
			curExp += incr;
			level++;
			incr += (level % 2 == 0) ? 3 : 4;
		}
		return level;
	}

	/**
	 * Adjust the player's XP by the given amount in an intelligent fashion. Works around some of the non-intuitive behaviour of the basic Bukkit player.giveExp() method.
	 * 
	 * @param player The player to change the experience of.
	 * @param amt Amount of XP, may be negative
	 */
	public static void changeExp(Player player, int amt) {
		setExp(player, getCurrentExp(player) + amt);
	}

	/**
	 * Set the player's experience
	 * 
	 * @param player The player to set the experience of.
	 * @param xp Amount of XP, should not be negative.
	 */
	public static void setExp(Player player, int xp) {
		if (xp < 0)
			xp = 0;

		int curLvl = player.getLevel();
		int newLvl = getLevelForExp(xp);
		if (curLvl != newLvl) {
			player.setLevel(newLvl);
		}

		float pct = ((float) (xp - getXpForLevel(newLvl)) / (float) xpRequiredForNextLevel[newLvl]);
		player.setExp(pct);

		player.setTotalExperience(xp); // Bone008 - set total experience as well to provide more compatibility
	}

	/**
	 * Get the player's current XP total.
	 * 
	 * @param player The player.
	 * @return the player's total XP
	 */
	public static int getCurrentExp(Player player) {
		int lvl = player.getLevel();
		int cur = getXpForLevel(lvl) + (int) Math.round(xpRequiredForNextLevel[lvl] * player.getExp());
		return cur;
	}

	/**
	 * Checks if the player has the given amount of XP.
	 * 
	 * @param player The player to check.
	 * @param amt The amount to check for.
	 * @return true if the player has enough XP, false otherwise
	 */
	public static boolean hasExp(Player player, int amt) {
		return getCurrentExp(player) >= amt;
	}

	/**
	 * Get the level that the given amount of XP falls within.
	 * 
	 * @param exp The amount to check for.
	 * @return The level that a player with this amount total XP would be.
	 */
	public static int getLevelForExp(int exp) {
		if (exp <= 0)
			return 0;
		if (exp > xpTotalToReachLevel[xpTotalToReachLevel.length - 1]) {
			// need to extend the lookup tables
			int newMax = calculateLevelForExp(exp) * 2;
			if (newMax > hardMaxLevel) {
				throw new IllegalArgumentException("Level for exp " + exp + " > hard max level " + hardMaxLevel);
			}
			initLookupTables(newMax);
		}
		int pos = Arrays.binarySearch(xpTotalToReachLevel, exp);
		return pos < 0 ? -pos - 2 : pos;
	}

	/**
	 * Return the total XP needed to be the given level.
	 * 
	 * @param level The level to check for.
	 * @return The amount of XP needed for the level.
	 */
	public static int getXpForLevel(int level) {
		if (level > hardMaxLevel) {
			throw new IllegalArgumentException("Level " + level + " > hard max level " + hardMaxLevel);
		}

		if (level >= xpTotalToReachLevel.length) {
			initLookupTables(level * 2);
		}
		return xpTotalToReachLevel[level];
	}
}
