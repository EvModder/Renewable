package net.evmodder.Renewable;

import java.util.HashSet;
import org.bukkit.Material;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.SculkShrieker;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.Repairable;
import net.evmodder.EvLib.extras.EntityUtils;
import net.evmodder.EvLib.extras.TypeUtils;
import net.evmodder.EvLib.util.UnionFind;

public class RenewableChecker{
	static final HashSet<Material> rescueList = new HashSet<Material>();
	static final HashSet<Material> artificiallyRenewable = new HashSet<Material>();
	static final UnionFind<Material> reversible = new UnionFind<Material>();
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

	final boolean UNRENEWABLE_LAVA, UNRENEWABLE_MOBS, UNRENEWABLE_GRAVITY, UNRENEWABLE_ENCHANTS;
	final boolean UNRENEWABLE_UNOBT, OBT_SPAWNERS, OBT_MOB_EGGS, OBT_INFESTED, OBT_CMD_BLOCKS,
					OBT_BEDROCK, OBT_END_PORTALS, OBT_BARRIERS, OBT_STRUCTURE_BLOCKS, OBT_LIGHT, OBT_PETRIFIED_SLABS, OBT_REINFORCED_DEEPSLATE, OBT_PLAYER_HEADS;
	RenewableChecker(Renewable pl){
		pl.getLogger().fine("These are unrenewable: ");
		pl.getLogger().fine("Lava: "+(UNRENEWABLE_LAVA = !pl.getConfig().getBoolean("renewable-lava", true)));
		pl.getLogger().fine("Certain mobs: "+(UNRENEWABLE_MOBS =  !pl.getConfig().getBoolean("renewable-mob-drops", false)));
		pl.getLogger().fine("Certain gravity blocks: "+(UNRENEWABLE_GRAVITY = !pl.getConfig().getBoolean("renewable-gravity-blocks", false)));
		pl.getLogger().fine("Items with natural treasure enchants: "+(UNRENEWABLE_ENCHANTS = !pl.getConfig().getBoolean("renewable-rc0", false)));
		//
		pl.getLogger().fine("Unobtainable items: "+(UNRENEWABLE_UNOBT = !pl.getConfig().getBoolean("ignore-unobtainable-items", false)));
		if(UNRENEWABLE_UNOBT){
			pl.getLogger().fine("Spawners: "+!(OBT_SPAWNERS = pl.getConfig().getBoolean("spawners-obtainable", false)));
			pl.getLogger().fine("SpawnEggs: "+!(OBT_MOB_EGGS = pl.getConfig().getBoolean("spawn-eggs-obtainable", false)));
			pl.getLogger().fine("Infested blocks(as item): "+!(OBT_INFESTED = pl.getConfig().getBoolean("infested-blocks-obtainable", false)));
			pl.getLogger().fine("Command blocks: "+!(OBT_CMD_BLOCKS = pl.getConfig().getBoolean("command-blocks-obtainable", false)));
			pl.getLogger().fine("Bedrock: "+!(OBT_BEDROCK = pl.getConfig().getBoolean("bedrock-obtainable", false)));
			pl.getLogger().fine("End portals (and frames): "+!(OBT_END_PORTALS = pl.getConfig().getBoolean("end-portals-obtainable", false)));
			pl.getLogger().fine("Barriers: "+!(OBT_BARRIERS = pl.getConfig().getBoolean("barriers-obtainable", false)));
			pl.getLogger().fine("Structure blocks: "+!(OBT_STRUCTURE_BLOCKS = pl.getConfig().getBoolean("structure-blocks-obtainable", false)));
			pl.getLogger().fine("Light blocks: "+!(OBT_LIGHT = pl.getConfig().getBoolean("light-blocks-obtainable", false)));
			pl.getLogger().fine("Petrified slabs: "+!(OBT_PETRIFIED_SLABS = pl.getConfig().getBoolean("petrified-slabs-obtainable", false)));
			pl.getLogger().fine("Reinforced Deepslate: "+!(OBT_REINFORCED_DEEPSLATE = pl.getConfig().getBoolean("reinforced-deepslate-obtainable", false)));
			pl.getLogger().fine("Player heads: "+!(OBT_PLAYER_HEADS = pl.getConfig().getBoolean("player-heads-obtainable", false)));
		}
		else OBT_SPAWNERS = OBT_MOB_EGGS = OBT_INFESTED = OBT_CMD_BLOCKS = OBT_BEDROCK
				= OBT_END_PORTALS = OBT_BARRIERS = OBT_STRUCTURE_BLOCKS = OBT_LIGHT = OBT_PETRIFIED_SLABS = OBT_REINFORCED_DEEPSLATE = OBT_PLAYER_HEADS = false;
		//
		for(String name : pl.getConfig().getStringList("rescued-renewables")){
			try{ rescueList.add(Material.valueOf(name.toUpperCase())); }
			catch(IllegalArgumentException ex){}
		}
		for(String name : pl.getConfig().getStringList("artificial-renewables")){
			try{ artificiallyRenewable.add(Material.valueOf(name.toUpperCase())); }
			catch(IllegalArgumentException ex){}
		}
	}

