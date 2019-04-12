package Evil_Code_Renewable;

import java.util.HashSet;
import org.bukkit.Material;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Levelled;
import org.bukkit.inventory.ItemStack;
import EvLib.TypeUtils;
import EvLib.UnionFind;

public class RenewableChecker{
	static final HashSet<Material> rescueList = new HashSet<Material>();
	static final UnionFind<Material> reversible = new UnionFind<Material>();
	static{
		reversible.add(Material.DIRT);
		reversible.addToSet(Material.GRASS_BLOCK, Material.DIRT);
		reversible.addToSet(Material.FARMLAND, Material.DIRT);
		reversible.addToSet(Material.MYCELIUM, Material.DIRT);
		reversible.addToSet(Material.GRASS_PATH, Material.DIRT);
		reversible.addToSet(Material.PODZOL, Material.DIRT);

		reversible.add(Material.CLAY);
		reversible.addToSet(Material.CLAY_BALL, Material.CLAY);

		reversible.add(Material.DIAMOND);
		reversible.addToSet(Material.DIAMOND_BLOCK, Material.DIAMOND);

//		reversible.add(new ItemDesc(Material.FLOWER_POT));
//		reversible.addToSet(new ItemDesc(Material.FLOWER_POT_ITEM), new ItemDesc(Material.FLOWER_POT));

//		reversible.add(new ItemDesc(Material.REDSTONE_COMPARATOR));
//		reversible.addToSet(new ItemDesc(Material.REDSTONE_COMPARATOR_ON), new ItemDesc(Material.REDSTONE_COMPARATOR));
//		reversible.addToSet(new ItemDesc(Material.REDSTONE_COMPARATOR_OFF), new ItemDesc(Material.REDSTONE_COMPARATOR));

		reversible.add(Material.CHISELED_SANDSTONE);//Chiseled Sandstone & slabs
		reversible.addToSet(Material.SANDSTONE_SLAB, Material.CHISELED_SANDSTONE);
		//reversible.addToSet(Material.DOUBLE_SANDSTONE_SLAB, Material.CHISELED_SANDSTONE);

		reversible.add(Material.CHISELED_RED_SANDSTONE);//Red Chiseled Sandstone & slabs
		reversible.addToSet(Material.RED_SANDSTONE_SLAB, Material.CHISELED_RED_SANDSTONE);
		//reversible.addToSet(Material.DOUBLE_RED_SANDSTONE_SLAB, Material.CHISELED_RED_SANDSTONE);

		reversible.add(Material.BRICK_SLAB);//Brick slabs & d_slabs
		//reversible.addToSet(Material.DOUBLE_BRICK_SLAB, Material.BRICK_SLAB);

		reversible.add(Material.NETHER_BRICK_SLAB);//Netherbrick slabs & d_slabs
		//reversible.addToSet(Material.DOUBLE_NETHER_BRICK_SLAB, Material.NETHER_BRICK_SLAB);

		reversible.add(Material.QUARTZ_SLAB);//Chiseled Quartz & Slabs
		reversible.addToSet(Material.CHISELED_QUARTZ_BLOCK, Material.QUARTZ_SLAB);
		//reversible.addToSet(Material.DOUBLE_QUARTZ_SLAB, Material.QUARTZ_SLAB);

//		reversible.add(new ItemDesc(Material.PURPUR_SLAB));//Purpur slabs & d_slabs //NOTE: Purpur is renewable!
//		reversible.addToSet(new ItemDesc(Material.PURPUR_DOUBLE_SLAB), new ItemDesc(Material.PURPUR_SLAB));

		reversible.add(Material.SPONGE);//Sponge & WetSponge
		reversible.addToSet(Material.WET_SPONGE, Material.SPONGE);

		reversible.add(Material.TNT);//TNT & TNT Minecart
		reversible.addToSet(Material.TNT_MINECART, Material.TNT);
	}

