package Evil_Code_Renewable.listeners;

import java.util.HashSet;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.inventory.ItemStack;
import Evil_Code_Renewable.Renewable;
import Evil_Code_Renewable.Utils;

public class ItemCraftListener implements Listener{
	Renewable plugin;
	boolean saveItems, punishUnrenewableProcess, preventUnrenewableProcess, preventUnrenewableDeath, normalizeRescuedItems;

	public ItemCraftListener(){
		plugin = Renewable.getPlugin();
		saveItems = plugin.getConfig().getBoolean("rescue-items", true);
		punishUnrenewableProcess = plugin.getConfig().getBoolean("punish-for-irreversible-process", true);
		preventUnrenewableProcess = plugin.getConfig().getBoolean("prevent-irreversible-process", false);
		preventUnrenewableDeath = plugin.getConfig().getBoolean("prevent-unrenewable-destruction", false);
		normalizeRescuedItems = plugin.getConfig().getBoolean("standardize-rescued-items", true);
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onItemCraft(CraftItemEvent evt){
		if(evt.isCancelled() || evt.getWhoClicked().getGameMode() == GameMode.CREATIVE) return;
		//TODO: Put all recipes which are reversible in this if statement
		if(evt.getInventory().getResult().getType() == Material.DIAMOND_BLOCK) return;

		HashSet<ItemStack> destroyed = new HashSet<ItemStack>();
		ItemStack output = Utils.standardize(evt.getInventory().getResult());
		int amt = output.getAmount();
		for(ItemStack item : evt.getInventory().getMatrix()){
			if(item != null && item.getType() != Material.AIR && Utils.isUnrenewable(item)){
				if(amt == 0) destroyed.add(item);
				ItemStack stdItem = Utils.standardize(item);
				stdItem.setAmount(output.getAmount());
				if(stdItem.equals(output)){
					amt -= item.getAmount();
					stdItem.setAmount(item.getAmount());
					if(amt < 0){
						stdItem.setAmount(-amt);
						destroyed.add(stdItem);
						amt = 0;
					}
					else if(!stdItem.equals(item) && Utils.isUnrenewableProcess(item, output)){
						if(punishUnrenewableProcess){
							plugin.punish(evt.getWhoClicked().getUniqueId(), item.getType());
						}
						if(preventUnrenewableProcess){
							evt.setCancelled(true);
							return;
						}
					}
				}
				else destroyed.add(item);
			}
		}
		if(!destroyed.isEmpty()){
			if(preventUnrenewableDeath) evt.setCancelled(true);
			for(ItemStack item : destroyed){
				plugin.punish(evt.getWhoClicked().getUniqueId(), item.getType());
				if(saveItems && !preventUnrenewableDeath) plugin.rescueItem(item);
			}
		}
	}
}