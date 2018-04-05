package Evil_Code_Renewable.listeners;

import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.BlockFormEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import Evil_Code_Renewable.Renewable;
import Evil_Code_Renewable.Utils;

public class BlockDeathListener implements Listener{
	Renewable plugin;
	boolean saveItems;
	
	public BlockDeathListener(){
		plugin = Renewable.getPlugin();
		saveItems = Renewable.getPlugin().getConfig().getBoolean("rescue-items");
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onBlockBurn(BlockBurnEvent evt){
		plugin.getLogger().info("Burn at "+evt.getBlock().getX()+","+evt.getBlock().getY()+","+evt.getBlock().getZ()
				+": "+evt.getBlock().getType());
		if(!evt.isCancelled() && Utils.isUnrenewable(evt.getBlock().getState())){
			if(saveItems) plugin.rescueItem(Utils.getUnewnewableItemForm(evt.getBlock().getState()));
		}
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onOverwrite(BlockFormEvent evt){//For lava->obby
		plugin.getLogger().info("Form at "+evt.getBlock().getX()+","+evt.getBlock().getY()+","+evt.getBlock().getZ()
				+": "+evt.getBlock().getType());
		if(!evt.isCancelled() && Utils.isUnrenewable(evt.getBlock().getState())){
			if(saveItems) plugin.rescueItem(Utils.getUnewnewableItemForm(evt.getBlock().getState()));
		}
	}
	
	@EventHandler(priority = EventPriority.MONITOR)
	public void onBlockExplode(BlockExplodeEvent evt){
		plugin.getLogger().info("Explode at "+evt.getBlock().getX()+","+evt.getBlock().getY()+","+evt.getBlock().getZ()
				+": "+evt.getBlock().getType());
		if(!evt.isCancelled() && Utils.isUnrenewable(evt.getBlock().getState())){
			if(saveItems) plugin.rescueItem(Utils.getUnewnewableItemForm(evt.getBlock().getState()));
		}
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onEntityExplode(EntityExplodeEvent evt){
		if(!evt.isCancelled())
		for(Block block : evt.blockList()){
			if(Utils.isUnrenewable(block.getState())){
				if(saveItems) plugin.rescueItem(Utils.getUnewnewableItemForm(block.getState()));
			}
		}
	}
	//TODO: detect portal generation (overwrites blocks)
}