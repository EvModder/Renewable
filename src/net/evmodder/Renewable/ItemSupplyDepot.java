package net.evmodder.Renewable;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collector;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Container;
import org.bukkit.block.Hopper;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import net.evmodder.EvLib.bukkit.EvUtils;

public class ItemSupplyDepot{
	final private ArrayDeque<Container> depotInvs;
	public ItemSupplyDepot(Plugin pl, Location loc){
		depotInvs = getStorageDepot(loc);
		pl.getLogger().info("Found depot. Size in blocks: "+depotInvs.size());
	}

	final private static List<BlockFace> dirs6 = Arrays.asList(
		BlockFace.UP, BlockFace.DOWN, BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST);
	private static ArrayDeque<Container> getStorageDepot(Location loc){
		return EvUtils.getConnectedBlocks(loc.getBlock(), (b -> b.getState() instanceof Container), dirs6, 1000).stream()
				.map(b -> (Container)b.getState()).collect(
						Collector.of(ArrayDeque::new,
								ArrayDeque::add,
								(a, b) -> {a.addAll(b); return a;}
						//(deq, t) -> deq.addFirst(t),
						//(d1, d2) -> {d2.addAll(d1); return d2;}
						));
	}

	public boolean takeItem(ItemStack item){
		if(item == null || item.getType() == Material.AIR) return true;
		Iterator<Container> it = depotInvs.iterator();
		while(it.hasNext()){
			Container c = it.next();
			// Check to make sure this inventory is still valid
			if(c.getBlock().getState() instanceof Container == false){it.remove(); continue;}
			if(c instanceof Hopper) continue;
			int idx;
			while((idx = c.getInventory().first(item.getType())) != -1){
				ItemStack i = c.getInventory().getItem(idx);
				if(i.getAmount() < item.getAmount()){
					item.setAmount(item.getAmount() - i.getAmount());
					c.getInventory().clear(idx);
					continue;
				}
				else if(i.getAmount() > item.getAmount()) i.setAmount(i.getAmount() - item.getAmount());
				else c.getInventory().clear(idx);
				//Renewable.getPlugin().getLogger().info("sourcing from: "+EvUtils.locationToStringXYZ(c.getLocation()));
				return true;
			}
		}
		it = depotInvs.iterator();
		while(it.hasNext()){
			Container c = it.next();
			int idx;
			while((idx = c.getInventory().first(item.getType())) != -1){
				ItemStack i = c.getInventory().getItem(idx);
				if(i.getAmount() < item.getAmount()){
					item.setAmount(item.getAmount() - i.getAmount());
					c.getInventory().clear(idx);
					continue;
				}
				else if(i.getAmount() > item.getAmount()) i.setAmount(i.getAmount() - item.getAmount());
				else c.getInventory().clear(idx);
				//Renewable.getPlugin().getLogger().info("sourcing from hopper: "
				//+EvUtils.locationToStringXYZ(c.getLocation()));
				return true;
			}
		}
		return false;
	}
	public boolean takeItem(Material mat){
		if(mat == null || mat == Material.AIR) return true;
		Iterator<Container> it = depotInvs.iterator();
		while(it.hasNext()){
			Container c = it.next();
			// Check to make sure this inventory is still valid
			if(c.getBlock().getState() instanceof Container == false){it.remove(); continue;}
			if(c instanceof Hopper) continue;
			int idx = c.getInventory().first(mat);
			if(idx != -1){
				ItemStack i = c.getInventory().getItem(idx);
				if(i.getAmount() == 1) c.getInventory().clear(idx);
				else i.setAmount(i.getAmount() - 1);
				//Renewable.getPlugin().getLogger().info("sourcing from: "+EvUtils.locationToStringXYZ(c.getLocation()));
				return true;
			}
		}
		it = depotInvs.iterator();
		while(it.hasNext()){
			Container c = it.next();
			int idx = c.getInventory().first(mat);
			if(idx != -1){
				ItemStack i = c.getInventory().getItem(idx);
				if(i.getAmount() == 1) c.getInventory().clear(idx);
				else i.setAmount(i.getAmount() - 1);
				//Renewable.getPlugin().getLogger().info("sourcing from: "+EvUtils.locationToStringXYZ(c.getLocation()));
				return true;
			}
		}
		return false;
	}
	public ItemStack addItem(ItemStack item){
		if(item == null || item.getType() == Material.AIR) return null;
		Iterator<Container> it = depotInvs.iterator();
		while(it.hasNext()){
			Container c = it.next();
			// Check to make sure this inventory is still valid
			if(c.getBlock().getState() instanceof Container == false){it.remove(); continue;}
			// check != Hopper (so that this doesn't break item filters)
			if(!(c instanceof Hopper) && c.getInventory().contains(item.getType())){
				HashMap<Integer, ItemStack> leftovers = c.getInventory().addItem(item);
				if(leftovers.isEmpty()){
					//Renewable.getPlugin().getLogger()
					//.info("deposited to: "+EvUtils.locationToStringXYZ(c.getLocation()));
					return null;
				}
				if(leftovers.size() > 1) Renewable.getPlugin().getLogger()
						.info("Unexpected leftovers in addToCreativeSupply()!");
				item = leftovers.values().iterator().next();
			}
		}
		it = depotInvs.iterator();
		while(it.hasNext()){// Try again but less picky (stick it anywhere it'll fit)
			HashMap<Integer, ItemStack> leftovers = it.next().getInventory().addItem(item);
			if(leftovers.isEmpty()) return null;
			if(leftovers.size() > 1) Renewable.getPlugin().getLogger()
					.info("Unexpected leftovers in addToCreativeSupply()!");
			item = leftovers.values().iterator().next();
		}
		return item;
	}
	public boolean addItem(Material mat){
		if(mat == null || mat == Material.AIR) return true;
		Iterator<Container> it = depotInvs.iterator();
		while(it.hasNext()){
			Container c = it.next();
			// Check to make sure this inventory is still valid
			if(c.getBlock().getState() instanceof Container == false){it.remove(); continue;}
			// check != Hopper (so that this doesn't break item filters)
			if(!(c instanceof Hopper) && c.getInventory().contains(mat)){
				if(c.getInventory().addItem(new ItemStack(mat, 1)).isEmpty()){
					//Renewable.getPlugin().getLogger().info("deposited to: "+EvUtils.locationToStringXYZ(c.getLocation()));
					return true;
				}
			}
		}
		it = depotInvs.iterator();
		while(it.hasNext()){// Try again but less picky (stick it anywhere it'll fit)
			if(it.next().getInventory().addItem(new ItemStack(mat, 1)).isEmpty()) return true;
		}
		return false;
	}
}