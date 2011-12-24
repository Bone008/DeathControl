package bone008.bukkit.deathcontrol.config;

import bone008.bukkit.deathcontrol.DeathControl;
import org.bukkit.Material;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.logging.Level;

public class DeathLists {

	private DeathControl plugin;
	private Map<String, List<ListItem>> lists = new HashMap<String, List<ListItem>>();
	
	public DeathLists(DeathControl plugin, File f) {
		this.plugin = plugin;
		parseFile(f);
	}
	
	
	
	/**
	 * gets a list by its name
	 * @param name the name of the list
	 * @return a list of {@code ListItem}s
	 */
	public List<ListItem> getList(String name){
		return lists.get(name);
	}
	
	public Set<String> getListNames(){
		return lists.keySet();
	}
	
	
	
	/**
	 * parses lists.txt and writes the entries to "lists"-member
	 */
	private void parseFile(File f){
		try{
			BufferedReader reader = new BufferedReader(new FileReader(f));
			
			List<ListItem> currentList = null;
			
			String line;
			while((line = reader.readLine()) != null){
				line = line.trim();
				int commentIndex = line.indexOf('#');
				if(commentIndex > -1)
					line = line.substring(0, commentIndex).trim();
				if(line.isEmpty())
					continue;
				
				// new list start
				String listPrefix = "$list ";
				if(line.startsWith(listPrefix)){
					String listName = line.substring(listPrefix.length()).toLowerCase().trim();
					if(listName.isEmpty())
						plugin.log(Level.WARNING, "lists.txt: found empty list name!");
					else{
						currentList = new ArrayList<ListItem>();
						lists.put(listName, currentList);
					}
						
				}
				
				// item declaration before any list started
				else if(currentList == null){
					plugin.log(Level.WARNING, "lists.txt: found item declaration before any list was specified!");
				}
				
				// regular list item
				else{
					String[] chunks = line.split(":");
					if(chunks.length > 2 || chunks.length < 1){
						plugin.log(Level.WARNING, "lists.txt: invalid formatting of "+line);
						continue;
					}
					
					Material mat = null;
					try{
						mat = Material.getMaterial(Integer.parseInt(chunks[0]));
					} catch(NumberFormatException e){
						mat = Material.matchMaterial(chunks[0]);
					}
					if(mat == null)
						plugin.log(Level.WARNING, "lists.txt: could not find material "+chunks[0]);
					
					else{
						
						Byte data = null;
						try{
							if(chunks.length == 2)
								data = Byte.parseByte(chunks[1]);
							
							ListItem item = new ListItem(mat, data);
							currentList.add(item);
							
						} catch(NumberFormatException e){
							plugin.log(Level.WARNING, "lists.txt: data value '"+chunks[1]+"' must be a number!");
						}
						
					}
				}
				
			}
			
			plugin.log("loaded "+lists.size()+" list"+(lists.size()==1 ? "":"s")+"!");
			
		} catch(IOException e){
			plugin.log(Level.WARNING, "Could not load lists.txt!");
		}
	}
	
	
	public int getListsAmount(){
		return lists.size();
	}
	
}
