package bone008.bukkit.deathcontrol.newconfig.conditions;

import java.util.List;

import org.bukkit.entity.Player;

import bone008.bukkit.deathcontrol.exceptions.DescriptorFormatException;
import bone008.bukkit.deathcontrol.newconfig.ConditionDescriptor;
import bone008.bukkit.deathcontrol.newconfig.DeathContext;
import bone008.bukkit.deathcontrol.util.Util;

public class PermissionCondition extends ConditionDescriptor {

	private boolean isKiller;
	private String permNode;

	public PermissionCondition(List<String> args) throws DescriptorFormatException {
		if (args.size() < 2)
			throw new DescriptorFormatException("not enough arguments");
		if (args.size() > 2)
			throw new DescriptorFormatException("too many arguments");

		if (args.get(0).equalsIgnoreCase("victim"))
			isKiller = false;
		else if (args.get(0).equalsIgnoreCase("killer"))
			isKiller = true;
		else
			throw new DescriptorFormatException("invalid subject: only \"victim\" or \"killer\" is allowed!");

		permNode = args.get(1);
	}

	@Override
	public boolean matches(DeathContext context) {
		if (isKiller) {
			// don't use getKiller() here to stay consistent with the rest of the plugin
			Player killer = Util.getPlayerAttackerFromEvent(context.getVictim().getLastDamageCause());

			return killer != null && killer.hasPermission(permNode);
		}
		else {
			return context.getVictim().hasPermission(permNode);
		}
	}

}
