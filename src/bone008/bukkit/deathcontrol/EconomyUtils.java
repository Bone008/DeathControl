package bone008.bukkit.deathcontrol;

import java.util.logging.Level;

import net.milkbowl.vault.economy.Economy;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;

import bone008.bukkit.deathcontrol.config.CauseSettings;

import com.nijikokun.register.payment.Method;
import com.nijikokun.register.payment.Method.MethodAccount;
import com.nijikokun.register.payment.Methods;

public final class EconomyUtils {

	private EconomyUtils() {
	}

	private static Economy vaultEconomy = null;

	/**
	 * Initializes the economy functionalities.
	 */
	public static void init() {
		// delay everything by one tick to make sure that all plugins are fully loaded. Softdepend doesn't always work!
		Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(DeathControl.instance, new Runnable() {
			@Override
			public void run() {
				try {
					setupVault();
				} catch (NoClassDefFoundError err) {
				}
			}
		});
	}

	/**
	 * Tries to hook Vault.
	 * 
	 * @throws NoClassDefFoundError Thrown when Vault is not loaded. Should be caught by the calling method because it is (more or less) intended behavior!
	 */
	private static void setupVault() {
		RegisteredServiceProvider<Economy> economyProvider = Bukkit.getServer().getServicesManager().getRegistration(Economy.class);
		if (economyProvider != null) {
			vaultEconomy = economyProvider.getProvider();
		}
	}

	public static double calcCost(Player ply, CauseSettings causeSettings) {
		if (ply == null || causeSettings == null)
			throw new IllegalArgumentException("null argument");

		if (!causeSettings.hasPotentialCost())
			return 0;

		double currBalance;
		Method m = getRegisterMethod();
		if (m != null) {
			MethodAccount acc = m.getAccount(ply.getName());
			currBalance = acc.balance();
		} else if (vaultEconomy != null) {
			currBalance = vaultEconomy.getBalance(ply.getName());
		} else {
			logNotice(ply.getName());
			return 0;
		}
		return causeSettings.getCost(currBalance);
	}

	/**
	 * Checks if <i>ply</i> has enough money to pay the cost specified in <i>causeSettings</i>.<br>
	 * Prints a warning if no way to manage economy was found.
	 * 
	 * @return true if the player has enough money or no economy management plugin was found, otherwise false
	 */
	public static boolean canAfford(Player ply, double cost) {
		if (ply == null)
			throw new IllegalArgumentException("player cannot be null");
		if (cost <= 0)
			return true;

		Method m = getRegisterMethod();
		if (m != null) {
			MethodAccount acc = m.getAccount(ply.getName());
			return acc.hasEnough(cost);
		} else if (vaultEconomy != null) {
			return vaultEconomy.has(ply.getName(), cost);
		} else {
			logNotice(ply.getName());
			return true;
		}

	}

	public static boolean payCost(Player ply, double cost) {
		if (ply == null)
			throw new IllegalArgumentException("player cannot be null");
		if (cost <= 0)
			return true;

		Method m = getRegisterMethod();
		if (m != null) {
			MethodAccount acc = m.getAccount(ply.getName());
			if (!acc.hasEnough(cost))
				return false;
			return acc.subtract(cost);
		} else if (vaultEconomy != null) {
			return vaultEconomy.withdrawPlayer(ply.getName(), cost).transactionSuccess();
		} else {
			logNotice(ply.getName());
			return true;
		}
	}

	public static String formatMoney(double cost) {
		Method m = getRegisterMethod();
		if (m != null)
			return m.format(cost);
		else if (vaultEconomy != null)
			return vaultEconomy.format(cost);
		else
			return Double.toString(cost);
	}

	private static void logNotice(String pName) {
		DeathControl.instance.log(Level.WARNING, "Couldn't calculate money for " + pName + " because no economy management plugin was found!");
	}

	/**
	 * Attempts to get the active Register Method.
	 * 
	 * @return The Method, or null if there is no active one or Register is not loaded.
	 */
	private static Method getRegisterMethod() {
		try {
			return Methods.getMethod();
		} catch (NoClassDefFoundError err) {
		} // ugly solution, I know ...
		return null;
	}

}
