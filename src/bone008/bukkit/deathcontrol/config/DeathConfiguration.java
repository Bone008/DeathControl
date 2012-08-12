package bone008.bukkit.deathcontrol.config;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Level;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import bone008.bukkit.deathcontrol.DeathCause;
import bone008.bukkit.deathcontrol.DeathControl;
import bone008.bukkit.deathcontrol.Utilities;
import bone008.bukkit.deathcontrol.config.CauseData.HandlingMethod;
import bone008.bukkit.deathcontrol.exceptions.IllegalPropertyException;
import bone008.bukkit.deathcontrol.exceptions.ListNotFoundException;

public class DeathConfiguration {

	/**
	 * The char that separates the cause name from its metadata.
	 */
	public static final char SEPARATOR = '|';

	public static final boolean default_keepInventory = false;
	public static final boolean default_keepExperience = false;
	public static final double default_cost = 0;
	public static final String default_cost_raw = String.valueOf(default_cost);
	public static final HandlingMethod default_method = HandlingMethod.AUTO;
	public static final int default_timeout = 15;
	public static final int default_timeoutOnQuit = 30;
	public static final double default_loss = 0;
	public static final double default_lossExp = default_loss;

	public static final boolean default_bukkitPerms = true;
	public static final String default_loggingLevel = "standard";
	public static final boolean default_allowCrossworld = true;
	public static final List<String> default_limitedWorlds = new ArrayList<String>();
	public static final boolean default_showMessages = true;

	private DeathControl plugin;
	private FileConfiguration config;

	// the results that were parsed from the config
	public EnumMap<DeathCause, CauseData> handlings = new EnumMap<DeathCause, CauseData>(DeathCause.class);
	public boolean bukkitPerms;
	public int loggingLevel;
	public boolean allowCrossworld;
	private Set<String> limitedWorlds; // access through API, hence private
	public boolean showMessages;

	// a list of errors that occurred while parsing
	public List<String> errors = new ArrayList<String>();

	public DeathConfiguration(DeathControl plugin, FileConfiguration config) {
		this.plugin = plugin;
		this.config = config;

		load();

		if (errors.size() > 0) {
			plugin.log(Level.WARNING, errors.size() + " errors in config.yml:", true);
			for (String err : errors) {
				plugin.log(Level.WARNING, "-> " + err, true);
			}
		}

		plugin.log(Level.CONFIG, "loaded " + handlings.size() + " valid death cause" + (handlings.size() == 1 ? "" : "s") + "!", true);
	}

	public void load() {
		handlings.clear();
		errors.clear();

		bukkitPerms = config.getBoolean("use-bukkit-permissions", default_bukkitPerms);

		if (config.get("logging-level") instanceof Number)
			config.set("logging-level", default_loggingLevel);
		String rawLoggingLevel = config.getString("logging-level", default_loggingLevel);
		loggingLevel = parseLoggingLevel(rawLoggingLevel);
		if (loggingLevel == -1) {
			errors.add("invalid logging-level: " + rawLoggingLevel);
			loggingLevel = parseLoggingLevel(default_loggingLevel);
		}

		allowCrossworld = config.getBoolean("multi-world.allow-cross-world", default_allowCrossworld);

		List<String> rawLimitedWorlds = config.getStringList("multi-world.limited-worlds");
		if (rawLimitedWorlds == null || rawLimitedWorlds.isEmpty())
			rawLimitedWorlds = default_limitedWorlds;
		limitedWorlds = new HashSet<String>(rawLimitedWorlds);

		showMessages = config.getBoolean("show-messages", default_showMessages);

		// parse causes
		ConfigurationSection causesSection = config.getConfigurationSection("DeathCauses");
		if (causesSection != null) {
			Map<String, ConfigurationSection> causes = new HashMap<String, ConfigurationSection>();

			Set<String> cfgEntries = causesSection.getKeys(false);
			for (String rawEntry : cfgEntries) {
				for (String splittedEntry : rawEntry.split(",")) {
					causes.put(splittedEntry.trim(), causesSection.getConfigurationSection(rawEntry));
				}
			}

			for (Entry<String, ConfigurationSection> causeEntry : causes.entrySet()) {
				String causeEntryName = causeEntry.getKey();
				DeathCause cause = DeathCause.parseCause(getCauseNameFromValue(causeEntryName), getCauseMetaFromValue(causeEntryName));

				if (cause == null) {
					errors.add("invalid cause: " + causeEntryName);
				} else {
					try {
						handlings.put(cause, new CauseData(plugin, new RawOptions(causeEntry.getValue())));
					} catch (IllegalPropertyException e) {
						errors.add("invalid property '" + e.propertyName + "' in " + causeEntryName + ": " + e.propertyValue);
					} catch (ListNotFoundException e) {
						errors.add("invalid list in " + causeEntryName + ": " + e.getListName());
					}
				}
			}
		}
	}

