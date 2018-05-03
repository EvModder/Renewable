package Evil_Code_Renewable.listeners;

import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;
import Evil_Code_Renewable.Renewable;

public class MobDeathListener implements Listener{
	Renewable plugin;
	boolean saveItems, punishUnrenewableProcess, preventUnrenewableProcess, normalizeRescuedItems;

	public MobDeathListener(){
		plugin = Renewable.getPlugin();
		saveItems = plugin.getConfig().getBoolean("rescue-items", true);
	}

	@EventHandler
	public void onMobDeath(EntityDeathEvent evt){
		if(evt.getEntityType() == EntityType.SHULKER
				&& !evt.getDrops().contains(new ItemStack(Material.SHULKER_SHELL))){

			if(saveItems) plugin.rescueItem(new ItemStack(Material.SHULKER_SHELL));
			if(evt.getEntity().getLastDamageCause() instanceof EntityDamageByEntityEvent){
				EntityDamageByEntityEvent damage = (EntityDamageByEntityEvent)evt.getEntity().getLastDamageCause();
				if(damage.getDamager() instanceof Player){
					plugin.punish(damage.getDamager().getUniqueId(), Material.SHULKER_SHELL);
				}
			}
		}
	}
}