package Evil_Code_Renewable;

import java.util.HashSet;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.BlockState;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Levelled;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;
import EvLib.TypeUtils;

public class UnrenewableList{
	static final HashSet<Material> rescueList = new HashSet<Material>();

	static int SILK_SPAWNER_LVL;
	static boolean LAVA_UNRENEWABLE, DIA_ARMOR_UNRENEWABLE, MOB_UNRENEWABLE,
					GRAVITY_UNRENEWABLE, UNGET_UNRENEWABLE, DIRT_TO_GRAVEL,
					STANDARD_LORE, STANDARD_NAME, STANDARD_ENCHANTS, STANDARD_FLAGS, STANDARD_OTHER_META,
					SILK_SPAWNERS;
	static boolean UNGET_SPAWNERS, UNGET_MOB_EGGS, UNGET_INFESTED, UNGET_SMOOTH_BRICKS, UNGET_CMD_BLOCKS,
					UNGET_BEDROCK, UNGET_END_PORTALS, UNGET_BARRIERS, UNGET_STRUCTURE_BLOCKS;
	UnrenewableList(Renewable pl){
		LAVA_UNRENEWABLE = !pl.getConfig().getBoolean("renewable-lava", true);
		DIA_ARMOR_UNRENEWABLE = !pl.getConfig().getBoolean("renewable-diamond-armor", true);
		MOB_UNRENEWABLE =  !pl.getConfig().getBoolean("renewable-mob-drops", false);
		GRAVITY_UNRENEWABLE = !pl.getConfig().getBoolean("renewable-gravity-blocks", false);
		//
		UNGET_UNRENEWABLE = !pl.getConfig().getBoolean("renewable-unobtainable-items", false);
		UNGET_SPAWNERS = !pl.getConfig().getBoolean("spawners-obtainable", false);
		UNGET_MOB_EGGS = !pl.getConfig().getBoolean("spawn-eggs-obtainable", false);
		UNGET_INFESTED = !pl.getConfig().getBoolean("infested-blocks-obtainable", false);
		UNGET_SMOOTH_BRICKS = !pl.getConfig().getBoolean("smooth-bricks-obtainable", false);
		UNGET_CMD_BLOCKS = !pl.getConfig().getBoolean("command-blocks-obtainable", false);
		UNGET_BEDROCK = !pl.getConfig().getBoolean("bedrock-obtainable", false);
		UNGET_END_PORTALS = !pl.getConfig().getBoolean("end-portals-obtainable", false);
		UNGET_BARRIERS = !pl.getConfig().getBoolean("barriers-obtainable", false); 
		UNGET_STRUCTURE_BLOCKS = !pl.getConfig().getBoolean("structure-blocks-obtainable", false);
		
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
	static boolean isUnrenewable(ItemStack item){
		//Custom list of (renewable) items to rescue (considered unrenewable)
		if(rescueList.contains(item.getType())) return true;

		//Note: (Somewhat) Sorted by ID, from least to greatest
		switch(item.getType()){
			case DIAMOND:
			case DIAMOND_SHOVEL:
			case DIAMOND_HOE:
			case BRICK:
			case CLAY_BALL:
			case FLOWER_POT:
//			case LAPIS_LAZULI://Note: renewable (villagers)
//			case GLASS_BOTTLE://Note: renewable (villagers & witches)
			case NETHER_BRICK:
			case QUARTZ:
			case COMPARATOR:
			case IRON_HORSE_ARMOR:
			case GOLDEN_HORSE_ARMOR:
			case DIAMOND_HORSE_ARMOR:
			case ELYTRA:
			case WRITTEN_BOOK://Note: Technically these are renewable
			case ENCHANTED_GOLDEN_APPLE:
			case DRAGON_HEAD:
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
				return UNGET_UNRENEWABLE && UNGET_CMD_BLOCKS;
			case SMOOTH_SANDSTONE://TODO: these will be renewable (via smelting) in 1.14!
			case SMOOTH_RED_SANDSTONE://TODO:
			case SMOOTH_STONE://TODO:
				return UNGET_UNRENEWABLE && UNGET_SMOOTH_BRICKS;
			default:
				if(TypeUtils.isSpawnEgg(item.getType())) return UNGET_UNRENEWABLE && UNGET_MOB_EGGS;
				return isUnrenewableBlock(item.getType(), null);
		}	
	}

	static boolean isUnrenewable(BlockState block){
		return isUnrenewableBlock(block.getType(), block.getBlockData());
	}
	static boolean isUnrenewableBlock(Material mat, BlockData data){
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
			case LILY_PAD://Note: renewable (fishing)//TODO: This is only set to unrenewable for Eventials
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
			case PACKED_ICE:
//			case MAGMA_BLOCK://Note: renewable! (4 magma cream)
				return true;
			case SAND://Note: Sand and Red Sand are considered unrenewable.
			case GRAVEL:
			case DRAGON_EGG:
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
			case COMMAND_BLOCK:
			case CHAIN_COMMAND_BLOCK:
			case REPEATING_COMMAND_BLOCK:
				return UNGET_UNRENEWABLE && UNGET_CMD_BLOCKS;
			case SPAWNER:
				return UNGET_UNRENEWABLE && UNGET_SPAWNERS;
			case BEDROCK:
				return UNGET_UNRENEWABLE && UNGET_BEDROCK;
			case END_PORTAL:
			case END_PORTAL_FRAME:
				return UNGET_UNRENEWABLE && UNGET_END_PORTALS;
			case BARRIER:
				return UNGET_UNRENEWABLE && UNGET_BARRIERS;
			case STRUCTURE_BLOCK:
			case STRUCTURE_VOID:
				return UNGET_UNRENEWABLE && UNGET_STRUCTURE_BLOCKS;
			default:
				if(TypeUtils.isConcrete(mat) || TypeUtils.isConcretePowder(mat)) return GRAVITY_UNRENEWABLE;
				if(TypeUtils.isShulkerBox(mat)) return MOB_UNRENEWABLE;
				if(TypeUtils.isInfested(mat)) return UNGET_UNRENEWABLE && UNGET_INFESTED;
				return TypeUtils.isOre(mat) || TypeUtils.isTerracotta(mat) || TypeUtils.isGlazedTerracotta(mat);
		}
	}

	static ItemStack getUnewnewableItemForm(BlockState block){
		switch(block.getType()){
			case LAVA:
				return new ItemStack(Material.LAVA_BUCKET);
			case SPAWNER:
				ItemStack item = new ItemStack(Material.SPAWNER);
				BlockStateMeta meta = (BlockStateMeta) item.getItemMeta();
				meta.setBlockState(block);
				String name = Utils.getNormalizedName(((CreatureSpawner)block).getSpawnedType());
				meta.setDisplayName(ChatColor.WHITE+name+" Spawner");
				item.setItemMeta(meta);
				return item;
			default:
				ItemStack is = new ItemStack(block.getType());
				if(block.getData() != null) is.setData(block.getData());
				return is;
		}
	}
}