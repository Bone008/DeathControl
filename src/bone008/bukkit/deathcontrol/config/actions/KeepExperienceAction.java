package bone008.bukkit.deathcontrol.config.actions;

import java.util.Arrays;
import java.util.List;

import bone008.bukkit.deathcontrol.config.ActionAgent;
import bone008.bukkit.deathcontrol.config.ActionDescriptor;
import bone008.bukkit.deathcontrol.config.DeathContext;
import bone008.bukkit.deathcontrol.exceptions.DescriptorFormatException;
import bone008.bukkit.deathcontrol.util.ParserUtil;

public class KeepExperienceAction extends ActionDescriptor {

	double keepPct;
	boolean dropLeftovers;

	public KeepExperienceAction(List<String> args) throws DescriptorFormatException {
		keepPct = 1.0;

		if (args.size() > 0) {
			keepPct = ParserUtil.parsePercentage(args.get(0));
			if (keepPct == -1 || keepPct > 1.0)
				throw new DescriptorFormatException("invalid percentage: " + args.get(0));
		}

		dropLeftovers = (args.size() > 1 && args.get(1).equalsIgnoreCase("drop-leftovers"));
	}

	@Override
	public ActionAgent createAgent(DeathContext context) {
		return new KeepExperienceActionAgent(context, this);
	}

	@Override
	public List<String> toParameters() {
		return Arrays.asList(String.format("%.0f%%", keepPct * 100), (dropLeftovers ? "drop-leftovers" : ""));
	}
}
