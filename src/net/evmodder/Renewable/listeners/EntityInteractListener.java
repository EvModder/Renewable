package net.evmodder.Renewable.listeners;

import java.util.UUID;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import net.evmodder.Renewable.Renewable;

public class EntityInteractListener implements Listener{
	final private Renewable pl;
	final boolean DO_ITEM_RESCUE, ignoreGM1, supplyGM1;

	public EntityInteractListener(){
		pl = Renewable.getPlugin();
		DO_ITEM_RESCUE = pl.getConfig().getBoolean("rescue-items");
		ignoreGM1 = pl.getConfig().getBoolean("creative-mode-ignore", true);
		supplyGM1 = pl.getConfig().getBoolean("creative-unrenewable-supply", false);
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onHorseFeed(PlayerInteractEntityEvent evt){
		if(evt.getRightClicked().getType() == EntityType.ITEM_FRAME) return;
		ItemStack hand = evt.getPlayer().getInventory().getItemInMainHand();
		if(pl.getAPI().isUnrenewable(hand)){
			final UUID uuid = evt.getPlayer().getUniqueId();
			final ItemStack item = hand.clone();
			new BukkitRunnable(){@Override public void run(){
				Player p = pl.getServer().getPlayer(uuid);
				if(p != null && !p.getInventory().contains(item)){
					if(p.getGameMode() == GameMode.CREATIVE && (supplyGM1 || ignoreGM1)){
						if(supplyGM1){
							ItemStack leftover = pl.getAPI().addToCreativeSupply(item);
							if(leftover != null){
								evt.getPlayer().sendMessage(ChatColor.RED+"Failed attempt to add item:"
									+ChatColor.GOLD+item.getType()+ChatColor.RED+" to creative-supply-depot");
								evt.getRightClicked().getWorld().dropItem(evt.getRightClicked().getLocation(), leftover);
							}
						}
						return;
					}
					pl.getLogger().fine("Fed: "+item.getType()+" to "+evt.getRightClicked().getType());
					pl.getAPI().punishDestroyed(uuid, item.getType());
					if(DO_ITEM_RESCUE) pl.getAPI().rescueItem(item);
				}
			}}.runTaskLater(pl, 1);
		}
	}
}