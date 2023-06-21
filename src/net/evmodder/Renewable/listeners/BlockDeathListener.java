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
import net.evmodder.EvLib.extras.TextUtils;

public class BlockDeathListener implements Listener{
	final Renewable plugin;
	boolean saveItems;
	
	public BlockDeathListener(){
		plugin = Renewable.getPlugin();
		saveItems = Renewable.getPlugin().getConfig().getBoolean("rescue-items");
	}

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
						plugin.getLogger().info("dead_bush lost support");
						plugin.getAPI().punish(null, Material.DEAD_BUSH);//TODO
						if(saveItems) plugin.getAPI().rescueItem(RenewableAPI.getUnewnewableItemForm(evt.getBlock().getState()));
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
					case GRASS:
					case TALL_GRASS:
					case FERN:
					case LARGE_FERN:
					case VINE:
					case GLOW_LICHEN:
					case HANGING_ROOTS:
					case WARPED_ROOTS:
					case CRIMSON_ROOTS:
					case NETHER_SPROUTS:
						plugin.getLogger().info("sus sand/gravel lost support");
						final ItemStack hiddenItem = ((BrushableBlock)evt.getBlock()).getItem();
						if(plugin.getAPI().isUnrenewable(hiddenItem)){
							plugin.getAPI().punish(null, hiddenItem.getType());//TODO
							if(saveItems) plugin.getAPI().rescueItem(hiddenItem);
						}
						//FallingBlock death listening is actually done in ItemDeathListener
					default:
						return;
				}
			default:
				return;
			
		}
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onBlockBurn(BlockBurnEvent evt){
		if(plugin.getAPI().isUnrenewable(evt.getBlock().getBlockData())){
			plugin.getLogger().info("Burn at "+TextUtils.locationToString(evt.getBlock().getLocation())+": "+evt.getBlock().getType());
			plugin.getAPI().punish(null, evt.getBlock().getType());//TODO
			if(saveItems) plugin.getAPI().rescueItem(RenewableAPI.getUnewnewableItemForm(evt.getBlock().getState()));
		}
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onOverwrite(BlockFormEvent evt){//For lava->obby, might not be necessary anymore
		if(plugin.getAPI().isUnrenewable(evt.getBlock().getBlockData())){
			plugin.getLogger().info("Form at "+TextUtils.locationToString(evt.getBlock().getLocation())+": "+evt.getBlock().getType());
			plugin.getAPI().punish(null, evt.getBlock().getType());//TODO
			if(saveItems) plugin.getAPI().rescueItem(RenewableAPI.getUnewnewableItemForm(evt.getBlock().getState()));
		}
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onBlockExplode(BlockExplodeEvent evt){
		if(plugin.getAPI().isUnrenewable(evt.getBlock().getBlockData())){
			plugin.getLogger().info("Explode at "+TextUtils.locationToString(evt.getBlock().getLocation())+": "+evt.getBlock().getType());
			plugin.getAPI().punish(null, evt.getBlock().getType());//TODO
			if(saveItems) plugin.getAPI().rescueItem(RenewableAPI.getUnewnewableItemForm(evt.getBlock().getState()));
		}
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onEntityExplode(EntityExplodeEvent evt){
		for(Block block : evt.blockList()){
			if(plugin.getAPI().isUnrenewable(block.getBlockData())){
				plugin.getLogger().info("Explode at "+TextUtils.locationToString(block.getLocation())+": "+block.getType());
				plugin.getAPI().punish(null, block.getType());//TODO
				if(saveItems) plugin.getAPI().rescueItem(RenewableAPI.getUnewnewableItemForm(block.getState()));
			}
		}
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onPortalCreate(PortalCreateEvent evt){
		for(BlockState newState : evt.getBlocks()){
			final Block oldBlock = newState.getBlock();
			if(plugin.getAPI().isUnrenewable(oldBlock.getBlockData())){
				plugin.getLogger().info("Portal at "+TextUtils.locationToString(oldBlock.getLocation())+": "+oldBlock.getType());
				plugin.getAPI().punish(evt.getEntity().getUniqueId(), oldBlock.getType());
				if(saveItems) plugin.getAPI().rescueItem(RenewableAPI.getUnewnewableItemForm(oldBlock.getState()));
			}
		}
	}
}