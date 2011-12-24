package bone008.bukkit.deathcontrol.exceptions;

public class ResourceNotFoundError extends Error {
	private static final long serialVersionUID = 1L;
	
	public ResourceNotFoundError(String res){
		super("Resource of "+res+" not found!");
	}
}
