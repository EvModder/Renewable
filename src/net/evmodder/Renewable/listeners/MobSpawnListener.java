package net.evmodder.Renewable.listeners;

import java.util.HashMap;
import java.util.UUID;
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
import net.evmodder.EvLib.TypeUtils;
import net.evmodder.Renewable.Renewable;
import net.md_5.bungee.api.ChatColor;

public class MobSpawnListener implements Listener{
	final Renewable plugin;
	final boolean saveItems, ignoreGM1, supplyGM1, spawnEggsUnrenewable;
	final HashMap<EntityType, Material> mobDrops = new HashMap<EntityType, Material>();

	public MobSpawnListener(){
		plugin = Renewable.getPlugin();
		saveItems = plugin.getConfig().getBoolean("rescue-items", true);
		ignoreGM1 = plugin.getConfig().getBoolean("creative-mode-ignore", true);
		supplyGM1 = plugin.getConfig().getBoolean("creative-unrenewable-sourcing", false);
		spawnEggsUnrenewable = plugin.getAPI().isUnrenewable(new ItemStack(Material.BAT_SPAWN_EGG));
		mobDrops.put(EntityType.SHULKER, Material.SHULKER_SHELL);
		mobDrops.put(EntityType.EVOKER, Material.TOTEM_OF_UNDYING);
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onSpawnEggClick(PlayerInteractEvent evt){
		if(evt.getItem() != null && TypeUtils.isSpawnEgg(evt.getItem().getType())){
			final boolean gm1Flags = (ignoreGM1 || supplyGM1) && evt.getPlayer().getGameMode() == GameMode.CREATIVE;
			final EntityType eType = TypeUtils.getSpawnedMob(evt.getItem().getType());
			if(spawnEggsUnrenewable || (gm1Flags && mobDrops.containsKey(eType)))
				listenForMobSpawn(evt.getPlayer().getUniqueId(), evt.getClickedBlock().getLocation(), eType, gm1Flags);
		}
	}

	void listenForMobSpawn(final UUID badPlayer, final Location loc, final EntityType etype, final boolean gm1Flags){
		final Listener spawnListener = new Listener(){
			@EventHandler(priority = EventPriority.MONITOR)
			public void onMobSpawn(CreatureSpawnEvent evt){
				if(evt.getEntityType() == etype && !evt.isCancelled() && evt.getLocation().distanceSquared(loc) < 10){
					if(gm1Flags){
						if(supplyGM1){
							Material typeToSupply;
							if((spawnEggsUnrenewable &&
									!plugin.getAPI().deductFromCreativeSupply(typeToSupply=TypeUtils.getSpawnEgg(etype)))
							|| (mobDrops.containsKey(etype) &&
									!plugin.getAPI().deductFromCreativeSupply(typeToSupply=mobDrops.get(etype))))
							{
								Player p = plugin.getServer().getPlayer(badPlayer);
								if(p != null) p.sendMessage(ChatColor.RED+"Failed attempt to supply item:"
										+ChatColor.GOLD+typeToSupply+ChatColor.RED+" from creative-supply-depot");
								evt.setCancelled(true);
							}
						}
						else if(ignoreGM1){
							evt.getEntity().setMetadata("spawned_by_gm1", new FixedMetadataValue(plugin, badPlayer));
						}
						return;
					}
					//Assumption: This Listener is only registered if spawn eggs
					// (and thus Shulkers/Evokers) are considered unrenewable
					plugin.getAPI().punish(badPlayer, TypeUtils.getSpawnEgg(etype));
					if(saveItems) plugin.getAPI().rescueItem(new ItemStack(TypeUtils.getSpawnEgg(etype)));
				}
			}
		};
		plugin.getServer().getPluginManager().registerEvents(spawnListener, plugin);
		new BukkitRunnable(){@Override public void run(){
			HandlerList.unregisterAll(spawnListener);
		}}.runTaskLater(plugin, 2);
	}
}