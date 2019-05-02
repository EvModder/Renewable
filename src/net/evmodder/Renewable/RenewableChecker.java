package net.evmodder.Renewable;

import java.util.HashSet;
import org.bukkit.Material;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Levelled;
import org.bukkit.inventory.ItemStack;
import net.evmodder.EvLib.TypeUtils;
import net.evmodder.EvLib.UnionFind;

public class RenewableChecker{
	static final HashSet<Material> rescueList = new HashSet<Material>();
	static final UnionFind<Material> reversible = new UnionFind<Material>();
	static{
		reversible.add(Material.DIAMOND);
		reversible.addToSet(Material.DIAMOND_BLOCK, Material.DIAMOND);

		//reversible.add(Material.NETHER_BRICK_SLAB);//Netherbrick slabs & d_slabs
		//reversible.addToSet(Material.DOUBLE_NETHER_BRICK_SLAB, Material.NETHER_BRICK_SLAB);

		reversible.add(Material.SPONGE);//Sponge & WetSponge
		reversible.addToSet(Material.WET_SPONGE, Material.SPONGE);
	}

	final boolean UNRENEWABLE_LAVA, UNRENEWABLE_MOBS, UNRENEWABLE_GRAVITY;
	final boolean INCLUDE_UNOBT, OBT_SPAWNERS, OBT_MOB_EGGS, OBT_INFESTED, OBT_CMD_BLOCKS,
					OBT_BEDROCK, OBT_END_PORTALS, OBT_BARRIERS, OBT_STRUCTURE_BLOCKS, OBT_PETRIFIED_SLABS;
	RenewableChecker(Renewable pl){
		UNRENEWABLE_LAVA = !pl.getConfig().getBoolean("renewable-lava", true);
		UNRENEWABLE_MOBS =  !pl.getConfig().getBoolean("renewable-mob-drops", false);
		UNRENEWABLE_GRAVITY = !pl.getConfig().getBoolean("renewable-gravity-blocks", false);
		//
		INCLUDE_UNOBT = !pl.getConfig().getBoolean("ignore-unobtainable-items", false);
		OBT_SPAWNERS = pl.getConfig().getBoolean("spawners-obtainable", false);
		OBT_MOB_EGGS = pl.getConfig().getBoolean("spawn-eggs-obtainable", false);
		OBT_INFESTED = pl.getConfig().getBoolean("infested-blocks-obtainable", false);
		OBT_CMD_BLOCKS = pl.getConfig().getBoolean("command-blocks-obtainable", false);
		OBT_BEDROCK = pl.getConfig().getBoolean("bedrock-obtainable", false);
		OBT_END_PORTALS = pl.getConfig().getBoolean("end-portals-obtainable", false);
		OBT_BARRIERS = pl.getConfig().getBoolean("barriers-obtainable", false); 
		OBT_STRUCTURE_BLOCKS = pl.getConfig().getBoolean("structure-blocks-obtainable", false);
		OBT_PETRIFIED_SLABS = pl.getConfig().getBoolean("petrified-slabs-obtainable", false);
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
				return UNRENEWABLE_MOBS && !OBT_MOB_EGGS;
			case NETHER_STAR:
				return UNRENEWABLE_MOBS;
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
			case TERRACOTTA://Note: only uncolored terracotta is unrenewable
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
			case NETHERRACK:
			case SOUL_SAND:
			case NETHER_BRICKS:
			case NETHER_BRICK_FENCE:
			case NETHER_BRICK_SLAB:
			case NETHER_BRICK_STAIRS:
			case NETHER_BRICK_WALL:
			case RED_NETHER_BRICKS:
			case RED_NETHER_BRICK_SLAB:
			case RED_NETHER_BRICK_STAIRS:
			case RED_NETHER_BRICK_WALL:
//			case ENDSTONE://Note: renewable! :o (When dragon is respawned, endstone under platform)
			case QUARTZ_BLOCK:
			case QUARTZ_STAIRS://Note: slab, pillar, and chiseled are renewable
			case SMOOTH_QUARTZ:
			case SMOOTH_QUARTZ_SLAB:
			case SMOOTH_QUARTZ_STAIRS:
//			case MAGMA_BLOCK://Note: renewable! (4 magma cream)
				return true;
//			case SAND://Note: renewable in 1.14! (wandering trader)
//			case RED_SAND://Note: ^
			case GRAVEL:
			case FLETCHING_TABLE:
			case DRAGON_EGG:
				return UNRENEWABLE_GRAVITY;
			case LAVA://Flowing lava is renewable
				return UNRENEWABLE_LAVA && ((Levelled)data).getLevel() == 0;
			case BEACON:
				return UNRENEWABLE_MOBS;
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
			case JIGSAW:
				return INCLUDE_UNOBT && !OBT_STRUCTURE_BLOCKS;
			case PETRIFIED_OAK_SLAB:
				return INCLUDE_UNOBT && !OBT_PETRIFIED_SLABS;
			default:
				if(TypeUtils.isConcretePowder(mat)) return UNRENEWABLE_GRAVITY;
				if(TypeUtils.isShulkerBox(mat)) return UNRENEWABLE_MOBS;
				if(TypeUtils.isInfested(mat)) return INCLUDE_UNOBT && !OBT_INFESTED;
				return TypeUtils.isOre(mat);
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