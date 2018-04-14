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
	private boolean saveItems, normalizeRescuedItems, silkSpawners;
	private boolean preventUnrenewableProcess, punishUnrenewableProcess;
	private int maxOreDrops, silkSpawnersLvl;

	public BlockMineListener(){
		plugin = Renewable.getPlugin();
		saveItems = plugin.getConfig().getBoolean("rescue-items", true);
		normalizeRescuedItems = plugin.getConfig().getBoolean("standardize-rescued-items", true);
		preventUnrenewableProcess = plugin.getConfig().getBoolean("prevent-irreversible-process", true);
		punishUnrenewableProcess = plugin.getConfig().getBoolean("punish-for-irreversible-process", true);
		silkSpawners = plugin.getConfig().getBoolean("silktouch-spawners", false);
		silkSpawnersLvl = plugin.getConfig().getInt("silktouch-level", 1);
		maxOreDrops = plugin.getConfig().getInt("max-fortune-level", 3) + 1;
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onBlockMine(BlockBreakEvent evt) {
		if(evt.isCancelled() || evt.getPlayer().getGameMode() == GameMode.CREATIVE
				|| !Utils.isUnrenewable(evt.getBlock().getState())) return;

		ItemStack tool = evt.getPlayer().getInventory().getItemInMainHand();
		int silkLvl = tool == null ? 0 : tool.containsEnchantment(Enchantment.SILK_TOUCH)
									? tool.getEnchantmentLevel(Enchantment.SILK_TOUCH) : 0;

		//If the mine event results in the proper item being dropped
		ItemStack item = Utils.getUnewnewableItemForm(evt.getBlock().getState());
		ItemStack stdItem = Utils.standardize(item);
		boolean stdMatch = false;
		for(ItemStack drop : evt.getBlock().getDrops(tool)){
			if(Utils.isUnrenewable(drop)){
				if(drop.equals(item) || Utils.isUnrenewableProcess(item, drop)) return;
				if(normalizeRescuedItems && stdItem.equals(Utils.standardize(drop))) stdMatch = true;
			}
		}

		UUID uuid = evt.getPlayer().getUniqueId();

		switch(evt.getBlock().getType()){
			case GRAVEL:
				listenForGravelDrop(uuid);
				return;
			case DIAMOND_ORE:
				if(normalizeRescuedItems && evt.isDropItems()){
					if(punishUnrenewableProcess) plugin.punish(uuid, evt.getBlock().getType());
					if(preventUnrenewableProcess) evt.setCancelled(true);
					else listenForOreDrop(uuid, Material.DIAMOND, evt.getBlock().getLocation(), maxOreDrops);
					return;
				}
			case QUARTZ_ORE:
				if(normalizeRescuedItems && evt.isDropItems()){
					if(punishUnrenewableProcess) plugin.punish(uuid, evt.getBlock().getType());
					if(preventUnrenewableProcess) evt.setCancelled(true);
					else listenForOreDrop(uuid, Material.QUARTZ, evt.getBlock().getLocation(), maxOreDrops);
					return;
				}
			case MOB_SPAWNER://It's okay if the above cases fall into here, since they (presumably) don't have silktouch
				if(silkSpawners && silkLvl >= silkSpawnersLvl) return;
			default:
				if(stdMatch && punishUnrenewableProcess) plugin.punish(uuid, evt.getBlock().getType());
				if(!stdMatch && saveItems) plugin.rescueItem(Utils.getUnewnewableItemForm(evt.getBlock().getState()));
				else if(preventUnrenewableProcess) evt.setCancelled(true);//Prevent mine only if won't be saved otherwise
		}
	}

	void listenForGravelDrop(final UUID playerResponsible){
		plugin.getServer().getPluginManager().registerEvents(new Listener(){
			@EventHandler public void gravelItemDropEvent(ItemSpawnEvent evt){
				if(evt.getEntity().getItemStack().getType() == Material.FLINT){
					if(punishUnrenewableProcess){
						plugin.punish(playerResponsible, Material.GRAVEL);
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
				if(saveItems) plugin.rescueItem(new ItemStack(dropType, need));
				plugin.getLogger().fine("Didn't get enough "+dropType+" drops, needed "+need+" more items!");
				plugin.punish(badPlayer, dropType);
			}
			else if(need < 0) plugin.getLogger().warning("Ore drops exceeded expected maximum of "
											+maxOreDrops+": "+dropListener.numOreDrops);
		}}.runTaskLater(plugin, 1);
	}
}