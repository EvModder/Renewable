package Evil_Code_Renewable;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockState;
import org.bukkit.inventory.ItemStack;
import EvLib.FileIO;
import org.bukkit.entity.EntityType;
//import org.bukkit.metadata.FixedMetadataValue;
//import net.minecraft.server.v1_11_R1.NBTTagCompound;
//import org.bukkit.craftbukkit.v1_11_R1.inventory.CraftItemStack;
import org.bukkit.entity.Player;

@SuppressWarnings("deprecation")
public class Utils{
	static final HashMap<Material, Fraction> rescuedParts = new HashMap<Material, Fraction>();
	static{
		rescuedParts.put(Material.QUARTZ, new Fraction(0, 2));
		rescuedParts.put(Material.SAND, new Fraction(0, 2));
		rescuedParts.put(Material.GRAVEL, new Fraction(0, 2));
		rescuedParts.put(Material.NETHERRACK, new Fraction(0, 2));
		rescuedParts.put(Material.DIAMOND_ORE, new Fraction(0, 9));
	}
	static final UnionFind<ItemDesc> reversible = new UnionFind<ItemDesc>();
	static{
		reversible.add(new ItemDesc(Material.DIRT));
		reversible.addToSet(new ItemDesc(Material.GRASS), new ItemDesc(Material.DIRT));
		reversible.addToSet(new ItemDesc(Material.SOIL), new ItemDesc(Material.DIRT));
		reversible.addToSet(new ItemDesc(Material.MYCEL), new ItemDesc(Material.DIRT));
		reversible.addToSet(new ItemDesc(Material.GRASS_PATH), new ItemDesc(Material.DIRT));

		reversible.add(new ItemDesc(Material.CLAY));
		reversible.addToSet(new ItemDesc(Material.CLAY_BALL), new ItemDesc(Material.CLAY));

		reversible.add(new ItemDesc(Material.DIAMOND));
		reversible.addToSet(new ItemDesc(Material.DIAMOND_BLOCK), new ItemDesc(Material.DIAMOND));

		reversible.add(new ItemDesc(Material.FLOWER_POT));
		reversible.addToSet(new ItemDesc(Material.FLOWER_POT_ITEM), new ItemDesc(Material.FLOWER_POT));

		reversible.add(new ItemDesc(Material.REDSTONE_COMPARATOR));
		reversible.addToSet(new ItemDesc(Material.REDSTONE_COMPARATOR_ON), new ItemDesc(Material.REDSTONE_COMPARATOR));
		reversible.addToSet(new ItemDesc(Material.REDSTONE_COMPARATOR_OFF), new ItemDesc(Material.REDSTONE_COMPARATOR));

		reversible.add(new ItemDesc(Material.SANDSTONE, (byte)1));//Chiseled Sandstone & slabs
		reversible.addToSet(new ItemDesc(Material.STEP, (byte)1), new ItemDesc(Material.SANDSTONE, (byte)1));
		reversible.addToSet(new ItemDesc(Material.DOUBLE_STEP, (byte)1), new ItemDesc(Material.SANDSTONE, (byte)1));

		reversible.add(new ItemDesc(Material.RED_SANDSTONE, (byte)1));//Red Chiseled Sandstone & slabs
		reversible.addToSet(new ItemDesc(Material.STONE_SLAB2), new ItemDesc(Material.RED_SANDSTONE, (byte)1));
		reversible.addToSet(new ItemDesc(Material.DOUBLE_STONE_SLAB2), new ItemDesc(Material.RED_SANDSTONE, (byte)1));

		reversible.add(new ItemDesc(Material.STEP, (byte)4));//Brick slabs & d_slabs
		reversible.addToSet(new ItemDesc(Material.DOUBLE_STEP, (byte)4), new ItemDesc(Material.STEP, (byte)4));

		reversible.add(new ItemDesc(Material.STEP, (byte)6));//Netherbrick slabs & d_slabs
		reversible.addToSet(new ItemDesc(Material.DOUBLE_STEP, (byte)6), new ItemDesc(Material.STEP, (byte)6));

		reversible.add(new ItemDesc(Material.QUARTZ_BLOCK, (byte)1));//Chiseled Quartz & Slabs
		reversible.addToSet(new ItemDesc(Material.STEP, (byte)7), new ItemDesc(Material.QUARTZ_BLOCK, (byte)1));
		reversible.addToSet(new ItemDesc(Material.DOUBLE_STEP, (byte)7), new ItemDesc(Material.QUARTZ_BLOCK, (byte)1));

//		reversible.add(new ItemDesc(Material.PURPUR_SLAB));//Purpur slabs & d_slabs //NOTE: Purpur is renewable!
//		reversible.addToSet(new ItemDesc(Material.PURPUR_DOUBLE_SLAB), new ItemDesc(Material.PURPUR_SLAB));

		reversible.add(new ItemDesc(Material.SPONGE));//Sponge & WetSponge
		reversible.addToSet(new ItemDesc(Material.SPONGE), new ItemDesc(Material.SPONGE, (byte)1));

		reversible.add(new ItemDesc(Material.TNT));//TNT & TNT Minecart
		reversible.addToSet(new ItemDesc(Material.EXPLOSIVE_MINECART), new ItemDesc(Material.TNT));
	}
	static final HashSet<Material> rescueList = new HashSet<Material>();

