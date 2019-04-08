package Evil_Code_Renewable;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.BlockData;
import org.bukkit.inventory.ItemStack;
import EvLib.FileIO;
import EvLib.TypeUtils;
import org.bukkit.entity.EntityType;
//import org.bukkit.metadata.FixedMetadataValue;
//import net.minecraft.server.v1_11_R1.NBTTagCompound;
//import org.bukkit.craftbukkit.v1_11_R1.inventory.CraftItemStack;
import org.bukkit.entity.Player;

public class Utils{
	static final HashMap<Material, Fraction> rescuedParts = new HashMap<Material, Fraction>();
	static{
		rescuedParts.put(Material.QUARTZ, new Fraction(0, 2));
		rescuedParts.put(Material.SAND, new Fraction(0, 2));
		rescuedParts.put(Material.GRAVEL, new Fraction(0, 2));
		rescuedParts.put(Material.NETHERRACK, new Fraction(0, 2));
		rescuedParts.put(Material.DIAMOND_ORE, new Fraction(0, 9));
	}
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
	public static boolean isUnrenewableBlock(Material mat, BlockData data){
		return UnrenewableList.isUnrenewableBlock(mat, data); }
	public static ItemStack getUnewnewableItemForm(BlockState block){
		return UnrenewableList.getUnewnewableItemForm(block); }

	public static boolean pickIsAtLeast(Material pickType, Material needPick){
		switch(pickType){
			case DIAMOND_PICKAXE:
				return true;
			case IRON_PICKAXE:
				return needPick != Material.DIAMOND_PICKAXE;
			case STONE_PICKAXE:
				return needPick != Material.DIAMOND_PICKAXE && needPick != Material.IRON_PICKAXE;
			case GOLDEN_PICKAXE:
			case WOODEN_PICKAXE:
			default:
				return needPick != Material.DIAMOND_PICKAXE && needPick != Material.IRON_PICKAXE
					&& needPick != Material.STONE_PICKAXE;
				
		}
	}
	public static boolean swordIsAtLeast(Material swordType, Material needSword){
		switch(swordType){
			case DIAMOND_SWORD:
				return true;
			case IRON_SWORD:
				return needSword != Material.DIAMOND_SWORD;
			case STONE_SWORD:
				return needSword != Material.IRON_SWORD && needSword != Material.DIAMOND_SWORD;
			case GOLDEN_SWORD:
			case WOODEN_SWORD:
			default:
				return needSword != Material.IRON_SWORD && needSword != Material.DIAMOND_SWORD
					&& needSword != Material.STONE_SWORD;
				
		}
	}

