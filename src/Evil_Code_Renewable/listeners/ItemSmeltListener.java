package Evil_Code_Renewable.listeners;

import java.util.UUID;
import org.bukkit.block.Furnace;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.FurnaceBurnEvent;
import org.bukkit.event.inventory.FurnaceSmeltEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;
import Evil_Code_Renewable.NBTFlagUtils;
import Evil_Code_Renewable.Renewable;
import Evil_Code_Renewable.Utils;

public class ItemSmeltListener implements Listener{
	final Renewable plugin;
	final boolean saveItems, normalizeRescuedItems;
	final boolean preventUnrenewableProcess, punishUnrenewableProcess;
	final int maxFortuneLevel;

	public ItemSmeltListener(){
		plugin = Renewable.getPlugin();
		saveItems = Renewable.getPlugin().getConfig().getBoolean("rescue-items");
		maxFortuneLevel = plugin.getConfig().getInt("max-fortune-level", 3);
		normalizeRescuedItems = plugin.getConfig().getBoolean("standardize-rescued-items", true);
		preventUnrenewableProcess = plugin.getConfig().getBoolean("prevent-irreversible-process", false);
		punishUnrenewableProcess = plugin.getConfig().getBoolean("punish-for-irreversible-process", true);
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onFurnaceOpen(InventoryOpenEvent evt){
		if(evt.getInventory().getType() == InventoryType.FURNACE && !evt.isCancelled()){
			NBTFlagUtils.setLastPlayerInContact(((Furnace)evt.getInventory().getHolder()).getBlock().getState(),
					evt.getPlayer().getUniqueId());
		}
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onItemSmelt(FurnaceSmeltEvent evt){
		if(!evt.isCancelled() && Utils.isUnrenewableProcess(evt.getSource(), evt.getResult())){
			if(Utils.isUnrenewable(evt.getResult())){
				if(punishUnrenewableProcess){
					UUID uuid = NBTFlagUtils.getLastPlayerInContact(evt.getBlock().getState());
					plugin.punish(uuid, evt.getSource().getType());
				}
				if(preventUnrenewableProcess){
					evt.setCancelled(true);
					evt.getBlock().breakNaturally();//drop fuel, source, and furnace block
				}
			}
			else{
				//TODO: Punish!!
				if(saveItems){
					ItemStack item = evt.getSource().clone();
					// Normalization code, but ores are now considered more normalized than their item form
//					if(item.getType() == Material.DIAMOND_ORE && normalizeRescuedItems){
//						plugin.rescueItem(new ItemStack(Material.DIAMOND, maxFortuneLevel));
//					}
//					else if(item.getType() == Material.QUARTZ_ORE && normalizeRescuedItems){
//						plugin.rescueItem(new ItemStack(Material.QUARTZ, maxFortuneLevel));
//					}
//					else{
						item.setAmount(1);
						UUID uuid = NBTFlagUtils.getLastPlayerInContact(evt.getBlock().getState());
						plugin.punish(uuid, item.getType());
						plugin.rescueItem(item);
//					}
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