package se.shard.kaustic.mdfpvp.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import se.shard.kaustic.mdfpvp.CommandHandler;
import se.shard.kaustic.mdfpvp.MDFPvP;

public class PvPCommand extends CommandHandler {

	public PvPCommand(MDFPvP plugin) {
		super(plugin);
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if(isAnonymous(sender))
			return false;
		
		Player player = (Player)sender;
		if(plugin.canChangePvP(player)) {
			if(plugin.isPvPEnabled(player)) {
				player.setPlayerListName(player.getName());
				plugin.getServer().broadcastMessage(player.getName() + " left PvP mode.");
			}
			else {
				player.setPlayerListName(ChatColor.GOLD + player.getName());
				plugin.getServer().broadcastMessage(player.getName() + " entered PvP mode.");			
			}
			return true;
		}
		else {
			sender.sendMessage("Can't change yet.");
			return false;
		}
	}
}