	static int SILK_SPAWNER_LVL;
	static boolean LAVA_UNRENEWABLE, DIA_ARMOR_UNRENEWABLE, MOB_UNRENEWABLE,
					GRAVITY_UNRENEWABLE, UNGET_UNRENEWABLE, DIRT_TO_GRAVEL,
					STANDARD_LORE, STANDARD_NAME, STANDARD_ENCHANTS, STANDARD_FLAGS, STANDARD_OTHER_META,
					SILK_SPAWNERS;
	Utils(Renewable pl){
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
		pl.getLogger().fine("Gravity Unrenewable: "+GRAVITY_UNRENEWABLE);
	}
	static void loadFractionalRescues(){
		for(String str : FileIO.loadFile("fractional-rescues.txt", "").split(" ")){
			int i = str.indexOf(',');
			if(i == -1) continue;
			Material mat = Material.getMaterial(str.substring(0, i));
			Fraction frac = Fraction.fromString(str.substring(i+1));
			if(mat != null && frac != null) rescuedParts.put(mat, frac);
		}
	}
	static void saveFractionalRescues(){
		if(!rescuedParts.isEmpty()){
			StringBuilder builder = new StringBuilder("");
			for(Entry<Material, Fraction> e : rescuedParts.entrySet())
				builder.append(' ').append(e.getKey().name()).append(',').append(e.getValue());
			FileIO.saveFile("", builder.substring(1));
		}
	}

	public static boolean isUnrenewable(ItemStack item){
		return UnrenewableList.isUnrenewable(item); }
	public static boolean isUnrenewable(BlockState block){
		return UnrenewableList.isUnrenewable(block); }
	public static boolean isUnrenewableBlock(Material mat, byte dataValue){
		return UnrenewableList.isUnrenewableBlock(mat, dataValue); }
	public static ItemStack getUnewnewableItemForm(BlockState block){
		return UnrenewableList.getUnewnewableItemForm(block); }

	public static boolean isOre(Material mat){
		switch(mat){
			case QUARTZ_ORE:
			case COAL_ORE:
			case IRON_ORE:
			case GOLD_ORE:
			case REDSTONE_ORE:
			case GLOWING_REDSTONE_ORE:
			case LAPIS_ORE:
			case EMERALD_ORE:
			case DIAMOND_ORE:
				return true;
			default:
				return false;
		}
	}