	final boolean UNRENEWABLE_LAVA, UNRENEWABLE_DIA_ARMOR, UNRENEWABLE_MOBS, UNRENEWABLE_GRAVITY;
	final boolean INCLUDE_UNOBT, OBT_SPAWNERS, OBT_MOB_EGGS, OBT_INFESTED, OBT_SMOOTH_BRICKS, OBT_CMD_BLOCKS,
					OBT_BEDROCK, OBT_END_PORTALS, OBT_BARRIERS, OBT_STRUCTURE_BLOCKS;
	RenewableChecker(Renewable pl){
		UNRENEWABLE_LAVA = !pl.getConfig().getBoolean("renewable-lava", true);
		UNRENEWABLE_DIA_ARMOR = !pl.getConfig().getBoolean("renewable-diamond-armor", true);
		UNRENEWABLE_MOBS =  !pl.getConfig().getBoolean("renewable-mob-drops", false);
		UNRENEWABLE_GRAVITY = !pl.getConfig().getBoolean("renewable-gravity-blocks", false);
		//
		INCLUDE_UNOBT = !pl.getConfig().getBoolean("ignore-unobtainable-items", false);
		OBT_SPAWNERS = pl.getConfig().getBoolean("spawners-obtainable", false);
		OBT_MOB_EGGS = pl.getConfig().getBoolean("spawn-eggs-obtainable", false);
		OBT_INFESTED = pl.getConfig().getBoolean("infested-blocks-obtainable", false);
		OBT_SMOOTH_BRICKS = pl.getConfig().getBoolean("smooth-bricks-obtainable", false);
		OBT_CMD_BLOCKS = pl.getConfig().getBoolean("command-blocks-obtainable", false);
		OBT_BEDROCK = pl.getConfig().getBoolean("bedrock-obtainable", false);
		OBT_END_PORTALS = pl.getConfig().getBoolean("end-portals-obtainable", false);
		OBT_BARRIERS = pl.getConfig().getBoolean("barriers-obtainable", false); 
		OBT_STRUCTURE_BLOCKS = pl.getConfig().getBoolean("structure-blocks-obtainable", false);
		//
		for(String name : pl.getConfig().getStringList("rescued-renewables")){
			try{ rescueList.add(Material.valueOf(name.toUpperCase())); }
			catch(IllegalArgumentException ex){}
		}
		pl.getLogger().fine("Gravity Unrenewable: "+UNRENEWABLE_GRAVITY);
	}

	// Calls isUnrenewableBlock()
	boolean isUnrenewableItem(ItemStack item){
		//Note: (Somewhat) Sorted by ID, from least to greatest
		switch(item.getType()){
			case DIAMOND:
			case DIAMOND_SHOVEL:
			case DIAMOND_HOE:
			case BRICK:
			case CLAY_BALL:
//			case LAPIS_LAZULI://Note: renewable (villagers)
//			case GLASS_BOTTLE://Note: renewable (villagers & witches)
			case NETHER_BRICK:
			case QUARTZ:
			case IRON_HORSE_ARMOR:
			case GOLDEN_HORSE_ARMOR:
			case DIAMOND_HORSE_ARMOR:
			case ELYTRA:
			case ENCHANTED_GOLDEN_APPLE:
				return true;
			case TOTEM_OF_UNDYING:
			case SHULKER_SHELL:
			case NETHER_STAR:
				return UNRENEWABLE_MOBS;
			case DIAMOND_HELMET:
//			case DIAMOND_CHESTPLATE://Note: renewable (villagers)
			case DIAMOND_LEGGINGS:
			case DIAMOND_BOOTS:
				return UNRENEWABLE_DIA_ARMOR;
			case FLINT:
			case FLINT_AND_STEEL:
			case TNT_MINECART:
				return UNRENEWABLE_GRAVITY;
			case LAVA_BUCKET:
				return UNRENEWABLE_LAVA;
			case COMMAND_BLOCK_MINECART:
				return INCLUDE_UNOBT && !OBT_CMD_BLOCKS;
			default:
				if(TypeUtils.isSpawnEgg(item.getType())) return INCLUDE_UNOBT && !OBT_MOB_EGGS;
				return isUnrenewableBlock(item.getType(), null);
		}	
	}

