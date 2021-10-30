package net.evmodder.Renewable;

import java.util.HashSet;
import org.bukkit.Material;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Levelled;
import org.bukkit.inventory.ItemStack;
import net.evmodder.EvLib.extras.EntityUtils;
import net.evmodder.EvLib.extras.TypeUtils;
import net.evmodder.EvLib.util.UnionFind;

public class RenewableChecker{
	static final HashSet<Material> rescueList = new HashSet<Material>();
	static final HashSet<Material> artificiallyRenewable = new HashSet<Material>();
	static final UnionFind<Material> reversible = new UnionFind<Material>();
	static{
		reversible.add(Material.DIAMOND);
		reversible.addToSet(Material.DIAMOND_BLOCK, Material.DIAMOND);

		reversible.add(Material.NETHERITE_INGOT);
		reversible.addToSet(Material.NETHERITE_BLOCK, Material.NETHERITE_INGOT);

		//reversible.add(Material.NETHER_BRICK_SLAB);//Netherbrick slabs & d_slabs
		//reversible.addToSet(Material.DOUBLE_NETHER_BRICK_SLAB, Material.NETHER_BRICK_SLAB);

		reversible.add(Material.SPONGE);//Sponge & WetSponge
		reversible.addToSet(Material.WET_SPONGE, Material.SPONGE);
	}

	final boolean UNRENEWABLE_LAVA, UNRENEWABLE_MOBS, UNRENEWABLE_GRAVITY;
	final boolean UNRENEWABLE_UNOBT, OBT_SPAWNERS, OBT_MOB_EGGS, OBT_INFESTED, OBT_CMD_BLOCKS,
					OBT_BEDROCK, OBT_END_PORTALS, OBT_BARRIERS, OBT_STRUCTURE_BLOCKS, OBT_PETRIFIED_SLABS;
	RenewableChecker(Renewable pl){
		pl.getLogger().fine("These are unrenewable: ");
		pl.getLogger().fine("Lava: "+(UNRENEWABLE_LAVA = !pl.getConfig().getBoolean("renewable-lava", true)));
		pl.getLogger().fine("Certain mobs: "+(UNRENEWABLE_MOBS =  !pl.getConfig().getBoolean("renewable-mob-drops", false)));
		pl.getLogger().fine("Certain gravity blocks: "+(UNRENEWABLE_GRAVITY = !pl.getConfig().getBoolean("renewable-gravity-blocks", false)));
		//
		pl.getLogger().fine("Unobtainable items: "+(UNRENEWABLE_UNOBT = !pl.getConfig().getBoolean("ignore-unobtainable-items", false)));
		if(UNRENEWABLE_UNOBT){
			pl.getLogger().fine("Spawners: "+!(OBT_SPAWNERS = pl.getConfig().getBoolean("spawners-obtainable", false)));
			pl.getLogger().fine("SpawnEggs: "+!(OBT_MOB_EGGS = pl.getConfig().getBoolean("spawn-eggs-obtainable", false)));
			pl.getLogger().fine("Infested blocks(as item): "+!(OBT_INFESTED = pl.getConfig().getBoolean("infested-blocks-obtainable", false)));
			pl.getLogger().fine("Command blocks: "+!(OBT_CMD_BLOCKS = pl.getConfig().getBoolean("command-blocks-obtainable", false)));
			pl.getLogger().fine("Bedrock: "+!(OBT_BEDROCK = pl.getConfig().getBoolean("bedrock-obtainable", false)));
			pl.getLogger().fine("End portals (and frames): "+!(OBT_END_PORTALS = pl.getConfig().getBoolean("end-portals-obtainable", false)));
			pl.getLogger().fine("Barriers: "+!(OBT_BARRIERS = pl.getConfig().getBoolean("barriers-obtainable", false)));
			pl.getLogger().fine("Structure blocks: "+!(OBT_STRUCTURE_BLOCKS = pl.getConfig().getBoolean("structure-blocks-obtainable", false)));
			pl.getLogger().fine("Petrified slabs: "+!(OBT_PETRIFIED_SLABS = pl.getConfig().getBoolean("petrified-slabs-obtainable", false)));
		}
		else OBT_SPAWNERS = OBT_MOB_EGGS = OBT_INFESTED = OBT_CMD_BLOCKS = OBT_BEDROCK
				= OBT_END_PORTALS = OBT_BARRIERS = OBT_STRUCTURE_BLOCKS = OBT_PETRIFIED_SLABS = false;
		//
		for(String name : pl.getConfig().getStringList("rescued-renewables")){
			try{ rescueList.add(Material.valueOf(name.toUpperCase())); }
			catch(IllegalArgumentException ex){}
		}
		for(String name : pl.getConfig().getStringList("artificial-renewables")){
			try{ artificiallyRenewable.add(Material.valueOf(name.toUpperCase())); }
			catch(IllegalArgumentException ex){}
		}
	}

