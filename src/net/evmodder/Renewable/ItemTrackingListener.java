package net.evmodder.Renewable;

import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.inventory.InventoryPickupItemEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.inventory.ItemStack;

public class ItemTrackingListener implements Listener{
	Renewable plugin;
	ItemTrackingListener(){plugin = Renewable.getPlugin();}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onItemBarf(PlayerDropItemEvent evt){
		if(plugin.getAPI().isUnrenewable(evt.getItemDrop().getItemStack())){
			plugin.getLogger().info("flagging item drop");
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
				plugin.getLogger().info("unflagging item drop entity pickup");
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
			plugin.getLogger().info("unflagging item drop inventory pickup");
			flaggedItem = ItemTaggingUtil.unflag(flaggedItem);
			evt.getItem().setItemStack(flaggedItem);
		}
	}
}