	// Blocks can come as ItemStacks, but Items can't come as BlockStates
	// Thus, if our input is a Block, we know it can't be an item
	boolean isUnrenewableBlock(Material mat, BlockData data){
		//Custom list of (renewable) items to rescue (considered unrenewable)
		if(rescueList.contains(mat)) return true;

		//Note: (Somewhat) Sorted by ID, from least to greatest
		switch(mat){
			case GRANITE:
			case DIORITE:
			case ANDESITE:
			case GRASS_BLOCK:
			case GRASS_PATH:
			case FARMLAND:
			case DIRT:
			case PODZOL:
			case COARSE_DIRT:
			case SPONGE:
//			case GLASS://Note: glass is renewable! (Villagers)
			case ENCHANTING_TABLE:
			case JUKEBOX:
			case FLOWER_POT:
			case DIAMOND_BLOCK:
			case COMPARATOR:
			case OBSERVER:
			case DAYLIGHT_DETECTOR:
			case COBWEB:
			case DEAD_BUSH:
//			case MOSSY_COBBLESTONE://Note: renewable! (Vines)
			case CLAY:
			case BRICKS:
			case BRICK_STAIRS://Note: Same (red) brick type as above, just as stairs
			case NETHERRACK:
			case SOUL_SAND:
			case MYCELIUM://Note: Since dirt is unrenewable, this is as well.
			case NETHER_BRICK:
			case NETHER_BRICK_STAIRS:
			case NETHER_BRICK_FENCE:
			case RED_NETHER_BRICKS:
//			case ENDSTONE://Note: renewable! :o (When dragon is respawned, endstone under platform)
			case QUARTZ_BLOCK:
			case QUARTZ_STAIRS:
//			case PACKED_ICE://Note: craftable with ice in 1.13
//			case MAGMA_BLOCK://Note: renewable! (4 magma cream)
				return true;
			case SAND://Note: Sand and Red Sand are considered unrenewable.
			case GRAVEL:
			case DRAGON_EGG:
			case DRAGON_HEAD:
			case SANDSTONE:
			case RED_SANDSTONE:
			case SANDSTONE_SLAB:
			case RED_SANDSTONE_SLAB:
			case SANDSTONE_STAIRS:
			case RED_SANDSTONE_STAIRS:
			case TNT:
				return UNRENEWABLE_GRAVITY;
			case LAVA://Flowing lava is renewable
				return UNRENEWABLE_LAVA && ((Levelled)data).getLevel() == 0;
			case BEACON:
				return UNRENEWABLE_MOBS;
			case SMOOTH_SANDSTONE://TODO: these will be renewable (via smelt) in 1.14!
			case SMOOTH_RED_SANDSTONE://TODO: ^
			case SMOOTH_STONE://TODO: ^
				return INCLUDE_UNOBT && !OBT_SMOOTH_BRICKS;
			case SPAWNER:
				return INCLUDE_UNOBT && !OBT_SPAWNERS;
			case BEDROCK:
				return INCLUDE_UNOBT && !OBT_BEDROCK;
			case END_PORTAL:
			case END_PORTAL_FRAME:
				return INCLUDE_UNOBT && !OBT_END_PORTALS;
			case BARRIER:
				return INCLUDE_UNOBT && !OBT_BARRIERS;
			case COMMAND_BLOCK:
			case CHAIN_COMMAND_BLOCK:
			case REPEATING_COMMAND_BLOCK:
				return INCLUDE_UNOBT && !OBT_CMD_BLOCKS;
			case STRUCTURE_BLOCK:
			case STRUCTURE_VOID:
				return INCLUDE_UNOBT && !OBT_STRUCTURE_BLOCKS;
			default:
				if(TypeUtils.isConcrete(mat) || TypeUtils.isConcretePowder(mat)) return UNRENEWABLE_GRAVITY;
				if(TypeUtils.isShulkerBox(mat)) return UNRENEWABLE_MOBS;
				if(TypeUtils.isInfested(mat)) return INCLUDE_UNOBT && !OBT_INFESTED;
				return TypeUtils.isOre(mat) || TypeUtils.isTerracotta(mat) || TypeUtils.isGlazedTerracotta(mat);
		}
	}

	//For irreversible processes: takes two unrenewable items as input
	boolean isIrreversibleProcess(ItemStack in, ItemStack out){
		if(!isUnrenewableItem(in)) return false;// If input is renewable, process is renewable
		return !in.getType().equals(out.getType()) && !reversible.sameSet(in.getType(), out.getType());
	}
	boolean isIrreversibleProcess(Material inMat, BlockData inData, Material outMat, BlockData outData){
		if(!isUnrenewableBlock(inMat, inData)) return false;// If input is renewable, process is renewable
		return !inMat.equals(outMat) && !reversible.sameSet(inMat, outMat);
	}
}