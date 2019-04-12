package EvLib;

import java.util.Vector;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class EvUtils{// version = X1.0+-
	public static String getNormalizedName(EntityType entity){
		//TODO: improve this algorithm / test for errors
		switch(entity){
		case PIG_ZOMBIE:
			return "Zombie Pigman";
		case MUSHROOM_COW:
			return "Mooshroom";
		default:
			boolean wordStart = true;
			char[] arr = entity.name().toCharArray();
			for(int i=0; i<arr.length; ++i){
				if(wordStart) wordStart = false;
				else if(arr[i] == '_' || arr[i] == ' '){arr[i] = ' '; wordStart = true;}
				else arr[i] = Character.toLowerCase(arr[i]);
			}
			return new String(arr);
		}
	}

	public static Vector<String> installedEvPlugins(){
		Vector<String> evPlugins = new Vector<String>();
		for(Plugin pl : Bukkit.getServer().getPluginManager().getPlugins()){
			try{
				@SuppressWarnings("unused")
				String ver = pl.getClass().getField("EvLib_ver").get(null).toString();
				evPlugins.add(pl.getName());
				//TODO: potentially return list of different EvLib versions being used
			}
			catch(IllegalArgumentException | IllegalAccessException | NoSuchFieldException | SecurityException e){}
		}
		return evPlugins;
	}

	public static Player getNearbyPlayer(Location loc, int range){//+
		range = range*range;
		for(Player p : Bukkit.getServer().getOnlinePlayers()){
			if(p.getWorld().getName().equals(loc.getWorld().getName()) && p.getLocation().distanceSquared(loc) > range)
				return p;
		}
		return null;
	}

	public static boolean pickIsAtLeast(Material pickType, Material needPick){//+
		switch(pickType){
			case DIAMOND_PICKAXE:
				return true;
			case IRON_PICKAXE:
				return needPick != Material.DIAMOND_PICKAXE;
			case STONE_PICKAXE:
				return needPick != Material.DIAMOND_PICKAXE && needPick != Material.IRON_PICKAXE;
			case GOLDEN_PICKAXE:
			case WOODEN_PICKAXE:
			default:
				return needPick != Material.DIAMOND_PICKAXE && needPick != Material.IRON_PICKAXE
					&& needPick != Material.STONE_PICKAXE;
				
		}
	}
	public static boolean swordIsAtLeast(Material swordType, Material needSword){//+
		switch(swordType){
			case DIAMOND_SWORD:
				return true;
			case IRON_SWORD:
				return needSword != Material.DIAMOND_SWORD;
			case STONE_SWORD:
				return needSword != Material.IRON_SWORD && needSword != Material.DIAMOND_SWORD;
			case GOLDEN_SWORD:
			case WOODEN_SWORD:
			default:
				return needSword != Material.IRON_SWORD && needSword != Material.DIAMOND_SWORD
					&& needSword != Material.STONE_SWORD;
				
		}
	}

	public static Location getLocationFromString(String s){
		String[] data = s.split(",");
		World world = org.bukkit.Bukkit.getWorld(data[0]);
		if(world != null){
			try{return new Location(world,
					Integer.parseInt(data[1]), Integer.parseInt(data[2]), Integer.parseInt(data[3]));}
			catch(NumberFormatException ex){}
		}
		return null;
	}
}