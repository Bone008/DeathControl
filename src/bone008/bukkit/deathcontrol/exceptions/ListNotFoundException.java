package bone008.bukkit.deathcontrol.exceptions;

/**
 * Exception that is thrown when an invalid list is specified in the config file.
 */
public class ListNotFoundException extends Exception {

	private static final long serialVersionUID = 1L;
	private String listName;
	
	protected ListNotFoundException(){}
	
	public ListNotFoundException(String listName){
		this.listName = listName;
	}
	
	public String getListName(){
		return listName;
	}
}
