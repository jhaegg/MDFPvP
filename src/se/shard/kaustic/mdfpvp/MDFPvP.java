package se.shard.kaustic.mdfpvp;

import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;

import javax.persistence.PersistenceException;

import org.bukkit.event.Event;
import org.bukkit.event.Event.Priority;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import se.shard.kaustic.mdfpvp.commands.ClaimCommand;
import se.shard.kaustic.mdfpvp.commands.RemoveClaimCommand;
import se.shard.kaustic.mdfpvp.commands.SetXPCommand;
import se.shard.kaustic.mdfpvp.listeners.MDFPvPBlockListener;
import se.shard.kaustic.mdfpvp.listeners.MDFPvPPlayerListener;
import se.shard.kaustic.mdfpvp.persisting.Claim;
import se.shard.kaustic.mdfpvp.persisting.PlayerData;

/**
 * Bukkit PvP plugin for MDF Minecraft server.
 * @author Johan Hägg
 */

public class MDFPvP extends JavaPlugin {
	private DatabaseView databaseView;
	private final MDFPvPBlockListener blockListener = new MDFPvPBlockListener(this);
	private final MDFPvPPlayerListener playerListener = new MDFPvPPlayerListener(this);
	private final Class<?>[] databaseClasses = {PlayerData.class, Claim.class};
	private PluginDescriptionFile pdf; 
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
		
		// Register commands
		getCommand("claim").setExecutor(new ClaimCommand(this));
		getCommand("setxp").setExecutor(new SetXPCommand(this));
		getCommand("removeclaim").setExecutor(new RemoveClaimCommand(this));
		
		// Register player events.
		pm.registerEvent(Event.Type.PLAYER_MOVE, playerListener, Priority.Normal, this);
		
		// Register block events.
		pm.registerEvent(Event.Type.BLOCK_BREAK, blockListener, Priority.Normal, this);
		pm.registerEvent(Event.Type.BLOCK_IGNITE, blockListener, Priority.Normal, this);
		pm.registerEvent(Event.Type.BLOCK_PLACE, blockListener, Priority.Normal, this);
		
		// Register entity events.
		
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
	
	public DatabaseView getDatabaseView() {
		return databaseView;
	}

}
