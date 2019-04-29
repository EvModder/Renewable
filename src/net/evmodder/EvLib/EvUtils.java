package net.evmodder.EvLib;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Vector;
import java.util.function.Function;
import java.util.stream.Collector;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Container;
import org.bukkit.block.data.Directional;
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

	public static ArrayList<Player> getNearbyPlayers(Location loc, int range){//+
		range = range*range;
		ArrayList<Player> ppl = new ArrayList<Player>();
		for(Player p : Bukkit.getServer().getOnlinePlayers()){
			if(p.getWorld().getName().equals(loc.getWorld().getName()) && p.getLocation().distanceSquared(loc) > range)
				ppl.add(p);
		}
		return ppl;
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

	public static List<Block> getBlockStructure(Block block0, Function<Block, Boolean> test, 
			List<BlockFace> dirs, int MAX_SIZE){//+
		HashSet<Block> visited = new HashSet<Block>();
		List<Block> results = new ArrayList<Block>();
		ArrayDeque<Block> toProcess = new ArrayDeque<Block>();
		toProcess.addLast(block0);
		while(results.size() < MAX_SIZE && !toProcess.isEmpty()){
			Block b = toProcess.pollFirst();
			if(b != null && test.apply(b) && !visited.contains(b)){
				results.add(b);
				visited.add(b);
				for(BlockFace dir : dirs) toProcess.addLast(b.getRelative(dir));
			}
		}
		return results;
	}

	final static List<BlockFace> dirs6 = Arrays.asList(BlockFace.UP, BlockFace.DOWN,
			BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST);//+
	public static ArrayDeque<Container> getStorageDepot(Location loc){//+
		return getBlockStructure(loc.getBlock(), (b -> b.getState() instanceof Container), dirs6, 1000).stream()
				.map(b -> (Container)b.getState()).collect(
						Collector.of(ArrayDeque::new,
								ArrayDeque::add,
								(a, b) -> {a.addAll(b); return a;}
						//(deq, t) -> deq.addFirst(t),
						//(d1, d2) -> {d2.addAll(d1); return d2;}
						));
	}

	public static BlockFace getFacing(Block block){
		return block.getBlockData() instanceof Directional ? ((Directional)block.getBlockData()).getFacing() : null;
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

	public static String locationToStringXYZ(Location loc){
		return new StringBuilder("x=").append(loc.getBlockX())
						.append(",y=").append(loc.getBlockY())
						.append(",z=").append(loc.getBlockZ())
						.toString();
	}
}