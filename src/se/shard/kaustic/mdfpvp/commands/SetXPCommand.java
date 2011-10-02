package se.shard.kaustic.mdfpvp.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import se.shard.kaustic.mdfpvp.CommandHandler;
import se.shard.kaustic.mdfpvp.MDFPvP;

/**
 * Class for handling command which sets the total experience of a player.
 * @author Johan Hägg
 */
public class SetXPCommand extends CommandHandler {

	public SetXPCommand(MDFPvP plugin) {
		super(plugin);
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		Player target = getTarget(sender, 2, args);
		int xp;
		
		if(target == null) {
			return false;
		}
		try {
			xp = Integer.parseInt(args[args.length - 1]);
		}
		catch (NumberFormatException ex) {
			sender.sendMessage("Invalid XP argument.");
			return false;
		}
		if(xp < 0) {
			sender.sendMessage("XP argument must be non-negative.");
			return false;
		}
		target.setTotalExperience(xp);
		plugin.getServer().broadcastMessage("Experience of " + target.getName() + " has been changed by administrators." );
		return true;
	}
}
