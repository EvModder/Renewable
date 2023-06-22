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
	final private Renewable pl;
	final private boolean DO_ITEM_RESCUE, PREVENT_IRREVERSIBLE_PROCESS, ignoreGM1, supplyGM1;
	final boolean RENEWABLE_MOBS;

	public BlockPlaceListener(){
		pl = Renewable.getPlugin();
		DO_ITEM_RESCUE = Renewable.getPlugin().getConfig().getBoolean("rescue-items");
		ignoreGM1 = pl.getConfig().getBoolean("creative-mode-ignore", true);
		supplyGM1 = pl.getConfig().getBoolean("creative-unrenewable-sourcing", false);
		PREVENT_IRREVERSIBLE_PROCESS = pl.getConfig().getBoolean("prevent-irreversible-process", true);
		RENEWABLE_MOBS =  pl.getConfig().getBoolean("renewable-mob-drops", false);
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onBlockPlace(BlockPlaceEvent evt){
		if(evt.getPlayer().getGameMode() == GameMode.CREATIVE && (supplyGM1 || ignoreGM1) &&
				pl.getAPI().isUnrenewable(evt.getBlockPlaced().getBlockData())){
			if(supplyGM1){
				if(!pl.getAPI().takeFromCreativeSupply(evt.getBlockPlaced().getType())){
					evt.getPlayer().sendMessage(ChatColor.RED+"Failed attempt to supply item: "
							+ChatColor.GOLD+evt.getItemInHand().getType()+ChatColor.RED+" from CrSupply depot");
					evt.setCancelled(true);
				}
			}
			return;
		}

		if(pl.getAPI().isUnrenewable(evt.getBlockReplacedState().getBlockData())){
			ItemStack oldBlock = RenewableAPI.getUnewnewableItemForm(evt.getBlockReplacedState());
			ItemStack newBlock = RenewableAPI.getUnewnewableItemForm(evt.getBlockPlaced().getState());
			if(pl.getAPI().isUnrenewableProcess(oldBlock, newBlock)){
				if(pl.getAPI().sameWhenStandardized(oldBlock, newBlock)){
					pl.getLogger().info("[PlaceBlock] irreversible process "+oldBlock.getType()+" -> "+newBlock.getType());
					//Assumption: We can standardize back to oldBlock from newBlock
					pl.getAPI().punishIrreversible(evt.getPlayer().getUniqueId(), oldBlock.getType());
					if(PREVENT_IRREVERSIBLE_PROCESS) evt.setCancelled(true);
				}
				else{
					pl.getLogger().info("[PlaceBlock] flat out killed");
					if(evt.getPlayer().getGameMode() == GameMode.CREATIVE && (supplyGM1 || ignoreGM1)){
						if(supplyGM1){
							if(pl.getAPI().addToCreativeSupply(oldBlock.getType()) != null){
								evt.getPlayer().sendMessage(ChatColor.RED+"Failed attempt to add item:"
								+ChatColor.GOLD+oldBlock.getType()+ChatColor.RED+" to CrSupply depot");
								evt.setCancelled(true);
							}
						}
						return;
					}
					pl.getAPI().punishDestroyed(evt.getPlayer().getUniqueId(), oldBlock.getType());
					if(DO_ITEM_RESCUE) pl.getAPI().rescueItem(oldBlock);
					else if(PREVENT_IRREVERSIBLE_PROCESS) evt.setCancelled(true);
				}
			}
		}
	}
}