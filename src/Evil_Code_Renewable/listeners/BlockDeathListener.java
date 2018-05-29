package Evil_Code_Renewable.listeners;

import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.BlockFormEvent;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import Evil_Code_Renewable.Utils;
import Evil_Code_Renewable.Renewable;

public class BlockDeathListener implements Listener{
	Renewable plugin;
	boolean saveItems;
	
	public BlockDeathListener(){
		plugin = Renewable.getPlugin();
		saveItems = Renewable.getPlugin().getConfig().getBoolean("rescue-items");
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onBlockPhysics(BlockPhysicsEvent evt){
		if(!evt.isCancelled() && evt.getChangedType() == evt.getBlock().getType()
				&& Utils.isFragile(evt.getChangedType())
				&& (evt.getBlock().getY() > 0 && evt.getBlock().getRelative(BlockFace.DOWN).getType().isSolid() == false)
				&& Utils.isUnrenewable(evt.getBlock().getState())){
			plugin.getLogger().info("Changed Type: "+evt.getChangedType());
			plugin.getLogger().info("Block Type: "+evt.getBlock().getType());
			if(saveItems) plugin.rescueItem(Utils.getUnewnewableItemForm(evt.getBlock().getState()));
		}
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onBlockBurn(BlockBurnEvent evt){
		plugin.getLogger().info("Burn at "+evt.getBlock().getX()+","+evt.getBlock().getY()+","+evt.getBlock().getZ()
				+": "+evt.getBlock().getType());//TODO
		if(!evt.isCancelled() && Utils.isUnrenewable(evt.getBlock().getState())){
			plugin.punish(null, evt.getBlock().getType());
			if(saveItems) plugin.rescueItem(Utils.getUnewnewableItemForm(evt.getBlock().getState()));
		}
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onOverwrite(BlockFormEvent evt){//For lava->obby
		plugin.getLogger().info("Form at "+evt.getBlock().getX()+","+evt.getBlock().getY()+","+evt.getBlock().getZ()
				+": "+evt.getBlock().getType());
		if(!evt.isCancelled() && Utils.isUnrenewable(evt.getBlock().getState())){
			plugin.punish(null, evt.getBlock().getType());//TODO
			if(saveItems) plugin.rescueItem(Utils.getUnewnewableItemForm(evt.getBlock().getState()));
		}
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onBlockBelowEtcBreak(BlockPhysicsEvent evt){//Example: breaking dirt under a dead_bush

	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onBlockExplode(BlockExplodeEvent evt){
		plugin.getLogger().info("Explode at "+evt.getBlock().getX()+","+evt.getBlock().getY()+","+evt.getBlock().getZ()
				+": "+evt.getBlock().getType());
		if(!evt.isCancelled() && Utils.isUnrenewable(evt.getBlock().getState())){
			plugin.punish(null, evt.getBlock().getType());//TODO
			if(saveItems) plugin.rescueItem(Utils.getUnewnewableItemForm(evt.getBlock().getState()));
		}
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onEntityExplode(EntityExplodeEvent evt){
		if(!evt.isCancelled())
		for(Block block : evt.blockList()){
			if(Utils.isUnrenewable(block.getState())){
				plugin.punish(null, block.getType());//TODO
				if(saveItems) plugin.rescueItem(Utils.getUnewnewableItemForm(block.getState()));
			}
		}
	}
	//TODO: detect portal generation (overwrites blocks)
}