	private int parseLoggingLevel(String name) {
		if (name.equalsIgnoreCase("errors") || name.equalsIgnoreCase("error"))
			return Level.SEVERE.intValue();
		if (name.equalsIgnoreCase("warnings") || name.equalsIgnoreCase("warning"))
			return Level.WARNING.intValue();
		if (name.equalsIgnoreCase("standard") || name.equalsIgnoreCase("info") || name.equalsIgnoreCase("standart")) // for the common typo ;)
			return Level.INFO.intValue();
		if (name.equalsIgnoreCase("detailed") || name.equalsIgnoreCase("detail") || name.equalsIgnoreCase("debug"))
			return Level.FINEST.intValue();
		return -1;
	}

	public CauseSettings getSettings(DeathCause deathCause) {
		if (deathCause == null)
			return null;
		CauseData data = handlings.get(deathCause);
		if (data != null)
			return new CauseSettings(this, deathCause, data);
		if (deathCause.parent != null)
			return getSettings(deathCause.parent);
		return null;
	}

	public boolean isWorldAllowed(String worldName) {
		if (limitedWorlds == null || limitedWorlds.isEmpty())
			return true;
		return limitedWorlds.contains(worldName);
	}

	public static String getCauseNameFromValue(String val) {
		int sepIndex = val.indexOf(SEPARATOR);
		// also check if not the first char is the SEPARATOR
		if (sepIndex > 0) {
			return val.substring(0, sepIndex);
		} else {
			return val;
		}
	}

	public static String getCauseMetaFromValue(String val) {
		int sepIndex = val.indexOf(SEPARATOR);
		// also check if not the first char is the SEPARATOR
		if (sepIndex > 0) {
			return val.substring(sepIndex + 1);
		} else {
			return null;
		}
	}

	public class RawOptions {

		public static final String NODE_KEEP_INVENTORY = "keep-inventory";
		public static final String NODE_KEEP_EXPERIENCE = "keep-experience";
		public static final String NODE_COST = "cost";
		public static final String NODE_METHOD = "method";
		public static final String NODE_TIMEOUT = "timeout";
		public static final String NODE_TIMEOUT_ON_QUIT = "timeout-on-quit";
		public static final String NODE_LOSS_PERCENTAGE = "loss-percentage";
		public static final String NODE_LOSS_PERCENTAGE_EXP = "loss-percentage-experience";
		public static final String NODE_WHITELIST = "whitelist";
		public static final String NODE_BLACKLIST = "blacklist";

		public final boolean keepInventory;
		public final boolean keepExperience;
		public final String rawCost;
		public final String method;
		public final int timeout;
		public final int timeoutOnQuit;
		public final double loss;
		public final double lossExp;
		public final List<String> whitelist;
		public final List<String> blacklist;

		private final ConfigurationSection sec;

		public RawOptions(ConfigurationSection sec) {
			this.sec = sec;

			// now store config options, defaults should not be actually used as of 1.3
			keepInventory = sec.getBoolean(NODE_KEEP_INVENTORY, default_keepInventory);
			keepExperience = sec.getBoolean(NODE_KEEP_EXPERIENCE, default_keepExperience);
			rawCost = Utilities.getConfigString(sec, NODE_COST, default_cost_raw);
			method = sec.getString(NODE_METHOD); // don't provide default, because that's not raw
			timeout = Utilities.getConfigInt(sec, NODE_TIMEOUT, default_timeout);
			timeoutOnQuit = Utilities.getConfigInt(sec, NODE_TIMEOUT_ON_QUIT, default_timeoutOnQuit);
			loss = Utilities.getConfigDouble(sec, NODE_LOSS_PERCENTAGE, default_loss);
			lossExp = Utilities.getConfigDouble(sec, NODE_LOSS_PERCENTAGE_EXP, default_lossExp);
			whitelist = sec.getStringList(NODE_WHITELIST);
			blacklist = sec.getStringList(NODE_BLACKLIST);
		}

		public boolean isDefined(String node) {
			return sec.isSet(node);
		}

	}

}
