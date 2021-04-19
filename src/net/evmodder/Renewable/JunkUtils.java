package net.evmodder.Renewable;

import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.Bell;
import org.bukkit.block.data.type.Lantern;
import net.evmodder.EvLib.extras.TypeUtils;

public class JunkUtils{
	public static BlockFace getFragileFace(BlockData data, BlockFace facing){
		final Material mat = data.getMaterial();
		switch(mat){
//			case WATER:
//			case STATIONARY_WATER:
//			case LAVA:
//			case STATIONARY_LAVA:
			case GRASS:
			case DEAD_BUSH:
			case DANDELION:
			case POPPY:
			case BROWN_MUSHROOM:
			case RED_MUSHROOM:
			case FIRE:
			case REDSTONE_WIRE:
			case WHEAT:
			case CARROTS:
			case POTATOES:
			case BEETROOTS:
			case MELON_STEM:
			case PUMPKIN_STEM:
			case REDSTONE_TORCH:
			case TORCH:
			case SNOW:
			case CACTUS:
			case SUGAR_CANE:
			case CAKE:
			case REPEATER:
			case COMPARATOR:
			case LILY_PAD:
			case NETHER_WART:
			case CARROT:
			case POTATO:
			case CHORUS_PLANT:
			case CHORUS_FLOWER:
			case BAMBOO:
			case SWEET_BERRY_BUSH:
			case SCAFFOLDING:
			case CORNFLOWER:
			case LILY_OF_THE_VALLEY:
			case WITHER_ROSE:
			case NETHER_SPROUTS:
			case CRIMSON_ROOTS:
			case WARPED_ROOTS:
			case CRIMSON_FUNGUS:
			case WARPED_FUNGUS:
			case TWISTING_VINES:
				return BlockFace.DOWN;
			//case VINE:
				//TODO: BlockFace.UP, but only if nothing behind this block! :o
			case WEEPING_VINES:
				return BlockFace.UP;
			case BELL:
				switch(((Bell)data).getAttachment()){
					case CEILING: return BlockFace.UP;
					case FLOOR: return BlockFace.DOWN;
					case SINGLE_WALL: return facing.getOppositeFace();
					case DOUBLE_WALL: default: return null;
					
				}
			case LANTERN:
			case SOUL_LANTERN:
				return ((Lantern)data).isHanging() ? BlockFace.UP : BlockFace.DOWN;
			case LADDER:
			case REDSTONE_WALL_TORCH:
			case WALL_TORCH:
			case LEVER:
			case PISTON_HEAD:
				return facing.getOppositeFace();
			default:
				if(TypeUtils.isCarpet(mat) || TypeUtils.isBanner(mat) || TypeUtils.isPressurePlate(mat) || TypeUtils.isDoor(mat)
				|| TypeUtils.isDoublePlant(mat) || TypeUtils.isSapling(mat) || TypeUtils.isFlowerPot(mat) || TypeUtils.isSign(mat)) return BlockFace.DOWN;
				if(TypeUtils.isButton(mat) || TypeUtils.isWallBanner(mat) || TypeUtils.isWallSign(mat)) return facing.getOppositeFace();
				return null;
		}
	}
}