package net.evmodder.Renewable;

import java.util.Arrays;
import java.util.HashMap;
import java.util.UUID;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.Container;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.SculkShrieker;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.BookMeta.Generation;
import net.evmodder.EvLib.TextUtils;
import net.evmodder.EvLib.bukkit.TypeUtils;

public class RenewableAPI{
//	final boolean UNRENEWABLE_LAVA, UNRENEWABLE_DIA_ARMOR, UNRENEWABLE_MOB, UNRENEWABLE_GRAVITY, UNRENEWABLE_UNGET;
	final private boolean DO_ITEM_RESCUE, DO_STANDARDIZE, DO_GM1_SOURCING, PUNISH_FOR_RENEWABLE;
	static boolean SILK_SPAWNERS; static int SILK_SPAWNER_REQ_LVL;
	final private String PUNISH_DESTROYED, PUNISH_IRREVERSIBLE;
	final private Location RESCUE_LOC;
	final private ItemSupplyDepot crSupply;
	final private Renewable pl;
	final RenewableStandardizer standardizer; // Accessed by Renewable.java
	final private RenewableChecker checker;

	RenewableAPI(Renewable pl){
		this.pl = pl;
		checker = new RenewableChecker(pl);
		// Standardizing
		DO_STANDARDIZE = pl.getConfig().getBoolean("standardize-rescued-items", true);
		standardizer = DO_STANDARDIZE ? new RenewableStandardizer(pl) : null;
		// Spawners
		SILK_SPAWNERS = pl.getConfig().getBoolean("silktouch-spawners", false);
		SILK_SPAWNER_REQ_LVL = pl.getConfig().getInt("silktouch-level", 1);
		// Punishments
		PUNISH_DESTROYED = pl.getConfig().getString("unrenewable-destroyed-trigger", "");
		PUNISH_IRREVERSIBLE = pl.getConfig().getString("irreversible-process-trigger", "");
		PUNISH_FOR_RENEWABLE = pl.getConfig().getBoolean("punish-rescued-renewables", false);
		// Rescue & CrSupply
		DO_ITEM_RESCUE = pl.getConfig().getBoolean("rescue-items", true);
		RESCUE_LOC = TextUtils.getLocationFromString(pl.getConfig().getString("store-items-at"));
		if(DO_ITEM_RESCUE && RESCUE_LOC == null) pl.getLogger().warning("Unable to parse item rescue location!");
		DO_GM1_SOURCING = pl.getConfig().getBoolean("creative-unrenewable-supply", true);
		if(DO_GM1_SOURCING){
			pl.getLogger().info("CrSupply specified, searching for depot...");
			Location crSupplyLoc = TextUtils.getLocationFromString(pl.getConfig().getString("creative-supply-at"));
			if(crSupplyLoc == null){
				pl.getLogger().warning("Unable to parse creative supply location!");
				crSupplyLoc = RESCUE_LOC;//Try this instead as a backup plan
			}
			crSupply = crSupplyLoc == null ? null : new ItemSupplyDepot(pl, crSupplyLoc);
		}
		else crSupply = null;
	}

	public void punishDestroyed(UUID uuid, Material mat){
		//These items are marked as unrenewable so that they will be rescued, but aren't actually unrenewable
		if(!PUNISH_FOR_RENEWABLE && RenewableChecker.rescueList.contains(mat)) return /*false*/;

		if(uuid == null){
			pl.getLogger().info("Unrenewable destroyed, no player detected!");
//			return false;
		}
		else{
//			boolean success = true;
			for(String command : PUNISH_DESTROYED.split("\n")){
				command = command.replaceAll("%name%", pl.getServer().getPlayer(uuid).getName()).replaceAll("%type%", mat.name());
				pl.getLogger().fine("Executing command: "+command);
				if(!pl.getServer().dispatchCommand(pl.getServer().getConsoleSender(), command))/*success = false*/;
			}
//			return success;
		}
	}
	public void punishIrreversible(UUID uuid, Material mat){
		if(uuid == null){
			pl.getLogger().info("Irreversible process, no player detected!");
//			return false;
		}
		else{
//			boolean success = true;
			for(String command : PUNISH_IRREVERSIBLE.split("\n")){
				command = command.replaceAll("%name%", pl.getServer().getPlayer(uuid).getName()).replaceAll("%type%", mat.name());
				pl.getLogger().fine("Executing command: "+command);
				if(!pl.getServer().dispatchCommand(pl.getServer().getConsoleSender(), command))/*success = false*/;
			}
//			return success;
		}
	}

