package net.evmodder.Renewable;

import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.inventory.InventoryPickupItemEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public class PlayerTrackingListener implements Listener{
	final private Renewable pl;
	final private boolean ignoreGM1, supplyGM1;

	PlayerTrackingListener(){
		pl = Renewable.getPlugin();
		ignoreGM1 = pl.getConfig().getBoolean("creative-mode-ignore", true);
		supplyGM1 = pl.getConfig().getBoolean("creative-unrenewable-sourcing", false);
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onItemBarf(PlayerDropItemEvent evt){
		if(pl.getAPI().isUnrenewable(evt.getItemDrop().getItemStack())){
			if(evt.getPlayer().getGameMode() == GameMode.CREATIVE){
				if(supplyGM1){
					if(!pl.getAPI().takeFromCreativeSupply(evt.getItemDrop().getItemStack())){
						ItemStack item = evt.getItemDrop().getItemStack();
						evt.getPlayer().sendMessage(ChatColor.RED+"Failed attempt to supply item: "
								+ChatColor.GOLD+item.getType()+"x"+item.getAmount()
								+ChatColor.RED+" from creative-supply-depot");
					}
				}
				else if(ignoreGM1) return;
			}
			pl.getLogger().info("flagging item drop - player barf");
			ItemStack flaggedItem = evt.getItemDrop().getItemStack();
			flaggedItem = TaggingUtil.setLastPlayerInContact(flaggedItem, evt.getPlayer().getUniqueId());
			evt.getItemDrop().setItemStack(flaggedItem);
		}
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onItemPickup(EntityPickupItemEvent evt){
		if(evt.getEntityType() == EntityType.PLAYER){
			ItemStack flaggedItem = evt.getItem().getItemStack();
			if(TaggingUtil.getLastPlayerInContact(flaggedItem) != null){
				pl.getLogger().fine("unflagging item drop - entity pickup");
				flaggedItem = TaggingUtil.unflag(flaggedItem);
				evt.getItem().setItemStack(flaggedItem);
				evt.setCancelled(true);//Otherwise updates to evt.getItem() are ignored
			}
		}
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onItemPickup(InventoryPickupItemEvent evt){
		ItemStack flaggedItem = evt.getItem().getItemStack();
		if(TaggingUtil.getLastPlayerInContact(flaggedItem) != null){
			pl.getLogger().fine("unflagging item drop - inventory pickup");
			flaggedItem = TaggingUtil.unflag(flaggedItem);
			evt.getItem().setItemStack(flaggedItem);
		}
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void playerClickBlock(PlayerInteractEvent evt){
		if(evt.getClickedBlock() != null){
			pl.getLogger().fine("flagging block - clicked");
			TaggingUtil.setLastPlayerInContact(evt.getClickedBlock().getState(), evt.getPlayer().getUniqueId());
		}
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void playerClickEntity(PlayerInteractEntityEvent evt){
		pl.getLogger().fine("flagging entity - right clicked");
		TaggingUtil.setLastPlayerInContact(evt.getRightClicked(), evt.getPlayer().getUniqueId());
	}
}