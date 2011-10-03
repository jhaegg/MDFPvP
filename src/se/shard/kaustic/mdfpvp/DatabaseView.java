package se.shard.kaustic.mdfpvp;

import java.util.logging.Level;

import org.bukkit.Chunk;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;

import se.shard.kaustic.mdfpvp.persisting.Claim;
import se.shard.kaustic.mdfpvp.persisting.DeathChest;
import se.shard.kaustic.mdfpvp.persisting.PlayerData;

import com.avaje.ebean.EbeanServer;
import com.avaje.ebean.QueryIterator;

/**
 * Class providing a view of the database for the rest of the plugin.
 * @author Johan Hägg
 */
public class DatabaseView {
	private MDFPvP plugin;
	private EbeanServer database;
	
	public DatabaseView(MDFPvP plugin, EbeanServer database) {
		this.plugin = plugin;
		this.database = database;
	}

	/**
	 * Claims the chunk the player is standing on.
	 * @param player the player which should claim the chunk.
	 */
	public void claimChunk(Player player) {
		PlayerData playerData = getPlayerData(player);
		Claim claim = new Claim(player.getLocation().getBlock().getChunk());
		
		playerData.addClaim(claim);
		database.insert(claim);
	}
	
	/**
	 * Gets the number of claims owned by the player.
	 * @param player the player for which the number of claims should be counted.
	 * @return the number of claims owned by the player.
	 */
	public int getNumberOfClaims(Player player) {
		if(getPlayerData(player).getClaims() == null) {
			return 0;
		}
		
		return getPlayerData(player).getClaims().size();
	}
	
	/**
	 * Gets the number of experience points needed for the player to claim another chunk.
	 * @param player the player for which the experience requirements should be calculated.
	 * @return the number of experience points needed for the player to claim another chunk.
	 */
	public int getXPRequired(Player player) {
		return (int)(50 + 20 * Math.pow(getNumberOfClaims(player), 1.337));		
	}
	
	/**
	 * Checks if the player has a claim adjacent to the chunk they are standing on.
	 * @param player the player that should be evaluated.
	 * @return True if there is an adjacent claim owned by the player, otherwise false.
	 */
	public boolean hasAdjacentClaim(Player player) {
		PlayerData playerData = getPlayerData(player);
		int chunkX = player.getLocation().getBlock().getChunk().getX(); 
		int chunkZ = player.getLocation().getBlock().getChunk().getZ();
		
		// Check if there is a non-diagonal adjacent claim in the same world
		for(Claim claim : playerData.getClaims()) {			
			if(claim.getWorldUUID().equals(player.getLocation().getWorld().getUID()) 
				&& (claim.getChunkX() == chunkX && (claim.getChunkZ() - 1 == chunkZ || claim.getChunkZ() + 1 == chunkZ)) 
				|| (claim.getChunkZ() == chunkZ && (claim.getChunkX() - 1 == chunkX || claim.getChunkX() + 1 == chunkX))) {
				return true;
			}
		}		
		return false;		
	}
	
	/**
	 * Checks if the player is the owner of the chunk.
	 * @param player the player to be checked.
	 * @param chunk the chunk to be checked.
	 * @return true if player owns chunk, otherwise false.
	 */
	public boolean isOwner(Player player, Chunk chunk) {
		Claim claim = getClaim(chunk);
		
		if(claim == null) {
			return false;
		}
		
		return claim.getOwner().getPlayerUUID().equals(player.getUniqueId());
	}
	
	/**
	 * Removes any claim on the chunk.
	 * @param chunk the chunk to be unclaimed.
	 * @return true if removed, otherwise false.
	 */
	public boolean removeClaim(Chunk chunk) {
		Claim claim = getClaim(chunk);
		
		if(claim == null) {
			return false;
		}		
				
		database.delete(claim);
		
		return true;
	}
	
