package se.shard.kaustic.mdfpvp.listeners;

import org.bukkit.Chunk;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockIgniteEvent.IgniteCause;
import org.bukkit.event.block.BlockListener;
import org.bukkit.event.block.BlockPlaceEvent;

import se.shard.kaustic.mdfpvp.MDFPvP;

/**
 * Class for handling block related events.
 * @author Johan H�gg
 */
public class MDFPvPBlockListener extends BlockListener {
	private MDFPvP plugin;
	
	public MDFPvPBlockListener(MDFPvP plugin) {
		this.plugin = plugin;
	}

	/**
	 * Check if the player is allowed to perform an action on the chunk.
	 * @param player the player to be checked
	 * @param chunk the chunk the player should be checked against.
	 * @return true if the action is allowed, otherwise false.
	 */
	private boolean isAllowedAction(Player player, Chunk chunk) {
		if(!plugin.getDatabaseView().canChange(player, chunk)) {
			player.sendMessage("Action not allowed, chunk owned by " + plugin.getDatabaseView().getOwner(chunk) + ".");
			return false;
		}
		
		return true;
	}
	
	@Override
	public void onBlockBreak(BlockBreakEvent event) {
		event.setCancelled(!isAllowedAction(event.getPlayer(), event.getBlock().getChunk()));
	}

	@Override
	public void onBlockIgnite(BlockIgniteEvent event) {
		if(event.getCause() == IgniteCause.FLINT_AND_STEEL) {
			event.setCancelled(!isAllowedAction(event.getPlayer(), event.getBlock().getChunk()));
		}
	}

	@Override
	public void onBlockPlace(BlockPlaceEvent event) {
		if(!isAllowedAction(event.getPlayer(), event.getBlock().getChunk())) {
			// Return placed block to owners hand.
			event.getItemInHand().setAmount(event.getItemInHand().getAmount() + 1);
			event.setCancelled(true);
		}
	}
}
