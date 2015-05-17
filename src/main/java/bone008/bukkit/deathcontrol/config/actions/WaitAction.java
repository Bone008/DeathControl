package bone008.bukkit.deathcontrol.config.actions;

import java.util.Arrays;
import java.util.List;

import org.bukkit.scheduler.BukkitRunnable;

import bone008.bukkit.deathcontrol.DeathControl;
import bone008.bukkit.deathcontrol.config.ActionAgent;
import bone008.bukkit.deathcontrol.config.ActionDescriptor;
import bone008.bukkit.deathcontrol.config.ActionResult;
import bone008.bukkit.deathcontrol.config.DeathContext;
import bone008.bukkit.deathcontrol.exceptions.DescriptorFormatException;
import bone008.bukkit.deathcontrol.util.ParserUtil;

public class WaitAction extends ActionDescriptor {

	private final boolean isCommand;
	private final int time;

	public WaitAction(List<String> args) throws DescriptorFormatException {
		if (args.isEmpty())
			throw new DescriptorFormatException("no wait time given");

		isCommand = args.get(0).equalsIgnoreCase("command");

		if (isCommand && args.size() < 2)
			time = -1;
		else
			time = ParserUtil.parseTime(args.get(isCommand ? 1 : 0), -1);

		if (!isCommand && time < 0)
			throw new DescriptorFormatException("can't wait indefinitely");
	}

	@Override
	public ActionAgent createAgent(DeathContext context) {
		if (isCommand)
			return new WaitAgentCmd(context);
		else
			return new WaitAgent(context);
	}

	@Override
	public List<String> toParameters() {
		if (isCommand)
			return Arrays.asList("command", time + "s");
		else
			return Arrays.asList(time + "s");
	}

	private class WaitAgentCmd extends ActionAgent {
		private BukkitRunnable task = null;

		public WaitAgentCmd(DeathContext context) {
			super(context, WaitAction.this);
		}

		@Override
		public void preprocess() {
		}

		@Override
		public ActionResult execute() {
			// timeout
			if (time > -1) {
				task = new BukkitRunnable() {
					@Override
					public void run() {
						context.cancel();
					}
				};
				task.runTaskLater(DeathControl.instance, time * 20L);
			}

			return ActionResult.BLOCK_COMMAND;
		}

		@Override
		public void cancel() {
			try {
				if (task != null)
					task.cancel();
			} catch (IllegalStateException ignored) {
			}
		}
	}

	private class WaitAgent extends ActionAgent {
		private BukkitRunnable task = null;

		public WaitAgent(DeathContext context) {
			super(context, WaitAction.this);
		}

		@Override
		public void preprocess() {
		}

		@Override
		public ActionResult execute() {
			// schedule timer to resume
			task = new BukkitRunnable() {
				@Override
				public void run() {
					context.continueExecution(ActionResult.BLOCK_TIMER);
				}
			};
			task.runTaskLater(DeathControl.instance, time * 20L);

			return ActionResult.BLOCK_TIMER;
		}

		@Override
		public void cancel() {
			try {
				if (task != null)
					task.cancel();
			} catch (IllegalStateException ignored) {
			}
		}
	}
}
