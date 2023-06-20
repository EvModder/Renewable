package net.evmodder.Renewable.listeners;

import java.util.UUID;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.type.SculkShrieker;
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
	final private Renewable plugin;
	final private boolean saveItems, normalizeRescuedItems, ignoreGM1, supplyGM1;
	final private boolean preventUnrenewableProcess, punishUnrenewableProcess;
	final private int maxOreDrops;

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

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onBlockMine(BlockBreakEvent evt){
		if(!plugin.getAPI().isUnrenewable(evt.getBlock().getState())) return;
		plugin.getLogger().fine("mined unrenewable block");
		final Block block = evt.getBlock();

		if(evt.getPlayer().getGameMode() == GameMode.CREATIVE && (supplyGM1 || ignoreGM1)){
			if(supplyGM1){
				if(plugin.getAPI().addToCreativeSupply(block.getType()) != null){
					evt.getPlayer().sendMessage(ChatColor.RED+"Failed attempt to add item:"
							+ChatColor.GOLD+block.getType()+ChatColor.RED+" to creative-supply-depot");
					block.getWorld().dropItem(block.getLocation(), RenewableAPI.getUnewnewableItemForm(block.getState()));//opt1
					//evt.setCancelled(true);//opt2
				}
			}
			return;
		}
		final UUID uuid = evt.getPlayer().getUniqueId();
		
		if(block.getType() == Material.SCULK_SHRIEKER && ((SculkShrieker)block.getBlockData()).isCanSummon()){
			if(saveItems) plugin.getAPI().rescueItem(RenewableAPI.getUnewnewableItemForm(block.getState()));
			plugin.getAPI().punish(uuid, block.getType());
			return;
		}

		ItemStack tool = evt.getPlayer().getInventory().getItemInMainHand();
		if(tool == null) tool = new ItemStack(Material.AIR);
		final int silkLvl = tool.containsEnchantment(Enchantment.SILK_TOUCH) ? tool.getEnchantmentLevel(Enchantment.SILK_TOUCH) : 0;

		if(RenewableAPI.willDropSelf(block.getType(), tool.getType(), silkLvl)) return;
		plugin.getLogger().info("won't drop itself: "+block.getType());

		//If the mine event results in the proper item being dropped
		final ItemStack item = RenewableAPI.getUnewnewableItemForm(block.getState());
		boolean stdMatch = false;
		for(ItemStack drop : block.getDrops(tool)){
			if(plugin.getAPI().isUnrenewable(drop)){
				plugin.getLogger().info("Drop for tool: "+tool.getType()+": "+drop.getType());
				if(!plugin.getAPI().isUnrenewableProcess(item, drop)) return;
				if(normalizeRescuedItems && !stdMatch){
					if(plugin.getAPI().sameWhenStandardized(drop, item)) stdMatch = true; // Side effect: rescuedParts
				}
			}
		}
		plugin.getLogger().info("drops something not from same set");

		switch(block.getType()){
			case DIAMOND_ORE:
			case GOLD_ORE:
			case IRON_ORE:
			case COPPER_ORE:
				if(normalizeRescuedItems && evt.isDropItems()){
					if(punishUnrenewableProcess) plugin.getAPI().punish(uuid, block.getType());
					if(preventUnrenewableProcess) evt.setCancelled(true);
					else{
						if(block.getType() == Material.DIAMOND_ORE) listenForOreDrop(uuid, Material.DIAMOND, block.getLocation(), maxOreDrops);//4
						else if(block.getType() == Material.GOLD_ORE) listenForOreDrop(uuid, Material.RAW_GOLD, block.getLocation(), maxOreDrops);//4
						else if(block.getType() == Material.IRON_ORE) listenForOreDrop(uuid, Material.RAW_IRON, block.getLocation(), maxOreDrops);//4
						else if(block.getType() == Material.COPPER_ORE) listenForOreDrop(uuid, Material.RAW_COPPER, block.getLocation(), maxOreDrops+16);//20
					}
					return;
				}
			default:
				plugin.getLogger().fine("saving block: "+block.getType());
				if(stdMatch){
					if(punishUnrenewableProcess) plugin.getAPI().punish(uuid, block.getType());
					if(preventUnrenewableProcess) evt.setCancelled(true); // Cancel mine event ONLY if won't be saved otherwise
				}
				else{
					if(saveItems) plugin.getAPI().rescueItem(RenewableAPI.getUnewnewableItemForm(block.getState()));
					plugin.getAPI().punish(uuid, block.getType());
				}
		}
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
			final int need = maxOreDrops - dropListener.numOreDrops;
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