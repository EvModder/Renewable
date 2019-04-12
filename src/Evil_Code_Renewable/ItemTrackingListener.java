package Evil_Code_Renewable;

import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.inventory.InventoryPickupItemEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.inventory.ItemStack;

public class ItemTrackingListener implements Listener{
	@EventHandler(priority = EventPriority.MONITOR)
	public void onItemBarf(PlayerDropItemEvent evt){
		ItemStack flaggedItem = evt.getItemDrop().getItemStack();
		flaggedItem = ItemTaggingUtil.setLastPlayerInContact(flaggedItem, evt.getPlayer().getUniqueId());
		evt.getItemDrop().setItemStack(flaggedItem);
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onItemPickup(EntityPickupItemEvent evt){
		if(evt.getEntityType() == EntityType.PLAYER){
			ItemStack flaggedItem = evt.getItem().getItemStack();
			flaggedItem = ItemTaggingUtil.unflag(flaggedItem);
			evt.getItem().setItemStack(flaggedItem);
		}
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onItemPickup(InventoryPickupItemEvent evt){
		ItemStack flaggedItem = evt.getItem().getItemStack();
		flaggedItem = ItemTaggingUtil.unflag(flaggedItem);
		evt.getItem().setItemStack(flaggedItem);
	}
}