package net.evmodder.Renewable;

import java.util.HashSet;
import org.bukkit.Material;
import org.bukkit.block.DecoratedPot;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.SculkShrieker;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkEffectMeta;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.Repairable;
import net.evmodder.EvLib.extras.EntityUtils;
import net.evmodder.EvLib.extras.TypeUtils;
import net.evmodder.EvLib.util.UnionFind;

public class RenewableChecker{
	static final HashSet<Material> rescueList = new HashSet<Material>(); // Accessed by RenewableAPI
	static final private HashSet<Material> artificiallyRenewable = new HashSet<Material>();
	static final private UnionFind<Material> reversible = new UnionFind<Material>();
	static{
		reversible.add(Material.DIAMOND);
		reversible.addToSet(Material.DIAMOND_BLOCK, Material.DIAMOND); // Via crafting

		reversible.add(Material.NETHERITE_INGOT);
		reversible.addToSet(Material.NETHERITE_BLOCK, Material.NETHERITE_INGOT); // Via crafting

		reversible.add(Material.DEEPSLATE);
		reversible.addToSet(Material.COBBLED_DEEPSLATE, Material.DEEPSLATE); // Via smelting

		reversible.add(Material.SPONGE);
		reversible.addToSet(Material.WET_SPONGE, Material.SPONGE); // Via smelting
	}

	final private boolean UNRENEWABLE_MOBS, UNRENEWABLE_GRAVITY, UNRENEWABLE_RC;
	final private boolean UNRENEWABLE_UNOBT, OBT_SPAWNERS, OBT_MOB_EGGS, OBT_INFESTED, OBT_CMD_BLOCKS,
					OBT_BEDROCK, OBT_END_PORTALS, OBT_BARRIERS, OBT_STRUCTURE_BLOCKS, OBT_LIGHT, OBT_PETRIFIED_SLABS, OBT_REINFORCED_DEEPSLATE,
					OBT_PLAYER_HEADS, OBT_ILLEGAL_ENCHANTS, OBT_CONFLICTING_ENCHANTS, OBT_OVERSIZED_ENCHANTS;
	RenewableChecker(Renewable pl){
		final FileConfiguration config = pl.getConfig();
		pl.getLogger().fine("All mob drops renewable: "+!(UNRENEWABLE_MOBS =  !config.getBoolean("renewable-mob-drops", false)));
		pl.getLogger().fine("All gravity blocks renewable: "+!(UNRENEWABLE_GRAVITY = !config.getBoolean("renewable-gravity-blocks", false)));
		pl.getLogger().fine("All RepairCosts renewable: "+!(UNRENEWABLE_RC = !config.getBoolean("renewable-rc0", false)));

		pl.getLogger().fine("Unobtainable items are renewable: "+!(UNRENEWABLE_UNOBT = !config.getBoolean("renewable-unobtainables", false)));
		pl.getLogger().fine("Obtainable spawners: "+(OBT_SPAWNERS = config.getBoolean("obtainable.spawners", false)));
		pl.getLogger().fine("Obtainable spawn eggs: "+(OBT_MOB_EGGS = config.getBoolean("obtainable.spawn-eggs", false)));
		pl.getLogger().fine("Obtainable infested blocks: "+(OBT_INFESTED = config.getBoolean("obtainable.infested-blocks", false)));
		pl.getLogger().fine("Obtainable command blocks: "+(OBT_CMD_BLOCKS = config.getBoolean("obtainable.command-blocks", false)));
		pl.getLogger().fine("Obtainable bedrock: "+(OBT_BEDROCK = config.getBoolean("obtainable.bedrock", false)));
		pl.getLogger().fine("Obtainable end portals: "+(OBT_END_PORTALS = config.getBoolean("obtainable.end-portals", false)));
		pl.getLogger().fine("Obtainable barriers: "+!(OBT_BARRIERS = config.getBoolean("obtainable.barriers", false)));
		pl.getLogger().fine("Obtainable structure blocks: "+(OBT_STRUCTURE_BLOCKS = config.getBoolean("obtainable.structure-blocks", false)));
		pl.getLogger().fine("Obtainable light blocks: "+(OBT_LIGHT = config.getBoolean("obtainable.light-blocks", false)));
		pl.getLogger().fine("Obtainable petrified slabs: "+(OBT_PETRIFIED_SLABS = config.getBoolean("obtainable.petrified-slabs", false)));
		pl.getLogger().fine("Obtainable reinforced Deepslate: "+(OBT_REINFORCED_DEEPSLATE = config.getBoolean("obtainable.reinforced-deepslate", false)));
		pl.getLogger().fine("Obtainable player heads: "+(OBT_PLAYER_HEADS = config.getBoolean("obtainable.player-heads", false)));
		pl.getLogger().fine("Obtainable not-item-valid enchants: "+(OBT_ILLEGAL_ENCHANTS = config.getBoolean("obtainable.illegal-enchantments", false)));
		pl.getLogger().fine("Obtainable conflict enchants: "+(OBT_CONFLICTING_ENCHANTS = config.getBoolean("obtainable.conflicting-enchantments", false)));
		pl.getLogger().fine("Obtainable over-max-lvl enchants: "+(OBT_OVERSIZED_ENCHANTS = config.getBoolean("obtainable.oversized-enchantments", false)));

		for(String name : config.getStringList("rescued-renewables")){
			try{ rescueList.add(Material.valueOf(name.toUpperCase())); }
			catch(IllegalArgumentException ex){}
		}
		for(String name : config.getStringList("artificial-renewables")){
			try{ artificiallyRenewable.add(Material.valueOf(name.toUpperCase())); }
			catch(IllegalArgumentException ex){}
		}
	}

