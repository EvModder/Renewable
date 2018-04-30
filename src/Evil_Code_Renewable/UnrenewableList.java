package Evil_Code_Renewable;

import java.util.HashSet;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.BlockState;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;

@SuppressWarnings("deprecation")
public class UnrenewableList{
	static final HashSet<Material> rescueList = new HashSet<Material>();

	static int SILK_SPAWNER_LVL;
	static boolean LAVA_UNRENEWABLE, DIA_ARMOR_UNRENEWABLE, MOB_UNRENEWABLE,
					GRAVITY_UNRENEWABLE, UNGET_UNRENEWABLE, DIRT_TO_GRAVEL,
					STANDARD_LORE, STANDARD_NAME, STANDARD_ENCHANTS, STANDARD_FLAGS, STANDARD_OTHER_META,
					SILK_SPAWNERS;
	UnrenewableList(Renewable pl){
		LAVA_UNRENEWABLE = !pl.getConfig().getBoolean("renewable-lava", true);
		DIA_ARMOR_UNRENEWABLE = !pl.getConfig().getBoolean("renewable-diamond-armor", true);
		MOB_UNRENEWABLE =  !pl.getConfig().getBoolean("renewable-mob-drops", false);
		GRAVITY_UNRENEWABLE = !pl.getConfig().getBoolean("renewable-gravity-blocks", false);
		UNGET_UNRENEWABLE = !pl.getConfig().getBoolean("renewable-unobtainable-items", false);
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
		byte dataValue = item.getData().getData();

		switch(item.getType()){
			case DIAMOND:
			case DIAMOND_SPADE:
			case DIAMOND_HOE:
			case BRICK:
			case CLAY_BALL:
//			case LAPIS_LAZULI://Note: renewable (villagers)
//			case GLASS_BOTTLE://Note: renewable (villagers & witches)
			case NETHER_BRICK_ITEM:
			case QUARTZ:
			case IRON_BARDING:
			case GOLD_BARDING:
			case DIAMOND_BARDING:
			case ELYTRA:
			case WRITTEN_BOOK://Note: Technically these are renewable
				return true;
			case TOTEM:
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
			case EXPLOSIVE_MINECART:
				return GRAVITY_UNRENEWABLE;
			case LAVA_BUCKET:
				return LAVA_UNRENEWABLE;
			case COMMAND_MINECART:
			case MONSTER_EGG:
			case MONSTER_EGGS:
				return UNGET_UNRENEWABLE;
			case GOLDEN_APPLE:
				return dataValue == 1;
			case SKULL:
				return dataValue == 5;
			default:
				return isUnrenewableBlock(item.getType(), dataValue);
		}	
	}