	public static boolean pickIsAtLeast(Material pickType, Material needPick){
		switch(needPick){
			case DIAMOND_PICKAXE:
				return pickType == Material.DIAMOND_PICKAXE;
			case IRON_PICKAXE:
				return pickType == Material.IRON_PICKAXE || pickType == Material.DIAMOND_PICKAXE;
			case STONE_PICKAXE:
				return pickType == Material.STONE_PICKAXE || pickType == Material.IRON_PICKAXE
					|| pickType == Material.DIAMOND_PICKAXE;
			case GOLD_PICKAXE:
			case WOOD_PICKAXE:
			default:
				return pickType == Material.WOOD_PICKAXE || pickType == Material.GOLD_PICKAXE
					|| pickType == Material.STONE_PICKAXE || pickType == Material.IRON_PICKAXE
					|| pickType == Material.DIAMOND_PICKAXE;
				
		}
	}
	public static boolean swordIsAtLeast(Material pickType, Material needSword){
		switch(needSword){
			case DIAMOND_SWORD:
				return pickType == Material.DIAMOND_SWORD;
			case IRON_SWORD:
				return pickType == Material.IRON_SWORD || pickType == Material.DIAMOND_SWORD;
			case STONE_SWORD:
				return pickType == Material.STONE_SWORD || pickType == Material.IRON_SWORD
					|| pickType == Material.DIAMOND_SWORD;
			case GOLD_SWORD:
			case WOOD_SWORD:
			default:
				return pickType == Material.WOOD_SWORD || pickType == Material.GOLD_SWORD
					|| pickType == Material.STONE_SWORD || pickType == Material.IRON_SWORD
					|| pickType == Material.DIAMOND_SWORD;
				
		}
	}

	public static boolean willDropSelf(Material mat, Material tool, int silkLvl){	
		switch(mat){
			case STONE:
			case CLAY_BRICK:
			case BRICK_STAIRS:
			case ENCHANTMENT_TABLE:
			case OBSERVER:
			case NETHERRACK:
			case NETHER_BRICK:
			case NETHER_BRICK_STAIRS:
			case NETHER_FENCE:
			case RED_NETHER_BRICK:
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
			case SANDSTONE:
			case RED_SANDSTONE:
			case SANDSTONE_STAIRS:
			case RED_SANDSTONE_STAIRS:
			case CONCRETE:
				return pickIsAtLeast(tool, Material.WOOD_PICKAXE);
			case COAL_ORE:
			case QUARTZ_ORE:
				return silkLvl > 0 && pickIsAtLeast(tool, Material.WOOD_PICKAXE);
			case IRON_ORE:
			case LAPIS_ORE:
				return silkLvl > 0 && pickIsAtLeast(tool, Material.STONE_PICKAXE);
			case GOLD_ORE:
			case REDSTONE_ORE:
			case GLOWING_REDSTONE_ORE:
			case DIAMOND_ORE:
			case EMERALD_ORE:
			case DIAMOND_BLOCK:
				return silkLvl > 0 && pickIsAtLeast(tool, Material.IRON_AXE);
			case WEB:
				return tool == Material.SHEARS || (silkLvl > 0 && swordIsAtLeast(tool, Material.WOOD_SWORD));
			case DEAD_BUSH:
			case LONG_GRASS:// An old form of DEAD_BUSH, name displays as "Shrub"
				return tool == Material.SHEARS;
			case PACKED_ICE:
				return silkLvl > 0;
			case MOB_SPAWNER:
				return SILK_SPAWNERS && silkLvl > SILK_SPAWNER_LVL;
			case BEDROCK:
			case COMMAND:
			case COMMAND_CHAIN:
			case COMMAND_REPEATING:
			case ENDER_PORTAL:
			case ENDER_PORTAL_FRAME:
			case BARRIER:
			case STRUCTURE_BLOCK:
			case STRUCTURE_VOID:
				return false;
			default:
				return true;
		}
	}

