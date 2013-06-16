package bone008.bukkit.deathcontrol.config;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;

import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.ConfigurationSection;

import bone008.bukkit.deathcontrol.DeathControl;
import bone008.bukkit.deathcontrol.util.ErrorObserver;
import bone008.bukkit.deathcontrol.util.ParserUtil;
import bone008.bukkit.deathcontrol.util.Util;

public class NewConfiguration {

	private static final boolean default_bukkitPerms = true;
	private static final String default_loggingLevel = "standard";
	private static final boolean default_allowCrossworld = true;
	private static final List<String> default_limitedWorlds = new ArrayList<String>();

	private boolean bukkitPerms;
	private int loggingLevel;
	private boolean allowCrossworld;
	private Set<String> limitedWorlds;

	private Set<HandlingDescriptor> handlings = new TreeSet<HandlingDescriptor>(); // automatically ordered by priority

	public NewConfiguration(Configuration config) {
		ErrorObserver errors = new ErrorObserver();
		errors.setPrefix("-> ");

		bukkitPerms = config.getBoolean("use-bukkit-permissions", default_bukkitPerms);

		if (config.get("logging-level") instanceof Number)
			config.set("logging-level", default_loggingLevel);
		String rawLoggingLevel = config.getString("logging-level", default_loggingLevel);
		loggingLevel = ParserUtil.parseLoggingLevel(rawLoggingLevel);
		if (loggingLevel == -1) {
			errors.addWarning("invalid logging-level: " + rawLoggingLevel);
			loggingLevel = ParserUtil.parseLoggingLevel(default_loggingLevel);
		}

		allowCrossworld = config.getBoolean("multi-world.allow-cross-world", default_allowCrossworld);

		List<String> rawLimitedWorlds = config.getStringList("multi-world.limited-worlds");
		if (rawLimitedWorlds == null || rawLimitedWorlds.isEmpty())
			rawLimitedWorlds = default_limitedWorlds;
		limitedWorlds = new HashSet<String>(rawLimitedWorlds);

		// parse causes
		ConfigurationSection handlingsSec = config.getConfigurationSection("handlings");
		if (handlingsSec != null) {
			Set<String> handlingNames = handlingsSec.getKeys(false);
			for (String name : handlingNames) {
				ErrorObserver handlingLog = new ErrorObserver();
				handlingLog.setPrefix("  ");

				handlings.add(new HandlingDescriptor(name, handlingsSec.getConfigurationSection(name), handlingLog));

				handlingLog.logTo(errors, "Handling " + name + ":");
			}
		}

		errors.log("Errors while parsing configuration:");
		DeathControl.instance.log(Level.CONFIG, "loaded " + Util.pluralNum(handlings.size(), "valid handling") + "!", true);
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
		if (limitedWorlds == null || limitedWorlds.isEmpty())
			return true;
		return limitedWorlds.contains(worldName);
	}

	public Set<HandlingDescriptor> getHandlings() {
		return handlings;
	}

}
