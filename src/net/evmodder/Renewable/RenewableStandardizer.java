package net.evmodder.Renewable;

import java.util.HashMap;
import java.util.Map.Entry;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import net.evmodder.EvLib.FileIO;
import net.evmodder.EvLib.util.Fraction;
import net.evmodder.EvLib.extras.TypeUtils;

class RenewableStandardizer{//TODO: standardize slabs/stairs using stone-cutter values (not crafting table costs)
	final boolean STD_LORE, STD_NAME, STD_ENCHANTS, STD_FLAGS, STD_OTHER_META;
	final static HashMap<Material, Fraction> rescuedParts = new HashMap<Material, Fraction>();
	static{
		rescuedParts.put(Material.DEEPSLATE, new Fraction(0, 2));
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

	public ItemStack standardize(ItemStack item, boolean addOrTake){
		if(item.hasItemMeta()){//STD_LORE, STD_NAME, STD_ENCHANTS, STD_FLAGS, STD_META;
			ItemMeta meta = item.getItemMeta();
			if(!STD_NAME && meta.hasDisplayName()) return item;
			if(!STD_LORE && meta.hasLore()) return item;
			if(!STD_ENCHANTS && !meta.getEnchants().isEmpty()) return item;
			if(!STD_FLAGS && !meta.getItemFlags().isEmpty()) return item;
			if(!STD_OTHER_META && (meta.hasDisplayName() || meta.hasLore() ||
					!meta.getEnchants().isEmpty() || !meta.getItemFlags().isEmpty())) return item;
		}
		Material type = item.getType();
		int mult = addOrTake ? 1 : -1;

		// 1 granite = 2 quartz
		// 1 diorite = 1 quartz
		// 1 andesite=.5 quartz
		switch(type){
			case RAW_COPPER_BLOCK:
				return new ItemStack(Material.RAW_COPPER, item.getAmount()*9);
			case RAW_IRON_BLOCK:
				return new ItemStack(Material.RAW_IRON, item.getAmount()*9);
			case RAW_GOLD_BLOCK:
				return new ItemStack(Material.RAW_GOLD, item.getAmount()*9);
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
			case NETHERITE_SCRAP:
				return new ItemStack(Material.ANCIENT_DEBRIS, item.getAmount());
			case NETHERITE_INGOT:
			case NETHERITE_HELMET: case NETHERITE_CHESTPLATE: case NETHERITE_LEGGINGS: case NETHERITE_BOOTS:
			case NETHERITE_SWORD: case NETHERITE_AXE: case NETHERITE_PICKAXE: case NETHERITE_SHOVEL: case NETHERITE_HOE:
			case LODESTONE:
				return new ItemStack(Material.ANCIENT_DEBRIS, item.getAmount()*4);
			case NETHERITE_BLOCK:
				return new ItemStack(Material.ANCIENT_DEBRIS, item.getAmount()*36);
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
				return new ItemStack(Material.QUARTZ, item.getAmount()*4/*6 without stonecutter*/);
			case NETHER_BRICK_STAIRS:
				return new ItemStack(Material.NETHERRACK, item.getAmount()*4/*6 without stonecutter*/);
			case NETHER_BRICKS:
			case NETHER_BRICK_WALL:
				return new ItemStack(Material.NETHERRACK, item.getAmount()*4);
			case NETHER_BRICK_FENCE:
				return new ItemStack(Material.NETHERRACK, item.getAmount()*3);
			case RED_NETHER_BRICK_STAIRS:
				return new ItemStack(Material.NETHERRACK, item.getAmount()*2/*3 without stonecutter*/);
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
				rescuedParts.get(Material.GRAVEL).add(mult*item.getAmount(), 2);
				int leftovers = rescuedParts.get(Material.GRAVEL).take1s();
				if(leftovers != 0) return new ItemStack(Material.GRAVEL, leftovers*mult);
			case COBBLED_DEEPSLATE:
			case COBBLED_DEEPSLATE_STAIRS:
			case COBBLED_DEEPSLATE_WALL:
			case CHISELED_DEEPSLATE:
			case POLISHED_DEEPSLATE:
			case POLISHED_DEEPSLATE_STAIRS:
			case POLISHED_DEEPSLATE_WALL:
			case DEEPSLATE_BRICKS:
			case CRACKED_DEEPSLATE_BRICKS:
			case DEEPSLATE_BRICK_STAIRS:
			case DEEPSLATE_BRICK_WALL:
			case DEEPSLATE_TILES:
			case CRACKED_DEEPSLATE_TILES:
			case DEEPSLATE_TILE_STAIRS:
			case DEEPSLATE_TILE_WALL:
//			case INFESTED_DEEPSLATE:
				return new ItemStack(Material.DEEPSLATE, item.getAmount());
			case COBBLED_DEEPSLATE_SLAB:
			case POLISHED_DEEPSLATE_SLAB:
			case DEEPSLATE_BRICK_SLAB:
			case DEEPSLATE_TILE_SLAB:
				rescuedParts.get(Material.DEEPSLATE).add(mult*item.getAmount(), 2);
				leftovers = rescuedParts.get(Material.DEEPSLATE).take1s();
				if(leftovers != 0) return new ItemStack(Material.DEEPSLATE, leftovers*mult);
			case GRANITE:
			case GRANITE_STAIRS:
			case GRANITE_WALL:
			case POLISHED_GRANITE:
			case POLISHED_GRANITE_STAIRS:
				return new ItemStack(Material.QUARTZ, item.getAmount()*2);
			case DIORITE:
			case DIORITE_STAIRS:
			case DIORITE_WALL:
			case POLISHED_DIORITE:
			case POLISHED_DIORITE_STAIRS:
			case GRANITE_SLAB:
			case POLISHED_GRANITE_SLAB:
				return new ItemStack(Material.QUARTZ, item.getAmount());
			case ANDESITE:
			case ANDESITE_STAIRS:
			case ANDESITE_WALL:
			case POLISHED_ANDESITE:
			case POLISHED_ANDESITE_STAIRS:
			case DIORITE_SLAB:
			case POLISHED_DIORITE_SLAB:
				rescuedParts.get(Material.QUARTZ).add(mult*item.getAmount(), 2);
				leftovers = rescuedParts.get(Material.QUARTZ).take1s();
				if (leftovers != 0) return new ItemStack(Material.QUARTZ, leftovers*mult);
				else return new ItemStack(Material.AIR);
			case ANDESITE_SLAB:
			case POLISHED_ANDESITE_SLAB:
				rescuedParts.get(Material.QUARTZ).add(mult*item.getAmount(), 4);
				leftovers = rescuedParts.get(Material.QUARTZ).take1s();
				if (leftovers != 0) return new ItemStack(Material.QUARTZ, leftovers*mult);
				else return new ItemStack(Material.AIR);
			default:
				if(TypeUtils.isShulkerBox(type)){
					return new ItemStack(Material.SHULKER_SHELL, item.getAmount()*2);
				}
				if(TypeUtils.isConcrete(type) || TypeUtils.isConcretePowder(type)){
					rescuedParts.get(Material.GRAVEL).add(mult*item.getAmount(), 2);
					leftovers = rescuedParts.get(Material.GRAVEL).take1s();
					if (leftovers != 0) return new ItemStack(Material.GRAVEL, leftovers*mult);
					else return new ItemStack(Material.AIR);
				}
				return item;
		}
	}

	void addRescuedParts(Material type, int numer, int denom){
		if(!rescuedParts.containsKey(type)) rescuedParts.put(type, new Fraction(numer, denom));
		rescuedParts.get(type).add(numer, denom);
	}
}