	public static boolean willDropSelf(Material mat, Material tool, int silkLvl){	
		switch(mat){
			case STONE:
			case CLAY:
			case BRICK_STAIRS:
			case ENCHANTING_TABLE:
			case OBSERVER:
			case NETHERRACK:
			case NETHER_BRICK:
			case NETHER_BRICK_STAIRS:
			case NETHER_BRICK_FENCE:
			case RED_NETHER_BRICKS:
			case QUARTZ_BLOCK:
			case QUARTZ_STAIRS:
			case SANDSTONE:
			case RED_SANDSTONE:
			case SANDSTONE_STAIRS:
			case RED_SANDSTONE_STAIRS:
				return pickIsAtLeast(tool, Material.WOODEN_PICKAXE);
			case COAL_ORE:
			case NETHER_QUARTZ_ORE:
				return silkLvl > 0 && pickIsAtLeast(tool, Material.WOODEN_PICKAXE);
			case IRON_ORE:
			case LAPIS_ORE:
				return silkLvl > 0 && pickIsAtLeast(tool, Material.STONE_PICKAXE);
			case GOLD_ORE:
			case REDSTONE_ORE:
			case DIAMOND_ORE:
			case EMERALD_ORE:
			case DIAMOND_BLOCK:
				return silkLvl > 0 && pickIsAtLeast(tool, Material.IRON_AXE);
			case COBWEB:
				return tool == Material.SHEARS || (silkLvl > 0 && swordIsAtLeast(tool, Material.WOODEN_SWORD));
			case DEAD_BUSH:
				return tool == Material.SHEARS;
//			case PACKED_ICE:
//				return silkLvl > 0;
			case SPAWNER:
				return SILK_SPAWNERS && silkLvl > SILK_SPAWNER_LVL;
			case BEDROCK:
			case COMMAND_BLOCK:
			case CHAIN_COMMAND_BLOCK:
			case REPEATING_COMMAND_BLOCK:
			case END_PORTAL:
			case END_PORTAL_FRAME:
			case BARRIER:
			case STRUCTURE_BLOCK:
			case STRUCTURE_VOID:
				return false;
			default:
				if(TypeUtils.isConcrete(mat) || TypeUtils.isTerracotta(mat) || TypeUtils.isGlazedTerracotta(mat))
					return pickIsAtLeast(tool, Material.WOODEN_PICKAXE);
				return true;
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
		Material type = item.getType();

		switch(type){
			case TERRACOTTA:
			case CLAY:
			case BRICKS:
				return new ItemStack(Material.CLAY_BALL, item.getAmount()*4);
			case BRICK:
				return new ItemStack(Material.CLAY_BALL, item.getAmount());
			case FLINT:
			case FLINT_AND_STEEL:
				return new ItemStack(Material.GRAVEL, item.getAmount());
			case DIAMOND_SHOVEL:
			case JUKEBOX:
				return new ItemStack(Material.DIAMOND, item.getAmount());
			case DIAMOND_HOE:
			case ENCHANTING_TABLE:
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
			case COMPARATOR:
			case OBSERVER:
				return new ItemStack(Material.QUARTZ, item.getAmount());
			case DAYLIGHT_DETECTOR:
				return new ItemStack(Material.QUARTZ, item.getAmount()*3);
			case TNT:
			case TNT_MINECART:
			case SANDSTONE:
				return new ItemStack(Material.SAND, item.getAmount()*4);
			case RED_SANDSTONE:
				return new ItemStack(Material.RED_SAND, item.getAmount()*4);
			case SANDSTONE_STAIRS:
				return new ItemStack(Material.SAND, item.getAmount()*6);
			case RED_SANDSTONE_STAIRS:
				return new ItemStack(Material.RED_SAND, item.getAmount()*6);
			case NETHER_BRICKS:
			case NETHER_BRICK_FENCE:
				return new ItemStack(Material.NETHERRACK, item.getAmount()*4);
			case NETHER_BRICK:
				return new ItemStack(Material.NETHERRACK, item.getAmount());
			case NETHER_BRICK_STAIRS:
				if(ignoreLeftovers) return item;
				rescuedParts.get(Material.NETHERRACK).add(item.getAmount()*3, 2);
				return new ItemStack(Material.NETHERRACK, rescuedParts.get(Material.NETHERRACK).take1s());
			case RED_NETHER_BRICKS:
				if(ignoreLeftovers) return item;
				rescuedParts.get(Material.NETHERRACK).add(item.getAmount(), 2);
				return new ItemStack(Material.NETHERRACK, rescuedParts.get(Material.NETHERRACK).take1s());
			case NETHER_BRICK_SLAB:
				if(ignoreLeftovers) return item;
				rescuedParts.get(Material.NETHERRACK).add(item.getAmount(), 2);
				return new ItemStack(Material.NETHERRACK, rescuedParts.get(Material.NETHERRACK).take1s());
			case QUARTZ_SLAB:
				return new ItemStack(Material.QUARTZ, item.getAmount()*2);
			case SPONGE:
			case WET_SPONGE:
				return new ItemStack(Material.SPONGE, item.getAmount());
			case GRASS_BLOCK:
			case GRASS_PATH:
			case FARMLAND:
			case DIRT:
			case PODZOL:
				return new ItemStack(DIRT_TO_GRAVEL ? Material.GRAVEL : Material.DIRT, item.getAmount());
			case COARSE_DIRT:
				if(DIRT_TO_GRAVEL) return new ItemStack(Material.GRAVEL, item.getAmount());
				rescuedParts.get(Material.DIRT).add(item.getAmount(), 2);
				rescuedParts.get(Material.GRAVEL).add(item.getAmount(), 2);
				int gravel = rescuedParts.get(Material.GRAVEL).take1s();
				if(gravel != 0) return new ItemStack(Material.GRAVEL, gravel);
				else return new ItemStack(Material.DIRT, rescuedParts.get(Material.DIRT).take1s());
			case GRANITE:
			case POLISHED_GRANITE:
				return new ItemStack(Material.QUARTZ, item.getAmount()*2);
			case DIORITE:
			case POLISHED_DIORITE:
				return new ItemStack(Material.QUARTZ, item.getAmount());
			case ANDESITE:
			case POLISHED_ANDESITE:
				if(ignoreLeftovers) return item;
				rescuedParts.get(Material.QUARTZ).add(item.getAmount(), 2);
				return new ItemStack(Material.QUARTZ, rescuedParts.get(Material.QUARTZ).take1s());
			default:
				if(TypeUtils.isShulkerBox(type)){
					return new ItemStack(Material.SHULKER_SHELL, item.getAmount()*2);
				}
				if(TypeUtils.isTerracotta(type) || TypeUtils.isGlazedTerracotta(type)){
					return new ItemStack(Material.CLAY_BALL, item.getAmount()*4);
				}
				if(TypeUtils.isConcrete(type) || TypeUtils.isConcretePowder(type)){
					if(ignoreLeftovers) return item;
					rescuedParts.get(Material.SAND).add(item.getAmount(), 2);
					rescuedParts.get(Material.GRAVEL).add(item.getAmount(), 2);
					gravel = rescuedParts.get(Material.GRAVEL).take1s();
					if(gravel != 0) return new ItemStack(Material.GRAVEL, gravel);
					else return new ItemStack(Material.SAND, rescuedParts.get(Material.SAND).take1s());
				}
				if(TypeUtils.isFlowerPot(type))
					return new ItemStack(Material.CLAY_BALL, item.getAmount()*3);
				return item;
		}
	}

	//For irreversible processes: takes two unrenewable items as input
	public static boolean isUnrenewableProcess(ItemStack in, ItemStack out){
		if(!UnrenewableList.isUnrenewable(in)) return false;// If input is renewable, process is renewable
		return !in.getType().equals(out.getType()) && !reversible.sameSet(in.getType(), out.getType());
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