package bone008.bukkit.deathcontrol.command;

import bone008.bukkit.deathcontrol.DeathControl;
import bone008.bukkit.deathcontrol.exceptions.CommandException;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

public class CommandManager implements CommandExecutor {

	DeathControl plugin;
	
	public Map<String, SubCommand> commandMap = new HashMap<String, SubCommand>();
	
	public CommandManager(DeathControl plugin){
		this.plugin = plugin;

		commandMap.put("help", new HelpCommand(this));
		commandMap.put("back", new BackCommand(this));
		commandMap.put("reload", new ReloadCommand(this));
		commandMap.put("info", new InfoCommand(this));
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (args.length > 0) {
			String[] subArgs = new String[args.length - 1];
			System.arraycopy(args, 1, subArgs, 0, subArgs.length);

			return handleCommand(sender, cmd, label, args[0].toLowerCase(), subArgs);
		}
		return false;
	}
	
	private boolean handleCommand(CommandSender sender, Command mainCmd, String mainLabel, String cmdName, String[] args) {
		SubCommand cmd = commandMap.get(cmdName);
		if (cmd == null) {
			return false;
		} else {
			try {
				cmd.checkPermission(sender, cmd.getPermission());
				cmd.execute(sender, mainCmd, mainLabel, args);
			} catch (CommandException e) {
				if (sender instanceof Player)
					sender.sendMessage(ChatColor.RED + e.getMessage());
				else
					sender.sendMessage(e.getMessage());
			}
			return true;
		}
	}

}
