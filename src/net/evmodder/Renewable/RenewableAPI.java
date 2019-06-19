package net.evmodder.Renewable;

import java.util.HashMap;
import java.util.UUID;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.Chest;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.BookMeta.Generation;
import net.evmodder.EvLib.EvUtils;
import net.evmodder.EvLib.extras.TypeUtils;

public class RenewableAPI{
//	final boolean UNRENEWABLE_LAVA, UNRENEWABLE_DIA_ARMOR, UNRENEWABLE_MOB, UNRENEWABLE_GRAVITY, UNRENEWABLE_UNGET;
	final boolean DO_ITEM_RESCUE, DO_STANDARDIZE, DO_GM1_SOURCING, PUNISH_FOR_RENEWABLE;
	static boolean SILK_SPAWNERS; static int SILK_SPAWNER_REQ_LVL;
	final String PUNISH_COMMAND;
	final Location rescueLoc;
	final ItemSupplyDepot crSupply;
	final Renewable pl;
	final RenewableStandardizer standardizer;
	final RenewableChecker ren;

	RenewableAPI(Renewable pl){
		this.pl = pl;
		ren = new RenewableChecker(pl);
		// Standardizing
		DO_STANDARDIZE = pl.getConfig().getBoolean("standardize-rescued-items", true);
		standardizer = DO_STANDARDIZE ? new RenewableStandardizer(pl) : null;
		// Spawners
		SILK_SPAWNERS = pl.getConfig().getBoolean("silktouch-spawners", false);
		SILK_SPAWNER_REQ_LVL = pl.getConfig().getInt("silktouch-level", 1);
		// Rescue & Punish
		PUNISH_COMMAND = pl.getConfig().getString("punish-command", "");
		PUNISH_FOR_RENEWABLE = pl.getConfig().getBoolean("punish-rescued-renewables", false);
		DO_ITEM_RESCUE = pl.getConfig().getBoolean("rescue-items", true);
		rescueLoc = EvUtils.getLocationFromString(pl.getConfig().getString("store-items-at"));
		if(DO_ITEM_RESCUE && rescueLoc == null) pl.getLogger().warning("Unable to parse item rescue location!");
		// Locations
		DO_GM1_SOURCING = pl.getConfig().getBoolean("creative-unrenewable-supply", true);
		if(DO_GM1_SOURCING){
			pl.getLogger().info("CrSupply specified, searching for depot...");
			Location crSupplyLoc = EvUtils.getLocationFromString(pl.getConfig().getString("creative-supply-at"));
			if(crSupplyLoc == null){
				pl.getLogger().warning("Unable to parse creative supply location!");
				crSupplyLoc = rescueLoc;//Try this instead as a backup plan
			}
			crSupply = crSupplyLoc == null ? null : new ItemSupplyDepot(crSupplyLoc);
			if(crSupply != null) pl.getLogger().info("Found depot. Size in blocks: "+crSupply.depotInvs.size());
		}
		else crSupply = null;
	}

	public void punish(UUID uuid, Material mat){
		//These items are marked as unrenewable so that they will be rescued, but aren't actually unrenewable
		if(!PUNISH_FOR_RENEWABLE && RenewableChecker.rescueList.contains(mat)) return /*false*/;

		if(uuid == null){
			pl.getLogger().info("Unrenewable item destroyed, no player detected!");
//			return false;
		}
		else{
//			boolean success = true;
			for(String command : PUNISH_COMMAND.split("\n")){
				command = command.replaceAll("%name%", pl.getServer().getPlayer(uuid).getName())
								.replaceAll("%type%", mat.name());
				pl.getLogger().info("Executing command: "+command);
				if(!pl.getServer().dispatchCommand(pl.getServer().getConsoleSender(), command))/*success = false*/;
			}
//			return success;
		}
	}