	boolean isUnrenewablyEnchanted(ItemStack item){
		// Item must have an RC greater or equal to this value otherwise it is unrenewable
		int minRenewableRC = 0;
//		int nonTreasureEnchants = 0;
		ItemMeta meta = item.getItemMeta();
		for(Enchantment ench : item.getEnchantments().keySet()){
			meta.removeEnchant(ench); // Otherwise the enchant will conflict with itself
			final int lvl = item.getEnchantmentLevel(ench);
			if(!ench.canEnchantItem(item) || meta.hasConflictingEnchant(ench) || lvl > ench.getMaxLevel()){
				// Has non-vanilla enchants!
				return UNRENEWABLE_UNOBT;
			}
			if(ench.isTreasure() ||
				(ench == Enchantment.THORNS && (!TypeUtils.isChestplate(item.getType()) || lvl > 2))
			) minRenewableRC = minRenewableRC*2+1;
//			else if(++nonTreasureEnchants > 5){
//				minRenewableRC = minRenewableRC*2+1;
//				nonTreasureEnchants = 1;
//			}
		}
		return ((Repairable)meta).getRepairCost() < minRenewableRC;
	}

	// Calls isUnrenewableBlock()
	boolean isUnrenewableItem(ItemStack item){
		if(artificiallyRenewable.contains(item.getType())) return false;
		//Note: (Somewhat) Sorted by ID, from least to greatest
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
			case MUSIC_DISC_5:
//			case NETHER_BRICK: // Renewable in 1.16+ (Bartering)
//			case QUARTZ: // Renewable in 1.16+ (Bartering)
			case IRON_HORSE_ARMOR:
			case GOLDEN_HORSE_ARMOR:
			case DIAMOND_HORSE_ARMOR:
			case ELYTRA:
			case ENCHANTED_GOLDEN_APPLE:
			case MOJANG_BANNER_PATTERN:
			case PIGLIN_BANNER_PATTERN:
			case TALL_GRASS: // These are only unrenewable in item form
			case LARGE_FERN:
				return true;
//			case TOTEM_OF_UNDYING: // Renewable in 1.14+ (Raids)
			case SHULKER_SHELL:
				return UNRENEWABLE_MOBS;// && !OBT_MOB_EGGS;
//			case NETHER_STAR: // Renewable in 1.16+ (Bartering)
//				return UNRENEWABLE_MOBS;
//			case FLINT: // Renewable in 1.16+ (Bartering)
//			case FLINT_AND_STEEL:
//				return UNRENEWABLE_GRAVITY;
			case LAVA_BUCKET:
				return UNRENEWABLE_LAVA;
			case CHORUS_PLANT: // Only unrenewable in item form
			case FARMLAND:
			case DIRT_PATH:
			case SCULK_SENSOR: // Not obtainable in survival yet
			case SPORE_BLOSSOM:
			case BUNDLE:
				return UNRENEWABLE_UNOBT;
			case COMMAND_BLOCK_MINECART:
				return UNRENEWABLE_UNOBT && !OBT_CMD_BLOCKS;
			default:
				if(UNRENEWABLE_UNOBT && !OBT_MOB_EGGS && EntityUtils.isSpawnEgg(item.getType())) return true;
				// These are only unrenewable in item form (infested blocks can be renewably created)
				if(UNRENEWABLE_UNOBT && !OBT_INFESTED && TypeUtils.isInfested(item.getType())) return true;
				if(UNRENEWABLE_ENCHANTS && isUnrenewablyEnchanted(item)) return true;
				return isUnrenewableBlock(item.getType(), null);
		}	
	}

	// Blocks can come as ItemStacks, but Items can't come as BlockStates
	// Thus, if our input is a Block, we know it can't be an item
	boolean isUnrenewableBlock(Material mat, BlockData data){
		if(artificiallyRenewable.contains(mat)) return false;
		//Custom list of (renewable) items to rescue (considered unrenewable)
		if(rescueList.contains(mat)) return true;

		//Note: (Somewhat) Sorted by ID, from least to greatest
		switch(mat){
			//case TERRACOTTA: //WTrader sells clay -> smelt into terracotta -> dye
//			case FLOWER_POT:
//			case GRANITE: // Renewable in 1.16+ (Bartering, Quartz)
//			case GLASS: // Renewable in 1.14+ (Villagers)
//			case BEE_HIVE: // Renewable in 1.15.2+
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
			case INFESTED_DEEPSLATE: // Note: not renewable even if infested blocks are
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
//			case COMPARATOR:
//			case OBSERVER:
//			case DAYLIGHT_DETECTOR:
			case COBWEB:
			case DEAD_BUSH:
			case NETHERRACK:
//			case SOUL_SAND:// Renewable in 1.16+ (Bartering)
//			case NETHER_BRICKS:// Renewable in 1.16+ (Bartering)
//			case ENDSTONE: // Renewable! :o (When dragon is respawned, endstone under platform)
//			case QUARTZ_BLOCK: // Renewable in 1.16+ (Bartering)
//			case QUARTZ_STAIRS:// slab, pillar, and chiseled are renewable from villagers
//			case SMOOTH_QUARTZ:
//			case SMOOTH_QUARTZ_SLAB:
//			case SMOOTH_QUARTZ_STAIRS:
//			case MAGMA_BLOCK: //Note: renewable! (4 magma cream)
				return true;
//			case SAND: // Renewable in 1.14+ (Wandering Trader)
//			case RED_SAND: // ^
//			case GRAVEL: // Renewable in 1.16+ (Bartering)
//			case FLETCHING_TABLE: // Renewable in 1.16+ (Bartering, Flint)
			case SPONGE:
			case WET_SPONGE:
				return UNRENEWABLE_MOBS; // Elder Guardian
			case DRAGON_EGG:
				return UNRENEWABLE_GRAVITY;
//			case LAVA: // Renewable in 1.17+ (Dripstone)
//				return UNRENEWABLE_LAVA && ((Levelled)data).getLevel() == 0/* Flowing lava is renewable*/;
//			case BEACON: // Renewable in 1.16+ (Bartering)
//				return UNRENEWABLE_MOBS;
			case SCULK_SHRIEKER:
				return data instanceof SculkShrieker && ((SculkShrieker)data).isCanSummon();
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

	//For irreversible processes: takes two unrenewable items as input
	boolean isIrreversibleProcess(ItemStack in, ItemStack out){
		if(!isUnrenewableItem(in)) return false;// If input is renewable, process is renewable
		return !in.getType().equals(out.getType()) && !reversible.sameSet(in.getType(), out.getType());
	}
	boolean isIrreversibleProcess(Material inMat, BlockData inData, Material outMat, BlockData outData){
		if(!isUnrenewableBlock(inMat, inData)) return false;// If input is renewable, process is renewable
		return !inMat.equals(outMat) && !reversible.sameSet(inMat, outMat);
	}
}