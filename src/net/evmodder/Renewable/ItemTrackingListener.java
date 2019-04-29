package net.evmodder.Renewable;

import org.bukkit.GameMode;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.inventory.InventoryPickupItemEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.inventory.ItemStack;
import net.md_5.bungee.api.ChatColor;

public class ItemTrackingListener implements Listener{
	final Renewable plugin;
	final boolean ignoreGM1, supplyGM1;

	ItemTrackingListener(){
		plugin = Renewable.getPlugin();
		ignoreGM1 = plugin.getConfig().getBoolean("creative-mode-ignore", true);
		supplyGM1 = plugin.getConfig().getBoolean("creative-unrenewable-sourcing", false);
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onItemBarf(PlayerDropItemEvent evt){
		if(plugin.getAPI().isUnrenewable(evt.getItemDrop().getItemStack())){
			if(evt.getPlayer().getGameMode() == GameMode.CREATIVE){
				if(supplyGM1){
					if(!plugin.getAPI().deductFromCreativeSupply(evt.getItemDrop().getItemStack())){
						ItemStack item = evt.getItemDrop().getItemStack();
						evt.getPlayer().sendMessage(ChatColor.RED+"Failed attempt to supply item: "
								+ChatColor.GOLD+item.getType()+"x"+item.getAmount()
								+ChatColor.RED+" from creative-supply-depot");
					}
				}
				else if(ignoreGM1) return;
			}
			plugin.getLogger().fine("flagging item drop");
			ItemStack flaggedItem = evt.getItemDrop().getItemStack();
			flaggedItem = ItemTaggingUtil.setLastPlayerInContact(flaggedItem, evt.getPlayer().getUniqueId());
			evt.getItemDrop().setItemStack(flaggedItem);
		}
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onItemPickup(EntityPickupItemEvent evt){
		if(evt.getEntityType() == EntityType.PLAYER){
			ItemStack flaggedItem = evt.getItem().getItemStack();
			if(ItemTaggingUtil.getLastPlayerInContact(flaggedItem) != null){
				plugin.getLogger().fine("unflagging item drop entity pickup");
				flaggedItem = ItemTaggingUtil.unflag(flaggedItem);
				evt.getItem().setItemStack(flaggedItem);
				evt.setCancelled(true);//Otherwise updates to evt.getItem() are ignored
			}
		}
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onItemPickup(InventoryPickupItemEvent evt){
		ItemStack flaggedItem = evt.getItem().getItemStack();
		if(ItemTaggingUtil.getLastPlayerInContact(flaggedItem) != null){
			plugin.getLogger().fine("unflagging item drop inventory pickup");
			flaggedItem = ItemTaggingUtil.unflag(flaggedItem);
			evt.getItem().setItemStack(flaggedItem);
		}
	}
}