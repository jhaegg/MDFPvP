package se.shard.kaustic.mdfpvp.listeners;

import java.util.ArrayList;
import java.util.HashMap;

import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.entity.EndermanPickupEvent;
import org.bukkit.event.entity.EndermanPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntityListener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;

import se.shard.kaustic.mdfpvp.MDFPvP;

/**
 * Class for handling entity related events.
 * @author Johan H�gg
 */
public class MDFPvPEntityListener extends EntityListener {
	private MDFPvP plugin;
 	
	public MDFPvPEntityListener(MDFPvP plugin) {
		this.plugin = plugin;
	}

	@Override
	public void onEndermanPickup(EndermanPickupEvent event) {
		event.setCancelled(plugin.getDatabaseView().isProtected(event.getBlock().getChunk()));
	}

	@Override
	public void onEndermanPlace(EndermanPlaceEvent event) {
		event.setCancelled(plugin.getDatabaseView().isProtected(event.getLocation().getBlock().getChunk()));
	}

	@Override
	public void onEntityDamage(EntityDamageEvent event) {
		Player damaged = null;
		Player damager = null;
		// Negate any player-player damage if both players are not in PvP mode.
		if(event instanceof EntityDamageByEntityEvent){
			EntityDamageByEntityEvent subevent = (EntityDamageByEntityEvent)event;
			if(subevent.getEntity() instanceof Player) {
				damaged = (Player)subevent.getEntity();
				if(subevent.getDamager() instanceof Player)
					damager = (Player)subevent.getDamager();
				else if(subevent.getDamager() instanceof Projectile) {
					if(((Projectile)subevent.getDamager()).getShooter() instanceof Player)
						damager = (Player)((Projectile)subevent.getDamager()).getShooter();						
				}
			}
		}
		if(damaged == null || damager == null)
			return;
		
		if(!plugin.isPvPEnabled(damaged) || !plugin.isPvPEnabled(damager))
			event.setCancelled(true);
	}