	public static boolean isFragile(Material mat){
		switch(mat){
			case SAPLING:
//			case WATER:
//			case STATIONARY_WATER:
//			case LAVA:
//			case STATIONARY_LAVA:
			case POWERED_RAIL:
			case DETECTOR_RAIL:
			case ACTIVATOR_RAIL:
			case LONG_GRASS:
			case DEAD_BUSH:
			case YELLOW_FLOWER:
			case RED_ROSE:
			case BROWN_MUSHROOM:
			case RED_MUSHROOM:
			case TORCH:
			case FIRE:
			case REDSTONE_WIRE:
			case CROPS:
			case LADDER:
			case RAILS:
			case WALL_SIGN:
			case SIGN_POST:
			case LEVER:
			case STONE_PLATE:
			case WOOD_PLATE:
			case GOLD_PLATE:
			case IRON_PLATE:
			case REDSTONE_TORCH_ON:
			case REDSTONE_TORCH_OFF:
			case STONE_BUTTON:
			case WOOD_BUTTON:
			case SNOW:
			case CACTUS:
			case SUGAR_CANE:
			case CAKE:
			case DIODE_BLOCK_ON:
			case DIODE_BLOCK_OFF:
			case PUMPKIN_STEM:
			case MELON_STEM:
			case WATER_LILY:
			case NETHER_WART_BLOCK:
			case FLOWER_POT:
			case CARROT:
			case POTATO:
			case REDSTONE_COMPARATOR_ON:
			case REDSTONE_COMPARATOR_OFF:
			case CARPET:
			case DOUBLE_PLANT:
			case STANDING_BANNER:
			case WALL_BANNER:
			case WOODEN_DOOR:
			case SPRUCE_DOOR:
			case BIRCH_DOOR:
			case JUNGLE_DOOR:
			case ACACIA_DOOR:
			case DARK_OAK_DOOR:
			case CHORUS_PLANT:
			case CHORUS_FLOWER:
				return true;
			default:
				return false;
		}
	}

