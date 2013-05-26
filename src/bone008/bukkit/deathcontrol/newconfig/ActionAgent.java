package bone008.bukkit.deathcontrol.newconfig;


public abstract class ActionAgent {

	protected final DeathContext context;

	public ActionAgent(DeathContext context) {
		this.context = context;
	}

	public abstract PreprocessResult preprocess();

	public abstract ActionResult execute();

	public abstract void cancel();

}
