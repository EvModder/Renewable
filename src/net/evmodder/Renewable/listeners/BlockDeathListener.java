package net.evmodder.Renewable.listeners;

import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.Directional;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.BlockFormEvent;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.world.PortalCreateEvent;
import net.evmodder.Renewable.Renewable;
import net.evmodder.Renewable.RenewableAPI;
import net.evmodder.EvLib.extras.TextUtils;
import net.evmodder.Renewable.JunkUtils;

public class BlockDeathListener implements Listener{
	final Renewable plugin;
	boolean saveItems;
	
	public BlockDeathListener(){
		plugin = Renewable.getPlugin();
		saveItems = Renewable.getPlugin().getConfig().getBoolean("rescue-items");
	}	
	public static BlockFace getFacing(Block block){
		return block.getBlockData() instanceof Directional ? ((Directional) block.getBlockData()).getFacing() : null;
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onBlockPhysics(BlockPhysicsEvent evt){
		if(evt.getChangedType() == evt.getBlock().getType()){
			BlockFace fragileDirection = JunkUtils.getFragileFace(evt.getBlock().getBlockData(), getFacing(evt.getBlock()));
			if(fragileDirection != null && evt.getBlock().getRelative(fragileDirection).getType().isSolid() == false
					&& plugin.getAPI().isUnrenewable(evt.getBlock().getState()))
			{
				plugin.getAPI().punish(null, evt.getBlock().getType());//TODO
				plugin.getLogger().info("Changed Type: "+evt.getChangedType());
				plugin.getLogger().info("Block Type: "+evt.getBlock().getType());
				if(saveItems) plugin.getAPI().rescueItem(RenewableAPI.getUnewnewableItemForm(evt.getBlock().getState()));
			}
		}
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onBlockBurn(BlockBurnEvent evt){
		if(plugin.getAPI().isUnrenewable(evt.getBlock().getState())){
			plugin.getLogger().info("Burn at "+TextUtils.locationToString(evt.getBlock().getLocation())+": "+evt.getBlock().getType());
			plugin.getAPI().punish(null, evt.getBlock().getType());//TODO
			if(saveItems) plugin.getAPI().rescueItem(RenewableAPI.getUnewnewableItemForm(evt.getBlock().getState()));
		}
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onOverwrite(BlockFormEvent evt){//For lava->obby
		if(plugin.getAPI().isUnrenewable(evt.getBlock().getState())){
			plugin.getLogger().info("Form at "+TextUtils.locationToString(evt.getBlock().getLocation())+": "+evt.getBlock().getType());
			plugin.getAPI().punish(null, evt.getBlock().getType());//TODO
			if(saveItems) plugin.getAPI().rescueItem(RenewableAPI.getUnewnewableItemForm(evt.getBlock().getState()));
		}
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onBlockExplode(BlockExplodeEvent evt){
		if(plugin.getAPI().isUnrenewable(evt.getBlock().getState())){
			plugin.getLogger().info("Explode at "+TextUtils.locationToString(evt.getBlock().getLocation())+": "+evt.getBlock().getType());
			plugin.getAPI().punish(null, evt.getBlock().getType());//TODO
			if(saveItems) plugin.getAPI().rescueItem(RenewableAPI.getUnewnewableItemForm(evt.getBlock().getState()));
		}
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onEntityExplode(EntityExplodeEvent evt){
		for(Block block : evt.blockList()){
			if(plugin.getAPI().isUnrenewable(block.getState())){
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
			if(plugin.getAPI().isUnrenewable(oldBlock.getState())){
				plugin.getLogger().info("Portal at "+TextUtils.locationToString(oldBlock.getLocation())+": "+oldBlock.getType());
				plugin.getAPI().punish(evt.getEntity().getUniqueId(), oldBlock.getType());
				if(saveItems) plugin.getAPI().rescueItem(RenewableAPI.getUnewnewableItemForm(oldBlock.getState()));
			}
		}
	}
}