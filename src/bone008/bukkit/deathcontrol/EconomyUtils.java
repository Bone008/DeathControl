package bone008.bukkit.deathcontrol;

import java.util.logging.Level;

import org.bukkit.entity.Player;

import bone008.bukkit.deathcontrol.config.CauseSettings;

import com.nijikokun.register.payment.Method;
import com.nijikokun.register.payment.Method.MethodAccount;
import com.nijikokun.register.payment.Methods;

public final class EconomyUtils {

	private EconomyUtils() {
	}

	public static double calcCost(Player ply, CauseSettings causeSettings) {
		if (ply == null || causeSettings == null)
			throw new IllegalArgumentException("null argument");

		if (!causeSettings.hasPotentialCost())
			return 0;

		Method m = getRegisterMethod();
		if (m == null) {
			logNotice(ply.getName());
			return 0;
		}
		MethodAccount acc = m.getAccount(ply.getName());
		return causeSettings.getCost(acc.balance());
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
		Method m = getRegisterMethod();
		if (m == null) {
			logNotice(ply.getName());
			return true;
		}
		MethodAccount acc = m.getAccount(ply.getName());
		return acc.hasEnough(cost);
	}

	public static boolean payCost(Player ply, double cost) {
		if (ply == null)
			throw new IllegalArgumentException("player cannot be null");
		if (cost <= 0)
			return true;
		Method m = getRegisterMethod();
		if (m == null) {
			logNotice(ply.getName());
			return true;
		}
		MethodAccount acc = m.getAccount(ply.getName());
		if (!acc.hasEnough(cost))
			return false;
		return acc.subtract(cost);
	}

	public static String formatMoney(double cost) {
		Method m = getRegisterMethod();
		if (m == null)
			return Double.toString(cost);
		else
			return m.format(cost);
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
