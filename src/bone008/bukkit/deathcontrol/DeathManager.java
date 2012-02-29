package bone008.bukkit.deathcontrol;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.nijikokun.register.payment.Method.MethodAccount;

import bone008.bukkit.deathcontrol.config.CauseSettings;
import bone008.bukkit.deathcontrol.config.CauseData.HandlingMethod;

public class DeathManager {

	private boolean valid = false;

	private DeathControl plugin;
	private Player ply;
	private List<ItemStack> drops;
	private DeathCause deathCause;

	public HandlingMethod method;
	private int timeout;
	private double cost;
	private MethodAccount acc;

	private Location deathLocation;
	private List<ItemStack> savedItems = new ArrayList<ItemStack>();

	private Timer timer;

	public DeathManager(DeathControl plugin, Player player, DeathCause deathCause, List<ItemStack> drops) {
		if (plugin == null || player == null || deathCause == null || drops == null)
			throw new NullPointerException("null argument");
		this.plugin = plugin;
		this.ply = player;
		this.deathCause = deathCause;
		this.drops = drops;

		if (plugin.managers.get(ply) != null) {
			plugin.managers.get(ply).expire(true);
		}
		if (plugin.managers.get(ply) != null) {
			throw new Error("Old DeathManager didn't unregister itself!");
		}

		plugin.managers.put(ply, this);
		this.valid = true;
	}

	public Response handle() {
		// check valid
		if (!this.valid) {
			unregister();
			return new Response();
		}

		// check use permission
		if (!plugin.hasPermission(ply, DeathControl.PERMISSION_USE)) {
			unregister();
			return new Response();
		}

		// get the CauseHandling for the cause
		CauseSettings settings = plugin.config.getSettings(deathCause);

		// check if we have a handling
		if (settings == null) {
			unregister();
			return new Response();
		}

		// calculate the cost
		if (plugin.hasPermission(ply, DeathControl.PERMISSION_FREE)) {
			cost = 0;
		} else {
			cost = settings.getCost(calcMoney(settings));
		}

		// store handling settings in members
		method = settings.getMethod();
		timeout = settings.getTimeout();
		// save the death location
		deathLocation = ply.getLocation();

		List<ItemStack> kept = null;
		byte keptItemsMode = Response.KEPT_NONE;

		// store the inventory if configured
		if (settings.keepInventory()) {
			kept = calculateItems(drops, settings);

			// cancel if player doesn't have any items to be restored
			if (kept.size() <= 0) {
				unregister();
				return new Response();
			}

		} else {
			// make sure no item interactions take place when we don't keep
			// anything
			unregister();
		}

		// pay the configured money
		if (plugin.getRegisterMethod() == null) {
			plugin.log(Level.WARNING, ply.getName() + " can't pay for his death because no economy plugin was found!");
			cost = 0;
		} else {
			acc = plugin.getRegisterMethod().getAccount(ply.getName());
			if (acc.hasEnough(cost)) {
				if (method == HandlingMethod.AUTO)
					acc.subtract(cost);
			} else {
				unregister();
				return new Response(true, false, null, false, Response.KEPT_NONE);
			}
		}

		if (kept != null) {
			for (ItemStack i : kept) {
				savedItems.add(i.clone());
				drops.remove(i);
			}

			if (drops.isEmpty())
				keptItemsMode = Response.KEPT_ALL;
			else
				keptItemsMode = Response.KEPT_SOME;
		}

		if (keptItemsMode != Response.KEPT_NONE && method == HandlingMethod.COMMAND && timeout > 0) {
			timer = new Timer();
			timer.schedule(new TimerTask() {
				@Override
				public void run() {
					expire(true);
				}
			}, timeout * 1000L);
		}

		return new Response(true, true, cost, (method == HandlingMethod.COMMAND), keptItemsMode);

	}

