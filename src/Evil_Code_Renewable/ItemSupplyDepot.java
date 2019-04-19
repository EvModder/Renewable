package Evil_Code_Renewable;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Iterator;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Container;
import org.bukkit.block.Hopper;
import org.bukkit.inventory.ItemStack;
import EvLib.EvUtils;

public class ItemSupplyDepot{
	final ArrayDeque<Container> depotInvs;
	
	public ItemSupplyDepot(Location loc){depotInvs = EvUtils.getStorageDepot(loc);}

	public boolean takeItem(ItemStack item){
		Iterator<Container> it = depotInvs.iterator();
		while(it.hasNext()){
			Container c = it.next();
			// Check to make sure this inventory is still valid
			if(c.getBlock().getState() instanceof Container == false){it.remove(); continue;}
			int idx;
			while((idx = c.getInventory().first(item.getType())) != -1){
				ItemStack i = c.getInventory().getItem(idx);
				if(i.getAmount() < item.getAmount()){
					item.setAmount(item.getAmount() - i.getAmount());
					i.setType(Material.AIR);
					continue;
				}
				else if(i.getAmount() > item.getAmount()) i.setAmount(i.getAmount() - item.getAmount());
				else i.setType(Material.AIR);
				return true;
			}
		}
		return false;
	}
	public boolean takeItem(Material mat){
		Iterator<Container> it = depotInvs.iterator();
		while(it.hasNext()){
			Container c = it.next();
			// Check to make sure this inventory is still valid
			if(c.getBlock().getState() instanceof Container == false){it.remove(); continue;}
			int idx = c.getInventory().first(mat);
			if(idx != -1){
				ItemStack i = c.getInventory().getItem(idx);
				if(i.getAmount() == 1) i.setType(Material.AIR);
				else i.setAmount(i.getAmount() - 1);
				return true;
			}
		}
		return false;
	}
	public ItemStack addItem(ItemStack item){
		Iterator<Container> it = depotInvs.descendingIterator();
		while(it.hasNext()){
			Container c = it.next();
			// Check to make sure this inventory is still valid
			if(c.getBlock().getState() instanceof Container == false){it.remove(); continue;}
			// check == Hopper (so that this doesn't break item filters)
			if((c instanceof Hopper) && c.getInventory().contains(item.getType())){
				HashMap<Integer, ItemStack> leftovers = c.getInventory().addItem(item);
				if(leftovers.isEmpty()) return null;
				if(leftovers.size() > 1) Renewable.getPlugin().getLogger()
						.info("Unexpected leftovers in addToCreativeSupply()!");
				item = leftovers.values().iterator().next();
			}
		}
		it = depotInvs.descendingIterator();
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
		Iterator<Container> it = depotInvs.descendingIterator();
		while(it.hasNext()){
			Container c = it.next();
			// Check to make sure this inventory is still valid
			if(c.getBlock().getState() instanceof Container == false){it.remove(); continue;}
			// check == Hopper (so that this doesn't break item filters)
			if((c instanceof Hopper) && c.getInventory().contains(mat)){
				if(it.next().getInventory().addItem(new ItemStack(mat, 1)).isEmpty()) return true;
			}
		}
		it = depotInvs.descendingIterator();
		while(it.hasNext()){// Try again but less picky (stick it anywhere it'll fit)
			if(it.next().getInventory().addItem(new ItemStack(mat, 1)).isEmpty()) return true;
		}
		return false;
	}
}