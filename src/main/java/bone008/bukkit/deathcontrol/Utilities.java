package bone008.bukkit.deathcontrol;

import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Logger;

public final class Utilities {
	private Utilities(){}
	
	public static void dropItem(Location l, ItemStack i, boolean naturally){
		if(l == null || i == null || i.getTypeId() < 1 || i.getAmount() < 1)
			return;
		
		if(naturally)
			l.getWorld().dropItemNaturally(l, i);
		else
			l.getWorld().dropItem(l, i);
	}
	
	public static void dropItems(Location l, ItemStack[] items, boolean naturally){
		if(items == null) return;
		for(ItemStack i: items){
			dropItem(l, i, naturally);
		}
	}
	
	public static void dropItems(Location l, Collection<ItemStack> items, boolean naturally){
		if(items == null) return;
		dropItems(l, items.toArray(new ItemStack[items.size()]), naturally);
	}
	
	public static void dropItems(Location l, Map<?, ItemStack> items, boolean naturally){
		if(items == null) return;
		dropItems(l, items.values(), naturally);
	}
	
	
	
	/**
	 * Gets a list from the given {@code ConfigurationSection} with a special type given through the formal type parameter.
	 * @param <T> The type which the list is expected to be. <u>Note: Must be explicitly declared in the method call!</u>
	 * @param sec The ConfigurationSection to use
	 * @param node The node to use
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static <T> List<T> getConfigList(ConfigurationSection sec, String node){
		return (List<T>)sec.getList(node);
	}


	public static int getConfigInt(ConfigurationSection sec, String node, int def){
		Object o = sec.get(node, def);
		if(o == null)
			return def;
		try{
			return Integer.valueOf(o.toString());
		} catch(NumberFormatException ex){
			return 0;
		}
	}
	public static double getConfigDouble(ConfigurationSection sec, String node, double def){
		Object o = sec.get(node, def);
		if(o == null)
			return def;
		try{
			return Double.valueOf(o.toString());
		} catch(NumberFormatException ex){
			return 0;
		}
	}
	
	public static String getConfigString(ConfigurationSection sec, String node, String def){
		Object o = sec.get(node, def);
		if(o == null)
			return def;
		return o.toString();
	}
	
	
	/**
	 * Joins the elements of a given Collection with a delimiter.
	 * @param delimiter The delimiter to separate the elements.
	 * @param collection The Collection to take the elements from.
	 * @return A String with the joined elements, or null if the Collection was null. Returns an empty String if the Collection was empty.
	 */
	public static String joinCollection(String delimiter, Collection<?> collection) {
		if(collection == null)
			return null;
		StringBuilder ret = new StringBuilder();
		Iterator<?> it = collection.iterator();
		while(it.hasNext()){
			ret.append(it.next());
			if(it.hasNext())
				ret.append(delimiter);
		}
		return ret.toString();
	}
	

	/**
	 * If {@code obj} equals {@code search}, {@code repl} is returned. Otherwise obj will be returned unchanged.
	 * @param obj The object to validate.
	 * @param search The condition to replace {@code obj}
	 * @param repl The value to replace {@code obj} with if {@code search} matched
	 */
	public static <T> T replaceValue(T obj, T search, T repl) {
		if(obj.equals(search))
			return repl;
		return obj;
	}
	
	// dump is only used for developing purposes
	
	public static void dump(Iterable<?> c){
		Logger logger = Logger.getLogger("Minecraft");
		logger.info(c.getClass().getSimpleName()+":");
		for(Object o: c){
			logger.info("\t"+o.toString());
		}
	}
	
	public static void dump(Map<?,?> obj){
		dump(obj, false);
	}
	public static void dump(Map<?,?> obj, boolean deep){
		Logger logger = Logger.getLogger("Minecraft");
		for(Entry<?,?> entry: obj.entrySet()){
			logger.info(entry.getKey().getClass().getSimpleName()+"(" + entry.getKey().toString() + ")");
			if(deep && entry.getValue() instanceof Iterable){
				logger.info("\t" + entry.getValue().getClass().getSimpleName()+":");
				for(Object val: (Iterable<?>)entry.getValue())
					logger.info("\t\t" + val.toString());
			} else
				logger.info("\t" + entry.getValue().toString());
		}
	}

	
}