	/**
	 * Calculates a list of {@link ItemStack}s that the player keeps. Considers
	 * lists and loss-percentage.
	 * 
	 * @param droppedItems
	 *            the original drops. Not affected!
	 * @param handling
	 *            The cause handling associated with the death cause.
	 */
	private List<ItemStack> calculateItems(List<ItemStack> droppedItems, CauseSettings settings) {
		List<ItemStack> ret = new ArrayList<ItemStack>();

		// save the items that may be kept due to whitelist/blacklist limits in
		// "temp"
		for (ItemStack item : droppedItems) {
			if (settings.isValidItem(item))
				ret.add(item);
		}

		// no need to do anything when there are no items
		if (ret.isEmpty())
			return ret;

		if (settings.getLoss() > 0) {
			// apply the loss-percentage
			double lossMultiplier = settings.getLoss() / 100;
			int lostStacks = Math.round((float) (ret.size() * lossMultiplier));
			Random rand = new Random();

			for (int i = 0; i < lostStacks; i++) {
				ret.remove(rand.nextInt(ret.size()));
			}
		}

		return ret;
	}

	public class Response {
		public static final byte KEPT_NONE = 0;
		public static final byte KEPT_SOME = 1;
		public static final byte KEPT_ALL = 2;

		/**
		 * true if there was a valid handling and the use permission
		 */
		public final boolean didSomething;
		/**
		 * true if a handling successfully happened
		 */
		public final boolean success;
		/**
		 * the amount of money that was paid or null if there was not enough
		 * money.
		 */
		public final Double money;
		/**
		 * determines if items are given back via command
		 */
		public final boolean isCommand;
		public final byte keptItems;

		public Response() {
			this(false, false, 0D, false, KEPT_NONE);
		}

		public Response(boolean didSomething) {
			this(didSomething, false, 0D, false, KEPT_NONE);
		}

		public Response(boolean didSomething, boolean success, Double money, boolean isCommand, byte keptItems) {
			this.didSomething = didSomething;
			this.success = success;
			this.money = money;
			this.isCommand = isCommand;
			this.keptItems = keptItems;
		}
	}

	/*
	 * public enum Response{ NO_PERMISSIONS, NO_HANDLING, LACK_OF_MONEY,
	 * ISSUED_COMMAND, ISSUED_AUTO; }
	 */

	public void expire(boolean showMessage) {
		// cancel if stuff was already given back
		if (!this.valid) {
			return;
		}

		// drops items
		if (deathLocation == null)
			throw new Error("deathLocation should not be null");
		Utilities.dropItems(deathLocation, this.savedItems, true);

		// sends notification to the player
		if (showMessage) {
			plugin.display(ply, ChatColor.DARK_RED + "Time is up.");
			plugin.display(ply, ChatColor.DARK_RED + "Your items are dropped at your death location.");
		}

		// logs to console
		plugin.log("Timer for " + ply.getName() + " expired! Items dropped.");

		unregister();
	}

	public void respawned() {
		if (method == HandlingMethod.AUTO) {
			restore();
			plugin.log(ply.getName() + " respawned and got back their items.");
		}
	}

	public boolean commandIssued() {
		if (method == HandlingMethod.COMMAND && this.valid) {
			if (cost > 0 && acc != null) {
				if (!acc.hasEnough(cost)) {
					plugin.display(ply, ChatColor.RED + "You don't have enough money for that!");
					return true;
				} else {
					acc.subtract(cost);
				}
			}
			restore();
			plugin.display(ply, "You got your items back!");
			plugin.log(ply.getName() + " got back their items via command.");
			return true;
		}
		return false;
	}

	private void restore() {
		if (savedItems != null) {
			Iterator<ItemStack> iter = savedItems.iterator();
			while (iter.hasNext()) {
				HashMap<Integer, ItemStack> rest = ply.getInventory().addItem(iter.next());
				if (rest.size() > 0) {
					Utilities.dropItems(ply.getLocation(), rest, false);
				}
				iter.remove();
			}
		}

		unregister();
	}

	private void unregister() {
		plugin.managers.remove(ply);
		this.valid = false;
	}

	private double calcMoney(CauseSettings settings) {
		if (plugin.getRegisterMethod() != null) {
			MethodAccount plyAccount = plugin.getRegisterMethod().getAccount(ply.getName());
			if (plyAccount != null)
				return plyAccount.balance();
		}
		return 0;
	}

	public int getTimeout() {
		return timeout;
	}

}
