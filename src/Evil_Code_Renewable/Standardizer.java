package Evil_Code_Renewable;

import java.util.HashMap;
import java.util.Map.Entry;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import EvLib.FileIO;
import EvLib.Fraction;
import EvLib.TypeUtils;

class Standardizer{
	final boolean STD_DIRT, STD_LORE, STD_NAME, STD_ENCHANTS, STD_FLAGS, STD_OTHER_META;
	final static HashMap<Material, Fraction> rescuedParts = new HashMap<Material, Fraction>();
	static{
		rescuedParts.put(Material.QUARTZ, new Fraction(0, 2));
		rescuedParts.put(Material.SAND, new Fraction(0, 2));
		rescuedParts.put(Material.GRAVEL, new Fraction(0, 2));
		rescuedParts.put(Material.NETHERRACK, new Fraction(0, 2));
		rescuedParts.put(Material.DIAMOND_ORE, new Fraction(0, 9));
	}
	final Renewable pl;

	Standardizer(Renewable pl){
		this.pl = pl;
		STD_DIRT = pl.getConfig().getBoolean("dirt-standardizes-to-gravel", true);
		STD_LORE = pl.getConfig().getBoolean("standardize-if-has-lore", false);
		STD_NAME = pl.getConfig().getBoolean("standardize-if-has-name", true);
		STD_ENCHANTS = pl.getConfig().getBoolean("standardize-if-has-enchants", true);
		STD_FLAGS = pl.getConfig().getBoolean("standardize-if-has-flags", false);
		STD_OTHER_META = pl.getConfig().getBoolean("standardize-if-has-other-meta", false);
	}

	void loadFractionalRescues(){
		for(String str : FileIO.loadFile("fractional-rescues.txt", "").split(" ")){
			int i = str.indexOf(',');
			if(i == -1) continue;
			Material mat = Material.getMaterial(str.substring(0, i));
			Fraction frac = Fraction.fromString(str.substring(i+1));
			if(mat != null && frac != null) rescuedParts.put(mat, frac);
		}
	}
	void saveFractionalRescues(){
		if(!rescuedParts.isEmpty()){
			StringBuilder builder = new StringBuilder("");
			for(Entry<Material, Fraction> e : rescuedParts.entrySet())
				builder.append(' ').append(e.getKey().name()).append(',').append(e.getValue());
			FileIO.saveFile("", builder.substring(1));
		}
	}

	public ItemStack standardize(ItemStack item){return standardize(item, false);}
	public ItemStack standardize(ItemStack item, boolean ignoreLeftovers){
		if(item.hasItemMeta()){//STD_LORE, STD_NAME, STD_ENCHANTS, STD_FLAGS, STD_META;
			boolean oneOfAbove = false;
			if((oneOfAbove |= item.getItemMeta().hasDisplayName()) && !STD_NAME) return item;
			if((oneOfAbove |= item.getItemMeta().hasLore()) && !STD_LORE) return item;
			if((oneOfAbove |=!item.getItemMeta().getEnchants().isEmpty()) && !STD_ENCHANTS) return item;
			if((oneOfAbove |=!item.getItemMeta().getItemFlags().isEmpty()) && !STD_FLAGS) return item;
			if(!oneOfAbove && !STD_OTHER_META) return item;
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
//			case DIAMOND_CHESTPLATE:
//				return new ItemStack(Material.DIAMOND, item.getAmount()*8);//Note: renewable -- villagers
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
				return new ItemStack(STD_DIRT ? Material.GRAVEL : Material.DIRT, item.getAmount());
			case COARSE_DIRT:
				if(STD_DIRT) return new ItemStack(Material.GRAVEL, item.getAmount());
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

	void addRescuedParts(Material type, int numer, int denom){
		if(!rescuedParts.containsKey(type)) rescuedParts.put(type, new Fraction(numer, denom));
		rescuedParts.get(type).add(numer, denom);
	}
}