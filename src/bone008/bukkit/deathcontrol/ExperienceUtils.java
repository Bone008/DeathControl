package bone008.bukkit.deathcontrol;

import org.bukkit.entity.Player;

/**
 * @author desht
 * @author nisovin
 * @author Bone008
 * 
 * Note by Bone008:
 * 	I heavily modified the class so it doesn't use pre-cached values.
 *  The algorithm is now slower, but for this purpose it doesn't have to be fast.
 *  I kept the author tags of the original version because I based my calculations on them.
 *  In {@code changeExp(Player, int)} the total experience is now also set to improve compatibility. My best attempt to improve the already broken experience system ...
 *  @see net.minecraft.server.EntityHuman.getExpToLevel()
 */
public final class ExperienceUtils {

	/**
	 * Reliably returns the total current amount of experience of the given player.
	 * @param player The player wanted
	 * @return The total amount of experience
	 */
	public static int getActualExp(Player player) {
		int lvl = player.getLevel();
		return getTotalExpForLevel(lvl) + Math.round(getExpToReachLevel(lvl + 1) * player.getExp());
		// total experience at the beginning of the current level + (experience needed for next level * ratio)
	}

	/**
	 * Returns the total amount of experience needed to get to the given level.
	 * @param lvl The level wanted
	 * @return The amount of experience needed
	 */
	public static int getTotalExpForLevel(int lvl) {
		if (lvl <= 0)
			return 0;
		return getTotalExpForLevel(lvl - 1) + getExpToReachLevel(lvl);
	}

	/**
	 * Get the experience needed to get the given level measured <u>from the previous level</u>.
	 * @param lvl The level wanted
	 * @return The amount of experience needed
	 */
	public static int getExpToReachLevel(int lvl) {
		return 7 + ((lvl - 1) * 7 >> 1);
	}

	/**
	 * Give the player some experience (possibly negative) and ensure that everything is updated correctly.
	 * 
	 * @param player The player to grant XP to
	 * @param amt The amount of XP to grant
	 */
	public static void changeExp(Player player, int amt) {
		int xp = getActualExp(player) + amt;
		if (xp < 0)
			xp = 0;

		int curLvl = player.getLevel();
		int newLvl = getLevelAtExp(xp);
		if (curLvl != newLvl) {
			player.setLevel(newLvl);
		}

		float pct = ((float) (xp - getTotalExpForLevel(newLvl)) / (float) getExpToReachLevel(newLvl));
		player.setExp(pct);
		
		player.setTotalExperience(xp);
	}

	/**
	 * Gets the level you are at with the given experience amount.
	 * @param xp The total experience to check the level for.
	 * @return The level you are at with the given experiencce amount.
	 */
	public static int getLevelAtExp(final int xp) {
		int lvl = 0;
		while(getTotalExpForLevel(lvl) <= xp)
			lvl++;
		return lvl-1;
	}

}