package Evil_Code_Renewable.listeners;

import java.util.UUID;
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
import Evil_Code_Renewable.Renewable;
import Evil_Code_Renewable.Utils;

public class BlockMineListener implements Listener{
	private Renewable plugin;
	private UUID badPlayer;
	private boolean saveItems, normalizeRescuedItems, preventUnrenewableProcess, silkSpawners;
	private Listener diaDropListener;
	private int numOreDrops, maxOreDrops, silkSpawnersLvl;
	private Location oreMineLoc;
	private Material drop;
//	private int clayCounter; private Listener tempClayListener;

	public BlockMineListener(){
		plugin = Renewable.getPlugin();
		saveItems = plugin.getConfig().getBoolean("rescue-items", true);
		normalizeRescuedItems = plugin.getConfig().getBoolean("standardize-rescued-items", true);
		preventUnrenewableProcess = plugin.getConfig().getBoolean("prevent-irreversible-process", false);
		silkSpawners = plugin.getConfig().getBoolean("silktouch-spawners", false);
		silkSpawnersLvl = plugin.getConfig().getInt("silktouch-level", 1);
		maxOreDrops = plugin.getConfig().getInt("max-fortune-level", 3) + 1;
	}

	@SuppressWarnings("deprecation")
	@EventHandler(priority = EventPriority.MONITOR)
	public void onBlockMine(BlockBreakEvent evt) {
		if(evt.isCancelled() || evt.getPlayer().getGameMode() == GameMode.CREATIVE
				|| !Utils.isUnrenewable(evt.getBlock().getState())
				|| (evt.isDropItems() &&
					Utils.isUnrenewableBlockThatDropsEquivalentType(evt.getBlock().getType()))) return;

//		Utils.setLastPlayerInContact(evt.getBlock(), evt.getPlayer().getUniqueId());

		ItemStack tool = evt.getPlayer().getInventory().getItemInMainHand();
		int silkLvl = tool == null ? 0 : tool.containsEnchantment(Enchantment.SILK_TOUCH)
									? tool.getEnchantmentLevel(Enchantment.SILK_TOUCH) : 0;

		boolean noDrops = true, noThisType = true;
		byte blockDataValue = evt.getBlock().getState().getRawData();
		Material expectedDropType = Utils.getUnewnewableItemForm(evt.getBlock().getState()).getType();
		if(evt.isDropItems()) for(ItemStack drop : evt.getBlock().getDrops(tool)){
			if(drop.getType() == expectedDropType && drop.getData().getData() == blockDataValue)
				noThisType = false;
			noDrops = false;
		}
		if(evt.getBlock().getType() == Material.GRAVEL){
			badPlayer = evt.getPlayer().getUniqueId();

			plugin.getServer().getPluginManager().registerEvents(new Listener(){
				@EventHandler public void gravelItemDropEvent(ItemSpawnEvent evt){
					if(evt.getEntity().getItemStack().getType() == Material.FLINT){
						plugin.punish(badPlayer, Material.GRAVEL);
						if(saveItems){
							//TODO: this currently changes flint to gravel, alternative?
							evt.getEntity().setItemStack(new ItemStack(Material.GRAVEL));
						}
					}
					HandlerList.unregisterAll(this);
				}
			}, plugin);
		}
		else if(evt.getBlock().getType() == Material.MOB_SPAWNER){
			if(!silkSpawners || silkLvl < silkSpawnersLvl){
				plugin.punish(evt.getPlayer().getUniqueId(), Material.MOB_SPAWNER);
				if(saveItems) plugin.rescueItem(Utils.getUnewnewableItemForm(evt.getBlock().getState()));
			}
		}
		else if(noDrops || (noThisType && silkLvl == 0)){
//			evt.setCancelled(true);
//			evt.getBlock().setType(Material.AIR);
			if(saveItems){
				drop = null;
				if(evt.getBlock().getType() == Material.DIAMOND_ORE) drop = Material.DIAMOND;
				else if(evt.getBlock().getType() == Material.QUARTZ_ORE) drop = Material.QUARTZ;

				if(drop != null && normalizeRescuedItems){
					oreMineLoc = evt.getBlock().getLocation();
					badPlayer = evt.getPlayer().getUniqueId();
					numOreDrops = 0;
					plugin.getServer().getPluginManager().registerEvents(diaDropListener = new Listener(){
						@EventHandler public void diamondItemDropEvent(ItemSpawnEvent evt){
							if(evt.getEntity().getItemStack().getType() == drop &&
									evt.getEntity().getLocation().distanceSquared(oreMineLoc) < 10){
								numOreDrops += evt.getEntity().getItemStack().getAmount();
							}
						}
					}, plugin);
					new BukkitRunnable(){@Override public void run() {
						HandlerList.unregisterAll(diaDropListener);
						int need = maxOreDrops - numOreDrops;
						if(need > 0){//destroyed diamond! :o
							plugin.rescueItem(new ItemStack(drop, need));
							plugin.getLogger().info("Mined "+drop.name()+"; rescuing "+need+" diamonds");
							plugin.punish(badPlayer, drop == Material.DIAMOND ?
									Material.DIAMOND_ORE : Material.QUARTZ_ORE);
						}
						else if(preventUnrenewableProcess){//Can't turn diamonds back to ore!
							plugin.punish(badPlayer, drop == Material.DIAMOND ?
									Material.DIAMOND_ORE : Material.QUARTZ_ORE);
						}

					}}.runTaskLater(plugin, 2);
					return;
				}
				plugin.rescueItem(Utils.getUnewnewableItemForm(evt.getBlock().getState()));
			}
			plugin.punish(evt.getPlayer().getUniqueId(), evt.getBlock().getType());
		}
		else{//flag item drops
			badPlayer = evt.getPlayer().getUniqueId();

			plugin.getServer().getPluginManager().registerEvents(new Listener(){
				@EventHandler public void gravelItemDropEvent(ItemSpawnEvent evt){
//					evt.getEntity().setItemStack(Utils.setLastPlayerInContact(
//							evt.getEntity().getItemStack(), badPlayer));
					HandlerList.unregisterAll(this);
				}
			}, plugin);
		}
	}
}