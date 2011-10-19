package bone008.bukkit.deathcontrol.config;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class ListItem {
	
	private int id = 0;
	private byte data = 0;
	private boolean hasData = false;
	
	public ListItem(Material mat, Byte data){
		this.id = mat.getId();
		if(data != null){
			this.data = data;
			hasData = true;
		}
	}
	
	
	public boolean matches(ItemStack itemStack){
		if(itemStack.getTypeId() == this.id){
			if(this.hasData){
				return (this.data == itemStack.getDurability());
			} else{
				return true;
			}
		}
		return false;
	}
	
	@Override
	public String toString() {
		return "ListItem@"+id+":"+data+"/"+hasData;
	}
	
}
