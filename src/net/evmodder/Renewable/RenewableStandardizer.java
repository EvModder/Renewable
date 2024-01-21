package net.evmodder.Renewable;

import java.util.Arrays;
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

	public class ItemWithFractionAmt{
		ItemStack item;
		Fraction amt;
		ItemWithFractionAmt(ItemStack i, Fraction f){item=i; amt=f;}
		@Override public boolean equals(Object o){
			return o instanceof ItemWithFractionAmt && ((ItemWithFractionAmt)o).item.equals(item) && ((ItemWithFractionAmt)o).amt.equals(amt);
		}
	}
	private ItemWithFractionAmt mF(Material mat, int amt){
		return new ItemWithFractionAmt(new ItemStack(mat, 999), new Fraction(amt, 1));
	}
	private ItemWithFractionAmt mF(Material mat, Fraction f){
		return new ItemWithFractionAmt(new ItemStack(mat, 999), f);
	}
	private ItemWithFractionAmt[] mFA(Material mat, int amt){
		return new ItemWithFractionAmt[]{mF(mat, amt)};
	}
	private ItemWithFractionAmt[] mFA(Material mat, Fraction f){
		return new ItemWithFractionAmt[]{mF(mat, f)};
	}
	public ItemWithFractionAmt[] standardize(ItemStack item){
		if(item.hasItemMeta()){//STD_LORE, STD_NAME, STD_ENCHANTS, STD_FLAGS, STD_META;
			final ItemMeta meta = item.getItemMeta();
			if((!STD_NAME && meta.hasDisplayName()) ||
				(!STD_LORE && meta.hasLore()) ||
				(!STD_ENCHANTS && !meta.getEnchants().isEmpty()) ||
				(!STD_FLAGS && !meta.getItemFlags().isEmpty()) ||
				(!STD_OTHER_META && (meta.hasDisplayName() || meta.hasLore() ||
					!meta.getEnchants().isEmpty() || !meta.getItemFlags().isEmpty())))
				{
				return new ItemWithFractionAmt[]{new ItemWithFractionAmt(item, new Fraction(item.getAmount(), 1))};
			}
		}
		switch(item.getType()){
			case RAW_COPPER_BLOCK:
				return mFA(Material.RAW_COPPER, item.getAmount()*9);
			case RAW_IRON_BLOCK:
				return mFA(Material.RAW_IRON, item.getAmount()*9);
			case RAW_GOLD_BLOCK:
				return mFA(Material.RAW_GOLD, item.getAmount()*9);
			case FLINT:
			case FLINT_AND_STEEL:
				return mFA(Material.GRAVEL, item.getAmount());
			case FLETCHING_TABLE:
				return mFA(Material.GRAVEL, item.getAmount()*2);
			case DIAMOND_SHOVEL:
			case JUKEBOX:
				return mFA(Material.DIAMOND, item.getAmount());
			case FIREWORK_STAR:
				if(item.hasItemMeta() && ((FireworkEffectMeta)item.getItemMeta()).getEffect().hasTrail()) return mFA(Material.DIAMOND, item.getAmount());
				else return mFA(Material.AIR, 1);
			case FIREWORK_ROCKET: {
				final long numTrails = !item.hasItemMeta() ? 0 :
					((FireworkMeta)item.getItemMeta()).getEffects().stream().filter(e -> e.hasTrail()).count();
				if(numTrails == 0) return mFA(Material.AIR, 1);
				else return mFA(Material.DIAMOND, new Fraction((int)numTrails*item.getAmount(), 3));
			}
			case ENCHANTING_TABLE:
				return mFA(Material.DIAMOND, item.getAmount()*2);
			case DIAMOND_BLOCK:
				return mFA(Material.DIAMOND, item.getAmount()*9);
			case NETHERITE_SCRAP:
				return mFA(Material.ANCIENT_DEBRIS, item.getAmount());
			case NETHERITE_INGOT:
			case NETHERITE_HELMET: case NETHERITE_CHESTPLATE: case NETHERITE_LEGGINGS: case NETHERITE_BOOTS:
			case NETHERITE_SWORD: case NETHERITE_AXE: case NETHERITE_PICKAXE: case NETHERITE_SHOVEL: case NETHERITE_HOE:
			case LODESTONE:
				return mFA(Material.ANCIENT_DEBRIS, item.getAmount()*4);
			case NETHERITE_BLOCK:
				return mFA(Material.ANCIENT_DEBRIS, item.getAmount()*36);
			case WET_SPONGE:
				return mFA(Material.SPONGE, item.getAmount());
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
				return mFA(Material.DEEPSLATE, item.getAmount());
			case COBBLED_DEEPSLATE_SLAB:
			case POLISHED_DEEPSLATE_SLAB:
			case DEEPSLATE_BRICK_SLAB:
			case DEEPSLATE_TILE_SLAB:
				return mFA(Material.DEEPSLATE, new Fraction(item.getAmount(), 2));
			case DECORATED_POT: {
				final ItemWithFractionAmt[] sherds = new ItemWithFractionAmt[4];
				int len = 0;
				for(Material mat : ((DecoratedPot)((BlockStateMeta)item.getItemMeta()).getBlockState()).getSherds().values()){
					boolean newSherd = true;
					for(int i=0; i<len; ++i) if(sherds[i].item.getType() == mat){
						sherds[i].amt.add(item.getAmount(), 1);
						newSherd=false;
						break;
					}
					if(newSherd){
						sherds[len] = mF(mat, item.getAmount());
						++len;
					}
				}
				if(len == 0) return mFA(Material.AIR, 1);
				return Arrays.copyOf(sherds, len);
			}
			case NETHERITE_UPGRADE_SMITHING_TEMPLATE:
			case RIB_ARMOR_TRIM_SMITHING_TEMPLATE:
				return new ItemWithFractionAmt[]{
					mF(Material.DIAMOND, item.getAmount()*7),
					mF(Material.NETHERRACK, item.getAmount())
				};
			case SILENCE_ARMOR_TRIM_SMITHING_TEMPLATE:
			case WARD_ARMOR_TRIM_SMITHING_TEMPLATE:
				return new ItemWithFractionAmt[]{
						mF(Material.DIAMOND, item.getAmount()*7),
						mF(Material.DEEPSLATE, item.getAmount())
					};
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
				return mFA(Material.DIAMOND, item.getAmount()*7);
			default:
				return mFA(item.getType(), item.getAmount());
		}
	}

	public ItemStack standardizeWithExtras(ItemStack item, boolean add){
		final ItemWithFractionAmt[] items = standardize(item);
		ItemWithFractionAmt itemToTake = null;
		for(ItemWithFractionAmt i : items){
			final Fraction f = rescuedParts.get(i.item.getType());
			if(f == null) itemToTake = i; // Take whatever component is NOT stored in rescuedParts
			else if(add){i.amt.add(f.getNumerator(), f.getDenominator()); rescuedParts.put(i.item.getType(), i.amt);}
			else{f.add(-i.amt.getNumerator(), i.amt.getDenominator());}
		}

		if(itemToTake != null){
			if(itemToTake.amt.getDenominator() != 1){
				Renewable.getPlugin().getLogger().severe("item NOT stored in rescuedParts has a fractional amt: "+itemToTake.item.getType());
			}
			itemToTake.item.setAmount(itemToTake.amt.take1s());
			return itemToTake.item;
		}
		else if(!add) return new ItemStack(Material.AIR);
		else{
			// Take whatever has the largest backlog of rescuedParts
			itemToTake = items[0];
			for(int i=1; i<items.length; ++i){
				if(items[i].amt.compareTo(itemToTake.amt) > 0) itemToTake = items[i];
			}
			itemToTake.item.setAmount(itemToTake.amt.take1s()); // Shared ref, so take1s() will also update value in rescuedParts
			return itemToTake.item;
		}
	}
}