package Evil_Code_Renewable.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.VillagerAcquireTradeEvent;

public class VillagerTradeListener implements Listener{
	
	@EventHandler(priority = EventPriority.MONITOR)
	public void onVillagerAcquireTrade(VillagerAcquireTradeEvent evt){
		//modify villagers' trades in here
	}
	
//	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerTradeWithVillager(/*VillagerTradeEvent evt*/){
		//modify villagers' trades in here
	}
}