	@Override
	public void onEntityDeath(EntityDeathEvent event) {
		// Get the killer if there is one.
		Player killer = null;
		if(event.getEntity().getLastDamageCause() instanceof EntityDamageByEntityEvent) {
			EntityDamageByEntityEvent subevent = (EntityDamageByEntityEvent)event.getEntity().getLastDamageCause();
			if(subevent.getDamager() instanceof Player) {
				killer = (Player)subevent.getDamager();
			}
			else if(subevent.getDamager() instanceof Projectile) {
				if(((Projectile)subevent.getDamager()).getShooter() instanceof Player) {
					killer = (Player)((Projectile)subevent.getDamager()).getShooter();
				}
			}
		}
		// Check if a player has died.
		if(event instanceof PlayerDeathEvent) {
			PlayerDeathEvent subevent = (PlayerDeathEvent)event;
			Player killed = (Player)event.getEntity();
			Chest deathChest = plugin.getDatabaseView().getDeathChest(killed);
			// Increment player death count
			plugin.getDatabaseView().addDeath(killed);
			// If the player has a death chest.
			if(deathChest != null) {
				ArrayList<ItemStack> save = new ArrayList<ItemStack>();
				// Put armor and hotbar in chest and remove those items from the dropped items.
				ItemStack[] armor = killed.getInventory().getArmorContents();
				
				for(int index = 0; index < armor.length; index++) {
					if(armor[index].getType() != Material.AIR) {
						save.add(armor[index]);
						event.getDrops().remove(armor[index]);
					}
				}
				for(int index = 0; index < 9; index++) {
					if(killed.getInventory().getItem(index).getType() != Material.AIR) {
						save.add(killed.getInventory().getItem(index));
						event.getDrops().remove(killed.getInventory().getItem(index));
					}
				}
				// Workaround for list as argument.
				ItemStack[] itemStack = new ItemStack[save.size()];
				// Add all items that will not fit in the death chest to the dropped items.
				event.getDrops().addAll(deathChest.getInventory().addItem(save.toArray(itemStack)).values());
			}
			// Calculate remaining, dropped and claimed experience
			int dropped = (int)Math.floor(killed.getTotalExperience() * 0.35);
			int remaining = (int)Math.floor(killed.getTotalExperience() * 0.40);
			Chunk chunk = killed.getLocation().getBlock().getChunk();
			boolean killedOnOwnClaim = false;
			
			// Give 70% of the dropped xp to the owner of the claim.
			if(plugin.getDatabaseView().isClaimed(chunk)) {
				int give = (int)Math.floor(dropped * 0.7);
				// Suppress notification if player was killed on their own claim.
				if(plugin.getDatabaseView().isOwner(killed, chunk)) {
					killedOnOwnClaim = true;
					remaining += give;
				} else {
					plugin.getDatabaseView().giveOrPostponeExperience(chunk, give, "the death of " + killed.getName());
				}
				dropped -= give;
			}
			// Set remaining and dropped experience.
			subevent.setNewExp(remaining);
			subevent.setDroppedExp(dropped);
			// Give experience reward to the killer.
			if(killer != null) {
				// 15% of total xp as well as 3xp bonus per claim owned by killer.
				int give = (int)Math.floor(killed.getTotalExperience() * 0.15) + plugin.getDatabaseView().getNumberOfClaims(killer) * 3;
				// Give a 75% bonus if the player was killed on their own claim.
				if(killedOnOwnClaim == true) {
					give = (int)Math.floor(give * 1.75);
					// Teleport killer to their spawn.
					killer.teleport(plugin.getDatabaseView().getSpawnLocation(killer));
				}
				killer.sendMessage("Awarded " + give + "xp for killing " + killed.getName());
				killer.setTotalExperience(killer.getTotalExperience() + give);
				// Add scoreboard data for killer and killed.
				plugin.getDatabaseView().addKilled(killed);
				plugin.getDatabaseView().addKill(killer);
			}
		}
		else if(killer != null) {
			// Mob killed by a player.
			Entity killed = event.getEntity();
			// Award killer 0.2 experience points for each claim he or she owns.
			killer.setTotalExperience(killer.getTotalExperience() + (int)Math.ceil(0.2 * plugin.getDatabaseView().getNumberOfClaims(killer)));
			Chunk chunk = killed.getLocation().getBlock().getChunk();
			// Award 0.1 experience points for each claim to the owner of the claim.
			if(plugin.getDatabaseView().isClaimed(chunk)) {
				// Suppress notification if the player is the killer.
				if(plugin.getDatabaseView().isOwner(killer, chunk)) {
					killer.setTotalExperience(killer.getTotalExperience() + (int)Math.ceil(0.1 * plugin.getDatabaseView().getNumberOfClaims(killer)));
				}
				else {
					plugin.getDatabaseView().giveOrPostponeExperience(chunk, (int)Math.ceil(0.1 * plugin.getDatabaseView().getNumberOfClaims(killer)), "death of " + killed.toString());
				}
			}
		}
	}

	@Override
	public void onEntityExplode(EntityExplodeEvent event) {
		// Local buffer to avoid unnecessary queries to the database.
		HashMap<Chunk, Boolean> protectedMap = new HashMap<Chunk, Boolean>();
		for(Block block : event.blockList()) {
			boolean protect;
			if(protectedMap.containsKey(block.getChunk())) {
				protect = protectedMap.get(block.getChunk());
			} 
			else {
				protect = plugin.getDatabaseView().isProtected(block.getChunk());
				protectedMap.put(block.getChunk(), protect);
			}
			
			if(protect) {
			    // Chunk should be protected from everything except TNT.
				if(!(event.getEntity() instanceof TNTPrimed)) {
				    event.setCancelled(true);
				    return;
				}
			}
			// Protect unprotected chunks with a death chest from everything.
			else if(block.getType() == Material.CHEST && plugin.getDatabaseView().isDeathChest(block)) {
				event.setCancelled(true);
				return;
			}
		}
	}
}
