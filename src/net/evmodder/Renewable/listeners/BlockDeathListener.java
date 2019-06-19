package net.evmodder.Renewable.listeners;

import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Directional;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.BlockFormEvent;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import net.evmodder.EvLib.extras.TypeUtils;
import net.evmodder.Renewable.Renewable;
import net.evmodder.Renewable.RenewableAPI;

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

	@EventHandler(priority = EventPriority.MONITOR)
	public void onBlockPhysics(BlockPhysicsEvent evt){
		if(!evt.isCancelled() && evt.getChangedType() == evt.getBlock().getType()){
			BlockFace fragileDirection = TypeUtils.getFragileFace(evt.getChangedType(), getFacing(evt.getBlock()));
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

	@EventHandler(priority = EventPriority.MONITOR)
	public void onBlockBurn(BlockBurnEvent evt){
		plugin.getLogger().info("Burn at "+evt.getBlock().getX()+","+evt.getBlock().getY()+","+evt.getBlock().getZ()
				+": "+evt.getBlock().getType());//TODO
		if(!evt.isCancelled() && plugin.getAPI().isUnrenewable(evt.getBlock().getState())){
			plugin.getAPI().punish(null, evt.getBlock().getType());
			if(saveItems) plugin.getAPI().rescueItem(RenewableAPI.getUnewnewableItemForm(evt.getBlock().getState()));
		}
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onOverwrite(BlockFormEvent evt){//For lava->obby
		//plugin.getLogger().fine("Form at "+evt.getBlock().getLocation().toString()
		//		+": "+evt.getBlock().getType());
		if(!evt.isCancelled() && plugin.getAPI().isUnrenewable(evt.getBlock().getState())){
			plugin.getAPI().punish(null, evt.getBlock().getType());//TODO
			if(saveItems)
				plugin.getAPI().rescueItem(RenewableAPI.getUnewnewableItemForm(evt.getBlock().getState()));
		}
	}

	@EventHandler(priority = EventPriority.MONITOR)//TODO: this
	public void onBlockBelowEtcBreak(BlockPhysicsEvent evt){//Example: breaking dirt under a dead_bush

	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onBlockExplode(BlockExplodeEvent evt){
		plugin.getLogger().info("Explode at "+evt.getBlock().getX()+","+evt.getBlock().getY()+","+evt.getBlock().getZ()
				+": "+evt.getBlock().getType());
		if(!evt.isCancelled() && plugin.getAPI().isUnrenewable(evt.getBlock().getState())){
			plugin.getAPI().punish(null, evt.getBlock().getType());//TODO
			if(saveItems) plugin.getAPI().rescueItem(RenewableAPI.getUnewnewableItemForm(evt.getBlock().getState()));
		}
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onEntityExplode(EntityExplodeEvent evt){
		if(!evt.isCancelled())
		for(Block block : evt.blockList()){
			if(plugin.getAPI().isUnrenewable(block.getState())){
				plugin.getLogger().info("Explode at "+block.getX()+","+block.getY()+","+block.getZ()+": "+block.getType());
				plugin.getAPI().punish(null, block.getType());//TODO
				if(saveItems) plugin.getAPI().rescueItem(RenewableAPI.getUnewnewableItemForm(block.getState()));
			}
		}
	}
	//TODO: detect portal generation (overwrites blocks)
}