	boolean isUnrenewablyEnchanted(ItemStack item){
		// Item must have an RC greater or equal to this value otherwise it is unrenewable
		int minRenewableRC = 0;
//		int nonTreasureEnchants = 0;
		ItemMeta meta = item.getItemMeta();
		if(meta == null) return false;
		for(Enchantment ench : item.getEnchantments().keySet()){
			meta.removeEnchant(ench); // Otherwise the enchant will conflict with itself
			if(!ench.canEnchantItem(item) && (UNRENEWABLE_UNOBT != OBT_ILLEGAL_ENCHANTS)) return true;
			if(meta.hasConflictingEnchant(ench) && (UNRENEWABLE_UNOBT != OBT_CONFLICTING_ENCHANTS)) return true;
			final int lvl = item.getEnchantmentLevel(ench);
			if(lvl > ench.getMaxLevel() && (UNRENEWABLE_UNOBT != OBT_OVERSIZED_ENCHANTS)) return true;
			// if(UNRENEWABLE_RC)
			if(ench.isTreasure() ||
				(ench == Enchantment.THORNS && (!TypeUtils.isChestplate(item.getType()) || lvl > 2))
			) minRenewableRC = minRenewableRC*2+1;
//			else if(++nonTreasureEnchants > 5){
//				minRenewableRC = minRenewableRC*2+1;
//				nonTreasureEnchants = 1;
//			}
		}
		return UNRENEWABLE_RC && ((Repairable)meta).getRepairCost() < minRenewableRC;
	}

