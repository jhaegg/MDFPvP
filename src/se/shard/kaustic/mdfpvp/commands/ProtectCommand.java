package se.shard.kaustic.mdfpvp.commands;

import org.bukkit.Chunk;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import se.shard.kaustic.mdfpvp.CommandHandler;
import se.shard.kaustic.mdfpvp.MDFPvP;

/**
 * Class for handling the protect command
 * @author Johan Hägg
 */

public class ProtectCommand extends CommandHandler {
	public ProtectCommand(MDFPvP plugin) {
		super(plugin);
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if(isAnonymous(sender)) {
			return false;
		}
		
		Player player = (Player)sender;
		if(player.getTotalExperience() < 100) {
			sender.sendMessage("You need 100xp to protect a claim.");
			return false;
		}
		Chunk chunk = player.getLocation().getBlock().getChunk();
		
		if(!plugin.getDatabaseView().isOwner(player, chunk)) {
			sender.sendMessage("You can only protect your own claims.");
			return false;
		}
		// Put after more cosly call since we do not want players to abuse this to find
		// unprotected chunks.
		if(plugin.getDatabaseView().isProtected(chunk)) {
			sender.sendMessage("Chunk is already protected.");
			return false;
		}
		plugin.getDatabaseView().protectClaim(chunk);
		player.setTotalExperience(player.getTotalExperience() - 100);
		sender.sendMessage("Chunk protected.");
		return true;
	}
}
