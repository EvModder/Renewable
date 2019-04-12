package Evil_Code_Renewable.listeners;

import java.util.UUID;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import Evil_Code_Renewable.Renewable;
import Evil_Code_Renewable.RenewableUtils;

public class EntityInteractListener implements Listener{
	final Renewable plugin;
	final boolean saveItems;

	public EntityInteractListener(){
		plugin = Renewable.getPlugin();
		saveItems = plugin.getConfig().getBoolean("rescue-items");
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onHorseFeed(PlayerInteractEntityEvent evt){
		if(evt.getRightClicked().getType() == EntityType.ITEM_FRAME) return;
		ItemStack hand = evt.getPlayer().getInventory().getItemInMainHand();
		if(RenewableUtils.isUnrenewable(hand)){
			final UUID uuid = evt.getPlayer().getUniqueId();
			final ItemStack item = hand.clone();
			new BukkitRunnable(){@Override public void run(){
				Player p = plugin.getServer().getPlayer(uuid);
				if(p != null && !p.getInventory().getItemInMainHand().equals(item)){
					plugin.punish(uuid, item.getType());
					if(saveItems) plugin.rescueItem(item);
				}
			}}.runTaskLater(plugin, 1);
		}
	}
}