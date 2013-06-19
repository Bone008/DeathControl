package bone008.bukkit.deathcontrol;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
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
import bone008.bukkit.deathcontrol.util.MessageUtil;
import bone008.bukkit.deathcontrol.util.Util;

public class DeathContextImpl implements DeathContext {

	private PlayerDeathEvent deathEvent;
	private String victimName; // we can't even store an OfflinePlayer, because Bukkit doesn't allow us to create one while the player is still online
	private Location deathLocation;
	private List<DeathCause> matchedDeathCauses;
	private List<StoredItemStack> itemDrops;

	private Map<String, Object> variables = new HashMap<String, Object>();

	// for processing
	private int disconnectTimeout = -1;
	private String cancelMessage = null;
	private AgentSet agents = new AgentSet();
	private AgentIterator executionIterator = null;

	private boolean tempBlocked = false;

	public DeathContextImpl(PlayerDeathEvent event) {
		Player victimp = event.getEntity();

		this.deathEvent = event;
		this.victimName = victimp.getName();
		this.deathLocation = victimp.getLocation();

		this.matchedDeathCauses = new ArrayList<DeathCause>();
		for (DeathCause dc : DeathCause.values()) {
			if (dc.appliesTo(victimp.getLastDamageCause()))
				matchedDeathCauses.add(dc);
		}

		// build an independant list item drops
		itemDrops = new ArrayList<StoredItemStack>();

		PlayerInventory playerInv = victimp.getInventory();
		int invSize = playerInv.getSize() + playerInv.getArmorContents().length;
		for (int slot = 0; slot < invSize; slot++) {
			ItemStack item = playerInv.getItem(slot);
			if (item != null)
				itemDrops.add(new StoredItemStack(slot, item.clone()));
		}

		Player playerKiller = Util.getPlayerAttackerFromEvent(victimp.getLastDamageCause());

		// initialize standard variables
		setVariable("plugin-prefix", MessageUtil.getPluginPrefix(false));
		setVariable("death-cause", matchedDeathCauses.get(0).toHumanString());
		setVariable("death-cause-formatted", Message.translatePath(matchedDeathCauses.get(0).toMsgPath()));
		setVariable("victim-name", victimp.getDisplayName());
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
		setVariable("items-damaged", 0);
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

	public void setCancelMessage(String cancelMessage) {
		// don't overwrite with a null value
		if (cancelMessage != null)
			this.cancelMessage = cancelMessage;
	}

	public String getCancelMessage() {
		return cancelMessage;
	}

	public boolean hasAgents() {
		return !agents.isEmpty();
	}

	public void preprocessAgents() {
		DeathControl.instance.log(Level.FINEST, "@" + victimName + ":  Preprocessing " + Util.pluralNum(agents.size(), "action") + " ...");

		for (ActionAgent agent : agents) {
			try {
				agent.preprocess();
			} catch (Throwable e) {
				DeathControl.instance.getLogger().log(Level.SEVERE, "Preprocessing action \"" + agent.getDescriptor().getName() + "\" caused an exception!", e);
			}
		}
	}

	public void executeAgents() {
		DeathControl.instance.log(Level.FINEST, "@" + victimName + ":  Starting execution of " + Util.pluralNum(agents.size(), "action") + " ...");

		agents.seal();
		executionIterator = agents.iteratorExecution();
		continueExecution(null);
	}

	public boolean isCancelled() {
		return DeathControl.instance.getActiveDeath(victimName) != this;
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
			ActionResult result;

			try {
				tempBlocked = true;
				result = agent.execute();
			} catch (Throwable e) {
				DeathControl.instance.getLogger().log(Level.SEVERE, "Executing action \"" + agent.getDescriptor().getName() + "\" caused an exception!", e);
				continue;
			} finally {
				tempBlocked = false;
			}

			if (result == null)
				result = ActionResult.STANDARD;

			DeathControl.instance.log(Level.FINEST, "@" + victimName + ":    " + agent.getDescriptor().getName() + " -> " + result);

			switch (result) {
			case STANDARD:
				break; // do nothing
			case PLAYER_OFFLINE:
				DeathControl.instance.log(Level.FINE, "@" + victimName + ":  Player was offline for action \"" + agent.getDescriptor().getName() + "\"!");
				// fall-through --> cancel if required
			case FAILED:
				if (agent.getDescriptor().isRequired()) {
					DeathControl.instance.log(Level.FINEST, "@" + victimName + ":  Cancelled because of action \"" + agent.getDescriptor().getName() + "\"!");
					cancel();
					return true; // cancel while loop
				}
				break;
			default:
				executionIterator.blockExecution(result);
				break;
			}
		}

		if (!executionIterator.hasNext()) {
			DeathControl.instance.log(Level.FINEST, "@" + victimName + ":  All actions executed!");
			cancel(); // no agents remaining, but we need to clean up behind ourselves
		}

		return true;
	}

	@Override
	public void cancel() {
		// don't show the cancel message when we finished successfully
		doCancel(executionIterator == null || executionIterator.hasNext());
	}

	public void cancelManually() {
		doCancel(false);
		MessageUtil.sendMessage(getVictim().getPlayer(), Message.CMD_CANCELLED);
	}

	private void doCancel(boolean withMessage) {
		if (tempBlocked)
			throw new IllegalStateException("can't cancel from within an agent");

		if (isCancelled())
			return;

		DeathControl.instance.clearActiveDeath(victimName);

		if (withMessage && cancelMessage != null && getVictim().isOnline()) {
			getVictim().getPlayer().sendMessage(cancelMessage);
		}

		// cancel all remaining agents, falling back to the beginning if not yet started
		Iterator<ActionAgent> agentIt;
		if (executionIterator == null)
			agentIt = agents.iterator();
		else
			agentIt = executionIterator;

		while (agentIt.hasNext()) {
			ActionAgent agent = agentIt.next();
			try {
				agent.cancel();
			} catch (Throwable e) {
				DeathControl.instance.getLogger().log(Level.SEVERE, "Cancelling action \"" + agent.getDescriptor().getName() + "\" caused an exception!", e);
			}
		}
	}

	@Override
	public Location getDeathLocation() {
		return deathLocation;
	}

	@Override
	public OfflinePlayer getVictim() {
		return Bukkit.getOfflinePlayer(victimName);
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
