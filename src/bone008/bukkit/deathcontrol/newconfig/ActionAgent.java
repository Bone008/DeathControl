package bone008.bukkit.deathcontrol.newconfig;


public abstract class ActionAgent {

	protected final DeathContext context;
	private final ActionDescriptor descriptor;

	public ActionAgent(DeathContext context, ActionDescriptor descriptor) {
		this.context = context;
		this.descriptor = descriptor;
	}

	public final ActionDescriptor getDescriptor() {
		return descriptor;
	}

	public abstract void preprocess();

	public abstract ActionResult execute();

	public abstract void cancel();

}
