package bone008.bukkit.deathcontrol;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.scheduler.BukkitRunnable;

import bone008.bukkit.deathcontrol.config.HandlingDescriptor;
import bone008.bukkit.deathcontrol.hooks.HooksManager;
import bone008.bukkit.deathcontrol.util.Message;
import bone008.bukkit.deathcontrol.util.MessageUtil;
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
				if (context != null) {
					// check for cross world respawn
					if (!DeathControl.instance.config.allowsCrossworld() && !DeathControl.instance.hasPermission(player, DeathControl.PERMISSION_CROSSWORLD) && !player.getWorld().equals(context.getDeathLocation().getWorld())) {
						MessageUtil.sendMessage(player, Message.NOTIF_NOCROSSWORLD);
						context.cancel();
					}
					else {
						context.executeAgents();
					}
				}
			}
		}.runTask(DeathControl.instance);
	}

	@EventHandler(priority = EventPriority.HIGH)
	// Note: Essentials listens on LOW
	public void onDeath(final PlayerDeathEvent event) {
		Player ply = event.getEntity();

		if (DeathControl.instance.getActiveDeath(ply) != null)
			DeathControl.instance.getActiveDeath(ply).cancelManually();

		if (!DeathControl.instance.hasPermission(ply, DeathControl.PERMISSION_USE))
			return;

		// create DeathContext as a key part of death handling
		DeathContextImpl context = new DeathContextImpl(event);


		// build list of death causes for logging purposes
		Set<String> deathCauses = new HashSet<String>();
		for (DeathCause dc : DeathCause.values()) {
			if (dc.appliesTo(ply.getLastDamageCause()))
				deathCauses.add(dc.toHumanString());
		}

		// short and detailed logs
		StringBuilder log1 = new StringBuilder(), log2 = new StringBuilder();

		log1.append(ply.getName()).append(" died (").append(Util.pluralNum(deathCauses.size(), "cause")).append(": ").append(Util.joinCollection(", ", deathCauses)).append(")");

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
			DeathControl.instance.log(Level.SEVERE, "The vanilla gamerule keepInventory is enabled in world \"" + ply.getWorld().getName() + "\"!");
			DeathControl.instance.log(Level.SEVERE, "You have to disable that rule to make the plugin work properly.");
			DeathControl.instance.log(Level.SEVERE, "Handling of " + ply.getName() + "'s death was cancelled.");
			return;
		}


		List<String> executed = new ArrayList<String>();

		for (HandlingDescriptor handling : DeathControl.instance.config.getHandlings()) {
			if (handling.areConditionsMet(context)) {
				handling.assignAgents(context);
				context.setDisconnectTimeout(handling.getTimeoutOnDisconnect());
				context.setCancelMessage(handling.getCancelMessage());
				executed.add(handling.getName());

				// if configured, don't allow any other handlings to execute
				if (handling.isLastHandling())
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


		// short log
		log1.append("; Executed handlings: " + Util.joinCollection(", ", executed));

		// detailed log
		log2.append("Handled death:\n");
		log2.append("| Player: ").append(ply.getName()).append('\n');
		for (String cause : deathCauses)
			log2.append("| Death cause: ").append(cause).append('\n');
		log2.append("| Executed handlings: " + Util.joinCollection(", ", executed)).append('\n');
		log2.append("| Disconnect timeout: " + context.getDisconnectTimeout());

		// message the console
		if (DeathControl.instance.config.getLoggingLevel() <= Level.FINE.intValue())
			DeathControl.instance.log(Level.FINE, log2.toString().trim());
		else if (DeathControl.instance.config.getLoggingLevel() <= Level.INFO.intValue())
			DeathControl.instance.log(Level.INFO, log1.toString().trim());
	}
}
