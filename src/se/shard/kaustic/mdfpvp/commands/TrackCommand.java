package se.shard.kaustic.mdfpvp.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import se.shard.kaustic.mdfpvp.CommandHandler;
import se.shard.kaustic.mdfpvp.MDFPvP;

public class TrackCommand extends CommandHandler {
	public TrackCommand(MDFPvP plugin) {
		super(plugin);
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if(isAnonymous(sender)) {
			return false;
		}
		if(args.length < 1) {
			sender.sendMessage("No player specified");
		}
		Player player = (Player)sender;
		Player target = getTarget(sender, 1, args);
		
		if(target == null) {
			return false;
		}
		player.setCompassTarget(target.getLocation());
		sender.sendMessage("Tracking " + target.getName() + ".");	
		return true;
	}
}
