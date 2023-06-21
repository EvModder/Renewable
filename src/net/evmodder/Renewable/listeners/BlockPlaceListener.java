package net.evmodder.Renewable.listeners;

import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;
import net.evmodder.Renewable.Renewable;
import net.evmodder.Renewable.RenewableAPI;

public class BlockPlaceListener implements Listener{
	final Renewable plugin;
	final boolean saveItems, normalizeRescuedItems, preventUnrenewableProcess, punishUnrenewableProcess,
					ignoreGM1, supplyGM1;
	final boolean RENEWABLE_MOBS;

	public BlockPlaceListener(){
		plugin = Renewable.getPlugin();
		saveItems = Renewable.getPlugin().getConfig().getBoolean("rescue-items");
		normalizeRescuedItems = plugin.getConfig().getBoolean("standardize-rescued-items", true);
		ignoreGM1 = plugin.getConfig().getBoolean("creative-mode-ignore", true);
		supplyGM1 = plugin.getConfig().getBoolean("creative-unrenewable-sourcing", false);
		preventUnrenewableProcess = plugin.getConfig().getBoolean("prevent-irreversible-process", true);
		punishUnrenewableProcess = plugin.getConfig().getBoolean("punish-for-irreversible-process", true);
		RENEWABLE_MOBS =  plugin.getConfig().getBoolean("renewable-mob-drops", false);
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onBlockPlace(BlockPlaceEvent evt){
		if(evt.getPlayer().getGameMode() == GameMode.CREATIVE && (supplyGM1 || ignoreGM1) &&
				plugin.getAPI().isUnrenewable(evt.getBlockPlaced().getBlockData())){
			if(supplyGM1){
				if(!plugin.getAPI().deductFromCreativeSupply(evt.getBlockPlaced().getType())){
					evt.getPlayer().sendMessage(ChatColor.RED+"Failed attempt to supply item: "
							+ChatColor.GOLD+evt.getItemInHand().getType()+ChatColor.RED+" from CrSupply depot");
					evt.setCancelled(true);
				}
			}
			return;
		}

		if(plugin.getAPI().isUnrenewable(evt.getBlockReplacedState().getBlockData())){
			ItemStack oldBlock = RenewableAPI.getUnewnewableItemForm(evt.getBlockReplacedState());
			ItemStack newBlock = RenewableAPI.getUnewnewableItemForm(evt.getBlockPlaced().getState());
			if(plugin.getAPI().isUnrenewableProcess(oldBlock, newBlock)){
				if(plugin.getAPI().sameWhenStandardizedIgnoreAmt(oldBlock, newBlock)){
					plugin.getLogger().info("[PlaceBlock] irreversible process "+
							oldBlock.getType()+" -> "+newBlock.getType());
					//Assumption: We can standardize back to oldBlock from newBlock
					if(punishUnrenewableProcess) plugin.getAPI().punish(evt.getPlayer().getUniqueId(), oldBlock.getType());
					if(preventUnrenewableProcess) evt.setCancelled(true);
				}
				else{
					plugin.getLogger().info("[PlaceBlock] flat out killed");
					if(evt.getPlayer().getGameMode() == GameMode.CREATIVE && (supplyGM1 || ignoreGM1)){
						if(supplyGM1){
							if(plugin.getAPI().addToCreativeSupply(oldBlock.getType()) != null){
								evt.getPlayer().sendMessage(ChatColor.RED+"Failed attempt to add item:"
								+ChatColor.GOLD+oldBlock.getType()+ChatColor.RED+" to CrSupply depot");
								evt.setCancelled(true);
							}
						}
						return;
					}
					if(saveItems) plugin.getAPI().rescueItem(oldBlock);
					else if(preventUnrenewableProcess) evt.setCancelled(true);
					plugin.getAPI().punish(evt.getPlayer().getUniqueId(), oldBlock.getType());
				}
			}
		}
	}
}