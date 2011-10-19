package bone008.bukkit.deathcontrol.config;

import org.bukkit.inventory.ItemStack;

import java.util.HashSet;
import java.util.Set;

import bone008.bukkit.deathcontrol.DeathCause;
import bone008.bukkit.deathcontrol.config.CauseData.HandlingMethod;

/**
 * General interface that represents the settings for a specific death cause.
 * Has some general methods implemented that are valid for every sub-class.
 * 
 * @see RootCauseSettings, ParentedCauseSettings
 */
public class CauseSettings {
	private final DeathCause cause;
	private final CauseData data;
	private final DeathConfiguration config;

	public CauseSettings(DeathConfiguration config, DeathCause dc, CauseData d) {
		this.config = config;
		cause = dc;
		data = d;
	}

	public double getCost(double currentMoney) {
		if (data.getCost(currentMoney) != null)
			return data.getCost(currentMoney);
		else if (cause.parent != null) {
			CauseSettings s = config.getSettings(cause.parent);
			if (s != null)
				return s.getCost(currentMoney);
		}

		return DeathConfiguration.default_cost;
	}

	public String getRawCost() {
		if (data.getRawCost() != null)
			return data.getRawCost();
		else if (cause.parent != null) {
			CauseSettings s = config.getSettings(cause.parent);
			if (s != null)
				return s.getRawCost();
		}
		
		return Double.toString(DeathConfiguration.default_cost);
	}

	public double getLoss() {
		if (data.loss != null)
			return data.loss;
		else if (cause.parent != null) {
			CauseSettings s = config.getSettings(cause.parent);
			if (s != null)
				return s.getLoss();
		}

		return DeathConfiguration.default_loss;
	}

	public HandlingMethod getMethod() {
		if (data.method != null)
			return data.method;
		else if (cause.parent != null) {
			CauseSettings s = config.getSettings(cause.parent);
			if (s != null)
				return s.getMethod();
		}

		return DeathConfiguration.default_method;
	}

	public int getTimeout() {
		if (data.timeout != null)
			return data.timeout;
		else if (cause.parent != null) {
			CauseSettings s = config.getSettings(cause.parent);
			if (s != null)
				return s.getTimeout();
		}

		return DeathConfiguration.default_timeout;
	}

	public boolean keepInventory() {
		if (data.keepInventory != null)
			return data.keepInventory;
		else if (cause.parent != null) {
			CauseSettings s = config.getSettings(cause.parent);
			if (s != null)
				return s.keepInventory();
		}

		return DeathConfiguration.default_keepInventory;
	}

	public Set<ListItem> getWhitelist() {
		Set<ListItem> ret = new HashSet<ListItem>();
		if (cause.parent != null) {
			CauseSettings s = config.getSettings(cause.parent);
			if (s != null) {
				Set<ListItem> parentList = s.getWhitelist();
				if (parentList != null)
					ret = parentList;
			}
		}

		if (data.whitelist != null)
			ret.addAll(data.whitelist);
		return ret;
	}

	public Set<ListItem> getBlacklist() {
		Set<ListItem> ret = new HashSet<ListItem>();
		if (cause.parent != null) {
			CauseSettings s = config.getSettings(cause.parent);
			if (s != null) {
				Set<ListItem> parentList = s.getBlacklist();
				if (parentList != null)
					ret = parentList;
			}
		}

		if (data.blacklist != null)
			ret.addAll(data.blacklist);
		return ret;
	}

	/**
	 * Checks if the given item may be kept. Combines whitelists and blacklists
	 * to match the result.
	 * 
	 * @param itemStack
	 *            the item stack to check for
	 * @return true, if the item may be kept, otherwise false.
	 */
	public boolean isValidItem(ItemStack itemStack) {
		// check blacklist match -> return false
		for (ListItem item : getBlacklist()) {
			if (item.matches(itemStack))
				return false;
		}

		Set<ListItem> whitelist = getWhitelist();
		// if a whitelist is defined
		if (whitelist.size() > 0) {
			// check whitelist match -> return true
			for (ListItem item : whitelist) {
				if (item.matches(itemStack))
					return true;
			}
			// false if no match
			return false;
		}

		// if no whitelist -> automatically valid
		return true;
	}

}
