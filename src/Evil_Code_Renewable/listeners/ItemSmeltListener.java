package Evil_Code_Renewable.listeners;

import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.FurnaceBurnEvent;
import org.bukkit.event.inventory.FurnaceSmeltEvent;
import org.bukkit.inventory.ItemStack;

import Evil_Code_Renewable.Renewable;
import Evil_Code_Renewable.Utils;

public class ItemSmeltListener implements Listener{
	Renewable plugin;
	boolean saveItems, normalizeRescuedItems, preventUnrenewableProcess;
	int maxFortuneLevel;

	public ItemSmeltListener(){
		plugin = Renewable.getPlugin();
		saveItems = Renewable.getPlugin().getConfig().getBoolean("rescue-items");
		maxFortuneLevel = plugin.getConfig().getInt("max-fortune-level", 3);
		normalizeRescuedItems = plugin.getConfig().getBoolean("standardize-rescued-items", true);
		preventUnrenewableProcess = plugin.getConfig().getBoolean("prevent-irreversible-process", false);
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onItemSmelt(FurnaceSmeltEvent evt) {
		if(!evt.isCancelled() && Utils.isUnrenewable(evt.getSource())){
			//punish
			if(preventUnrenewableProcess){
				evt.setCancelled(true);
				evt.getBlock().breakNaturally();//drop fuel, source, and furnace block
			}
			else if(saveItems){
				ItemStack item = evt.getSource().clone();
				if(item.getType() == Material.DIAMOND_ORE && normalizeRescuedItems){
					plugin.rescueItem(new ItemStack(Material.DIAMOND, maxFortuneLevel));
				}
				else if(item.getType() == Material.QUARTZ_ORE && normalizeRescuedItems){
					plugin.rescueItem(new ItemStack(Material.QUARTZ, maxFortuneLevel));
				}
				else{
					item.setAmount(1);
					plugin.rescueItem(item);
				}
			}
		}
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onFuelConsumption(FurnaceBurnEvent evt){
		if(!evt.isCancelled() && Utils.isUnrenewable(evt.getFuel())){
			if(preventUnrenewableProcess){
				evt.setCancelled(true);
				evt.getBlock().breakNaturally();//drop fuel, source, and furnace block
			}
			else if(saveItems){
				//duplicates fuel in furnace right before smelt
				ItemStack fuel = evt.getFuel().clone();
				fuel.setAmount(1);
				plugin.rescueItem(fuel);
			}
		}
	}
}