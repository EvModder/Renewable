package net.evmodder.Renewable.listeners;

import java.util.UUID;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
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
	final private Renewable pl;
	final private boolean DO_ITEM_RESCUE, normalizeRescuedItems, ignoreGM1, supplyGM1;
	final private boolean PREVENT_IRREVERSIBLE_PROCESS;
	final private int maxOreDrops;

	public BlockMineListener(){
		pl = Renewable.getPlugin();
		DO_ITEM_RESCUE = pl.getConfig().getBoolean("rescue-items", true);
		normalizeRescuedItems = pl.getConfig().getBoolean("standardize-rescued-items", true);
		ignoreGM1 = pl.getConfig().getBoolean("creative-mode-ignore", true);
		supplyGM1 = pl.getConfig().getBoolean("creative-unrenewable-sourcing", false);
		PREVENT_IRREVERSIBLE_PROCESS = pl.getConfig().getBoolean("prevent-irreversible-process", true);
		maxOreDrops = pl.getConfig().getInt("max-fortune-level", 3) + 1;
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onBlockMine(BlockBreakEvent evt){
		if(!pl.getAPI().isUnrenewable(evt.getBlock().getBlockData())) return;
		pl.getLogger().fine("mined unrenewable block");
		final Block block = evt.getBlock();

		if(evt.getPlayer().getGameMode() == GameMode.CREATIVE && (supplyGM1 || ignoreGM1)){
			if(supplyGM1){
				if(pl.getAPI().addToCreativeSupply(block.getType()) != null){
					evt.getPlayer().sendMessage(ChatColor.RED+"Failed attempt to add item:"
							+ChatColor.GOLD+block.getType()+ChatColor.RED+" to creative-supply-depot");
					block.getWorld().dropItem(block.getLocation(), RenewableAPI.getUnewnewableItemForm(block.getState()));//opt1
					//evt.setCancelled(true);//opt2
				}
			}
			return;
		}
		final UUID uuid = evt.getPlayer().getUniqueId();

		ItemStack tool = evt.getPlayer().getInventory().getItemInMainHand();
		if(tool == null) tool = new ItemStack(Material.AIR);
		final int SILK_LVL = tool.containsEnchantment(Enchantment.SILK_TOUCH) ? tool.getEnchantmentLevel(Enchantment.SILK_TOUCH) : 0;

		if(pl.getAPI().willDropSelf(block.getBlockData(), tool.getType(), SILK_LVL)) return;
		pl.getLogger().info("won't drop itself: "+block.getType());

		//If the mine event results in the proper item being dropped
		final ItemStack item = RenewableAPI.getUnewnewableItemForm(block.getState());
		boolean stdMatch = false;
		for(ItemStack drop : block.getDrops(tool)){
			if(pl.getAPI().isUnrenewable(drop)){
				pl.getLogger().info("Drop for tool: "+tool.getType()+": "+drop.getType());
				if(!pl.getAPI().isUnrenewableProcess(item, drop)) return;
				if(normalizeRescuedItems && !stdMatch){
					if(pl.getAPI().sameWhenStandardized(drop, item)) stdMatch = true; // Side effect: rescuedParts
				}
			}
		}
		pl.getLogger().info("drops something not from same set");

		switch(block.getType()){
			case DIAMOND_ORE:
			case GOLD_ORE:
			case IRON_ORE:
			case COPPER_ORE:
				if(normalizeRescuedItems && evt.isDropItems()){
					pl.getAPI().punishIrreversible(uuid, block.getType());
					if(PREVENT_IRREVERSIBLE_PROCESS) evt.setCancelled(true);
					else{
						if(block.getType() == Material.DIAMOND_ORE) listenForOreDrop(uuid, Material.DIAMOND, block.getLocation(), maxOreDrops);//4
						else if(block.getType() == Material.GOLD_ORE) listenForOreDrop(uuid, Material.RAW_GOLD, block.getLocation(), maxOreDrops);//4
						else if(block.getType() == Material.IRON_ORE) listenForOreDrop(uuid, Material.RAW_IRON, block.getLocation(), maxOreDrops);//4
						else if(block.getType() == Material.COPPER_ORE) listenForOreDrop(uuid, Material.RAW_COPPER, block.getLocation(), maxOreDrops+16);//20
					}
					return;
				}
			default:
				pl.getLogger().fine("saving block: "+block.getType());
				if(stdMatch){
					pl.getAPI().punishIrreversible(uuid, block.getType());
					if(PREVENT_IRREVERSIBLE_PROCESS) evt.setCancelled(true); // Cancel mine event ONLY if won't be saved otherwise
				}
				else{
					if(DO_ITEM_RESCUE) pl.getAPI().rescueItem(RenewableAPI.getUnewnewableItemForm(block.getState()));
					pl.getAPI().punishDestroyed(uuid, block.getType());
				}
		}
	}

	class ListenerWithNum implements Listener{int numOreDrops=0;};

	void listenForOreDrop(final UUID badPlayer, final Material dropType, final Location loc, final int maxDrops){
		final ListenerWithNum dropListener;
		pl.getServer().getPluginManager().registerEvents(dropListener = new ListenerWithNum(){
			@EventHandler public void diamondItemDropEvent(ItemSpawnEvent evt){
				if(evt.getEntity().getItemStack().getType() == dropType &&
						evt.getEntity().getLocation().distanceSquared(loc) < 10){
					numOreDrops += evt.getEntity().getItemStack().getAmount();
				}
			}
		}, pl);
		new BukkitRunnable(){@Override public void run() {
			HandlerList.unregisterAll(dropListener);
			final int need = maxOreDrops - dropListener.numOreDrops;
			if(need > 0){
				if(DO_ITEM_RESCUE) pl.getAPI().rescueItem(new ItemStack(dropType, need));
				pl.getLogger().info("Didn't get enough "+dropType+" drops, needed "+need+" more items!");
				pl.getAPI().punishDestroyed(badPlayer, dropType);
			}
			else if(need < 0) pl.getLogger().warning("Ore drops exceeded expected maximum of "
									+maxOreDrops+": "+dropListener.numOreDrops
									+"\nPlease double check 'max-fortune-level' in config-Renewable.yml");
		}}.runTaskLater(pl, 1);
	}
}