	boolean isUnrenewableBlock(Material mat, BlockData data){//TODO: deadbush in flower pot (drops self, etc)
		if(artificiallyRenewable.contains(mat)) return false;
		if(rescueList.contains(mat)) return true; // Custom list of items to rescue (ie treat as if unrenewable)

		switch(mat){
			case SUSPICIOUS_SAND:
			case SUSPICIOUS_GRAVEL:
			case DEEPSLATE:
			case COBBLED_DEEPSLATE:
			case COBBLED_DEEPSLATE_SLAB:
			case COBBLED_DEEPSLATE_STAIRS:
			case COBBLED_DEEPSLATE_WALL:
			case CHISELED_DEEPSLATE:
			case POLISHED_DEEPSLATE:
			case POLISHED_DEEPSLATE_SLAB:
			case POLISHED_DEEPSLATE_STAIRS:
			case POLISHED_DEEPSLATE_WALL:
			case DEEPSLATE_BRICKS:
			case CRACKED_DEEPSLATE_BRICKS:
			case DEEPSLATE_BRICK_SLAB:
			case DEEPSLATE_BRICK_STAIRS:
			case DEEPSLATE_BRICK_WALL:
			case DEEPSLATE_TILES:
			case CRACKED_DEEPSLATE_TILES:
			case DEEPSLATE_TILE_SLAB:
			case DEEPSLATE_TILE_STAIRS:
			case DEEPSLATE_TILE_WALL:
			case INFESTED_DEEPSLATE: // Deepslate is never renewable
			case TUFF:
			case CALCITE:
			case CONDUIT:
			case DRAGON_HEAD:
			case DRAGON_WALL_HEAD:
			case ENCHANTING_TABLE:
			case JUKEBOX:
			case LODESTONE:
			case GILDED_BLACKSTONE:
			case ANCIENT_DEBRIS:
			case DIAMOND_BLOCK:
			case NETHERITE_BLOCK:
			case BUDDING_AMETHYST:
			case RAW_COPPER_BLOCK:
			case RAW_IRON_BLOCK:
			case RAW_GOLD_BLOCK:
			case COBWEB:
			case DEAD_BUSH:
			case NETHERRACK:
				return true;
			case DECORATED_POT:
				return !((DecoratedPot)data).getSherds().isEmpty();
			case SPONGE:
			case WET_SPONGE:
				return UNRENEWABLE_MOBS; // Elder Guardian
			case DRAGON_EGG:
				return UNRENEWABLE_GRAVITY;
			case SCULK_SHRIEKER:
				return ((SculkShrieker)data).isCanSummon();
			case SPAWNER:
				return UNRENEWABLE_UNOBT && !OBT_SPAWNERS;
			case BEDROCK:
				return UNRENEWABLE_UNOBT && !OBT_BEDROCK;
			case END_PORTAL:
			case END_PORTAL_FRAME:
				return UNRENEWABLE_UNOBT && !OBT_END_PORTALS;
			case BARRIER:
				return UNRENEWABLE_UNOBT && !OBT_BARRIERS;
			case COMMAND_BLOCK:
			case CHAIN_COMMAND_BLOCK:
			case REPEATING_COMMAND_BLOCK:
				return UNRENEWABLE_UNOBT && !OBT_CMD_BLOCKS;
			case STRUCTURE_BLOCK:
			case STRUCTURE_VOID:
			case JIGSAW:
				return UNRENEWABLE_UNOBT && !OBT_STRUCTURE_BLOCKS;
			case LIGHT:
				return UNRENEWABLE_UNOBT && !OBT_LIGHT;
			case PETRIFIED_OAK_SLAB:
				return UNRENEWABLE_UNOBT && !OBT_PETRIFIED_SLABS;
			case REINFORCED_DEEPSLATE:
				return UNRENEWABLE_UNOBT && !OBT_REINFORCED_DEEPSLATE;
			case PLAYER_HEAD:
			case PLAYER_WALL_HEAD:
				return UNRENEWABLE_UNOBT && !OBT_PLAYER_HEADS;
			default:
				if(UNRENEWABLE_GRAVITY && (TypeUtils.isConcrete(mat) || TypeUtils.isConcretePowder(mat))) return true;
				if(UNRENEWABLE_MOBS && TypeUtils.isShulkerBox(mat)) return true;
				return TypeUtils.isOre(mat);
		}
	}

