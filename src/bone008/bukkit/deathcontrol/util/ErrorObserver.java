package bone008.bukkit.deathcontrol.util;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import bone008.bukkit.deathcontrol.DeathControl;

public class ErrorObserver {

	private String prefix = null;
	private List<String> warnings = new ArrayList<String>();

	public void setPrefix(String prefix) {
		this.prefix = prefix;
	}

	public String getPrefix() {
		return prefix;
	}

	public void addWarning(String msg, Object... args) {
		warnings.add(parseMsg(msg, args));
	}

	public void log() {
		log(null);
	}

	public void log(String introduction) {
		if (warnings.isEmpty())
			return;

		if (introduction != null)
			DeathControl.instance.log(Level.WARNING, introduction);

		for (String msg : warnings)
			DeathControl.instance.log(Level.WARNING, msg);
	}

	public void logTo(ErrorObserver target) {
		logTo(target, null);
	}

	public void logTo(ErrorObserver target, String introduction) {
		if (warnings.isEmpty())
			return;

		if (introduction != null)
			target.addWarning(introduction);

		for (String msg : warnings)
			target.addWarning(msg);
	}

	private String parseMsg(String msg, Object[] args) {
		if (args.length > 0)
			msg = String.format(msg, args);
		if (prefix != null)
			msg = prefix + msg;
		return msg;
	}

}
