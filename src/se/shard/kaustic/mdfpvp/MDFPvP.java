package se.shard.kaustic.mdfpvp;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;

import javax.persistence.PersistenceException;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.Event.Priority;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import se.shard.kaustic.mdfpvp.commands.AllowCommand;
import se.shard.kaustic.mdfpvp.commands.ClaimCommand;
import se.shard.kaustic.mdfpvp.commands.ProtectCommand;
import se.shard.kaustic.mdfpvp.commands.PvPCommand;
import se.shard.kaustic.mdfpvp.commands.RemoveClaimCommand;
import se.shard.kaustic.mdfpvp.commands.ResetDeathChestCommand;
import se.shard.kaustic.mdfpvp.commands.SetXPCommand;
import se.shard.kaustic.mdfpvp.commands.TrackCommand;
import se.shard.kaustic.mdfpvp.commands.XPCommand;
import se.shard.kaustic.mdfpvp.listeners.MDFPvPBlockListener;
import se.shard.kaustic.mdfpvp.listeners.MDFPvPEntityListener;
import se.shard.kaustic.mdfpvp.listeners.MDFPvPPlayerListener;
import se.shard.kaustic.mdfpvp.persisting.Claim;
import se.shard.kaustic.mdfpvp.persisting.DeathChest;
import se.shard.kaustic.mdfpvp.persisting.PlayerData;
import se.shard.kaustic.mdfpvp.persisting.SpawnLocation;
import se.shard.kaustic.mdfpvp.services.ClaimRegionExporter;

/**
 * Bukkit PvP plugin for MDF Minecraft server.
 * @author Johan Hägg
 */

public class MDFPvP extends JavaPlugin {
	private DatabaseView databaseView;
	private final MDFPvPBlockListener blockListener = new MDFPvPBlockListener(this);
	private final MDFPvPPlayerListener playerListener = new MDFPvPPlayerListener(this);
	private final MDFPvPEntityListener entityListener = new MDFPvPEntityListener(this);
	private final Class<?>[] databaseClasses = {PlayerData.class, Claim.class, DeathChest.class, SpawnLocation.class};
	private PluginDescriptionFile pdf; 
	private final HashMap<UUID, Long> pvpEnabled = new HashMap<UUID, Long>();
	private final long hourMilliseconds = 1000 * 60 * 60;
	
	@Override
	public void onDisable() {
		// Notify the the plugin has been disabled.
		getServer().getLogger().log(Level.INFO, "Disabled MDFPvP");
	}

	@Override
	public void onEnable() {
		PluginManager pm = getServer().getPluginManager();
		pdf = getDescription();
		
		// Notify the server the the plugin is being enabled.
		getServer().getLogger().log(Level.INFO, "Enabling " + pdf.getName() + " version " + pdf.getVersion() + ".");
				
		// Initialize database.
		initDatabase();
		
		// Register services in scheduler. Might need to be executed synchronously if 
		// chunk loading messes up the server.
		getServer().getScheduler().scheduleAsyncRepeatingTask(this, new ClaimRegionExporter(this), 200L, 18000L);
		
		// Register commands
		getCommand("allow").setExecutor(new AllowCommand(this, true));
		getCommand("claim").setExecutor(new ClaimCommand(this));
		getCommand("disallow").setExecutor(new AllowCommand(this, false));
		getCommand("protect").setExecutor(new ProtectCommand(this));
		getCommand("pvp").setExecutor(new PvPCommand(this));		
		getCommand("removeclaim").setExecutor(new RemoveClaimCommand(this));
		getCommand("resetdeathchest").setExecutor(new ResetDeathChestCommand(this));
		getCommand("setxp").setExecutor(new SetXPCommand(this));	
		getCommand("track").setExecutor(new TrackCommand(this));
		getCommand("xp").setExecutor(new XPCommand(this));
		
		// Register player events.
		pm.registerEvent(Event.Type.PLAYER_INTERACT, playerListener, Priority.Normal, this);
		pm.registerEvent(Event.Type.PLAYER_JOIN, playerListener, Priority.Normal, this);
		pm.registerEvent(Event.Type.PLAYER_MOVE, playerListener, Priority.Normal, this);
		pm.registerEvent(Event.Type.PLAYER_BED_ENTER, playerListener, Priority.Normal, this);
		
		// Register block events.
		pm.registerEvent(Event.Type.BLOCK_BREAK, blockListener, Priority.Normal, this);
		pm.registerEvent(Event.Type.BLOCK_IGNITE, blockListener, Priority.Normal, this);
		pm.registerEvent(Event.Type.BLOCK_PLACE, blockListener, Priority.Normal, this);
		
		// Register entity events.
		pm.registerEvent(Event.Type.ENTITY_DAMAGE, entityListener, Priority.Normal, this);
		pm.registerEvent(Event.Type.ENTITY_DEATH, entityListener, Priority.Normal, this);
		pm.registerEvent(Event.Type.ENTITY_EXPLODE, entityListener, Priority.Normal, this);
		pm.registerEvent(Event.Type.ENDERMAN_PICKUP, entityListener, Priority.Normal, this);
		pm.registerEvent(Event.Type.ENDERMAN_PLACE, entityListener, Priority.Normal, this);
		
		// Notify of completion.
		getServer().getLogger().log(Level.INFO, "Done.");
	}
		
	@Override
	public List<Class<?>> getDatabaseClasses() {
		return Arrays.asList(databaseClasses);
	}

	/**
	 * Initializes the database.
	 */
	private void initDatabase() {
		try {
			for(Class<?> databaseClass : databaseClasses) {
				getDatabase().find(databaseClass).findRowCount();
			}			
		}
		catch(PersistenceException ex) {
			getServer().getLogger().log(Level.INFO, "Creating database for " + pdf.getName() + ".");
			installDDL();
		}
		databaseView = new DatabaseView(this, getDatabase());
	}
	
	/**
	 * Gets the current database view.
	 * @return The current database view.
	 */
	public DatabaseView getDatabaseView() {
		return databaseView;
	}
	
	/**
	 * Checks if a player has PvP enabled.
	 * @param player The player which should be checked.
	 * @return True if the player has enabled PvP, otherwise false.
	 */
	public Boolean isPvPEnabled(Player player) {
		return isPvPEnabled(player.getUniqueId());
	}
	
	/**
	 * Checks if a player has PvP enabled.
	 * @param playerUUID The bukkit unique id of player which should be checked.
	 * @return True if the player has enabled PvP, otherwise false.
	 */
	public Boolean isPvPEnabled(UUID playerUUID) {
		if(pvpEnabled.containsKey(playerUUID))
			return pvpEnabled.get(playerUUID) != 0L;
		
		return false;
	}
	
	/**
	 * Sets if the player has PvP enabled or not.
	 * @param player The player which should be modified.
	 * @param pvp True if pvp should be enabled, otherwise false.
	 */
	public void setPvPEnabled(Player player, Boolean pvp) {
		if(pvp)
			pvpEnabled.put(player.getUniqueId(), System.currentTimeMillis());
		else
			pvpEnabled.put(player.getUniqueId(), 0L);
	}

	/**
	 * Checks if the player can modifie their PvP status.
	 * @param player The player which should be checked.
	 * @return True if the player can change PvP status.
	 */
	public Boolean canChangePvP(Player player) {
		if(pvpEnabled.containsKey(player.getUniqueId()))
			return pvpEnabled.get(player.getUniqueId()) + hourMilliseconds < System.currentTimeMillis();
		
		return true;
	}
	
}