	public void rescueItem(ItemStack item){
		if(rescueLoc == null){
			pl.getLogger().warning("Invalid rescue 'store-items-at' location: "
					+pl.getConfig().getString("store-items-at"));
			if(DO_STANDARDIZE) standardizer.addRescuedParts(item.getType(), item.getAmount(), 1);
		}
		if(!PUNISH_COMMAND.isEmpty()) item = ItemTaggingUtil.unflag(item);
		pl.getLogger().fine("Rescuing: "+item.getType());

		if(item.getType() == Material.WRITTEN_BOOK){
			BookMeta meta = (BookMeta) item.getItemMeta();
			meta.setGeneration(Generation.TATTERED);
			item.setItemMeta(meta);
		}
		else if(DO_STANDARDIZE){
			item = standardizer.standardize(item, true);
			pl.getLogger().fine("Standardized: "+item.getType());
			if(item.getAmount() == 0) return;
		}

		Block block = rescueLoc.getBlock();
		if(block.getType() != Material.CHEST) block.setType(Material.CHEST);
		Chest chest = (Chest) block.getState();
		HashMap<Integer, ItemStack> extras = chest.getBlockInventory().addItem(item);
		while(!extras.isEmpty() && block.getY() < 256){
			block = block.getRelative(BlockFace.UP);
			if(block.getType() != Material.CHEST) break;
			chest = (Chest) block.getState();
			extras = chest.getBlockInventory().addItem(extras.values().toArray(new ItemStack[]{}));
		}
		if(!extras.isEmpty()){
			for(ItemStack extra : extras.values()) block.getWorld().dropItem(rescueLoc, extra);
		}
	}

	public boolean deductFromCreativeSupply(ItemStack item){
		if(crSupply == null) return false;
		item = DO_STANDARDIZE ? standardizer.standardize(item, false) : item;
		if(item.getAmount() == 0) return true;
		pl.getLogger().info("Deducting from CrSupply: "+item.getType()+"x"+item.getAmount());
		return crSupply.takeItem(item);
	}
	public boolean deductFromCreativeSupply(Material mat){
		if(crSupply == null) return false;
		if(DO_STANDARDIZE){
			ItemStack item = standardizer.standardize(new ItemStack(mat), false);
			if(item.getAmount() == 0) return true;
			pl.getLogger().info("Deducting from CrSupply: "+item.getType()+"x"+item.getAmount());
			if(item.getAmount() == 1) return crSupply.takeItem(item.getType());
			else return crSupply.takeItem(item);
		}
		pl.getLogger().fine("Deducting from CrSupply: "+mat);
		return crSupply.takeItem(mat);
	}
	public ItemStack addToCreativeSupply(ItemStack item){
		if(crSupply == null) return item;
		item = DO_STANDARDIZE ? standardizer.standardize(item, true) : item;
		if(item.getAmount() == 0) return null;
		pl.getLogger().info("Adding to CrSupply: "+item.getType()+"x"+item.getAmount());
		return crSupply.addItem(item);
	}
	public ItemStack addToCreativeSupply(Material mat){
		if(crSupply == null) return new ItemStack(mat);
		if(DO_STANDARDIZE){
			ItemStack item = standardizer.standardize(new ItemStack(mat), true);
			if(item.getAmount() == 0) return null;
			pl.getLogger().info("Adding to CrSupply: "+item.getType()+"x"+item.getAmount());
			if(item.getAmount() == 1) return crSupply.addItem(item.getType()) ? null : item;
			else return crSupply.addItem(item);
		}
		pl.getLogger().info("Adding to CrSupply: "+mat);
		return crSupply.addItem(mat) ? null : new ItemStack(mat);
	}

	public boolean isUnrenewable(ItemStack item){return ren.isUnrenewableItem(item);}
	public boolean isUnrenewable(BlockState b){return ren.isUnrenewableBlock(b.getType(), b.getBlockData());}
	public boolean isUnrenewableProcess(ItemStack in, ItemStack out){return ren.isIrreversibleProcess(in, out);}
	public boolean isUnrenewableProcess(BlockState in, BlockState out){return ren.isIrreversibleProcess(
				in.getType(), in.getBlockData(), out.getType(), out.getBlockData());}

	public static ItemStack getUnewnewableItemForm(BlockState block){
		switch(block.getType()){
			case LAVA:
				return new ItemStack(Material.LAVA_BUCKET);
			case SPAWNER:
				ItemStack item = new ItemStack(Material.SPAWNER);
				BlockStateMeta meta = (BlockStateMeta) item.getItemMeta();
				meta.setBlockState(block);
				String name = EvUtils.getNormalizedName(((CreatureSpawner)block).getSpawnedType());
				meta.setDisplayName(ChatColor.WHITE+name+" Spawner");
				item.setItemMeta(meta);
				return item;
			default:
				ItemStack is = new ItemStack(block.getType());
				if(block.getData() != null) is.setData(block.getData());
				return is;
		}
	}

