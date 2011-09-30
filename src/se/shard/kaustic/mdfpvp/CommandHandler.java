package se.shard.kaustic.mdfpvp;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Extension abstract for CommandExecutor interface.
 * @author Johan Hägg
 */
public abstract class CommandHandler implements CommandExecutor {
	protected final MDFPvP plugin;
	

	public abstract boolean onCommand(CommandSender sender, Command command, String label, String[] args);
	
	/**
	 * CommandHandler default constructor
	 * @param plugin parent plugin.
	 */
	public CommandHandler(MDFPvP plugin) {
		this.plugin = plugin;
	}
	
	/**
	 * Checks if the sender is anonymous and notifies user if so.
	 * @param sender Sender to be checked.
	 * @return True if anonymous, otherwise false.
	 */	
	protected static boolean isAnonymous(CommandSender sender) {
		if(sender instanceof Player) {
			return false;
		}
		else {
			sender.sendMessage("Command does not accept anonymous usage.");
			return true;
		}
	}	
}
