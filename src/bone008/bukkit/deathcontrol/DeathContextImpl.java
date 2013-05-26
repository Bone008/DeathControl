package bone008.bukkit.deathcontrol;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.PlayerDeathEvent;

import bone008.bukkit.deathcontrol.newconfig.ActionAgent;
import bone008.bukkit.deathcontrol.newconfig.DeathContext;

public class DeathContextImpl implements DeathContext {

	private PlayerDeathEvent deathEvent;
	private Player victim;
	private Location deathLocation;
	private DeathCause deathCause;

	// for processing
	private int disconnectTimeout = -1;
	private List<ActionAgent> agents = new ArrayList<ActionAgent>();

	public DeathContextImpl(PlayerDeathEvent event, DeathCause deathCause) {
		this.deathEvent = event;
		this.victim = event.getEntity();
		this.deathLocation = victim.getLocation();
		this.deathCause = deathCause;
	}

	public void assignAgent(ActionAgent agent) {
		agents.add(agent);
	}

	public void setDisconnectTimeout(int timeout) {
		// 1. don't set to infinite timeout (can't override any other setting)
		// 2. always set when currently at infinite timeout
		// 3. otherwise only set when lower new timeout
		if (timeout > -1 && (disconnectTimeout == -1 || timeout < disconnectTimeout))
			disconnectTimeout = timeout;
	}

	public int getDisconnectTimeout() {
		return disconnectTimeout;
	}

	public boolean hasAgents() {
		return !agents.isEmpty();
	}

	public void preprocessAgents() {
		Bukkit.broadcastMessage("Context for " + victim.getName() + " preprocessed!");

		for (ActionAgent agent : agents)
			agent.preprocess();
	}

	public void executeAgents() {
		Bukkit.broadcastMessage("Context for " + victim.getName() + " executed!");

		for (ActionAgent agent : agents)
			agent.execute();

		DeathControl.instance.clearActiveDeath(victim);
	}

	public void cancel() {
		Bukkit.broadcastMessage("Context for " + victim.getName() + " cancelled!");

		for (ActionAgent agent : agents)
			agent.cancel();

		DeathControl.instance.clearActiveDeath(victim);
	}

	public boolean isCancelled() {
		return DeathControl.instance.getActiveDeath(victim) != this;
	}

	@Override
	public Location getDeathLocation() {
		return deathLocation;
	}

	@Override
	public Player getVictim() {
		return victim;
	}

	@Override
	public DeathCause getDeathCause() {
		return deathCause;
	}

	@Override
	public PlayerDeathEvent getDeathEvent() {
		return deathEvent;
	}

}
