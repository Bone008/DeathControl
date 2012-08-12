package bone008.bukkit.deathcontrol.config;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.bukkit.inventory.ItemStack;
import bone008.bukkit.deathcontrol.DeathControl;
import bone008.bukkit.deathcontrol.config.DeathConfiguration.RawOptions;
import bone008.bukkit.deathcontrol.exceptions.IllegalPropertyException;
import bone008.bukkit.deathcontrol.exceptions.ListNotFoundException;

/**
 *  stores configured data about how to deal with a specific cause
 */
public class CauseData {
	
	// auto-boxed types to allow null values
	public final Boolean keepInventory;
	public final Boolean keepExperience;
	public final HandlingMethod method;
	public final Integer timeout;
	public final Integer timeoutOnQuit;
	public final Double loss;
	public final Double lossExp;
	public final Set<ListItem> whitelist;
	public final Set<ListItem> blacklist;

	private final Double cost;
	private final boolean costIsPercentage;
	
	public RawOptions raw;
	
	public CauseData(DeathControl plugin, RawOptions raw) throws ListNotFoundException, IllegalPropertyException{
		this.raw = raw;

		this.keepInventory = (raw.isDefined(RawOptions.NODE_KEEP_INVENTORY) ? raw.keepInventory : null);
		this.keepExperience = (raw.isDefined(RawOptions.NODE_KEEP_EXPERIENCE) ? raw.keepExperience : null);
		
		if(raw.isDefined(RawOptions.NODE_COST)){
			try{
				int percentSignIndex = raw.rawCost.indexOf('%');
				// also check if it's not the first character
				if(percentSignIndex > 0){
					this.cost = Double.parseDouble(raw.rawCost.substring(0, percentSignIndex));
					this.costIsPercentage = true;
				} else{
					this.cost = Double.parseDouble(raw.rawCost);
					this.costIsPercentage = false;
				}
			} catch(NumberFormatException ex){
				throw new IllegalPropertyException(RawOptions.NODE_COST, raw.rawCost);
			}
		} else {
			this.cost = null;
			this.costIsPercentage = false;
		}
		
		try{
			this.method = (raw.isDefined(RawOptions.NODE_METHOD) ? HandlingMethod.valueOf(raw.method.toUpperCase()) : null);
		} catch(IllegalArgumentException e){
			throw new IllegalPropertyException(RawOptions.NODE_METHOD, raw.method);
		}

		this.timeout = (raw.isDefined(RawOptions.NODE_TIMEOUT) ? raw.timeout : null);
		this.timeoutOnQuit = (raw.isDefined(RawOptions.NODE_TIMEOUT_ON_QUIT) ? raw.timeoutOnQuit : null);

		if(raw.isDefined(RawOptions.NODE_LOSS_PERCENTAGE)){
			if(raw.loss < 0 || raw.loss > 100)
				throw new IllegalPropertyException(RawOptions.NODE_LOSS_PERCENTAGE, Double.toString(raw.loss));
			this.loss = raw.loss;
		} else this.loss = null;

		if(raw.isDefined(RawOptions.NODE_LOSS_PERCENTAGE_EXP)){
			if(raw.lossExp < 0 || raw.lossExp > 100)
				throw new IllegalPropertyException(RawOptions.NODE_LOSS_PERCENTAGE_EXP, Double.toString(raw.loss));
			this.lossExp = raw.lossExp;
		} else this.lossExp = null;
		
		
		if(raw.isDefined(RawOptions.NODE_WHITELIST)){
			this.whitelist = new HashSet<ListItem>();
			if(raw.whitelist != null){
				for(String listEntry: raw.whitelist){
					List<ListItem> currList = plugin.deathLists.getList(listEntry.toLowerCase());
					if(currList == null){
						throw new ListNotFoundException(listEntry.toLowerCase());
					} else{
						this.whitelist.addAll(currList);
					}
				}
			} else
				throw new IllegalPropertyException(RawOptions.NODE_WHITELIST, "NOT A LIST");
		} else
			this.whitelist = null;

		
		if(raw.isDefined(RawOptions.NODE_BLACKLIST)){
			this.blacklist = new HashSet<ListItem>();
			if(raw.blacklist != null){
				for(String listEntry: raw.blacklist){
					List<ListItem> currList = plugin.deathLists.getList(listEntry.toLowerCase());
					if(currList == null){
						throw new ListNotFoundException(listEntry.toLowerCase());
					} else{
						this.blacklist.addAll(currList);
					}
				}
			} else
				throw new IllegalPropertyException(RawOptions.NODE_BLACKLIST, "NOT A LIST");
		} else
			this.blacklist = null;
		
	}
	
	
	public Double getCost(double currentMoney){
		if(costIsPercentage)
			return currentMoney*cost/100;
		else
			return cost;
	}
	
	public String getRawCost() {
		return cost + (costIsPercentage ? "%" : "");
	}
	
	public boolean hasPotentialCost(){
		return (cost != null && cost > 0);
	}
	

	/**
	 * Checks if the given item may be kept. Combines whitelists and blacklists to match the result.
	 * @param itemStack the item stack to check for
	 * @return true, if the item may be kept, otherwise false.
	 */
	public boolean isValidItem(ItemStack itemStack){
		// check blacklist match -> return false
		for(ListItem item: blacklist){
			if(item.matches(itemStack))
				return false;
		}
		
		// if a whitelist is defined
		if(whitelist.size() > 0){
			// check whitelist match -> return true
			for(ListItem item: whitelist){
				if(item.matches(itemStack))
					return true;
			}
			// false if no match
			return false;
		}
		
		// if no whitelist -> automatically valid
		return true;
	}
	
	
	public enum HandlingMethod{
		AUTO, COMMAND;
	}
	
}
