package Evil_Code_Renewable.listeners;

import java.util.UUID;
import org.bukkit.Material;
import org.bukkit.SkullType;
import org.bukkit.block.BlockState;
import org.bukkit.block.Skull;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import Evil_Code_Renewable.Renewable;
import Evil_Code_Renewable.Utils;

public class BlockPlaceListener implements Listener{
	Renewable plugin;
	UUID badPlayer;
	boolean saveItems;
	Listener spawnListener;

	public BlockPlaceListener(){
		plugin = Renewable.getPlugin();
		saveItems = Renewable.getPlugin().getConfig().getBoolean("rescue-items");
	}

	@SuppressWarnings("deprecation")
	@EventHandler(priority = EventPriority.MONITOR)
	public void onBlockPlace(BlockPlaceEvent evt){
		if(evt.isCancelled()) return;

		BlockState block = evt.getBlockReplacedState();
		if(evt.getBlock().getType() == Material.SOIL || evt.getBlock().getType() == Material.GRASS_PATH){
			//If killing Podzol or Coarse-dirt
			if(block.getType() == Material.DIRT && block.getRawData() != 0){
				if(saveItems){
					plugin.rescueItem(Utils.getUnewnewableItemForm(block));
				}
				plugin.punish(evt.getPlayer().getUniqueId(), block.getType());
			}
		}
		else if(Utils.isUnrenewable(block)){
			if(saveItems){
				plugin.rescueItem(Utils.getUnewnewableItemForm(block));
			}
			plugin.punish(evt.getPlayer().getUniqueId(), block.getType());
		}

		else if(evt.getBlock().getState() instanceof Skull &&
				((Skull)evt.getBlock().getState()).getSkullType() == SkullType.WITHER){

			badPlayer = evt.getPlayer().getUniqueId();
			plugin.getServer().getPluginManager().registerEvents(spawnListener = new Listener(){
				@EventHandler(priority = EventPriority.MONITOR)
				public void onWitherSpawn(EntitySpawnEvent evt){
					if(evt.getEntityType() == EntityType.WITHER && !evt.isCancelled()){
						plugin.punish(badPlayer, Material.SOUL_SAND);
						if(saveItems) plugin.rescueItem(new ItemStack(Material.SOUL_SAND, 4));
					}
				}
			}, plugin);
			new BukkitRunnable(){@Override public void run() {
				HandlerList.unregisterAll(spawnListener);
			}}.runTaskLater(plugin, 2);
		}
	}
}