	// Calls isUnrenewableBlock()
	boolean isUnrenewableItem(ItemStack item){
		if(artificiallyRenewable.contains(item.getType())) return false;

		switch(item.getType()){
			case HEART_OF_THE_SEA:
			case DIAMOND:
			case RAW_COPPER:
			case RAW_IRON:
			case RAW_GOLD:
			case NETHERITE_INGOT:
			case NETHERITE_SCRAP:
			case NETHERITE_HELMET:
			case NETHERITE_CHESTPLATE:
			case NETHERITE_LEGGINGS:
			case NETHERITE_BOOTS:
			case NETHERITE_SWORD:
			case NETHERITE_AXE:
			case NETHERITE_PICKAXE:
			case NETHERITE_SHOVEL:
			case NETHERITE_HOE:
			case MUSIC_DISC_PIGSTEP:
			case MUSIC_DISC_OTHERSIDE:
			case MUSIC_DISC_RELIC:
			case MUSIC_DISC_5:
			case DISC_FRAGMENT_5:
			case ECHO_SHARD:
			case RECOVERY_COMPASS:
			case IRON_HORSE_ARMOR:
			case GOLDEN_HORSE_ARMOR:
			case DIAMOND_HORSE_ARMOR:
			case ELYTRA:
			case ENCHANTED_GOLDEN_APPLE:
			case MOJANG_BANNER_PATTERN:
			case PIGLIN_BANNER_PATTERN:
			case TALL_GRASS: // Only unrenewable in item form
			case LARGE_FERN:
			case NETHERITE_UPGRADE_SMITHING_TEMPLATE:
			case COAST_ARMOR_TRIM_SMITHING_TEMPLATE:
			case DUNE_ARMOR_TRIM_SMITHING_TEMPLATE:
			case EYE_ARMOR_TRIM_SMITHING_TEMPLATE:
			case HOST_ARMOR_TRIM_SMITHING_TEMPLATE:
			case RAISER_ARMOR_TRIM_SMITHING_TEMPLATE:
			case RIB_ARMOR_TRIM_SMITHING_TEMPLATE:
			case SENTRY_ARMOR_TRIM_SMITHING_TEMPLATE:
			case SHAPER_ARMOR_TRIM_SMITHING_TEMPLATE:
			case SILENCE_ARMOR_TRIM_SMITHING_TEMPLATE:
			case SNOUT_ARMOR_TRIM_SMITHING_TEMPLATE:
			case SPIRE_ARMOR_TRIM_SMITHING_TEMPLATE:
			case TIDE_ARMOR_TRIM_SMITHING_TEMPLATE:
			case VEX_ARMOR_TRIM_SMITHING_TEMPLATE:
			case WARD_ARMOR_TRIM_SMITHING_TEMPLATE:
			case WAYFINDER_ARMOR_TRIM_SMITHING_TEMPLATE:
			case WILD_ARMOR_TRIM_SMITHING_TEMPLATE:
				return true;
			case CHORUS_PLANT: // Only unrenewable in item form
			case FARMLAND:
			case DIRT_PATH:
			case SPORE_BLOSSOM:
			case BUNDLE:
				return UNRENEWABLE_UNOBT;
			case COMMAND_BLOCK_MINECART:
				return UNRENEWABLE_UNOBT && !OBT_CMD_BLOCKS;
			case FIREWORK_STAR:
				return item.hasItemMeta() && ((FireworkEffectMeta)item.getItemMeta()).getEffect().hasTrail();
			case FIREWORK_ROCKET:
				return item.hasItemMeta() && ((FireworkMeta)item).getEffects().stream().anyMatch(e -> e.hasTrail());
			default:
				if(JunkUtils.isPotterySherd(item.getType())) return true;
				if(UNRENEWABLE_UNOBT && !OBT_INFESTED && TypeUtils.isInfested(item.getType())) return true; // Only unrenewable in item form
				if(UNRENEWABLE_UNOBT && !OBT_MOB_EGGS && EntityUtils.isSpawnEgg(item.getType())) return true;
				if(isUnrenewablyEnchanted(item)) return true;
				return isUnrenewableBlock(item.getType(), null);
		}	
	}

	// For irreversible processes: takes two unrenewable items as input
	boolean isIrreversibleProcess(ItemStack in, ItemStack out){
		if(!isUnrenewableItem(in)) return false;// If input is renewable, process is renewable
		return in.getType() != out.getType() && !reversible.sameSet(in.getType(), out.getType());
	}
	boolean isIrreversibleProcess(Material inMat, BlockData inData, Material outMat, BlockData outData){
		if(!isUnrenewableBlock(inMat, inData)) return false;// If input is renewable, process is renewable
		return !inMat.equals(outMat) && !reversible.sameSet(inMat, outMat);
	}
}