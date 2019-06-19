package Evil_Code_Renewable.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import Evil_Code_Renewable.Renewable;
import Evil_Code_Renewable.Utils;
import EvLib.CommandBase;

public class CommandRenewable extends CommandBase{
	
	public CommandRenewable(Renewable p){
		super(p);
		//load anything necessary
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String args[]){
		//cmd:	/renewable
		
		if(sender instanceof Player == false){
			sender.sendMessage(ChatColor.RED+"This command can only be run by a player");
		}
		else{
			ItemStack item = ((Player)sender).getInventory().getItemInMainHand();
			if(item == null || !Utils.isUnrenewable(item))
				sender.sendMessage(ChatColor.GREEN+"This item is renewable");
			else
				sender.sendMessage(ChatColor.RED+"This item is unrenewable");
		}
		return true;
	}
}