	/**
	 * Add or remove player to list of tenants.
	 * @param owner the player who wants to allow tenants.
	 * @param tenant the player which status should be changed.
	 * @param allow true if add to list of allowed, false if remove. 
	 * @return true if successfully added or removed, otherwise false.
	 */
	public boolean changeTenantStatus(Player owner, Player tenant, boolean allow) {
		PlayerData ownerData = getPlayerData(owner);
		PlayerData tenantData = getPlayerData(tenant);
		
		if(allow) {
			if(ownerData.addTenant(tenantData)) {
				database.save(ownerData);
				return true;
			}			
		}
		else {
			if(ownerData.removeTenant(tenantData)) {
				database.save(ownerData);
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Checks if the player can change the chunk.
	 * @param player the player to be checked.
	 * @param chunk the chunk to be checked.
	 * @return true if player is owner or if the chunk is not claimed.  
	 */
	public boolean canChange(Player player, Chunk chunk) {
		Claim claim = getClaim(chunk);
				
		if(claim == null) {
			return true;
		}		
		if(claim.getOwner().getPlayerUUID().equals(player.getUniqueId())) {
			return true;
		}		
		PlayerData playerData = getPlayerData(player);
		
		return claim.getOwner().getTenants().contains(playerData);
	}
	
	/**
	 * Evaluates if the chunk is claimed by another player or not.
	 * @param chunk the chunk to be evaluated.
	 * @return true if the chunk is claimed, otherwise false.
	 */
	public boolean isClaimed(Chunk chunk) {
		return getClaim(chunk) != null;
	}

	/**
	 * Returns the owner of the chunk.
	 * @param chunk the chunk for which the owner should be found
	 * @return name of owner if found, otherwise null.
	 */
	public String getOwner(Chunk chunk) {
		Claim claim = getClaim(chunk);
		if(claim == null) {
			return null;
		}	
		return claim.getOwner().getPlayerName();
	}
	
	/**
	 * Gets the claim associated with the chunk.
	 * @param chunk the possibly claimed chunk to be searched for.
	 * @return chunk claim if found, otherwise null.
	 */
	private Claim getClaim(Chunk chunk) {
		return database.find(Claim.class).where().eq("worldUUID", chunk.getWorld().getUID()).eq("chunkX", chunk.getX()).eq("chunkZ", chunk.getZ()).findUnique();
	}
	
	/**
	 * Gets or creates the player data associated with the player. 
	 * @param player the player which the player data should be associated with.
	 * @return player data associated with the player.
	 */
	private PlayerData getPlayerData(Player player) {
		PlayerData playerData = database.find(PlayerData.class).where().eq("playerUUID", player.getUniqueId()).findUnique();
		
		// Create if missing
		if(playerData == null) {
			playerData = new PlayerData(player);
			database.insert(playerData);
		}	
		return playerData;
	}
	
	/**
	 * Check if the block is a death chest.
	 * @param chest the block which should be evaluated.
	 * @return true if the block is a death chest, otherwise false.
	 */
	public boolean isDeathChest(Block chest) {
		return getDeathChestData(chest) != null;
	}
	
	/**
	 * Checks if the player can open the chest.
	 * @param player the player to be evaluated.
	 * @param chest the chest to be evaluated.
	 * @return true if the chest is either not a death chest or owned by the player, otherwise false.
	 */
	public boolean canOpenChest(Player player, Block chest) {
		DeathChest deathChest = getDeathChestData(chest);
		
		if(deathChest == null) {
			return true;
		}
		
		PlayerData playerData = getPlayerData(player);
		
		// Long way around due to problems with Avaje.  
		return playerData.getDeathChest().equals(deathChest);
	}
	
	/**
	 * Gets the death chest data of a player.
	 * @param player the player the death chest should belong to. 
	 * @return null if not found, otherwise the death chest data.
	 */
	private DeathChest getDeathChestData(Player player) {
		return getPlayerData(player).getDeathChest();
	}
	
	/**
	 * Gets the death chest data of a block. 
	 * @param chest the block to be searched for.
	 * @return null if not found, otherwise death chest data.
	 */
	private DeathChest getDeathChestData(Block chest) {
		return database.find(DeathChest.class).where().eq("worldUUID", chest.getWorld().getUID())
		.eq("posX", chest.getX()).eq("posY", chest.getY()).eq("posZ", chest.getZ()).findUnique();
	}
	
	/**
	 * Gets the chest state of the death chest owned by the player.
	 * @param player the player which the death chest should belong to.
	 * @return null if not found, otherwise the chest.
	 */
	public Chest getDeathChest(Player player) {
		DeathChest deathChest = getDeathChestData(player);
		
		if(deathChest == null) {
			return null;
		}
		Block block = plugin.getServer().getWorld(deathChest.getWorldUUID()).getBlockAt(deathChest.getPosX(), deathChest.getPosY(), deathChest.getPosZ());
		
		if (block.getState() instanceof Chest) {
			return (Chest)block.getState();
		}
		// Death chest corrupt, log event.
		plugin.getServer().getLogger().log(Level.SEVERE, player.getName() + "s death chest has become corrupted.");
		
		return null;
	}
	
	/**
	 * Checks if there is a death chest adjacent to the block.
	 * @param block the block to be evaluated.
	 * @return true if there is a death chest adjacent to the block, otherwise false. 
	 */
	public boolean hasAdjacentDeathChest(Block block) {
		//Find chests which are on the same world and level as the block.
		QueryIterator<DeathChest> deathChests = database.find(DeathChest.class).where().eq("worldUUID", block.getWorld().getUID()).eq("posY", block.getY()).findIterate();
		
		while(deathChests.hasNext()) {
			DeathChest deathChest = deathChests.next();
			// Find adjacent death chest.
			if((deathChest.getPosX() == block.getX() && (block.getZ() - 1 == deathChest.getPosZ() || block.getZ() + 1 == deathChest.getPosZ()))
					|| deathChest.getPosZ() == block.getZ() && (block.getX() - 1 == deathChest.getPosX() || block.getX() + 1 == deathChest.getPosX())) {
						return true;
					}
		}
		return false;
	}
	
	/**
	 * Sets the death chest of the player.
	 * @param player the player who should be the owner of the death chest.
	 * @param chest the chest block which should be the players death chest.
	 */
	public void setDeathChest(Player player, Block chest) {
		PlayerData playerData = getPlayerData(player);
		DeathChest deathChest = new DeathChest(chest);
		playerData.setDeathChest(deathChest);
		database.insert(deathChest);
		database.update(playerData);
	}
	
	/**
	 * Resets the players death chest.
	 * @param player the owner of the death chest to be reset.
	 * @return true if chest is found and reset, false if not found.
	 */
	public boolean resetDeathChest(Player player) {
		PlayerData playerData = getPlayerData(player);
		DeathChest deathChest = playerData.getDeathChest();
		
		if(deathChest == null) {
			return false;
		}
		
		playerData.setDeathChest(null);
		database.delete(deathChest);
		database.update(playerData);
		
		return true;
	}
	
	/**
	 * Gets the player data associated with the player name
	 * @param player the name of the player which the data is associated with.
	 * @return the player data associated with the player.
	 * @throws IllegalArgumentException if the player is not found
	 * @deprecated Don't use unless absolutely necessary.
	 */
	private PlayerData getPlayerData(String player) throws IllegalArgumentException {
		PlayerData playerData = database.find(PlayerData.class).where().eq("playerName", player).findUnique();

		if(playerData == null) {
			throw new IllegalArgumentException("No such player " + player + ".");
		}		
		return playerData;
	}	
}
