package net.evmodder.Renewable.listeners;

import org.bukkit.GameMode;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import net.evmodder.Renewable.Renewable;

public class ItemConsumeListener implements Listener{
	final private Renewable pl;
	final private boolean DO_ITEM_RESCUE;

	public ItemConsumeListener(){
		pl = Renewable.getPlugin();
		DO_ITEM_RESCUE = pl.getConfig().getBoolean("rescue-items");
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onPlayerEatItem(PlayerItemConsumeEvent evt){
		if(evt.getPlayer().getGameMode() == GameMode.CREATIVE) return;
		if(pl.getAPI().isUnrenewable(evt.getItem())){
			pl.getLogger().info("Item eaten: "+evt.getItem().getType());
			pl.getAPI().punishDestroyed(evt.getPlayer().getUniqueId(), evt.getItem().getType());
			if(DO_ITEM_RESCUE) pl.getAPI().rescueItem(evt.getItem());
		}
	}
}