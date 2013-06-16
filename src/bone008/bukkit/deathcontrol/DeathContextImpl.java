package bone008.bukkit.deathcontrol;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import bone008.bukkit.deathcontrol.AgentSet.AgentIterator;
import bone008.bukkit.deathcontrol.config.ActionAgent;
import bone008.bukkit.deathcontrol.config.ActionResult;
import bone008.bukkit.deathcontrol.config.DeathContext;
import bone008.bukkit.deathcontrol.util.EconomyUtil;
import bone008.bukkit.deathcontrol.util.Message;
import bone008.bukkit.deathcontrol.util.Util;

public class DeathContextImpl implements DeathContext {

	private PlayerDeathEvent deathEvent;
	private Player victim;
	private Location deathLocation;
	private List<DeathCause> matchedDeathCauses;
	private List<StoredItemStack> itemDrops;

	private Map<String, Object> variables = new HashMap<String, Object>();

	// for processing
	private int disconnectTimeout = -1;
	private AgentSet agents = new AgentSet();
	private AgentIterator executionIterator = null;

	private boolean tempBlocked = false;

	public DeathContextImpl(PlayerDeathEvent event) {
		this.deathEvent = event;
		this.victim = event.getEntity();
		this.deathLocation = victim.getLocation();

		this.matchedDeathCauses = new ArrayList<DeathCause>();
		for (DeathCause dc : DeathCause.values()) {
			if (dc.appliesTo(victim.getLastDamageCause()))
				matchedDeathCauses.add(dc);
		}

		// build an independant list item drops
		itemDrops = new ArrayList<StoredItemStack>();

		PlayerInventory playerInv = victim.getInventory();
		int invSize = playerInv.getSize() + playerInv.getArmorContents().length;
		for (int slot = 0; slot < invSize; slot++) {
			ItemStack item = playerInv.getItem(slot);
			if (item != null)
				itemDrops.add(new StoredItemStack(slot, item.clone()));
		}

		Player playerKiller = Util.getPlayerAttackerFromEvent(victim.getLastDamageCause());

		// initialize standard variables
		setVariable("death-cause", matchedDeathCauses.get(0).toHumanString());
		setVariable("death-cause-formatted", Message.translatePath(matchedDeathCauses.get(0).toMsgPath()));
		setVariable("victim-name", victim.getDisplayName());
		setVariable("world", deathLocation.getWorld().getName());
		setVariable("killer-name", (playerKiller != null ? playerKiller.getDisplayName() : ""));
		setVariable("death-message", deathEvent.getDeathMessage());

		// defaults for variables set by agents
		setVariable("money-paid", EconomyUtil.formatMoney(0));
		setVariable("money-paid-raw", 0);
		setVariable("items-kept-percent", "0%");
		setVariable("items-dropped-percent", "100%");
		setVariable("items-destroyed-percent", "0%");
		setVariable("last-command", "null");
	}

	public void assignAgent(ActionAgent agent) {
		agents.add(agent);
	}

	public void setDisconnectTimeout(int timeout) {
		// always set to the new timeout, that means another handling took priority over the old one
		disconnectTimeout = timeout; // TODO test disconnect-timeouts
	}

	public int getDisconnectTimeout() {
		return disconnectTimeout;
	}

	public boolean hasAgents() {
		return !agents.isEmpty();
	}

	public void preprocessAgents() {
		Bukkit.broadcastMessage(ChatColor.GRAY + "||| Context for " + victim.getName() + " preprocessing!");

		for (ActionAgent agent : agents)
			agent.preprocess();
	}

	public void executeAgents() {
		Bukkit.broadcastMessage(ChatColor.GRAY + "||| Context for " + victim.getName() + " executing!");

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
			ActionResult result = agent.execute(); // FIXME catch exceptions of agents
			tempBlocked = false;

			if (result == null)
				result = ActionResult.STANDARD;

			DeathControl.instance.log(Level.FINEST, "@" + victim.getName() + ":  " + agent.getDescriptor().getName() + " -> " + result);

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

		Bukkit.broadcastMessage(ChatColor.GRAY + "||| Context for " + victim.getName() + " cancelling!");

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
	public List<StoredItemStack> getItemDrops() {
		return itemDrops;
	}

	@Override
	public PlayerDeathEvent getDeathEvent() {
		return deathEvent;
	}

	@Override
	public Object getVariable(String name) {
		return variables.get(name.toLowerCase());
	}

	@Override
	public void setVariable(String name, Object value) {
		if (value == null)
			variables.remove(name.toLowerCase());
		else
			variables.put(name.toLowerCase(), value);
	}

	@Override
	public String replaceVariables(CharSequence input) {
		if (input == null)
			return null;

		String replaced = input.toString();

		for (Entry<String, Object> var : variables.entrySet()) {
			String name = var.getKey();
			String value = var.getValue().toString(); // let the object itself handle string serialization

			replaced = replaced.replace("%" + name + "%", value);
		}

		return replaced;
	}

}
