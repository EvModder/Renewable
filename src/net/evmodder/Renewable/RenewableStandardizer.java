package net.evmodder.Renewable;

import java.util.HashMap;
import java.util.Map.Entry;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import net.evmodder.EvLib.FileIO;
import net.evmodder.EvLib.Fraction;
import net.evmodder.EvLib.TypeUtils;

class RenewableStandardizer{//TODO: standardize slabs/stairs using stone-cutter values (not crafting table costs)
	final boolean STD_LORE, STD_NAME, STD_ENCHANTS, STD_FLAGS, STD_OTHER_META;
	final static HashMap<Material, Fraction> rescuedParts = new HashMap<Material, Fraction>();
	static{
		rescuedParts.put(Material.QUARTZ, new Fraction(0, 2));
		rescuedParts.put(Material.GRAVEL, new Fraction(0, 2));
		rescuedParts.put(Material.NETHERRACK, new Fraction(0, 2));
		rescuedParts.put(Material.DIAMOND_ORE, new Fraction(0, 9));
	}
	final Renewable pl;

	RenewableStandardizer(Renewable pl){
		this.pl = pl;
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
			case FLINT:
			case FLINT_AND_STEEL:
				return new ItemStack(Material.GRAVEL, item.getAmount());
			case FLETCHING_TABLE:
				return new ItemStack(Material.GRAVEL, item.getAmount()*2);
			case DIAMOND_SHOVEL:
			case JUKEBOX:
				return new ItemStack(Material.DIAMOND, item.getAmount());
			case ENCHANTING_TABLE:
				return new ItemStack(Material.DIAMOND, item.getAmount()*2);
			case DIAMOND_BLOCK:
				return new ItemStack(Material.DIAMOND, item.getAmount()*9);
			case NETHER_STAR:
			case BEACON:
				return new ItemStack(Material.SOUL_SAND, item.getAmount()*4);
			case COMPARATOR:
			case OBSERVER:
				return new ItemStack(Material.QUARTZ, item.getAmount());
			case SMOOTH_QUARTZ_SLAB:
				return new ItemStack(Material.QUARTZ, item.getAmount()*2);
			case DAYLIGHT_DETECTOR:
				return new ItemStack(Material.QUARTZ, item.getAmount()*3);
			case QUARTZ_BLOCK:
			case SMOOTH_QUARTZ:
				return new ItemStack(Material.QUARTZ, item.getAmount()*4);
			case QUARTZ_STAIRS:
			case SMOOTH_QUARTZ_STAIRS:
				return new ItemStack(Material.QUARTZ, item.getAmount()*6);
			case NETHER_BRICK_STAIRS:
				return new ItemStack(Material.NETHERRACK, item.getAmount()*6);
			case NETHER_BRICKS:
			case NETHER_BRICK_WALL:
				return new ItemStack(Material.NETHERRACK, item.getAmount()*4);
			case NETHER_BRICK_FENCE:
			case RED_NETHER_BRICK_STAIRS:
				return new ItemStack(Material.NETHERRACK, item.getAmount()*3);
			case NETHER_BRICK_SLAB:
			case RED_NETHER_BRICKS:
			case RED_NETHER_BRICK_WALL:
				return new ItemStack(Material.NETHERRACK, item.getAmount()*2);
			case NETHER_BRICK:
			case RED_NETHER_BRICK_SLAB:
				return new ItemStack(Material.NETHERRACK, item.getAmount());
			case WET_SPONGE:
				return new ItemStack(Material.SPONGE, item.getAmount());
			case COARSE_DIRT:
				rescuedParts.get(Material.GRAVEL).add(item.getAmount(), 2);
				int gravel = rescuedParts.get(Material.GRAVEL).take1s();
				if(gravel != 0) return new ItemStack(Material.GRAVEL, gravel);
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
				if(TypeUtils.isConcretePowder(type)){
					if(ignoreLeftovers) return item;
					rescuedParts.get(Material.GRAVEL).add(item.getAmount(), 2);
					gravel = rescuedParts.get(Material.GRAVEL).take1s();
					if(gravel != 0) return new ItemStack(Material.GRAVEL, gravel);
				}
				return item;
		}
	}

	void addRescuedParts(Material type, int numer, int denom){
		if(!rescuedParts.containsKey(type)) rescuedParts.put(type, new Fraction(numer, denom));
		rescuedParts.get(type).add(numer, denom);
	}
}