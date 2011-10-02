package se.shard.kaustic.mdfpvp.listeners;

import java.util.HashMap;
import java.util.UUID;

import org.bukkit.Chunk;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerBedEnterEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

import se.shard.kaustic.mdfpvp.MDFPvP;

/**
 * Listener for handling player related events for MDFPvP
 * @author Johan Hägg
 */
public class MDFPvPPlayerListener extends PlayerListener {
	/**
	 * Internal storage class for a chunk-owner pair.
	 * @author Johan Hägg
	 */
	private class ChunkOwnerPair {
		private Chunk chunk;
		private String owner;
		
		public ChunkOwnerPair(Chunk chunk, String owner) {
			this.chunk = chunk;
			this.owner = owner;
		}
		/**
		 * Evaluates if the provided chunk is the same as the chunk stored in the pair.
		 * @param chunk the chunk to be evaluated.
		 * @return true if same chunk, otherwise false.
		 */
		public boolean isSameChunk(Chunk chunk) {
			return chunk.getWorld().getUID() == this.chunk.getWorld().getUID() 
			&& chunk.getX() == this.chunk.getX() && chunk.getZ() == this.chunk.getZ();
		}
		
		/**
		 * Evaluates if the provided owner is the same as the owner stored in the pair.
		 * @param owner the owner to be evaluated.
		 * @return true if the same owner, otherwise false.
		 */
		public boolean isSameOwner(String owner) {
			if(owner == null && this.owner == null) {
				return true;
			}
			if(owner == null ^ this.owner == null) {
				return false;
			}			
			return owner.equals(this.owner);
		}
			
		/**
		 * Sets the chunk part of the chunk-owner pair.
		 * @param chunk the new chunk part of the pair.
		 */
		public void setChunk(Chunk chunk) {
			this.chunk = chunk;
		}
		
		/**
		 * Gets the owner part of the chunk-owner pair.
		 * @return the owner part of the chunk-owner pair.
		 */
		public String getOwner() {
			return owner;
		}
		
		/**
		 * Sets the owner part of the chunk-owner pair.
		 * @param owner the new owner part of the chunk-owner pair.
		 */
		public void setOwner(String owner) {
			this.owner = owner;
		}		
	}
	
	private MDFPvP plugin;
	private HashMap<UUID, ChunkOwnerPair> chunkOwnerMap = new HashMap<UUID, ChunkOwnerPair>();
	
	public MDFPvPPlayerListener(MDFPvP plugin) {
		this.plugin = plugin;
	}

	@Override
	public void onPlayerBedEnter(PlayerBedEnterEvent event) {
		// TODO Auto-generated method stub
		super.onPlayerBedEnter(event);
	}

	@Override
	public void onPlayerInteract(PlayerInteractEvent event) {
		// TODO Auto-generated method stub
		super.onPlayerInteract(event);
	}

	@Override
	public void onPlayerJoin(PlayerJoinEvent event) {
		// TODO Auto-generated method stub
		super.onPlayerJoin(event);
	}

	@Override
	public void onPlayerMove(PlayerMoveEvent event) {		
		//Check if the player is stored in the chunk-owner pair map.
		Player player = event.getPlayer();
		Chunk chunk = player.getLocation().getBlock().getChunk();
		if(chunkOwnerMap.containsKey(player.getUniqueId())) {
			ChunkOwnerPair chunkOwnerPair = chunkOwnerMap.get(player.getUniqueId());			
			
			// Check if the player has moved to another chunk.
			if(!chunkOwnerPair.isSameChunk(chunk)) {
				String owner = plugin.getDatabaseView().getOwner(chunk);
				// Check if the new chunk is owned by the same player or still no-one.
				if(!chunkOwnerPair.isSameOwner(owner)) {
					if(owner == null) {
						player.sendMessage("Leaving " + chunkOwnerPair.getOwner() + "s claim.");
					}
					else {
						player.sendMessage("Entering " + owner + "s claim.");
					}
					chunkOwnerPair.setOwner(owner);
				}
				chunkOwnerPair.setChunk(chunk);
				chunkOwnerMap.put(player.getUniqueId(), chunkOwnerPair);
			}
		}
		else {
			// Create a new entry.
			chunkOwnerMap.put(player.getUniqueId(), new ChunkOwnerPair(chunk, plugin.getDatabaseView().getOwner(chunk)));
		}
	}

	@Override
	public void onPlayerRespawn(PlayerRespawnEvent event) {
		// TODO Auto-generated method stub
		super.onPlayerRespawn(event);
	}
	
	

}
