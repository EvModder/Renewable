package net.evmodder.Renewable.listeners;

import java.util.UUID;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import net.evmodder.Renewable.Renewable;
import net.evmodder.Renewable.RenewableAPI;
import net.md_5.bungee.api.ChatColor;

public class BlockPlaceListener implements Listener{
	final Renewable plugin;
	final boolean saveItems, normalizeRescuedItems, preventUnrenewableProcess, punishUnrenewableProcess,
					ignoreGM1, supplyGM1;
	final boolean RENEWABLE_MOBS;

	public BlockPlaceListener(){
		plugin = Renewable.getPlugin();
		saveItems = Renewable.getPlugin().getConfig().getBoolean("rescue-items");
		normalizeRescuedItems = plugin.getConfig().getBoolean("standardize-rescued-items", true);
		ignoreGM1 = plugin.getConfig().getBoolean("creative-mode-ignore", true);
		supplyGM1 = plugin.getConfig().getBoolean("creative-unrenewable-supply", false);
		preventUnrenewableProcess = plugin.getConfig().getBoolean("prevent-irreversible-process", true);
		punishUnrenewableProcess = plugin.getConfig().getBoolean("punish-for-irreversible-process", true);
		RENEWABLE_MOBS =  plugin.getConfig().getBoolean("renewable-mob-drops", false);
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onBlockPlace(BlockPlaceEvent evt){
		if(evt.isCancelled()) return;
		if(evt.getPlayer().getGameMode() == GameMode.CREATIVE && (supplyGM1 || ignoreGM1) &&
				plugin.getAPI().isUnrenewable(evt.getBlockPlaced().getState())){
			if(supplyGM1){
				if(!plugin.getAPI().deductFromCreativeSupply(evt.getItemInHand().getType())){
					evt.getPlayer().sendMessage(ChatColor.RED+"Failed attempt to supply item:"
							+ChatColor.GOLD+evt.getItemInHand().getType()+ChatColor.RED+" from creative-supply-depot");
					evt.setCancelled(true);
				}
			}
			return;
		}

		if(plugin.getAPI().isUnrenewable(evt.getBlockReplacedState())){
			ItemStack oldBlock = RenewableAPI.getUnewnewableItemForm(evt.getBlockReplacedState());
			ItemStack newBlock = RenewableAPI.getUnewnewableItemForm(evt.getBlockPlaced().getState());
			if(plugin.getAPI().isUnrenewableProcess(oldBlock, newBlock)){
				if(plugin.getAPI().sameWhenStandardizedIgnoreAmt(oldBlock, newBlock)){
					plugin.getLogger().info("[PlaceBlock] irreversible process "+
							oldBlock.getType()+" -> "+newBlock.getType());
					//Assumption: We can standardize back to oldBlock from newBlock
					if(punishUnrenewableProcess) plugin.getAPI().punish(evt.getPlayer().getUniqueId(), oldBlock.getType());
					if(preventUnrenewableProcess) evt.setCancelled(true);
				}
				else{
					plugin.getLogger().info("[PlaceBlock] flat out killed");
					if(evt.getPlayer().getGameMode() == GameMode.CREATIVE && (supplyGM1 || ignoreGM1)){
						if(supplyGM1){
							if(!plugin.getAPI().addToCreativeSupply(oldBlock.getType())){
								evt.getPlayer().sendMessage(ChatColor.RED+"Failed attempt to add item:"
								+ChatColor.GOLD+oldBlock.getType()+ChatColor.RED+" to creative-supply-depot");
								evt.setCancelled(true);
							}
						}
						return;
					}
					if(saveItems) plugin.getAPI().rescueItem(oldBlock);
					else if(preventUnrenewableProcess) evt.setCancelled(true);
					plugin.getAPI().punish(evt.getPlayer().getUniqueId(), oldBlock.getType());
				}
			}
		}
		else if(evt.getBlock().getType() == Material.WITHER_SKELETON_SKULL){
			listenForWitherSpawn(evt.getPlayer().getUniqueId(), evt.getBlock().getLocation());
		}
	}

	void listenForWitherSpawn(final UUID badPlayer, final Location loc){
		final Listener spawnListener = new Listener(){
			@EventHandler(priority = EventPriority.MONITOR)
			public void onWitherSpawn(EntitySpawnEvent evt){
				if(evt.getEntityType() == EntityType.WITHER && !evt.isCancelled()
						&& evt.getLocation().distanceSquared(loc) < 10){
					plugin.getAPI().punish(badPlayer, Material.SOUL_SAND);
					if(RENEWABLE_MOBS && saveItems) plugin.getAPI().rescueItem(new ItemStack(Material.SOUL_SAND, 4));
				}
			}
		};
		plugin.getServer().getPluginManager().registerEvents(spawnListener, plugin);
		new BukkitRunnable(){@Override public void run(){
			HandlerList.unregisterAll(spawnListener);
		}}.runTaskLater(plugin, 2);
	}
}