package net.evmodder.Renewable.listeners;

import java.util.HashMap;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;
import net.evmodder.Renewable.Renewable;

public class MobDeathListener_UNUSED implements Listener{
	final private Renewable pl;
	final private boolean DO_ITEM_RESCUE, ignoreGM1;
	final private HashMap<EntityType, ItemStack> unrenewableMobDrops;

	public MobDeathListener_UNUSED(){
		pl = Renewable.getPlugin();
		DO_ITEM_RESCUE = pl.getConfig().getBoolean("rescue-items", true);
		ignoreGM1 = pl.getConfig().getBoolean("creative-mode-ignore", true);
		unrenewableMobDrops = new HashMap<>();
		unrenewableMobDrops.put(EntityType.SHULKER, new ItemStack(Material.SHULKER_SHELL));
		unrenewableMobDrops.put(EntityType.EVOKER, new ItemStack(Material.TOTEM_OF_UNDYING));
	}

	@EventHandler
	public void onMobDeath(EntityDeathEvent evt){
		if(unrenewableMobDrops.containsKey(evt.getEntityType())){
			final ItemStack missingDrop = unrenewableMobDrops.get(evt.getEntityType());
			for(ItemStack item : evt.getDrops()){
				if(item.getType() == missingDrop.getType()){
					if(item.getAmount() < missingDrop.getAmount()){
						missingDrop.setAmount(missingDrop.getAmount()-item.getAmount());
						continue;
					}
					else if(item.getAmount() > missingDrop.getAmount()){
						pl.getLogger().warning("Unnaturally high number of drops from unrenewable mob: "
							+evt.getEntityType()+"\nPlease double check 'max-looting-level' in config-Renewable.yml");
					}
					return;
				}
			}
			if(ignoreGM1 && evt.getEntity().hasMetadata("spawned_by_gm1")) return;

			//Got here (missingDrop is > 0), now to find the guilty player
			if(evt.getEntity().getLastDamageCause() instanceof EntityDamageByEntityEvent){
				EntityDamageByEntityEvent damage = (EntityDamageByEntityEvent)evt.getEntity().getLastDamageCause();
				if(damage.getDamager() instanceof Player){
					if(ignoreGM1 && ((Player)damage.getDamager()).getGameMode() == GameMode.CREATIVE) return;
					pl.getAPI().punishDestroyed(damage.getDamager().getUniqueId(), missingDrop.getType());
				}
			}
			if(DO_ITEM_RESCUE) pl.getAPI().rescueItem(missingDrop);
		}
	}
}