package net.evmodder.Renewable.listeners;

import org.bukkit.Material;
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
import net.evmodder.Renewable.TaggingUtil;
import net.evmodder.Renewable.Renewable;

public class ItemDeathListener implements Listener{
	final private Renewable pl;
	final private boolean DO_ITEM_RESCUE;
	final private int FALLING_BLOCK_LIFE_LIMIT = 600;

	public ItemDeathListener(){
		pl = Renewable.getPlugin();
		DO_ITEM_RESCUE = pl.getConfig().getBoolean("rescue-items", true);

		try{
			@SuppressWarnings("unchecked")
			Class<? extends Event> clazz = (Class<? extends Event>)
				Class.forName("com.destroystokyo.paper.event.entity.EntityRemoveFromWorldEvent");
			pl.getServer().getPluginManager().registerEvent(clazz, this, EventPriority.MONITOR, new EventExecutor(){
				@Override public void execute(Listener listener, Event event){
					final Entity entity = ((EntityEvent)event).getEntity();
					final int voidThreshold = entity.getWorld().getEnvironment() == Environment.NORMAL ? -127 : -63;
					if(entity instanceof Item && entity.getLocation().getY() < voidThreshold && pl.getAPI().isUnrenewable(((Item)entity).getItemStack())){
						final ItemStack item = ((Item)entity).getItemStack();
						pl.getLogger().info("Item fell into void: "+TextUtils.locationToString(entity.getLocation()));
						pl.getAPI().punishDestroyed(TaggingUtil.getLastPlayerInContact(item), item.getType());
						if(DO_ITEM_RESCUE) pl.getAPI().rescueItem(item);
					}
					// Putting there here (instead of BlockDeathListener) for simplicity
					// Note1: If a falling block is alive for >600 ticks, it will drop as an item
					// Note2: FallingBlocks drop an item when falling into the void, so to avoid double-rescuing the item we ignore that case here
					else if(entity instanceof FallingBlock && entity.getTicksLived() < FALLING_BLOCK_LIFE_LIMIT && entity.getLocation().getY() > voidThreshold){
						final BlockData blockData = ((FallingBlock)entity).getBlockData();
						if(blockData.getMaterial() == Material.SUSPICIOUS_GRAVEL || blockData.getMaterial() == Material.SUSPICIOUS_SAND){
						//TODO: this is much more elegant, but aparently getDropItem() returns true (even though it shouldn't)
//						final FallingBlock fallingBlock = (FallingBlock)entity;
//						if((entity.getLocation().getY() < voidThreshold || !fallingBlock.getDropItem())
//								&& plugin.getAPI().isUnrenewable(fallingBlock.getBlockData())){
							pl.getLogger().info("FallingBlock voided/removed: "+TextUtils.locationToString(entity.getLocation()));
							pl.getLogger().info("ticks lived: "+entity.getTicksLived());
							pl.getAPI().punishDestroyed(TaggingUtil.getLastPlayerInContact(entity), blockData.getMaterial());//TODO
							if(DO_ITEM_RESCUE) pl.getAPI().rescueItem(new ItemStack(blockData.getMaterial()));
						}
					}
				}
			}, pl);
		}
		catch(ClassNotFoundException e){}
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void itemDespawnEvent(ItemDespawnEvent evt){
		if(pl.getAPI().isUnrenewable(evt.getEntity().getItemStack())){
			ItemStack item = evt.getEntity().getItemStack();
			pl.getLogger().info("Item Despawn: "+evt.getEntity().getLocation().toString());
			pl.getAPI().punishDestroyed(TaggingUtil.getLastPlayerInContact(item), item.getType());
			if(DO_ITEM_RESCUE) pl.getAPI().rescueItem(item);
		}
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void toolBreakEvent(PlayerItemBreakEvent evt){
		if(pl.getAPI().isUnrenewable(evt.getBrokenItem())){
			pl.getLogger().info("Tool broken: "+evt.getBrokenItem().getType());
			pl.getAPI().punishDestroyed(evt.getPlayer().getUniqueId(), evt.getBrokenItem().getType());
			if(DO_ITEM_RESCUE) pl.getAPI().rescueItem(evt.getBrokenItem());
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
		if(evt.getEntity() instanceof Item && pl.getAPI().isUnrenewable(((Item)evt.getEntity()).getItemStack())){
			ItemStack item = ((Item)evt.getEntity()).getItemStack();
			pl.getLogger().fine("Misc item damage event");
			pl.getAPI().punishDestroyed(TaggingUtil.getLastPlayerInContact(item), item.getType());
			if(DO_ITEM_RESCUE) pl.getAPI().rescueItem(item);
			evt.setCancelled(true);
			evt.getEntity().remove();
		}
	}
}
