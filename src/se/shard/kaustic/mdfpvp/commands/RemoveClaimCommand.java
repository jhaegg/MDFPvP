package se.shard.kaustic.mdfpvp.commands;

import org.bukkit.Chunk;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import se.shard.kaustic.mdfpvp.CommandHandler;
import se.shard.kaustic.mdfpvp.MDFPvP;

/**
 * Class for handling command which removes claims.
 * @author Johan Hägg
 */

public class RemoveClaimCommand extends CommandHandler {

	public RemoveClaimCommand(MDFPvP plugin) {
		super(plugin);
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if(isAnonymous(sender)) {
			return false;
		}		
		Player player = (Player)sender; 
		Chunk chunk = player.getLocation().getBlock().getChunk();
		
		if(!(plugin.getDatabaseView().isOwner(player, chunk) || player.hasPermission("mdfpvp.admin.removeclaim"))) {
			sender.sendMessage("You are not allowed to remove this claim.");
			return false;
		}		
		if(!plugin.getDatabaseView().removeClaim(chunk)) {
			sender.sendMessage("Chunk not claimed.");
		}
		
		sender.sendMessage("Claim removed.");
		return true;
	}
}
