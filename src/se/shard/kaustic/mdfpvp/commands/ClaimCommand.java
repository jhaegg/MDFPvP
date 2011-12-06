package se.shard.kaustic.mdfpvp.commands;

import org.bukkit.Chunk;
import org.bukkit.World.Environment;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import se.shard.kaustic.mdfpvp.CommandHandler;
import se.shard.kaustic.mdfpvp.MDFPvP;

/**
 * Command handling for claiming of chunks.
 * @author Johan Hägg
 */
public class ClaimCommand extends CommandHandler {

    private boolean newClaim;
    
	public ClaimCommand(MDFPvP plugin, boolean newClaim) {
		super(plugin);
		this.newClaim = newClaim;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if(isAnonymous(sender)) {
			return false;
		}
		
		Player player = (Player)sender;
		
		//Don't allow claims in Nether or The End.
		if(player.getLocation().getWorld().getEnvironment() == Environment.NETHER || player.getLocation().getWorld().getEnvironment() == Environment.THE_END) {
			sender.sendMessage("You cannot claim chunks in the Nether or The End.");
			return false;
		}
		
		//Check if the player is far enough from the spawn. 
		int distanceLeft = (int)(plugin.getConfig().getInt("MinDistanceFromSpawn", 350) - player.getWorld().getSpawnLocation().distance(player.getLocation()));

		if(distanceLeft > 0 && !player.hasPermission("mdfpvp.admin.claimanywhere")) {
			sender.sendMessage("To close to spawn, move another " + distanceLeft + " steps.");
			return false;
		}
		
		//Check if the chunk is already claimed.
		Chunk chunk = player.getLocation().getBlock().getChunk();
		if(plugin.getDatabaseView().isClaimed(chunk)) {
			sender.sendMessage("Chunk is already claimed.");
			return false;
		}
		
		//Check if the player has enough XP
		int remainingXP = player.getTotalExperience() - plugin.getDatabaseView().getXPRequired(player);
		if(newClaim)
		    remainingXP -= plugin.getDatabaseView().getSeparateClaimCount(player) * 100;
		
		if(remainingXP < 0) {
			sender.sendMessage("Not enough XP, you need another " + (-remainingXP) + "xp to claim a chunk.");
			return false;
		}
		
		//Check if the player has an adjacent claim.
		if(!(plugin.getDatabaseView().getNumberOfClaims(player) == 0 || plugin.getDatabaseView().hasAdjacentClaim(player))) {
			sender.sendMessage("No adjacent claim.");
			return false;
		}
		else if(newClaim) {
		    sender.sendMessage("Chunk has adjacent claims or you have no claim, no need to create a new.");
		    return false;
		}
		else {
		    int recentlyConnected = plugin.getDatabaseView().countUnconnectedNeighbors(chunk) - 1;
		    plugin.getDatabaseView().setSeparateClaimCount(player, plugin.getDatabaseView().getSeparateClaimCount(player) - recentlyConnected);
		}
		
		//Claim chunk and update experience.
		player.setTotalExperience(remainingXP);
		if(plugin.getDatabaseView().getNumberOfClaims(player) == 0 || newClaim) {
		    plugin.getDatabaseView().setSeparateClaimCount(player, plugin.getDatabaseView().getSeparateClaimCount(player) + 1);
		}
		
		plugin.getDatabaseView().claimChunk(player);
		sender.sendMessage("Chunk claimed.");
		return true;
	}
}