	public static ItemStack standardize(ItemStack item){return standardize(item, false);}
	public static ItemStack standardize(ItemStack item, boolean ignoreLeftovers){
		if(item.hasItemMeta()){//STANDARD_LORE, STANDARD_NAME, STANDARD_ENCHANTS, STANDARD_FLAGS, STANDARD_META;
			boolean oneOfAbove = false;
			if((oneOfAbove |= item.getItemMeta().hasDisplayName()) && !STANDARD_NAME) return item;
			if((oneOfAbove |= item.getItemMeta().hasLore()) && !STANDARD_LORE) return item;
			if((oneOfAbove |=!item.getItemMeta().getEnchants().isEmpty()) && !STANDARD_ENCHANTS) return item;
			if((oneOfAbove |=!item.getItemMeta().getItemFlags().isEmpty()) && !STANDARD_FLAGS) return item;
			if(!oneOfAbove && !STANDARD_OTHER_META) return item;
			return item;
		}
		byte data = item.getData().getData();

		switch(item.getType()){
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
			case STAINED_CLAY:
			case HARD_CLAY:
			case CLAY:
			case BRICK:
				return new ItemStack(Material.CLAY_BALL, item.getAmount()*4);
			case CLAY_BRICK:
				return new ItemStack(Material.CLAY_BALL, item.getAmount());
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
				return new ItemStack(Material.SHULKER_SHELL, item.getAmount()*2);
			case FLINT:
			case FLINT_AND_STEEL:
				return new ItemStack(Material.GRAVEL, item.getAmount());
			case DIAMOND_SPADE:
			case JUKEBOX:
				return new ItemStack(Material.DIAMOND, item.getAmount());
			case DIAMOND_HOE:
			case ENCHANTMENT_TABLE:
				return new ItemStack(Material.DIAMOND, item.getAmount()*2);
			case DIAMOND_BOOTS:
				return new ItemStack(Material.DIAMOND, item.getAmount()*4);
			case DIAMOND_HELMET:
				return new ItemStack(Material.DIAMOND, item.getAmount()*5);
			case DIAMOND_LEGGINGS:
				return new ItemStack(Material.DIAMOND, item.getAmount()*7);
			case DIAMOND_CHESTPLATE:
				return new ItemStack(Material.DIAMOND, item.getAmount()*8);
//			case DIAMOND_ORE:
//				return new ItemStack(Material.DIAMOND, item.getAmount()*maxDiaPerOre);
//			case DIAMOND:
//				rescuedParts.get(Material.DIAMOND_ORE).add(item.getAmount(), maxDiaPerOre);
//				return new ItemStack(Material.DIAMOND_ORE, rescuedParts.get(Material.DIAMOND_ORE).take1s());
			case DIAMOND_BLOCK:
				return new ItemStack(Material.DIAMOND, item.getAmount()*9);
			case NETHER_STAR:
			case BEACON:
				return new ItemStack(Material.SOUL_SAND, item.getAmount()*4);
			case QUARTZ_BLOCK:
				return new ItemStack(Material.QUARTZ, item.getAmount()*4);
			case QUARTZ_STAIRS:
				return new ItemStack(Material.QUARTZ, item.getAmount()*6);
			case REDSTONE_COMPARATOR:
			case OBSERVER:
				return new ItemStack(Material.QUARTZ, item.getAmount());
			case DAYLIGHT_DETECTOR:
			case DAYLIGHT_DETECTOR_INVERTED:
				return new ItemStack(Material.QUARTZ, item.getAmount()*3);
			case TNT:
			case EXPLOSIVE_MINECART:
			case SANDSTONE:
				return new ItemStack(Material.SAND, item.getAmount()*4);
			case RED_SANDSTONE:
				return new ItemStack(Material.SAND, item.getAmount()*4, (byte)1);
			case SANDSTONE_STAIRS:
				return new ItemStack(Material.SAND, item.getAmount()*6);
			case RED_SANDSTONE_STAIRS:
				return new ItemStack(Material.SAND, item.getAmount()*6, (byte)1);
			case NETHER_BRICK:
			case NETHER_FENCE:
				return new ItemStack(Material.NETHERRACK, item.getAmount()*4);
			case NETHER_BRICK_ITEM:
				return new ItemStack(Material.NETHERRACK, item.getAmount());
			case NETHER_BRICK_STAIRS:
				if(ignoreLeftovers) return item;
				rescuedParts.get(Material.NETHERRACK).add(item.getAmount()*3, 2);
				return new ItemStack(Material.NETHERRACK, rescuedParts.get(Material.NETHERRACK).take1s());
			case RED_NETHER_BRICK:
				if(ignoreLeftovers) return item;
				rescuedParts.get(Material.NETHERRACK).add(item.getAmount(), 2);
				return new ItemStack(Material.NETHERRACK, rescuedParts.get(Material.NETHERRACK).take1s());
			case STEP:
				if(item.getData().getData() == 6){
					if(ignoreLeftovers) return item;
					rescuedParts.get(Material.NETHERRACK).add(item.getAmount(), 2);
					return new ItemStack(Material.NETHERRACK, rescuedParts.get(Material.NETHERRACK).take1s());
				}
				if(item.getData().getData() == 7)
					return new ItemStack(Material.QUARTZ, item.getAmount()*2);
			case CONCRETE:
			case CONCRETE_POWDER:
				if(ignoreLeftovers) return item;
				rescuedParts.get(Material.SAND).add(item.getAmount(), 2);
				rescuedParts.get(Material.GRAVEL).add(item.getAmount(), 2);
				int gravel = rescuedParts.get(Material.GRAVEL).take1s();
				if(gravel != 0) return new ItemStack(Material.GRAVEL, gravel);
				else return new ItemStack(Material.SAND, rescuedParts.get(Material.SAND).take1s());
			case SPONGE:
				return new ItemStack(item.getType(), item.getAmount(), (byte)0);
			case GRASS:
			case GRASS_PATH:
			case SOIL:
//			case DIRT://Dirt, Coarse-dirt, and Podzol are distinct and not interchangeable (Podzol yes in 1.13).
				//Note: Coarse Dirt (dirt:1) and Dirt are distinct (Coarse dirt is crafted with gravel)
				return new ItemStack(DIRT_TO_GRAVEL ? Material.GRAVEL : Material.DIRT, item.getAmount());
			case DIRT:
				if(DIRT_TO_GRAVEL) return new ItemStack(Material.GRAVEL, item.getAmount());
				switch(item.getData().getData()){
					case 0:
						return new ItemStack(Material.DIRT, item.getAmount());
					case 1:
						if(ignoreLeftovers) return item;
						rescuedParts.get(Material.DIRT).add(item.getAmount(), 2);
						rescuedParts.get(Material.GRAVEL).add(item.getAmount(), 2);
						gravel = rescuedParts.get(Material.GRAVEL).take1s();
						if(gravel != 0) return new ItemStack(Material.GRAVEL, gravel);
						else return new ItemStack(Material.DIRT, rescuedParts.get(Material.DIRT).take1s());
					default:
						return item;
				}
			case STONE:
				if(data == 1 || data == 2){//granite
					return new ItemStack(Material.QUARTZ, item.getAmount()*2);
				}
				if(data == 3 || data == 4){//diorite
					return new ItemStack(Material.QUARTZ, item.getAmount());
				}
				if(data == 5 || data == 6){
					if(ignoreLeftovers) return item;
					rescuedParts.get(Material.QUARTZ).add(item.getAmount(), 2);
					return new ItemStack(Material.QUARTZ, rescuedParts.get(Material.QUARTZ).take1s());
				}
			default:
				return item;
		}
	}

