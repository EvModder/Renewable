package net.evmodder.Renewable.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import net.evmodder.Renewable.Renewable;

public class BucketEmptyListener implements Listener{
	boolean saveItems;

	public BucketEmptyListener(){
		saveItems = Renewable.getPlugin().getConfig().getBoolean("rescue-items");
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onBucketEmpty(PlayerBucketEmptyEvent evt) {
//		Bukkit.getLogger().info("Placing bucket in: "+evt.getBlockClicked().getRelative(evt.getBlockFace()));

		//TODO: placing lava bucket in lava or water
		//TODO: placing water bucket above lava
		//TODO: placing lava bucket that spreads and destroys unrenewable blocks (items already covered in burn event)
	}
}