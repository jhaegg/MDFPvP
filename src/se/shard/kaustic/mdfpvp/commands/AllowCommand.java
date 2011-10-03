package se.shard.kaustic.mdfpvp.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import se.shard.kaustic.mdfpvp.CommandHandler;
import se.shard.kaustic.mdfpvp.MDFPvP;

/**
 * Class for handling the allow/disallow command.
 * @author kaustic
 *
 */

public class AllowCommand extends CommandHandler {

	private boolean allow;
	
	public AllowCommand(MDFPvP plugin, boolean allow) {
		super(plugin);
		this.allow = allow;		
	}
		
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if(isAnonymous(sender)) {
			return false;
		}
		if(args.length < 1) {
			sender.sendMessage("Please provide at least one player");
			return false;
		}
		
		Player owner = (Player)sender;
		
		// Loop through provided list of players.
		for(String playerName : args) {
			Player tenant = plugin.getServer().matchPlayer(playerName).get(0);
						
			if(tenant == null) {
				sender.sendMessage("No such player " + playerName + ".");
			}
			// Try to add or remove player.
			else if (plugin.getDatabaseView().changeTenantStatus(owner, tenant, allow)) {
				// Notify player of success according to mode.
				if(allow) {
					sender.sendMessage("Added " + tenant.getName() + " to allowed list.");
				}
				else {
					sender.sendMessage("Removed " + tenant.getName() + " from allowed list.");
				}				
			}
			// Notify player of failure according to mode.
			else if(allow) {
				sender.sendMessage(playerName + " is already in the allowed list.");
			}
			else {
				sender.sendMessage(playerName + " is not in the allowed list.");
			}
		}
		return true;
	}
}