	public void rescueItem(ItemStack item){
		if(!DO_ITEM_RESCUE) pl.getLogger().warning("BUG: rescueItems() called even though config `rescue-items`=false!");

		if(RESCUE_LOC == null){
			pl.getLogger().warning("Invalid rescue 'store-items-at' location: "+pl.getConfig().getString("store-items-at"));
			if(DO_STANDARDIZE) standardizer.addRescuedParts(item.getType(), item.getAmount(), 1); // store in rescuedParts for now
		}
		pl.getLogger().fine("Rescuing: "+item.getType());

		if(item.getType() == Material.WRITTEN_BOOK){
			BookMeta meta = (BookMeta) item.getItemMeta();
			meta.setGeneration(Generation.TATTERED);
			item.setItemMeta(meta);
		}
		else if(DO_STANDARDIZE){
			item = standardizer.standardizeWithExtras(item, /*add=*/true);
			pl.getLogger().fine("Standardized: "+item.getType());
			if(item.getAmount() == 0) return;
		}

		// Store the item(s)
		Block block = RESCUE_LOC.getBlock();
		if(block.getState() instanceof Container == false) block.setType(Material.BARREL);
		Container container = (Container)block.getState();
		HashMap<Integer, ItemStack> extras = container.getInventory().addItem(item);
		while(!extras.isEmpty() && block.getY() < 256){
			block = block.getRelative(BlockFace.UP);
			if(block.getState() instanceof Container == false) break;
			container = (Container)block.getState();
			extras = container.getInventory().addItem(extras.values().toArray(new ItemStack[]{}));
		}
		if(!extras.isEmpty()) for(ItemStack extra : extras.values()) block.getWorld().dropItem(RESCUE_LOC, extra);
	}

