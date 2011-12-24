package bone008.bukkit.deathcontrol.exceptions;

/**
 * Exception that is thrown when an invalid list is specified in the config file.
 */
public class IllegalPropertyException extends Exception {

	private static final long serialVersionUID = 1L;
	public String propertyName;
	public String propertyValue;
	
	protected IllegalPropertyException(){}
	
	public IllegalPropertyException(String name, String value){
		this.propertyName = name;
		this.propertyValue = value;
	}
	
}
