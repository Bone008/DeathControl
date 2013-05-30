package bone008.bukkit.deathcontrol;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.scheduler.BukkitRunnable;

import bone008.bukkit.deathcontrol.hooks.HooksManager;
import bone008.bukkit.deathcontrol.newconfig.HandlingDescriptor;
import bone008.bukkit.deathcontrol.util.Util;

public class BukkitDeathHandler implements Listener {

	@EventHandler(priority = EventPriority.HIGH)
	public void onRespawn(PlayerRespawnEvent event) {
		final Player player = event.getPlayer();

		// delay this for the next tick to make sure the player fully respawned to get the correct location
		// don't use getRespawnLocation(), because it might still be changed by another plugin - this way is safer
		// this also allows the plugin to correctly view and handle other plugins' actions on the player (e.g. Essentials giving back exp automatically)
		new BukkitRunnable() {
			@Override
			public void run() {
				DeathContextImpl context = DeathControl.instance.getActiveDeath(player);
				if (context != null)
					context.executeAgents();
			}
		}.runTask(DeathControl.instance);
	}

	@EventHandler(priority = EventPriority.HIGH)
	// Note: Essentials listens on LOW
	public void onDeath(final PlayerDeathEvent event) {
		Player ply = event.getEntity();

		if (DeathControl.instance.getActiveDeath(ply) != null)
			DeathControl.instance.getActiveDeath(ply).cancel();

		if (!DeathControl.instance.hasPermission(ply, DeathControl.PERMISSION_USE))
			return;

		EntityDamageEvent damageEvent = ply.getLastDamageCause();
		DeathCause deathCause = DeathCause.getDeathCause(damageEvent);

		DeathContextImpl context = new DeathContextImpl(event, deathCause);

		StringBuilder log1 = new StringBuilder(), log2 = new StringBuilder();

		log1.append(ply.getName()).append(" died (cause: ").append(deathCause.toHumanString()).append(")");

		if (HooksManager.shouldCancelDeathHandling(ply)) {
			DeathControl.instance.log(Level.FINE, log1.append("; Other plugin has control of player!").toString());
			return;
		}

		if (!DeathControl.instance.hasPermission(ply, DeathControl.PERMISSION_NOLIMITS) && !DeathControl.instance.config.isWorldAllowed(ply.getWorld().getName())) {
			DeathControl.instance.log(Level.FINE, log1.append("; Not in a valid world!").toString());
			return;
		}

		// TODO figure out when to cancel with keepInventory gamerule enabled
		if (BukkitRuleNotifHandler.isProblematicRuleEnabled(ply.getWorld())) {
			DeathControl.instance.log(Level.SEVERE, "The vanilla gamerule keepInventory is enabled in world " + ply.getWorld().getName() + "!");
			DeathControl.instance.log(Level.SEVERE, "You have to disable that rule to make the plugin work properly.");
			DeathControl.instance.log(Level.SEVERE, "Handling of " + ply.getName() + "'s death was cancelled.");
			return;
		}


		List<String> executed = new ArrayList<String>();

		for (HandlingDescriptor handling : DeathControl.instance.config.getHandlings()) {
			if (handling.areConditionsMet(context)) {
				handling.assignAgents(context);
				context.setDisconnectTimeout(handling.getTimeoutOnDisconnect());
				executed.add(handling.getName());
				break;
			}
		}

		if (!context.hasAgents()) {
			DeathControl.instance.log(Level.FINE, log1.append("; No actions to be executed!").toString());
			return;
		}

		DeathControl.instance.addActiveDeath(ply, context);

		// call all the preprocessors and let them do their magic
		context.preprocessAgents();

		// replace drops with the processed ones from the context
		event.getDrops().clear();
		for (StoredItemStack dropped : context.getItemDrops())
			event.getDrops().add(dropped.itemStack);



		log1.append("; Executed handlings: " + Util.joinCollection(", ", executed));


		log2.append("Handling death:\n");
		log2.append("| Player: ").append(ply.getName()).append('\n');
		log2.append("| Death cause: ").append(deathCause.toHumanString()).append('\n');
		log2.append("| Executed handlings: " + Util.joinCollection(", ", executed));

		// message the console
		if (DeathControl.instance.config.getLoggingLevel() <= Level.FINEST.intValue())
			DeathControl.instance.log(Level.FINE, log2.toString().trim());
		else if (DeathControl.instance.config.getLoggingLevel() <= Level.INFO.intValue())
			DeathControl.instance.log(Level.INFO, log1.toString().trim());

		//		// message the player
		//		MessageUtil.sendMessage(ply, Message.DEATH_KEPT, "%cause-reason%", Message.translatePath(deathCause.toMsgPath()));
		//		if (method == HandlingMethod.COMMAND) {
		//			MessageUtil.sendMessage(ply, Message.DEATH_COMMAND_INDICATOR);
		//			if (causeSettings.getTimeout() > 0)
		//				MessageUtil.sendMessage(ply, Message.DEATH_TIMEOUT_INDICATOR, "%timeout%", String.valueOf(causeSettings.getTimeout()));
		//		}
		//
		//		if (cost > 0) {
		//			Message theMsg = (method == HandlingMethod.COMMAND ? Message.DEATH_COST_INDICATOR_COMMAND : Message.DEATH_COST_INDICATOR_DIRECT);
		//			MessageUtil.sendMessage(ply, theMsg, "%raw-cost%", String.valueOf(cost), "%formatted-cost%", EconomyUtil.formatMoney(cost));
		//		}
	}

}
