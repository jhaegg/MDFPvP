package se.shard.kaustic.mdfpvp.listeners;

import java.util.ArrayList;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntityListener;
import org.bukkit.inventory.ItemStack;

import se.shard.kaustic.mdfpvp.MDFPvP;

/**
 * Class for handling entity related events.
 * @author Johan Hägg
 */
public class MDFPvPEntityListener extends EntityListener {
	private MDFPvP plugin;
	
	public MDFPvPEntityListener(MDFPvP plugin) {
		this.plugin = plugin;
	}

	@Override
	public void onEntityDamage(EntityDamageEvent event) {
		// TODO Auto-generated method stub
		super.onEntityDamage(event);
	}

	@Override
	public void onEntityDeath(EntityDeathEvent event) {
		// Check if a player has died.
		if(event.getEntity() instanceof Player) {
			Player killed = (Player)event.getEntity();
			Chest deathChest = plugin.getDatabaseView().getDeathChest(killed);
			// If the player has a death chest.
			if(deathChest != null) {
				ArrayList<ItemStack> save = new ArrayList<ItemStack>();
				// Put armor and hotbar in chest.
				ItemStack[] armor = killed.getInventory().getArmorContents();
				for(int index = 0; index < armor.length; index++) {
					if(armor[index].getType() != Material.AIR) {
						save.add(armor[index]);
					}
				}
				for(int index = 0; index < 9; index++) {
					if(killed.getInventory().getItem(index).getType() != Material.AIR) {
						save.add(killed.getInventory().getItem(index));
					}
				}
				// Remove all saved items.
				event.getDrops().removeAll(save);
				// Workaround for list as argument.
				ItemStack[] itemStack = new ItemStack[save.size()];
				// Add all items that will not fit in the death chest to the dropped items.
				event.getDrops().addAll(deathChest.getInventory().addItem(save.toArray(itemStack)).values());
			}
		}
	}

	@Override
	public void onEntityExplode(EntityExplodeEvent event) {		
		for(Block block : event.blockList()) {
			// Protect death chests from explosions.
			if(block.getType() == Material.CHEST && plugin.getDatabaseView().isDeathChest(block)) {
				event.setCancelled(true);
				return;
			}
		}
	}
	
	
}