	//For irreversible processes: takes two unrenewable items as input
	public static boolean isUnrenewableProcess(ItemStack in, ItemStack out){
		if(!UnrenewableList.isUnrenewable(in)) return false;// If input is renewable, process is renewable
		ItemDesc inDesc = new ItemDesc(in.getType(), in.getData().getData());
		ItemDesc outDesc = new ItemDesc(out.getType(), out.getData().getData());
		return !inDesc.equals(outDesc) && !reversible.sameSet(inDesc, outDesc);
	}

	//For irreversible processes: takes two unrenewable items as input
	public static boolean sameWhenStandardized(ItemStack a, ItemStack b){
		return standardize(a, true).equals(standardize(b, true));
	}

	//For irreversible processes: takes two unrenewable items as input
	public static boolean sameWhenStandardizedIgnoreAmt(ItemStack a, ItemStack b){
		ItemStack stdA = standardize(a, true).clone(), stdB = standardize(b, true).clone();
		stdA.setAmount(1); stdB.setAmount(1);
		return stdA.equals(stdB);
	}

	public static Player getNearbyPlayer(Location loc, int range){
		range = range*range;
		for(Player p : Bukkit.getServer().getOnlinePlayers()){
			if(p.getWorld().getName().equals(loc.getWorld().getName()) && p.getLocation().distanceSquared(loc) > range)
				return p;
		}
		return null;
	}

	public static String getNormalizedName(EntityType type){
		//TODO: improve this algorithm / test for errors
		switch(type){
		case PIG_ZOMBIE:
			return "Zombie Pigman";
		case MUSHROOM_COW:
			return "Mooshroom";
		default:
			StringBuilder name = new StringBuilder();
			for(String str : type.name().split("_")){
				name.append(str.charAt(0));
				name.append(str.substring(1).toLowerCase());
				name.append(" ");
			}
			return name.substring(0, name.length()-1);
		}
	}
}