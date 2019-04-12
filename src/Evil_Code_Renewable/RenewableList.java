package Evil_Code_Renewable;

import java.util.HashSet;
import org.bukkit.Material;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Levelled;
import org.bukkit.inventory.ItemStack;
import EvLib.TypeUtils;

class RenewableList{
	static final HashSet<Material> rescueList = new HashSet<Material>();
	static int SILK_SPAWNER_LVL;
	static boolean LAVA_UNRENEWABLE, DIA_ARMOR_UNRENEWABLE, MOB_UNRENEWABLE,
					GRAVITY_UNRENEWABLE, SAVE_UNOBTAINABLE, DIRT_TO_GRAVEL,
					STANDARD_LORE, STANDARD_NAME, STANDARD_ENCHANTS, STANDARD_FLAGS, STANDARD_OTHER_META,
					SILK_SPAWNERS;
	static boolean OBT_SPAWNERS, OBT_MOB_EGGS, OBT_INFESTED, OBT_SMOOTH_BRICKS, OBT_CMD_BLOCKS,
					OBT_BEDROCK, OBT_END_PORTALS, OBT_BARRIERS, OBT_STRUCTURE_BLOCKS;
	RenewableList(Renewable pl){
		LAVA_UNRENEWABLE = !pl.getConfig().getBoolean("renewable-lava", true);
		DIA_ARMOR_UNRENEWABLE = !pl.getConfig().getBoolean("renewable-diamond-armor", true);
		MOB_UNRENEWABLE =  !pl.getConfig().getBoolean("renewable-mob-drops", false);
		GRAVITY_UNRENEWABLE = !pl.getConfig().getBoolean("renewable-gravity-blocks", false);
		//
		SAVE_UNOBTAINABLE = !pl.getConfig().getBoolean("ignore-unobtainable-items", false);
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
		DIRT_TO_GRAVEL = pl.getConfig().getBoolean("dirt-standardizes-to-gravel", true);
		STANDARD_LORE = pl.getConfig().getBoolean("standardize-if-has-lore", false);
		STANDARD_NAME = pl.getConfig().getBoolean("standardize-if-has-name", true);
		STANDARD_ENCHANTS = pl.getConfig().getBoolean("standardize-if-has-enchants", true);
		STANDARD_FLAGS = pl.getConfig().getBoolean("standardize-if-has-flags", false);
		STANDARD_OTHER_META = pl.getConfig().getBoolean("standardize-if-has-other-meta", false);
		SILK_SPAWNERS = pl.getConfig().getBoolean("silktouch-spawners", false);
		SILK_SPAWNER_LVL = pl.getConfig().getInt("silktouch-level", 1);

		for(String name : pl.getConfig().getStringList("rescued-renewables")){
			try{ rescueList.add(Material.valueOf(name.toUpperCase())); }
			catch(IllegalArgumentException ex){}
		}
	}

	// Calls isUnrenewableBlock()
	static boolean isUnrenewableItem(ItemStack item){
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
				return MOB_UNRENEWABLE;
			case DIAMOND_HELMET:
//			case DIAMOND_CHESTPLATE://Note: renewable (villagers)
			case DIAMOND_LEGGINGS:
			case DIAMOND_BOOTS:
				return DIA_ARMOR_UNRENEWABLE;
			case FLINT:
			case FLINT_AND_STEEL:
			case TNT_MINECART:
				return GRAVITY_UNRENEWABLE;
			case LAVA_BUCKET:
				return LAVA_UNRENEWABLE;
			case COMMAND_BLOCK_MINECART:
				return SAVE_UNOBTAINABLE || OBT_CMD_BLOCKS;
			default:
				if(TypeUtils.isSpawnEgg(item.getType())) return SAVE_UNOBTAINABLE || OBT_MOB_EGGS;
				return isUnrenewableBlock(item.getType(), null);
		}	
	}

	// Blocks can come as ItemStacks, but Items can't come as BlockStates
	// Thus, if our input is a Block, we know it can't be an item
	static boolean isUnrenewableBlock(Material mat, BlockData data){
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
				return GRAVITY_UNRENEWABLE;
			case LAVA://Flowing lava is renewable
				return LAVA_UNRENEWABLE && ((Levelled)data).getLevel() == 0;
			case BEACON:
				return MOB_UNRENEWABLE;
			case SMOOTH_SANDSTONE://TODO: these will be renewable (via smelt) in 1.14!
			case SMOOTH_RED_SANDSTONE://TODO: ^
			case SMOOTH_STONE://TODO: ^
				return SAVE_UNOBTAINABLE || OBT_SMOOTH_BRICKS;
			case SPAWNER:
				return SAVE_UNOBTAINABLE || OBT_SPAWNERS;
			case BEDROCK:
				return SAVE_UNOBTAINABLE || OBT_BEDROCK;
			case END_PORTAL:
			case END_PORTAL_FRAME:
				return SAVE_UNOBTAINABLE || OBT_END_PORTALS;
			case BARRIER:
				return SAVE_UNOBTAINABLE || OBT_BARRIERS;
			case COMMAND_BLOCK:
			case CHAIN_COMMAND_BLOCK:
			case REPEATING_COMMAND_BLOCK:
				return SAVE_UNOBTAINABLE || OBT_CMD_BLOCKS;
			case STRUCTURE_BLOCK:
			case STRUCTURE_VOID:
				return SAVE_UNOBTAINABLE || OBT_STRUCTURE_BLOCKS;
			default:
				if(TypeUtils.isConcrete(mat) || TypeUtils.isConcretePowder(mat)) return GRAVITY_UNRENEWABLE;
				if(TypeUtils.isShulkerBox(mat)) return MOB_UNRENEWABLE;
				if(TypeUtils.isInfested(mat)) return SAVE_UNOBTAINABLE || OBT_INFESTED;
				return TypeUtils.isOre(mat) || TypeUtils.isTerracotta(mat) || TypeUtils.isGlazedTerracotta(mat);
		}
	}
}