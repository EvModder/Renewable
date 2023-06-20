package net.evmodder.Renewable.listeners;

import org.bukkit.GameMode;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import net.evmodder.Renewable.Renewable;

public class ItemConsumeListener implements Listener{
	final Renewable plugin;
	final boolean saveItems;

	public ItemConsumeListener(){
		plugin = Renewable.getPlugin();
		saveItems = plugin.getConfig().getBoolean("rescue-items");
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onPlayerEatItem(PlayerItemConsumeEvent evt){
		if(evt.getPlayer().getGameMode() == GameMode.CREATIVE) return;
		if(plugin.getAPI().isUnrenewable(evt.getItem())){
			plugin.getLogger().fine("Item eaten: "+evt.getItem().getType());
			plugin.getAPI().punish(evt.getPlayer().getUniqueId(), evt.getItem().getType());
			if(saveItems) plugin.getAPI().rescueItem(evt.getItem());
		}
	}
}