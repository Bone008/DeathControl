package bone008.bukkit.deathcontrol;

import java.util.Iterator;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.PlayerDeathEvent;

import bone008.bukkit.deathcontrol.AgentSet.AgentIterator;
import bone008.bukkit.deathcontrol.newconfig.ActionAgent;
import bone008.bukkit.deathcontrol.newconfig.ActionResult;
import bone008.bukkit.deathcontrol.newconfig.DeathContext;

public class DeathContextImpl implements DeathContext {

	private PlayerDeathEvent deathEvent;
	private Player victim;
	private Location deathLocation;
	private DeathCause deathCause;

	// for processing
	private int disconnectTimeout = -1;
	private AgentSet agents = new AgentSet();
	private AgentIterator executionIterator = null;

	private boolean tempBlocked = false;

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
		Bukkit.broadcastMessage("Context for " + victim.getName() + " preprocessing!");

		for (ActionAgent agent : agents)
			agent.preprocess();
	}

	public void executeAgents() {
		Bukkit.broadcastMessage("Context for " + victim.getName() + " executing!");

		executionIterator = agents.iteratorExecution();
		continueExecution(null);
	}

	public boolean isCancelled() {
		return DeathControl.instance.getActiveDeath(victim) != this;
	}

	@Override
	public boolean continueExecution(ActionResult reason) {
		if (tempBlocked)
			throw new IllegalStateException("can't continue execution from within an agent");

		if (isCancelled())
			return false;

		if (executionIterator == null)
			throw new IllegalStateException("can't continue without having started");

		if (!executionIterator.unblockExecution(reason))
			return false;

		while (executionIterator.canContinue()) {
			ActionAgent agent = executionIterator.next();

			tempBlocked = true;
			ActionResult result = agent.execute();
			tempBlocked = false;

			if (result == null)
				result = ActionResult.STANDARD;

			switch (result) {
			case STANDARD:
				break; // do nothing
			case FAILED:
				if (agent.getDescriptor().isRequired()) {
					cancel();
					return true; // cancel while loop
				}
				break;
			default:
				executionIterator.blockExecution(result);
				break;
			}
		}

		if (!executionIterator.hasNext())
			cancel(); // no agents remaining, but we need to clean up behind ourselves

		return true;
	}

	@Override
	public void cancel() {
		if (tempBlocked)
			throw new IllegalStateException("can't cancel from within an agent");

		if (isCancelled())
			return;

		DeathControl.instance.clearActiveDeath(victim);

		Bukkit.broadcastMessage("Context for " + victim.getName() + " cancelling!");

		// cancel all remaining agents, falling back to the beginning if not yet started
		Iterator<ActionAgent> agentIt;
		if (executionIterator == null)
			agentIt = agents.iterator();
		else
			agentIt = executionIterator;

		while (agentIt.hasNext())
			agentIt.next().cancel();
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
