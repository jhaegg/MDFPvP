package se.shard.kaustic.mdfpvp.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import se.shard.kaustic.mdfpvp.CommandHandler;
import se.shard.kaustic.mdfpvp.MDFPvP;

/**
 * Class for handling the xp command.
 * @author Johan Hägg
 */
public class XPCommand extends CommandHandler {
	public XPCommand(MDFPvP plugin) {
		super(plugin);
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if(isAnonymous(sender)) {
			return false;
		}		
		Player player = (Player)sender;
		
		sender.sendMessage(player.getTotalExperience() + "/" + plugin.getDatabaseView().getXPRequired(player) + "xp towards claim " + plugin.getDatabaseView().getNumberOfClaims(player) + ".");
		return true;
	}
}