	// Calls isUnrenewableBlock()
	boolean isUnrenewableItem(ItemStack item){
		if(artificiallyRenewable.contains(item.getType())) return false;
		//Note: (Somewhat) Sorted by ID, from least to greatest
		switch(item.getType()){
			case DIAMOND:
			case NETHERITE_INGOT:
			case NETHERITE_SCRAP:
			case NETHERITE_HELMET:
			case NETHERITE_CHESTPLATE:
			case NETHERITE_LEGGINGS:
			case NETHERITE_BOOTS:
			case NETHERITE_SWORD:
			case NETHERITE_AXE:
			case NETHERITE_PICKAXE:
			case NETHERITE_SHOVEL:
			case NETHERITE_HOE:
			case MUSIC_DISC_PIGSTEP:
//			case NETHER_BRICK: // Renewable in 1.16+ (Bartering)
//			case QUARTZ: // Renewable in 1.16+ (Bartering)
			case IRON_HORSE_ARMOR:
			case GOLDEN_HORSE_ARMOR:
			case DIAMOND_HORSE_ARMOR:
			case ELYTRA:
			case ENCHANTED_GOLDEN_APPLE:
			case MOJANG_BANNER_PATTERN:
			case PIGLIN_BANNER_PATTERN:
				return true;
//			case TOTEM_OF_UNDYING: // Renewable in 1.14+ (Raids)
			case SHULKER_SHELL:
				return UNRENEWABLE_MOBS;// && !OBT_MOB_EGGS;
//			case NETHER_STAR: // Renewable in 1.16+ (Bartering)
//				return UNRENEWABLE_MOBS;
//			case FLINT: // Renewable in 1.16+ (Bartering)
//			case FLINT_AND_STEEL:
//				return UNRENEWABLE_GRAVITY;
			case LAVA_BUCKET:
				return UNRENEWABLE_LAVA;
			case COMMAND_BLOCK_MINECART:
				return UNRENEWABLE_UNOBT && !OBT_CMD_BLOCKS;
			default:
				if(EntityUtils.isSpawnEgg(item.getType())) return UNRENEWABLE_UNOBT && !OBT_MOB_EGGS;
				// These are only unrenewable in item form (infested blocks can be renewably created)
				if(TypeUtils.isInfested(item.getType())) return UNRENEWABLE_UNOBT && !OBT_INFESTED;
				return isUnrenewableBlock(item.getType(), null);
		}	
	}