	static boolean isUnrenewable(BlockState block){
		return isUnrenewableBlock(block.getType(), block.getRawData());
	}
	static boolean isUnrenewableBlock(Material mat, byte dataValue){
		//Note: (Somewhat) Sorted by ID, from least to greatest

		switch(mat){
			case STONE:
				return dataValue != 0;
			case GRASS:
			case DIRT:
			case SPONGE:
//			case GLASS://Note: glass is renewable! (Villagers)
			case ENCHANTMENT_TABLE:
			case JUKEBOX:
			case FLOWER_POT:
			case FLOWER_POT_ITEM://TODO: Check if this is what I think (pot w/ item)
			case DIAMOND_BLOCK:
			case REDSTONE_COMPARATOR:
			case REDSTONE_COMPARATOR_ON:
			case REDSTONE_COMPARATOR_OFF:
			case OBSERVER:
			case DAYLIGHT_DETECTOR:
			case DAYLIGHT_DETECTOR_INVERTED:
			case WEB:
			case DEAD_BUSH:
//			case MOSSY_COBBLESTONE://Note: renewable! (Vines)
			case CLAY_BRICK:
			case BRICK_STAIRS://Note: Same (red) brick type as above, just as stairs
			case WATER_LILY://Note: renewable (fishing)//TODO: This is only set to unrenewable for Eventials
			case CLAY:
			case NETHERRACK:
			case SOUL_SAND:
			case MYCEL://Note: Since dirt is unrenewable, this is as well.
			case NETHER_BRICK:
			case NETHER_BRICK_STAIRS:
			case NETHER_FENCE:
			case RED_NETHER_BRICK:
//			case ENDSTONE://Note: renewable! :o (When dragon is respawned, endstone under platform)
			case QUARTZ_BLOCK:
			case QUARTZ_STAIRS:
			case STAINED_CLAY:
			case HARD_CLAY:
			case BLACK_GLAZED_TERRACOTTA:
			case BLUE_GLAZED_TERRACOTTA:
			case BROWN_GLAZED_TERRACOTTA:
			case CYAN_GLAZED_TERRACOTTA:
			case GRAY_GLAZED_TERRACOTTA:
			case GREEN_GLAZED_TERRACOTTA:
			case LIGHT_BLUE_GLAZED_TERRACOTTA:
			case LIME_GLAZED_TERRACOTTA:
			case MAGENTA_GLAZED_TERRACOTTA:
			case ORANGE_GLAZED_TERRACOTTA:
			case PINK_GLAZED_TERRACOTTA:
			case PURPLE_GLAZED_TERRACOTTA:
			case RED_GLAZED_TERRACOTTA:
			case SILVER_GLAZED_TERRACOTTA:
			case WHITE_GLAZED_TERRACOTTA:
			case YELLOW_GLAZED_TERRACOTTA:
			case PACKED_ICE:
//			case SUNFLOWER://Note: renewable (Bonemeal on grass)
//			case LILAC:
//			case LARGE_FERN:
//			case ROSE_BUSH:
//			case PEONY:
//			case MAGMA_BLOCK://Note: renewable! (4 magma cream)
				return true;
			case SAND://Note: Sand and Red Sand are considered unrenewable.
			case GRAVEL:
			case DRAGON_EGG:
			case SANDSTONE:
			case RED_SANDSTONE:
			case SANDSTONE_STAIRS:
			case RED_SANDSTONE_STAIRS:
			case CONCRETE_POWDER:
			case CONCRETE:
			case TNT:
				return GRAVITY_UNRENEWABLE;
			case LAVA://Flowing lava is renewable
			case STATIONARY_LAVA:
				return LAVA_UNRENEWABLE && dataValue == 0;
			case STEP:
				return dataValue == 4 || dataValue == 6 || dataValue == 7
						|| (dataValue == 1 && GRAVITY_UNRENEWABLE);
			case STONE_SLAB2:
				return dataValue == 0 && GRAVITY_UNRENEWABLE;
			case LONG_GRASS:
				return dataValue == 0;//Shrub (An old form of DEAD_BUSH)
			case BLACK_SHULKER_BOX:
			case BLUE_SHULKER_BOX:
			case BROWN_SHULKER_BOX:
			case CYAN_SHULKER_BOX:
			case GRAY_SHULKER_BOX:
			case GREEN_SHULKER_BOX:
			case LIGHT_BLUE_SHULKER_BOX:
			case LIME_SHULKER_BOX:
			case MAGENTA_SHULKER_BOX:
			case ORANGE_SHULKER_BOX:
			case PINK_SHULKER_BOX:
			case PURPLE_SHULKER_BOX:
			case RED_SHULKER_BOX:
			case SILVER_SHULKER_BOX:
			case WHITE_SHULKER_BOX:
			case YELLOW_SHULKER_BOX:
			case BEACON:
				return MOB_UNRENEWABLE;
			case BEDROCK:
			case MOB_SPAWNER:
			case COMMAND:
			case COMMAND_CHAIN:
			case COMMAND_REPEATING:
			case ENDER_PORTAL:
			case ENDER_PORTAL_FRAME:
			case BARRIER:
			case STRUCTURE_BLOCK:
			case STRUCTURE_VOID:
				return UNGET_UNRENEWABLE;
			default:
				return Utils.isOre(mat);
		}
	}

	static ItemStack getUnewnewableItemForm(BlockState block){
		byte dataValue = block.getData().getData();
		switch(block.getType()){
			case LAVA:
			case STATIONARY_LAVA:
				return new ItemStack(Material.LAVA_BUCKET);
			case GLOWING_REDSTONE_ORE:
				return new ItemStack(Material.REDSTONE_ORE);
			case REDSTONE_COMPARATOR_ON:
			case REDSTONE_COMPARATOR_OFF:
				return new ItemStack(Material.REDSTONE_COMPARATOR);
			case DAYLIGHT_DETECTOR_INVERTED:
				return new ItemStack(Material.DAYLIGHT_DETECTOR);
			//Strip data values for stairs
			case BRICK_STAIRS:
				return new ItemStack(Material.BRICK_STAIRS);
			case QUARTZ_STAIRS:
				return new ItemStack(Material.QUARTZ_STAIRS);
			case NETHER_BRICK_STAIRS:
				return new ItemStack(Material.NETHER_BRICK_STAIRS);
			case MOB_SPAWNER:
				ItemStack item = new ItemStack(Material.MOB_SPAWNER);
				BlockStateMeta meta = (BlockStateMeta) item.getItemMeta();
				meta.setBlockState(block);
				String name = Utils.getNormalizedName(((CreatureSpawner)block).getSpawnedType());
				meta.setDisplayName(ChatColor.WHITE+name+" Spawner");
				item.setItemMeta(meta);
				return item;
			default:
				return new ItemStack(block.getType(), 1, dataValue);
		}
	}
}