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
	
	/**
	 * Returns the target player of the command
	 * @param sender the sender of the command
	 * @param maxArgs the number of arguments a non-self targeting version of the command takes
	 * @param args command arguments.
	 * @return sender if non-targeted and used by player, target if found or null on error.
	 */
	protected static Player getTarget(CommandSender sender, int maxArgs, String[] args) {
		if(args.length < maxArgs) {
			if(isAnonymous(sender)) {
				return null;
			}
			else {
				return (Player)sender;
			}
		}
		else {
			Player player = sender.getServer().matchPlayer(args[0]).get(0);
			if(player == null) {
				sender.sendMessage("No such player " + args[0] + ".");
				return null;
			}
			return player;
		}
	}
}
