package se.shard.kaustic.mdfpvp.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import se.shard.kaustic.mdfpvp.CommandHandler;
import se.shard.kaustic.mdfpvp.MDFPvP;

/**
 * Class for handling the resetdeathchest command.
 * @author Johan Hägg
 */
public class ResetDeathChestCommand extends CommandHandler {
	public ResetDeathChestCommand(MDFPvP plugin) {
		super(plugin);
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		Player target;
		if(sender.hasPermission("mdfpvp.admin.removedeathchest")) {
			target = getTarget(sender, 1, args);
			if(target == null) {
				return false;
			}
		}
		else if(isAnonymous(sender)) {
			return false;
		}
		else {
			target = (Player)sender;
		}
		
		if(plugin.getDatabaseView().resetDeathChest(target)) {
			sender.sendMessage("Death chest reset.");
			return true;
		}
		else {
			sender.sendMessage("You do not have a death chest.");
			return false;
		}
	}
}