	// Blocks can come as ItemStacks, but Items can't come as BlockStates
	// Thus, if our input is a Block, we know it can't be an item
	boolean isUnrenewableBlock(Material mat, BlockData data){
		if(artificiallyRenewable.contains(mat)) return false;
		//Custom list of (renewable) items to rescue (considered unrenewable)
		if(rescueList.contains(mat)) return true;

		//Note: (Somewhat) Sorted by ID, from least to greatest
		switch(mat){
			//case TERRACOTTA: //WTrader sells clay -> smelt into terracotta -> dye
//			case FLOWER_POT:
//			case GRANITE: // Renewable in 1.16+ (Bartering, Quartz)
//			case GRANITE_SLAB:
//			case GRANITE_STAIRS:
//			case GRANITE_WALL:
//			case POLISHED_GRANITE:
//			case POLISHED_GRANITE_SLAB:
//			case POLISHED_GRANITE_STAIRS:
//			case DIORITE:
//			case DIORITE_SLAB:
//			case DIORITE_STAIRS:
//			case DIORITE_WALL:
//			case POLISHED_DIORITE:
//			case POLISHED_DIORITE_SLAB:
//			case POLISHED_DIORITE_STAIRS:
//			case ANDESITE:
//			case ANDESITE_SLAB:
//			case ANDESITE_STAIRS:
//			case ANDESITE_WALL:
//			case POLISHED_ANDESITE:
//			case POLISHED_ANDESITE_SLAB:
//			case POLISHED_ANDESITE_STAIRS:
			case SPONGE:
			case WET_SPONGE:
//			case GLASS: // Renewable in 1.14+ (Villagers)
//			case BEE_HIVE: // Renewable in 1.15.2+
			case ENCHANTING_TABLE:
			case JUKEBOX:
			case LODESTONE:
			case GILDED_BLACKSTONE:
			case ANCIENT_DEBRIS:
			case DIAMOND_BLOCK:
			case NETHERITE_BLOCK:
//			case COMPARATOR:
//			case OBSERVER:
			case DAYLIGHT_DETECTOR:
			case COBWEB:
			case DEAD_BUSH:
			case NETHERRACK:
//			case SOUL_SAND:// Renewable in 1.16+ (Bartering)
//			case NETHER_BRICKS:// Renewable in 1.16+ (Bartering)
//			case NETHER_BRICK_FENCE:
//			case NETHER_BRICK_SLAB:
//			case NETHER_BRICK_STAIRS:
//			case NETHER_BRICK_WALL:
//			case RED_NETHER_BRICKS:
//			case RED_NETHER_BRICK_SLAB:
//			case RED_NETHER_BRICK_STAIRS:
//			case RED_NETHER_BRICK_WALL:
//			case ENDSTONE://Note: renewable! :o (When dragon is respawned, endstone under platform)
//			case QUARTZ_BLOCK: // Renewable in 1.16+ (Bartering)
//			case QUARTZ_STAIRS://Note: slab, pillar, and chiseled are renewable
//			case SMOOTH_QUARTZ:
//			case SMOOTH_QUARTZ_SLAB:
//			case SMOOTH_QUARTZ_STAIRS:
//			case MAGMA_BLOCK: //Note: renewable! (4 magma cream)
				return true;
//			case SAND: // Renewable in 1.14+ (Wandering Trader)
//			case RED_SAND://Note: ^
//			case GRAVEL: // Renewable in 1.16+ (Bartering)
//			case FLETCHING_TABLE: // Renewable in 1.16+ (Bartering, Flint)
			case DRAGON_EGG:
				return UNRENEWABLE_GRAVITY;
			case LAVA://Flowing lava is renewable
				return UNRENEWABLE_LAVA && ((Levelled)data).getLevel() == 0;
//			case BEACON: // Renewable in 1.16+ (Bartering)
//				return UNRENEWABLE_MOBS;
			case SPAWNER:
				return UNRENEWABLE_UNOBT && !OBT_SPAWNERS;
			case BEDROCK:
				return UNRENEWABLE_UNOBT && !OBT_BEDROCK;
			case END_PORTAL:
			case END_PORTAL_FRAME:
				return UNRENEWABLE_UNOBT && !OBT_END_PORTALS;
			case BARRIER:
				return UNRENEWABLE_UNOBT && !OBT_BARRIERS;
			case COMMAND_BLOCK:
			case CHAIN_COMMAND_BLOCK:
			case REPEATING_COMMAND_BLOCK:
				return UNRENEWABLE_UNOBT && !OBT_CMD_BLOCKS;
			case STRUCTURE_BLOCK:
			case STRUCTURE_VOID:
			case JIGSAW:
				return UNRENEWABLE_UNOBT && !OBT_STRUCTURE_BLOCKS;
			case PETRIFIED_OAK_SLAB:
				return UNRENEWABLE_UNOBT && !OBT_PETRIFIED_SLABS;
			default:
				if(TypeUtils.isConcrete(mat) || TypeUtils.isConcretePowder(mat)) return UNRENEWABLE_GRAVITY;
				if(TypeUtils.isShulkerBox(mat)) return UNRENEWABLE_MOBS;
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