	public boolean takeFromCreativeSupply(ItemStack item){
		if(crSupply == null) return false;
		item = DO_STANDARDIZE ? standardizer.standardizeWithExtras(item, /*add=*/false) : item;
		if(item.getAmount() == 0) return true;
		pl.getLogger().info("Deducting from CrSupply: "+item.getType()+"x"+item.getAmount());
		return crSupply.takeItem(item);
	}
	public boolean takeFromCreativeSupply(Material mat){
		if(crSupply == null) return false;
		if(DO_STANDARDIZE){
			ItemStack item = standardizer.standardizeWithExtras(new ItemStack(mat), /*add=*/false);
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
		item = DO_STANDARDIZE ? standardizer.standardizeWithExtras(item, /*add=*/true) : item;
		if(item.getAmount() == 0) return null;
		pl.getLogger().info("Adding to CrSupply: "+item.getType()+"x"+item.getAmount());
		return crSupply.addItem(item);
	}
	public ItemStack addToCreativeSupply(Material mat){
		if(crSupply == null) return new ItemStack(mat);
		if(DO_STANDARDIZE){
			ItemStack item = standardizer.standardizeWithExtras(new ItemStack(mat), /*add=*/true);
			if(item.getAmount() == 0) return null;
			pl.getLogger().info("Adding to CrSupply: "+item.getType()+"x"+item.getAmount());
			if(item.getAmount() == 1) return crSupply.addItem(item.getType()) ? null : item;
			else return crSupply.addItem(item);
		}
		pl.getLogger().info("Adding to CrSupply: "+mat);
		return crSupply.addItem(mat) ? null : new ItemStack(mat);
	}

	public boolean isUnrenewable(ItemStack item){return checker.isUnrenewableItem(item);}
	public boolean isUnrenewable(BlockData b){return checker.isUnrenewableBlock(b.getMaterial(), b);}
	public boolean isUnrenewableProcess(ItemStack in, ItemStack out){return checker.isIrreversibleProcess(in, out);}
	public boolean isUnrenewableProcess(BlockState in, BlockState out){return checker.isIrreversibleProcess(
				in.getType(), in.getBlockData(), out.getType(), out.getBlockData());}

	public boolean willDropSelf(BlockData data, Material tool, int silkLvl){
		final Material mat = data.getMaterial();
		switch(mat){
			case ENCHANTING_TABLE:
			case NETHERRACK:
			case TUFF:
			case CALCITE:
			case DEEPSLATE:
			case COBBLED_DEEPSLATE:
			case COBBLED_DEEPSLATE_SLAB:
			case COBBLED_DEEPSLATE_STAIRS:
			case COBBLED_DEEPSLATE_WALL:
			case CHISELED_DEEPSLATE:
			case POLISHED_DEEPSLATE:
			case POLISHED_DEEPSLATE_SLAB:
			case POLISHED_DEEPSLATE_STAIRS:
			case POLISHED_DEEPSLATE_WALL:
			case DEEPSLATE_BRICKS:
			case CRACKED_DEEPSLATE_BRICKS:
			case DEEPSLATE_BRICK_SLAB:
			case DEEPSLATE_BRICK_STAIRS:
			case DEEPSLATE_BRICK_WALL:
			case DEEPSLATE_TILES:
			case CRACKED_DEEPSLATE_TILES:
			case DEEPSLATE_TILE_SLAB:
			case DEEPSLATE_TILE_STAIRS:
			case DEEPSLATE_TILE_WALL:
			case INFESTED_DEEPSLATE:
				return TypeUtils.pickIsAtLeast(tool, Material.WOODEN_PICKAXE);
			case COAL_ORE:
				return silkLvl > 0 && TypeUtils.pickIsAtLeast(tool, Material.WOODEN_PICKAXE);
			case RAW_IRON_BLOCK:
			case RAW_COPPER_BLOCK:
				return TypeUtils.pickIsAtLeast(tool, Material.STONE_PICKAXE);
			case IRON_ORE:
			case COPPER_ORE:
			case LAPIS_ORE:
				return silkLvl > 0 && TypeUtils.pickIsAtLeast(tool, Material.STONE_PICKAXE);
			case DIAMOND_BLOCK:
			case RAW_GOLD_BLOCK:
				return TypeUtils.pickIsAtLeast(tool, Material.IRON_PICKAXE);
			case GOLD_ORE:
			case REDSTONE_ORE:
			case DIAMOND_ORE:
			case EMERALD_ORE:
				return silkLvl > 0 && TypeUtils.pickIsAtLeast(tool, Material.IRON_PICKAXE);
			case ANCIENT_DEBRIS:
			case NETHERITE_BLOCK:
				return TypeUtils.pickIsAtLeast(tool, Material.DIAMOND_PICKAXE);
			case COBWEB:
				return tool == Material.SHEARS || (silkLvl > 0 && TypeUtils.isSword(tool));
			case DEAD_BUSH:
				return tool == Material.SHEARS;
			case SPAWNER:
				return SILK_SPAWNERS && silkLvl >= SILK_SPAWNER_REQ_LVL;
			case SCULK_SHRIEKER:
				return !((SculkShrieker)data).isCanSummon();
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
			case REINFORCED_DEEPSLATE:
				return false;
			default:
				if(TypeUtils.isConcrete(mat) || TypeUtils.isTerracotta(mat) || TypeUtils.isGlazedTerracotta(mat))
					return TypeUtils.pickIsAtLeast(tool, Material.WOODEN_PICKAXE);
				return true;
		}
	}

	public boolean sameWhenStandardized(ItemStack a, ItemStack b){
		return Arrays.deepEquals(standardizer.standardize(a), standardizer.standardize(b));
	}

	public static ItemStack getUnewnewableItemForm(BlockState block){
		switch(block.getType()){
			case SPAWNER: {
				final ItemStack item = new ItemStack(Material.SPAWNER);
				final BlockStateMeta meta = (BlockStateMeta)item.getItemMeta();
				meta.setBlockState(block);
				@SuppressWarnings("deprecation") //TODO: Come up with a translated/localized solution for this!!
				final String name = TextUtils.getNormalizedName(((CreatureSpawner)block).getSpawnedType());
				meta.setDisplayName(ChatColor.WHITE+name+" Spawner");
				item.setItemMeta(meta);
				return item;
			}
			case SCULK_SHRIEKER: {
				final ItemStack item = new ItemStack(block.getType());
				if(block.getData() != null) item.setData(block.getData());
				if(((SculkShrieker)block.getBlockData()).isCanSummon()){
					final BlockStateMeta meta = (BlockStateMeta)item.getItemMeta();
					((SculkShrieker)meta.getBlockState().getBlockData()).setCanSummon(true);
					meta.setLore(Arrays.asList(TextUtils.locationToString(block.getLocation(), ChatColor.BLUE, ChatColor.GRAY)));
					item.setItemMeta(meta);
				}
				return item;
			}
			default: {
				final ItemStack item = new ItemStack(block.getType());
				if(block.getData() != null) item.setData(block.getData());
				return item;
			}
		}
	}
}