package bone008.bukkit.deathcontrol;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import bone008.bukkit.deathcontrol.config.ActionAgent;
import bone008.bukkit.deathcontrol.config.ActionResult;

public class AgentSet implements Iterable<ActionAgent> {

	private boolean sealed = false;
	private List<ActionAgent> list = new ArrayList<ActionAgent>();

	public AgentSet() {
	}

	public void add(ActionAgent agent) {
		if (sealed)
			throw new IllegalStateException("can't add agent to sealed set");
		list.add(agent);
	}

	public boolean isEmpty() {
		return list.isEmpty();
	}

	@Override
	public Iterator<ActionAgent> iterator() {
		return list.iterator();
	}

	public AgentIterator iteratorExecution() {
		return new AgentIterator();
	}

	public class AgentIterator implements Iterator<ActionAgent> {

		private ActionResult blockedReason = null;
		private int nextIndex = 0;

		public boolean canContinue() {
			return blockedReason == null && hasNext();
		}

		@Override
		public boolean hasNext() {
			return nextIndex < list.size();
		}

		@Override
		public ActionAgent next() {
			return list.get(nextIndex++);
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException();
		}

		public void blockExecution(ActionResult reason) {
			blockedReason = reason;
		}

		public boolean unblockExecution(ActionResult reason) {
			if (blockedReason == reason) {
				blockedReason = null;
				return true;
			}

			return false;
		}
	}

}
