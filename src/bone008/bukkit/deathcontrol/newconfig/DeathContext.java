package bone008.bukkit.deathcontrol.newconfig;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.PlayerDeathEvent;

import bone008.bukkit.deathcontrol.DeathCause;

public interface DeathContext {

	public Location getDeathLocation();

	public Player getVictim();

	public DeathCause getDeathCause();

	public PlayerDeathEvent getDeathEvent();

	public boolean continueExecution(ActionResult reason);

	public void cancel();

}
