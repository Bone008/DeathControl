package bone008.bukkit.deathcontrol.config;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;

import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.util.StringUtil;

import bone008.bukkit.deathcontrol.DeathControl;
import bone008.bukkit.deathcontrol.util.ErrorObserver;
import bone008.bukkit.deathcontrol.util.ParserUtil;
import bone008.bukkit.deathcontrol.util.Util;

public class NewConfiguration {

	private static final boolean default_bukkitPerms = true;
	private static final String default_loggingLevel = "standard";
	private static final boolean default_allowCrossworld = true;
	private static final List<String> default_blacklistedWorlds = new ArrayList<String>();

	private boolean bukkitPerms;
	private int loggingLevel;
	private boolean allowCrossworld;
	private Set<String> blacklistedWorlds;

	private Set<HandlingDescriptor> handlings = new TreeSet<HandlingDescriptor>(); // automatically ordered by priority

	public NewConfiguration(Configuration config) {
		ErrorObserver errors = new ErrorObserver();
		errors.setPrefix("-> ");

		bukkitPerms = !config.getBoolean("disable-permissions", default_bukkitPerms);

		if (config.get("logging-level") instanceof Number)
			config.set("logging-level", default_loggingLevel);
		String rawLoggingLevel = config.getString("logging-level", default_loggingLevel);
		loggingLevel = ParserUtil.parseLoggingLevel(rawLoggingLevel);
		if (loggingLevel == -1) {
			errors.addWarning("invalid logging-level: " + rawLoggingLevel);
			loggingLevel = ParserUtil.parseLoggingLevel(default_loggingLevel);
		}

		allowCrossworld = config.getBoolean("multi-world.allow-cross-world", default_allowCrossworld);

		// because of a mismatch between documentation and code, this setting carries both the name "disabled-worlds" and "blacklisted-worlds";
		// to maximize compatibility, both names are now supported
		List<String> rawBlacklistedWorlds = config.getStringList("multi-world.blacklisted-worlds");
		List<String> rawBlacklistedWorlds2 = config.getStringList("multi-world.disabled-worlds");
		if (rawBlacklistedWorlds == null || rawBlacklistedWorlds.isEmpty())
			rawBlacklistedWorlds = default_blacklistedWorlds;
		if (rawBlacklistedWorlds2 == null || rawBlacklistedWorlds2.isEmpty())
			rawBlacklistedWorlds2 = default_blacklistedWorlds;

		blacklistedWorlds = new HashSet<String>();
		blacklistedWorlds.addAll(rawBlacklistedWorlds);
		blacklistedWorlds.addAll(rawBlacklistedWorlds2);


		// parse causes
		ConfigurationSection handlingsSec = config.getConfigurationSection("handlings");
		if (handlingsSec != null) {
			Set<String> handlingNames = handlingsSec.getKeys(false);
			for (String name : handlingNames) {
				ErrorObserver handlingLog = new ErrorObserver();
				handlingLog.setPrefix("  ");

				ConfigurationSection hndSec = handlingsSec.getConfigurationSection(name);
				if (hndSec == null)
					handlingLog.addWarning("Invalid format!");
				else
					handlings.add(new HandlingDescriptor(name, hndSec, handlingLog));

				handlingLog.logTo(errors, "Handling " + name + ":");
			}
		}

		errors.log("Errors while parsing configuration:");
		DeathControl.instance.log(Level.CONFIG, "loaded " + Util.pluralNum(handlings.size(), "handling") + "!", true);
	}

	public boolean usesBukkitPerms() {
		return bukkitPerms;
	}

	public int getLoggingLevel() {
		return loggingLevel;
	}

	public boolean allowsCrossworld() {
		return allowCrossworld;
	}

	public boolean isWorldAllowed(String worldName) {
		if (blacklistedWorlds == null || blacklistedWorlds.isEmpty())
			return true;
		return !blacklistedWorlds.contains(worldName);
	}

	public Collection<String> getBlacklistedWorlds() {
		return blacklistedWorlds;
	}

	public HandlingDescriptor getHandling(String name) {
		for (HandlingDescriptor h : handlings) {
			if (h.getName().equalsIgnoreCase(name))
				return h;
		}

		return null;
	}

	public Set<HandlingDescriptor> getHandlings() {
		return handlings;
	}

	public <T extends Collection<String>> T getPartialHandlingNames(String search, T result) {
		for (HandlingDescriptor h : handlings) {
			if (StringUtil.startsWithIgnoreCase(h.getName(), search))
				result.add(h.getName());
		}

		return result;
	}
}