	public static boolean willDropSelf(Material mat, Material tool, int silkLvl){
		switch(mat){
			case TERRACOTTA:
			case GRANITE:
			case GRANITE_SLAB:
			case GRANITE_STAIRS:
			case GRANITE_WALL:
			case POLISHED_GRANITE:
			case POLISHED_GRANITE_SLAB:
			case POLISHED_GRANITE_STAIRS:
			case DIORITE:
			case DIORITE_SLAB:
			case DIORITE_STAIRS:
			case DIORITE_WALL:
			case POLISHED_DIORITE:
			case POLISHED_DIORITE_SLAB:
			case POLISHED_DIORITE_STAIRS:
			case ANDESITE:
			case ANDESITE_SLAB:
			case ANDESITE_STAIRS:
			case ANDESITE_WALL:
			case POLISHED_ANDESITE:
			case POLISHED_ANDESITE_SLAB:
			case POLISHED_ANDESITE_STAIRS:
			case ENCHANTING_TABLE:
			case OBSERVER:
			case NETHERRACK:
			case NETHER_BRICKS:
			case NETHER_BRICK_SLAB:
			case NETHER_BRICK_STAIRS:
			case NETHER_BRICK_FENCE:
			case NETHER_BRICK_WALL:
			case RED_NETHER_BRICKS:
			case RED_NETHER_BRICK_SLAB:
			case RED_NETHER_BRICK_STAIRS:
			case RED_NETHER_BRICK_WALL:
			case QUARTZ_BLOCK:
			case QUARTZ_STAIRS:
			case SMOOTH_QUARTZ:
			case SMOOTH_QUARTZ_SLAB:
			case SMOOTH_QUARTZ_STAIRS:
				return EvUtils.pickIsAtLeast(tool, Material.WOODEN_PICKAXE);
			case COAL_ORE:
			case NETHER_QUARTZ_ORE:
				return silkLvl > 0 && EvUtils.pickIsAtLeast(tool, Material.WOODEN_PICKAXE);
			case IRON_ORE:
				return EvUtils.pickIsAtLeast(tool, Material.STONE_PICKAXE);
			case LAPIS_ORE:
				return silkLvl > 0 && EvUtils.pickIsAtLeast(tool, Material.STONE_PICKAXE);
			case GOLD_ORE:
			case DIAMOND_BLOCK:
				return EvUtils.pickIsAtLeast(tool, Material.IRON_PICKAXE);
			case REDSTONE_ORE:
			case DIAMOND_ORE:
			case EMERALD_ORE:
				return silkLvl > 0 && EvUtils.pickIsAtLeast(tool, Material.IRON_PICKAXE);
			case COBWEB:
				return tool == Material.SHEARS ||
					(silkLvl > 0 && EvUtils.swordIsAtLeast(tool, Material.WOODEN_SWORD));
			case DEAD_BUSH:
				return tool == Material.SHEARS;
//			case PACKED_ICE:
//				return silkLvl > 0;
			case SPAWNER:
				return SILK_SPAWNERS && silkLvl >= SILK_SPAWNER_REQ_LVL;
			case BEDROCK:
			case COMMAND_BLOCK:
			case CHAIN_COMMAND_BLOCK:
			case REPEATING_COMMAND_BLOCK:
			case END_PORTAL:
			case END_PORTAL_FRAME:
			case BARRIER:
			case STRUCTURE_BLOCK:
			case STRUCTURE_VOID:
			case JIGSAW:
				return false;
			default:
				if(TypeUtils.isConcrete(mat) || TypeUtils.isTerracotta(mat) || TypeUtils.isGlazedTerracotta(mat))
					return EvUtils.pickIsAtLeast(tool, Material.WOODEN_PICKAXE);
				return true;
		}
	}

	//For irreversible processes: takes two unrenewable items as input
	public boolean sameWhenStandardized(ItemStack a, ItemStack b){
		return standardizer.standardize(a, true).equals(standardizer.standardize(b, true));
	}

	//For irreversible processes: takes two unrenewable items as input
	public boolean sameWhenStandardizedIgnoreAmt(ItemStack a, ItemStack b){
		ItemStack stdA = standardizer.standardize(a, true).clone(), stdB = standardizer.standardize(b, true).clone();
		stdA.setAmount(1); stdB.setAmount(1);
		return stdA.equals(stdB);
	}
}