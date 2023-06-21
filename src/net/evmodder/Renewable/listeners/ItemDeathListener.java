package net.evmodder.Renewable.listeners;

import org.bukkit.World.Environment;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Entity;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Item;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityEvent;
import org.bukkit.event.entity.ItemDespawnEvent;
import org.bukkit.event.player.PlayerItemBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.EventExecutor;
import net.evmodder.EvLib.extras.TextUtils;
import net.evmodder.Renewable.ItemTaggingUtil;
import net.evmodder.Renewable.Renewable;
import net.evmodder.Renewable.RenewableAPI;

public class ItemDeathListener implements Listener{
	final Renewable plugin;
	final boolean saveItems;

	public ItemDeathListener(){
		plugin = Renewable.getPlugin();
		saveItems = Renewable.getPlugin().getConfig().getBoolean("rescue-items", true);

		try{
			@SuppressWarnings("unchecked")
			Class<? extends Event> clazz = (Class<? extends Event>)
				Class.forName("com.destroystokyo.paper.event.entity.EntityRemoveFromWorldEvent");
			plugin.getServer().getPluginManager().registerEvent(clazz, this, EventPriority.MONITOR, new EventExecutor(){
				@Override public void execute(Listener listener, Event event){
					final Entity entity = ((EntityEvent)event).getEntity();
					final int voidThreshold = entity.getWorld().getEnvironment() == Environment.NORMAL ? -127 : -63;
					if(entity instanceof Item && entity.getLocation().getY() < voidThreshold && plugin.getAPI().isUnrenewable(((Item)entity).getItemStack())){
						final ItemStack item = ((Item)entity).getItemStack();
						plugin.getLogger().info("Item fell into void: "+TextUtils.locationToString(entity.getLocation()));
						plugin.getAPI().punish(ItemTaggingUtil.getLastPlayerInContact(item), item.getType());
						if(saveItems) plugin.getAPI().rescueItem(item);
					}
					// Putting there here (instead of BlockDeathListener) for simplicity
					else if(entity instanceof FallingBlock && (entity.getLocation().getY() < voidThreshold || !((FallingBlock)entity).getDropItem())
							&& plugin.getAPI().isUnrenewable(((FallingBlock)entity).getBlockData())){
						plugin.getLogger().info("FallingBlock voided/removed: "+TextUtils.locationToString(entity.getLocation()));
						final BlockData block = ((FallingBlock)entity).getBlockData();
						plugin.getAPI().punish(null, block.getMaterial());//TODO
						if(saveItems) plugin.getAPI().rescueItem(RenewableAPI.getUnewnewableItemForm(block));
					}
				}
			}, plugin);
		}
		catch(ClassNotFoundException e){}
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void itemDespawnEvent(ItemDespawnEvent evt){
		if(plugin.getAPI().isUnrenewable(evt.getEntity().getItemStack())){
			ItemStack item = evt.getEntity().getItemStack();
			plugin.getLogger().fine("Item Despawn: "+evt.getEntity().getLocation().toString());
			plugin.getAPI().punish(ItemTaggingUtil.getLastPlayerInContact(item), item.getType());
			if(saveItems) plugin.getAPI().rescueItem(item);
			evt.getEntity().remove();
		}
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void toolBreakEvent(PlayerItemBreakEvent evt){
		if(plugin.getAPI().isUnrenewable(evt.getBrokenItem())){
			plugin.getLogger().fine("Tool broken: "+evt.getBrokenItem().getType());
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


	//All damage except despawn & void:
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onItemMiscDamage(EntityDamageEvent evt){
		if(evt.getEntity() instanceof Item && plugin.getAPI().isUnrenewable(((Item)evt.getEntity()).getItemStack())){
			ItemStack item = ((Item)evt.getEntity()).getItemStack();
			plugin.getLogger().fine("Misc item damage event");
			plugin.getAPI().punish(ItemTaggingUtil.getLastPlayerInContact(item), item.getType());
			if(saveItems) plugin.getAPI().rescueItem(item);
			evt.setCancelled(true);
			evt.getEntity().remove();
		}
	}
}
