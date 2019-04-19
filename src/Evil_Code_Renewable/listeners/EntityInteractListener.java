package Evil_Code_Renewable.listeners;

import java.util.UUID;
import org.bukkit.GameMode;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import Evil_Code_Renewable.Renewable;
import net.md_5.bungee.api.ChatColor;

public class EntityInteractListener implements Listener{
	final Renewable plugin;
	final boolean saveItems, ignoreGM1, supplyGM1;

	public EntityInteractListener(){
		plugin = Renewable.getPlugin();
		saveItems = plugin.getConfig().getBoolean("rescue-items");
		ignoreGM1 = plugin.getConfig().getBoolean("creative-mode-ignore", true);
		supplyGM1 = plugin.getConfig().getBoolean("creative-unrenewable-supply", false);
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onHorseFeed(PlayerInteractEntityEvent evt){
		if(evt.isCancelled() || evt.getRightClicked().getType() == EntityType.ITEM_FRAME) return;
		ItemStack hand = evt.getPlayer().getInventory().getItemInMainHand();
		if(plugin.getAPI().isUnrenewable(hand)){
			final UUID uuid = evt.getPlayer().getUniqueId();
			final ItemStack item = hand.clone();
			new BukkitRunnable(){@Override public void run(){
				Player p = plugin.getServer().getPlayer(uuid);
				if(p != null && !p.getInventory().contains(item)){
					if(p.getGameMode() == GameMode.CREATIVE && (supplyGM1 || ignoreGM1)){
						if(supplyGM1){
							ItemStack leftover = plugin.getAPI().addToCreativeSupply(item);
							if(leftover != null){
								evt.getPlayer().sendMessage(ChatColor.RED+"Failed attempt to add item:"
									+ChatColor.GOLD+item.getType()+ChatColor.RED+" to creative-supply-depot");
								evt.getRightClicked().getWorld().dropItem(evt.getRightClicked().getLocation(), leftover);
							}
						}
						return;
					}
					plugin.getAPI().punish(uuid, item.getType());
					if(saveItems) plugin.getAPI().rescueItem(item);
				}
			}}.runTaskLater(plugin, 1);
		}
	}
}