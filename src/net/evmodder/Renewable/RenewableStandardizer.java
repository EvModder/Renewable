package net.evmodder.Renewable;

import java.util.HashMap;
import java.util.Map.Entry;
import org.bukkit.Material;
import org.bukkit.block.DecoratedPot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.inventory.meta.FireworkEffectMeta;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.inventory.meta.ItemMeta;
import net.evmodder.EvLib.FileIO;
import net.evmodder.EvLib.util.Fraction;

class RenewableStandardizer{//TODO: standardize slabs/stairs using stone-cutter values (not crafting table costs)
	final private boolean STD_LORE, STD_NAME, STD_ENCHANTS, STD_FLAGS, STD_OTHER_META;
	final private static HashMap<Material, Fraction> rescuedParts = new HashMap<Material, Fraction>();
	static{
		rescuedParts.put(Material.DEEPSLATE, new Fraction(0, 2));
		rescuedParts.put(Material.NETHERRACK, new Fraction(0, 1)); // From standardizing certain smithing templates
		rescuedParts.put(Material.DIAMOND, new Fraction(0, 3));
//		rescuedParts.put(Material.DIAMOND_ORE, new Fraction(0, 3*4));// 3rd firework * 4 @ F3
//		rescuedParts.put(Material.IRON_ORE, new Fraction(0, 4));//4 raw iron @ F3
//		rescuedParts.put(Material.GOLD_ORE, new Fraction(0, 4));//4 raw gold @ F3
//		rescuedParts.put(Material.COPPER_ORE, new Fraction(0, 20));//20 raw copper @ F3
		for(Material mat : Material.values()) if(JunkUtils.isPotterySherd(mat)) rescuedParts.put(mat, new Fraction(0, 1));
	}

	RenewableStandardizer(Renewable pl){
		STD_LORE = pl.getConfig().getBoolean("standardize-if-has-lore", false);
		STD_NAME = pl.getConfig().getBoolean("standardize-if-has-name", true);
		STD_ENCHANTS = pl.getConfig().getBoolean("standardize-if-has-enchants", true);
		STD_FLAGS = pl.getConfig().getBoolean("standardize-if-has-flags", false);
		STD_OTHER_META = pl.getConfig().getBoolean("standardize-if-has-other-meta", false);
	}

	void loadFractionalRescues(){
		for(String str : FileIO.loadFile("fractional-rescues.txt", "").split(" ")){
			final int i = str.indexOf(',');
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
			FileIO.saveFile("fractional-rescues.txt", builder.substring(1));
		}
	}

	public void addRescuedParts(Material type, int numer, int denom){
		if(!rescuedParts.containsKey(type)) rescuedParts.put(type, new Fraction(numer, denom));
		rescuedParts.get(type).add(numer, denom);
	}

