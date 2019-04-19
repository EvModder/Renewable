package Evil_Code_Renewable.listeners;

import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.ItemDespawnEvent;
import org.bukkit.event.player.PlayerItemBreakEvent;
import org.bukkit.inventory.ItemStack;
import Evil_Code_Renewable.Renewable;
import Evil_Code_Renewable.ItemTaggingUtil;

public class ItemDeathListener implements Listener{
	final Renewable plugin;
	final boolean saveItems;

	public ItemDeathListener(){
		plugin = Renewable.getPlugin();
		saveItems = Renewable.getPlugin().getConfig().getBoolean("rescue-items");
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void itemDespawnEvent(ItemDespawnEvent evt){
		if(!evt.isCancelled() && plugin.getAPI().isUnrenewable(evt.getEntity().getItemStack())){
			ItemStack item = evt.getEntity().getItemStack();
			plugin.getLogger().info("Item Despawn: "+evt.getEntity().getLocation().toString());
			plugin.getAPI().punish(ItemTaggingUtil.getLastPlayerInContact(item), item.getType());
			if(saveItems) plugin.getAPI().rescueItem(item);
			evt.getEntity().remove();
		}
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void toolBreakEvent(PlayerItemBreakEvent evt){
		if(plugin.getAPI().isUnrenewable(evt.getBrokenItem())){
			plugin.getLogger().info("Tool broken");
			plugin.getAPI().punish(evt.getPlayer().getUniqueId(), evt.getBrokenItem().getType());
			if(saveItems) plugin.getAPI().rescueItem(evt.getBrokenItem());
		}
	}

/*	@EventHandler(priority = EventPriority.MONITOR)//NOTE: This is currently handled in the crafting section
	public void onFireworkExplode(FireworkExplodeEvent evt){//Special case -- Fireworks!
		if(!evt.isCancelled() && evt.getEntity().getFireworkMeta().hasEffects()
				&& evt.getEntity().getFireworkMeta().getEffects().get(0).hasTrail()){
			plugin.getLogger().info("Detonated firework with built-in diamond!");
		}
	}*/


	//Includes damage from: explosion, void
	@EventHandler(priority = EventPriority.MONITOR)
	public void onItemMiscDamage(EntityDamageEvent evt){
		if(!evt.isCancelled() && evt.getEntity() instanceof Item
				&& plugin.getAPI().isUnrenewable(((Item)evt.getEntity()).getItemStack()))
		{
			ItemStack item = ((Item)evt.getEntity()).getItemStack();
			plugin.getLogger().info("Misc item damage event");
			plugin.getAPI().punish(ItemTaggingUtil.getLastPlayerInContact(item), item.getType());
			if(saveItems) plugin.getAPI().rescueItem(item);
			evt.setCancelled(true);
			evt.getEntity().remove();
		}
	}
}
