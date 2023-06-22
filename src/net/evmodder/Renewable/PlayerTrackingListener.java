package net.evmodder.Renewable;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;

public class PlayerTrackingListener implements Listener{
	final private Renewable pl;

	PlayerTrackingListener(){
		pl = Renewable.getPlugin();
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void playerClickBlock(PlayerInteractEvent evt){
		if(evt.getClickedBlock() != null){
			pl.getLogger().fine("flagging block - clicked");
			TaggingUtil.setLastPlayerInContact(evt.getClickedBlock().getState(), evt.getPlayer().getUniqueId());
		}
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void playerClickEntity(PlayerInteractEntityEvent evt){
		pl.getLogger().fine("flagging entity - right clicked");
		TaggingUtil.setLastPlayerInContact(evt.getRightClicked(), evt.getPlayer().getUniqueId());
	}
}