	// Side effect: rescuedParts
	public ItemStack standardize(ItemStack item, int mult){
		if(item.hasItemMeta()){//STD_LORE, STD_NAME, STD_ENCHANTS, STD_FLAGS, STD_META;
			ItemMeta meta = item.getItemMeta();
			if(!STD_NAME && meta.hasDisplayName()) return item;
			if(!STD_LORE && meta.hasLore()) return item;
			if(!STD_ENCHANTS && !meta.getEnchants().isEmpty()) return item;
			if(!STD_FLAGS && !meta.getItemFlags().isEmpty()) return item;
			if(!STD_OTHER_META && (meta.hasDisplayName() || meta.hasLore() ||
					!meta.getEnchants().isEmpty() || !meta.getItemFlags().isEmpty())) return item;
		}
		final Material type = item.getType();
		if(mult < -1 || mult > 1) Renewable.getPlugin().getLogger().warning("standardize() called with illegal multiplier: "+mult);

		int leftovers;
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
			case FIREWORK_STAR:
				if(item.hasItemMeta() && ((FireworkEffectMeta)item.getItemMeta()).getEffect().hasTrail())
					return new ItemStack(Material.DIAMOND, item.getAmount());
				else return new ItemStack(Material.AIR);
			case FIREWORK_ROCKET:
				if(item.hasItemMeta()) rescuedParts.get(Material.DIAMOND).add(
							(int)(((FireworkMeta)item.getItemMeta()).getEffects().stream().filter(e -> e.hasTrail()).count()
									*item.getAmount()*mult), 3);
				if(mult == 1 && (leftovers=rescuedParts.get(Material.DIAMOND).take1s()) != 0) return new ItemStack(Material.DIAMOND, leftovers*mult);
				else return new ItemStack(Material.AIR);
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
			case WET_SPONGE:
				return new ItemStack(Material.SPONGE, item.getAmount());
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
			case INFESTED_DEEPSLATE:
				return new ItemStack(Material.DEEPSLATE, item.getAmount());
			case COBBLED_DEEPSLATE_SLAB:
			case POLISHED_DEEPSLATE_SLAB:
			case DEEPSLATE_BRICK_SLAB:
			case DEEPSLATE_TILE_SLAB:
				rescuedParts.get(Material.DEEPSLATE).add(mult*item.getAmount(), 2);
				if(mult == 1 && (leftovers=rescuedParts.get(Material.DEEPSLATE).take1s()) != 0) return new ItemStack(Material.DEEPSLATE, leftovers);
				else return new ItemStack(Material.AIR);
			case DECORATED_POT: {
				Material take=null; int most=0;
				// Add all (1-4) Sherds to rescuedParts, and return whichever one is most backlogged
				for(Material mat : ((DecoratedPot)((BlockStateMeta)item.getItemMeta()).getBlockState()).getShards()){
					final Fraction f = rescuedParts.get(mat);
					f.add(mult*item.getAmount(), 1);
					if(f.getNumerator() > most){most = f.getNumerator(); take = mat;}
				}
				final Material takeFinal = take;
				if(most == 0) return new ItemStack(Material.AIR);
				else return new ItemStack(take, mult == 1 ? rescuedParts.get(take).take1s() : item.getAmount()*
						(int)((DecoratedPot)((BlockStateMeta)item.getItemMeta()).getBlockState()).getShards().stream()
						.filter(mat -> mat == takeFinal).count()
				);
			}
			case NETHERRACK:
				if(mult < 1) return item;
				return new ItemStack(Material.NETHERRACK, item.getAmount() + rescuedParts.get(Material.NETHERRACK).take1s());
			case DEEPSLATE:
				if(mult < 1) return item;
				return new ItemStack(Material.DEEPSLATE, item.getAmount() + rescuedParts.get(Material.DEEPSLATE).take1s());
			case NETHERITE_UPGRADE_SMITHING_TEMPLATE:
			case RIB_ARMOR_TRIM_SMITHING_TEMPLATE:
				rescuedParts.get(Material.NETHERRACK).add(mult*item.getAmount(), 1);
				return new ItemStack(Material.DIAMOND, item.getAmount()*7);
			case SILENCE_ARMOR_TRIM_SMITHING_TEMPLATE:
			case WARD_ARMOR_TRIM_SMITHING_TEMPLATE:
				rescuedParts.get(Material.DEEPSLATE).add(mult*item.getAmount(), 1);
				return new ItemStack(Material.DIAMOND, item.getAmount()*7);
			case COAST_ARMOR_TRIM_SMITHING_TEMPLATE:
			case DUNE_ARMOR_TRIM_SMITHING_TEMPLATE:
			case EYE_ARMOR_TRIM_SMITHING_TEMPLATE:
			case HOST_ARMOR_TRIM_SMITHING_TEMPLATE:
			case RAISER_ARMOR_TRIM_SMITHING_TEMPLATE:
			case SENTRY_ARMOR_TRIM_SMITHING_TEMPLATE:
			case SHAPER_ARMOR_TRIM_SMITHING_TEMPLATE:
			case SNOUT_ARMOR_TRIM_SMITHING_TEMPLATE:
			case SPIRE_ARMOR_TRIM_SMITHING_TEMPLATE:
			case TIDE_ARMOR_TRIM_SMITHING_TEMPLATE:
			case VEX_ARMOR_TRIM_SMITHING_TEMPLATE:
			case WAYFINDER_ARMOR_TRIM_SMITHING_TEMPLATE:
			case WILD_ARMOR_TRIM_SMITHING_TEMPLATE:
				return new ItemStack(Material.DIAMOND, item.getAmount()*7);
			default:
				return item;
		}
	}
}