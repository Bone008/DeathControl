package bone008.bukkit.deathcontrol.config;

import java.util.List;

import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.event.entity.PlayerDeathEvent;

import bone008.bukkit.deathcontrol.StoredItemStack;

public interface DeathContext {

	/**
	 * Returns the location at which the death happened.
	 * 
	 * @return the location where the player died
	 */
	public Location getDeathLocation();

	/**
	 * Returns the victim of the death as an {@link OfflinePlayer}.<br>
	 * A regular player can be retrieved with {@link OfflinePlayer#getPlayer()}, but the player may be offline.
	 * <p/>
	 * Exception: While a {@link ConditionDescriptor} is checking for a match or an {@link ActionAgent} is preprocessed, the player is guaranteed to be online.
	 * 
	 * @return the player who died in this context
	 */
	public OfflinePlayer getVictim();

	/**
	 * Gets a list of {@link StoredItemStack}s representing the current drops of the death. Changes to the list or its contents are reflected to what is actually dropped, as well as all later actions in the queue.
	 * 
	 * @return a list containing all the item stacks to drop
	 */
	public List<StoredItemStack> getItemDrops();

	/**
	 * Returns the {@link PlayerDeathEvent} that was used in this context.
	 * 
	 * @return the event
	 */
	public PlayerDeathEvent getDeathEvent();

	/**
	 * Replaces all "%var%" occurences of all currently set variables in a given input string.
	 * 
	 * @param input the string to change
	 * @return the string with all variables replaced with their respective contents
	 */
	public String replaceVariables(CharSequence input);

	/**
	 * Gets a raw context variable.
	 * 
	 * @param name the name of the variable, case-sensitive
	 * @return the stored value, or null if the variable wasn't set
	 */
	public Object getVariable(String name);

	/**
	 * Sets a context variable to a value or deletes it.
	 * 
	 * @param name the name of the variable, case-sensitive
	 * @param value the value of the variable, or null if it should be removed
	 */
	public void setVariable(String name, Object value);

	/**
	 * Tries to make the context continue its execution when it was previously interrupted for the given reason.
	 * 
	 * @param reason the result whose interruption should be recovered from; execution is only continued if it matches the reason the context was suspended
	 * 
	 * @return true if the execution was continued successfully, false if the reason didn't match the actual reason
	 * @throws IllegalStateException if the method is called from within an {@link ActionAgent} or if the execution hasn't started yet
	 */
	public boolean continueExecution(ActionResult reason);

	/**
	 * Cancels this context programatically by cancelling all remaining agents and removing it from the list of active deaths.
	 */
	public void cancel();

}
