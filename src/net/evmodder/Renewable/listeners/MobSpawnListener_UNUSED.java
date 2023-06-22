package net.evmodder.Renewable.listeners;

import java.util.HashMap;
import java.util.UUID;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.scheduler.BukkitRunnable;
import net.evmodder.EvLib.extras.EntityUtils;
import net.evmodder.Renewable.Renewable;

public class MobSpawnListener_UNUSED implements Listener{
	final private Renewable pl;
	final private boolean DO_ITEM_RESCUE, ignoreGM1, supplyGM1, spawnEggsUnrenewable;
	final private HashMap<EntityType, Material> mobDrops;

	public MobSpawnListener_UNUSED(){
		pl = Renewable.getPlugin();
		DO_ITEM_RESCUE = pl.getConfig().getBoolean("rescue-items", true);
		ignoreGM1 = pl.getConfig().getBoolean("creative-mode-ignore", true);
		supplyGM1 = pl.getConfig().getBoolean("creative-unrenewable-sourcing", false);
		spawnEggsUnrenewable = pl.getAPI().isUnrenewable(new ItemStack(Material.BAT_SPAWN_EGG));
		mobDrops = new HashMap<>();
		mobDrops.put(EntityType.SHULKER, Material.SHULKER_SHELL);
		mobDrops.put(EntityType.EVOKER, Material.TOTEM_OF_UNDYING);
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onSpawnEggClick(PlayerInteractEvent evt){
		if(evt.getItem() != null && EntityUtils.isSpawnEgg(evt.getItem().getType())){
			final boolean gm1Flags = (ignoreGM1 || supplyGM1) && evt.getPlayer().getGameMode() == GameMode.CREATIVE;
			final EntityType eType = EntityUtils.getSpawnedMob(evt.getItem().getType());
			if(spawnEggsUnrenewable || (gm1Flags && mobDrops.containsKey(eType))){
				listenForMobSpawn(evt.getPlayer().getUniqueId(), evt.getClickedBlock().getLocation(), eType, gm1Flags);
			}
		}
	}

	void listenForMobSpawn(final UUID badPlayer, final Location loc, final EntityType etype, final boolean gm1Flags){
		final Listener listener = new Listener(){
			@EventHandler(priority = EventPriority.MONITOR)
			public void onMobSpawn(CreatureSpawnEvent evt){
				if(evt.getEntityType() == etype && !evt.isCancelled() && evt.getLocation().distanceSquared(loc) < 10){
					if(gm1Flags){
						if(supplyGM1){
							Material typeToSupply;
							if((spawnEggsUnrenewable && !pl.getAPI().takeFromCreativeSupply(typeToSupply=EntityUtils.getSpawnEgg(etype)))
							|| (mobDrops.containsKey(etype) && !pl.getAPI().takeFromCreativeSupply(typeToSupply=mobDrops.get(etype))))
							{
								final Player p = pl.getServer().getPlayer(badPlayer);
								if(p != null) p.sendMessage(ChatColor.RED+"Failed attempt to supply item:"
										+ChatColor.GOLD+typeToSupply+ChatColor.RED+" from creative-supply-depot");
								evt.setCancelled(true);
							}
						}
						else if(ignoreGM1){
							evt.getEntity().setMetadata("spawned_by_gm1", new FixedMetadataValue(pl, badPlayer));
						}
						return;
					}
					//Assumption: This Listener is only registered if spawn eggs (and thus Shulkers/Evokers) are considered unrenewable
					pl.getAPI().punishDestroyed(badPlayer, EntityUtils.getSpawnEgg(etype));
					if(DO_ITEM_RESCUE) pl.getAPI().rescueItem(new ItemStack(EntityUtils.getSpawnEgg(etype)));
				}
			}
		};
		pl.getServer().getPluginManager().registerEvents(listener, pl);
		new BukkitRunnable(){@Override public void run(){HandlerList.unregisterAll(listener);}}.runTaskLater(pl, 2);
	}
}