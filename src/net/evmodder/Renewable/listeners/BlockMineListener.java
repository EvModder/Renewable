package net.evmodder.Renewable.listeners;

import java.util.UUID;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import net.evmodder.Renewable.Renewable;
import net.evmodder.Renewable.RenewableAPI;

public class BlockMineListener implements Listener{
	private Renewable plugin;
	final boolean saveItems, normalizeRescuedItems, ignoreGM1, supplyGM1;
	final boolean preventUnrenewableProcess, punishUnrenewableProcess;
	final int maxOreDrops;

	public BlockMineListener(){
		plugin = Renewable.getPlugin();
		saveItems = plugin.getConfig().getBoolean("rescue-items", true);
		normalizeRescuedItems = plugin.getConfig().getBoolean("standardize-rescued-items", true);
		ignoreGM1 = plugin.getConfig().getBoolean("creative-mode-ignore", true);
		supplyGM1 = plugin.getConfig().getBoolean("creative-unrenewable-sourcing", false);
		preventUnrenewableProcess = plugin.getConfig().getBoolean("prevent-irreversible-process", true);
		punishUnrenewableProcess = plugin.getConfig().getBoolean("punish-for-irreversible-process", true);
		maxOreDrops = plugin.getConfig().getInt("max-fortune-level", 3) + 1;
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onBlockMine(BlockBreakEvent evt){
		if(evt.isCancelled() || !plugin.getAPI().isUnrenewable(evt.getBlock().getState())) return;
		if(evt.getPlayer().getGameMode() == GameMode.CREATIVE && (supplyGM1 || ignoreGM1)){
			if(supplyGM1){
				if(plugin.getAPI().addToCreativeSupply(evt.getBlock().getType()) != null){
					evt.getPlayer().sendMessage(ChatColor.RED+"Failed attempt to add item:"
							+ChatColor.GOLD+evt.getBlock().getType()+ChatColor.RED+" to creative-supply-depot");
					//two options:
					evt.getBlock().getWorld().dropItem(evt.getBlock().getLocation(),
							RenewableAPI.getUnewnewableItemForm(evt.getBlock().getState()));//opt1
					//evt.setCancelled(true);//opt2
				}
			}
			return;
		}
		plugin.getLogger().info("mined unrenewable block");

		ItemStack tool = evt.getPlayer().getInventory().getItemInMainHand();
		int silkLvl = tool == null ? 0 : tool.containsEnchantment(Enchantment.SILK_TOUCH)
									? tool.getEnchantmentLevel(Enchantment.SILK_TOUCH) : 0;

		if(RenewableAPI.willDropSelf(evt.getBlock().getType(),
				tool == null ? Material.AIR : tool.getType(), silkLvl)) return;
		plugin.getLogger().info("won't drop itself");

		//If the mine event results in the proper item being dropped
		ItemStack item = RenewableAPI.getUnewnewableItemForm(evt.getBlock().getState());
		boolean stdMatch = false;
		for(ItemStack drop : evt.getBlock().getDrops(tool)){
			if(plugin.getAPI().isUnrenewable(drop)){
				plugin.getLogger().info("Drop for tool: "+tool.getType()+": "+drop.getType());
				if(!plugin.getAPI().isUnrenewableProcess(item, drop)) return;
				if(normalizeRescuedItems && !stdMatch){
					if(plugin.getAPI().sameWhenStandardized(drop, item)) stdMatch = true;
				}
			}
		}
		plugin.getLogger().info("drops something not from same set");

		UUID uuid = evt.getPlayer().getUniqueId();

		switch(evt.getBlock().getType()){
			case GRAVEL:
				listenForGravelDrop(uuid);
				return;
			case DIAMOND_ORE:
				if(normalizeRescuedItems && evt.isDropItems()){
					if(punishUnrenewableProcess) plugin.getAPI().punish(uuid, evt.getBlock().getType());
					if(preventUnrenewableProcess) evt.setCancelled(true);
					else listenForOreDrop(uuid, Material.DIAMOND, evt.getBlock().getLocation(), maxOreDrops);
					return;
				}
			case NETHER_QUARTZ_ORE:
				if(normalizeRescuedItems && evt.isDropItems()){
					if(punishUnrenewableProcess) plugin.getAPI().punish(uuid, evt.getBlock().getType());
					if(preventUnrenewableProcess) evt.setCancelled(true);
					else listenForOreDrop(uuid, Material.QUARTZ, evt.getBlock().getLocation(), maxOreDrops);
					return;
				}
			default:
				plugin.getLogger().info("saving block...");
				if(stdMatch){
					if(punishUnrenewableProcess) plugin.getAPI().punish(uuid, evt.getBlock().getType());
					//Prevent mine only if won't be saved otherwise
					if(preventUnrenewableProcess) evt.setCancelled(true);
				}
				else{
					if(saveItems) plugin.getAPI().rescueItem(
							RenewableAPI.getUnewnewableItemForm(evt.getBlock().getState()));
					plugin.getAPI().punish(uuid, evt.getBlock().getType());
				}
		}
	}

	void listenForGravelDrop(final UUID playerResponsible){
		plugin.getServer().getPluginManager().registerEvents(new Listener(){
			@EventHandler public void gravelItemDropEvent(ItemSpawnEvent evt){
				if(evt.getEntity().getItemStack().getType() == Material.FLINT){
					if(punishUnrenewableProcess){
						plugin.getAPI().punish(playerResponsible, Material.GRAVEL);
					}
					if(preventUnrenewableProcess){
						evt.getEntity().setItemStack(new ItemStack(Material.GRAVEL));
					}
				}
				HandlerList.unregisterAll(this);
			}
		}, plugin);
	}

	class ListenerWithNum implements Listener{int numOreDrops=0;};

	void listenForOreDrop(final UUID badPlayer, final Material dropType, final Location loc, final int maxDrops){
		final ListenerWithNum dropListener;
		plugin.getServer().getPluginManager().registerEvents(dropListener = new ListenerWithNum(){
			@EventHandler public void diamondItemDropEvent(ItemSpawnEvent evt){
				if(evt.getEntity().getItemStack().getType() == dropType &&
						evt.getEntity().getLocation().distanceSquared(loc) < 10){
					numOreDrops += evt.getEntity().getItemStack().getAmount();
				}
			}
		}, plugin);
		new BukkitRunnable(){@Override public void run() {
			HandlerList.unregisterAll(dropListener);
			int need = maxOreDrops - dropListener.numOreDrops;
			if(need > 0){
				if(saveItems) plugin.getAPI().rescueItem(new ItemStack(dropType, need));
				plugin.getLogger().fine("Didn't get enough "+dropType+" drops, needed "+need+" more items!");
				plugin.getAPI().punish(badPlayer, dropType);
			}
			else if(need < 0) plugin.getLogger().warning("Ore drops exceeded expected maximum of "
									+maxOreDrops+": "+dropListener.numOreDrops
									+"\nPlease double check 'max-fortune-level' in config-Renewable.yml");
		}}.runTaskLater(plugin, 1);
	}
}