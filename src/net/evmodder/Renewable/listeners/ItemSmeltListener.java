package net.evmodder.Renewable.listeners;

import org.bukkit.block.Furnace;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.FurnaceBurnEvent;
import org.bukkit.event.inventory.FurnaceSmeltEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;
import net.evmodder.Renewable.TaggingUtil;
import net.evmodder.Renewable.Renewable;

public class ItemSmeltListener implements Listener{
	final private Renewable pl;
	final private boolean DO_ITEM_RESCUE;
	final private boolean PREVENT_IRREVERSIBLE_PROCESS;

	public ItemSmeltListener(){
		pl = Renewable.getPlugin();
		DO_ITEM_RESCUE = pl.getConfig().getBoolean("rescue-items");
		PREVENT_IRREVERSIBLE_PROCESS = pl.getConfig().getBoolean("prevent-irreversible-process", false);
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onFurnaceOpen(InventoryOpenEvent evt){
		if(evt.getInventory().getType() == InventoryType.FURNACE){
			TaggingUtil.setLastPlayerInContact(((Furnace)evt.getInventory().getHolder()).getBlock().getState(),
					evt.getPlayer().getUniqueId());
		}
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onItemSmelt(FurnaceSmeltEvent evt){
		if(pl.getAPI().isUnrenewableProcess(evt.getSource(), evt.getResult())){
			if(pl.getAPI().isUnrenewable(evt.getResult())){
				pl.getAPI().punishIrreversible(TaggingUtil.getLastPlayerInContact(evt.getBlock().getState()), evt.getSource().getType());
				if(PREVENT_IRREVERSIBLE_PROCESS){
					evt.setCancelled(true);
					evt.getBlock().breakNaturally();//drop fuel, source, and furnace block
				}
			}
			else{
				pl.getAPI().punishDestroyed(TaggingUtil.getLastPlayerInContact(evt.getBlock().getState()), evt.getSource().getType());
				if(DO_ITEM_RESCUE){
					final ItemStack source = evt.getSource().clone();
					source.setAmount(1);
					pl.getAPI().rescueItem(source);
				}
			}
		}
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onFuelConsumption(FurnaceBurnEvent evt){
		if(pl.getAPI().isUnrenewable(evt.getFuel())){
			if(PREVENT_IRREVERSIBLE_PROCESS){
				evt.setCancelled(true);
				evt.getBlock().breakNaturally();//drop fuel, source, and furnace block
			}
			else if(DO_ITEM_RESCUE){
				//duplicates fuel in furnace right before smelt
				final ItemStack fuel = evt.getFuel().clone();
				fuel.setAmount(1);
				pl.getAPI().rescueItem(fuel);
			}
		}
	}
}