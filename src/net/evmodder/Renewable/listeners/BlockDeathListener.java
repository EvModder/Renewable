package net.evmodder.Renewable.listeners;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.BrushableBlock;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.BlockFormEvent;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.world.PortalCreateEvent;
import org.bukkit.inventory.ItemStack;
import net.evmodder.Renewable.Renewable;
import net.evmodder.Renewable.RenewableAPI;
import net.evmodder.Renewable.TaggingUtil;
import net.evmodder.EvLib.TextUtils;

public class BlockDeathListener implements Listener{
	final private Renewable pl;
	final private boolean DO_ITEM_RESCUE;
	
	public BlockDeathListener(){
		pl = Renewable.getPlugin();
		DO_ITEM_RESCUE = Renewable.getPlugin().getConfig().getBoolean("rescue-items");
	}

//	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
//	public void onBlockPhysics(EntityChangeBlockEvent evt){
//		plugin.getLogger().info("change evt block.type: "+evt.getBlock().getType());
//		plugin.getLogger().info("change evt to: "+evt.getTo());
//		plugin.getLogger().info("change evt blockdata.material: "+evt.getBlockData().getMaterial());
//		plugin.getLogger().info("change evt entitytype: "+evt.getEntityType());
//	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onBlockPhysics(BlockPhysicsEvent evt){//See if an unrenewable block has lost its support
		if(evt.getChangedType() != evt.getBlock().getType()) return;
		switch(evt.getChangedType()){
			case DEAD_BUSH:
				switch(evt.getBlock().getRelative(BlockFace.DOWN).getType()){
					// Blocks that can support DEAD_BUSH
					case DIRT:
					case COARSE_DIRT:
					case ROOTED_DIRT:
					case PODZOL:
					case MYCELIUM:
					case GRASS_BLOCK:
					case SAND:
					case RED_SAND:
					case MUD:
					case MOSS_BLOCK:
					case TERRACOTTA:
					case BLACK_TERRACOTTA:
					case BLUE_TERRACOTTA:
					case BROWN_TERRACOTTA:
					case CYAN_TERRACOTTA:
					case GRAY_TERRACOTTA:
					case GREEN_TERRACOTTA:
					case LIGHT_BLUE_TERRACOTTA:
					case LIGHT_GRAY_TERRACOTTA:
					case LIME_TERRACOTTA:
					case MAGENTA_TERRACOTTA:
					case ORANGE_TERRACOTTA:
					case PINK_TERRACOTTA:
					case PURPLE_TERRACOTTA:
					case RED_TERRACOTTA:
					case WHITE_TERRACOTTA:
					case YELLOW_TERRACOTTA:
						return;
					default:
						pl.getLogger().info("dead_bush lost support");
						pl.getAPI().punishDestroyed(TaggingUtil.getLastPlayerInContact(evt.getSourceBlock().getState()), Material.DEAD_BUSH);
						if(DO_ITEM_RESCUE) pl.getAPI().rescueItem(RenewableAPI.getUnewnewableItemForm(evt.getBlock().getState()));
				}
			case SUSPICIOUS_SAND:
			case SUSPICIOUS_GRAVEL:
				switch(evt.getBlock().getRelative(BlockFace.DOWN).getType()){
					// Blocks that cannot support sand/gravel
					case AIR:
					case CAVE_AIR:
					case VOID_AIR:
					case WATER:
					case LAVA:
					case STRUCTURE_VOID:
					case SHORT_GRASS:
					case TALL_GRASS:
					case FERN:
					case LARGE_FERN:
					case VINE:
					case GLOW_LICHEN:
					case HANGING_ROOTS:
					case WARPED_ROOTS:
					case CRIMSON_ROOTS:
					case NETHER_SPROUTS:
						pl.getLogger().info("sus sand/gravel lost support");
						final ItemStack hiddenItem = ((BrushableBlock)evt.getBlock().getState()).getItem();
						if(pl.getAPI().isUnrenewable(hiddenItem)){
							pl.getAPI().punishDestroyed(TaggingUtil.getLastPlayerInContact(evt.getSourceBlock().getState()), hiddenItem.getType());
							if(DO_ITEM_RESCUE) pl.getAPI().rescueItem(hiddenItem);
						}
						//TODO: tag FallingBlock entity
					default:
						return;
				}
			default:
				return;
			
		}
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onBlockBurn(BlockBurnEvent evt){
//		final UUID uuid = TaggingUtil.getLastPlayerInContact(evt.getIgnitingBlock().getState());
		if(pl.getAPI().isUnrenewable(evt.getBlock().getBlockData())){
			pl.getLogger().info("Burn at "+TextUtils.locationToString(evt.getBlock().getLocation())+": "+evt.getBlock().getType());
			pl.getAPI().punishDestroyed(TaggingUtil.getLastPlayerInContact(evt.getBlock().getState()), evt.getBlock().getType());
			if(DO_ITEM_RESCUE) pl.getAPI().rescueItem(RenewableAPI.getUnewnewableItemForm(evt.getBlock().getState()));
		}
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onOverwrite(BlockFormEvent evt){//For lava->obby, might not be necessary anymore
		if(pl.getAPI().isUnrenewable(evt.getBlock().getBlockData())){
			pl.getLogger().info("Form at "+TextUtils.locationToString(evt.getBlock().getLocation())+": "+evt.getBlock().getType());
			pl.getAPI().punishDestroyed(TaggingUtil.getLastPlayerInContact(evt.getBlock().getState()), evt.getBlock().getType());//TODO
			if(DO_ITEM_RESCUE) pl.getAPI().rescueItem(RenewableAPI.getUnewnewableItemForm(evt.getBlock().getState()));
		}
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onBlockExplode(BlockExplodeEvent evt){
		pl.getLogger().info("Explode at "+TextUtils.locationToString(evt.getBlock().getLocation())+": "+evt.getBlock().getType());
		//TODO: for TNT, check if any of the exploded blocks were unrenewable, and ONLY punish if they don't drop in self- item form
//		if(pl.getAPI().isUnrenewable(evt.getBlock().getBlockData())){
//			pl.getAPI().punishDestroyed(TaggingUtil.getLastPlayerInContact(evt.getBlock().getState()), evt.getBlock().getType());
//			if(DO_ITEM_RESCUE) pl.getAPI().rescueItem(RenewableAPI.getUnewnewableItemForm(evt.getBlock().getState()));
//		}
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onEntityExplode(EntityExplodeEvent evt){
		for(Block block : evt.blockList()){
			//TODO: ONLY punish for blocks that don't drop themself as an item
			if(pl.getAPI().isUnrenewable(block.getBlockData())){
				pl.getLogger().info("Explode at "+TextUtils.locationToString(block.getLocation())+": "+block.getType());
				pl.getAPI().punishDestroyed(TaggingUtil.getLastPlayerInContact(evt.getEntity()), block.getType());
				if(DO_ITEM_RESCUE) pl.getAPI().rescueItem(RenewableAPI.getUnewnewableItemForm(block.getState()));
			}
		}
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onPortalCreate(PortalCreateEvent evt){
		for(BlockState newState : evt.getBlocks()){
			final Block oldBlock = newState.getBlock();
			if(pl.getAPI().isUnrenewable(oldBlock.getBlockData())){
				pl.getLogger().info("Portal at "+TextUtils.locationToString(oldBlock.getLocation())+": "+oldBlock.getType());
				pl.getAPI().punishDestroyed(evt.getEntity().getUniqueId(), oldBlock.getType());
				if(DO_ITEM_RESCUE) pl.getAPI().rescueItem(RenewableAPI.getUnewnewableItemForm(oldBlock.getState()));
			}
		}
	}

	//TODO: piston event (deadbush, sus sand)
}