package Evil_Code_Renewable;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockState;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;
//import org.bukkit.metadata.FixedMetadataValue;
//import net.minecraft.server.v1_11_R1.NBTTagCompound;
//import org.bukkit.craftbukkit.v1_11_R1.inventory.CraftItemStack;
import org.bukkit.entity.Player;

@SuppressWarnings("deprecation")
public class Utils {
	static boolean LAVA_UNRENEWABLE, DIA_ARMOR_UNRENEWABLE, MOB_UNRENEWABLE,
					GRAVITY_UNRENEWABLE, UNGET_UNRENEWABLE;
	Utils(Renewable pl){
		LAVA_UNRENEWABLE = !pl.getConfig().getBoolean("renewable-lava", true);
		DIA_ARMOR_UNRENEWABLE = !pl.getConfig().getBoolean("renewable-diamond-armor", false);
		MOB_UNRENEWABLE =  !pl.getConfig().getBoolean("renewable-mob-drops", false);
		GRAVITY_UNRENEWABLE = !pl.getConfig().getBoolean("renewable-gravity-blocks", false);
		UNGET_UNRENEWABLE = !pl.getConfig().getBoolean("renewable-unobtainable-items", false);
		pl.getLogger().info("Gravity Unrenewable: "+GRAVITY_UNRENEWABLE);
	}

	public static boolean isUnrenewable(ItemStack item){
		//Note: (Somewhat) Sorted by ID, from least to greatest
		byte dataValue = item.getData().getData();

		switch(item.getType()){
			case DIAMOND:
			case DIAMOND_SPADE:
			case DIAMOND_HOE:
			case BRICK:
			case CLAY_BALL:
//			case LAPIS_LAZULI://Note: renewable (villagers)
//			case GLASS_BOTTLE://Note: renewable (witches)
			case NETHER_BRICK_ITEM:
			case QUARTZ:
			case IRON_BARDING:
			case GOLD_BARDING:
			case DIAMOND_BARDING:
			case ELYTRA:
			case WRITTEN_BOOK://Note: Technically these are renewable
				return true;
			case TOTEM:
			case SHULKER_SHELL:
			case NETHER_STAR:
				return MOB_UNRENEWABLE;
			case DIAMOND_HELMET:
			case DIAMOND_CHESTPLATE:
			case DIAMOND_LEGGINGS:
			case DIAMOND_BOOTS:
				return DIA_ARMOR_UNRENEWABLE;
			case FLINT:
			case FLINT_AND_STEEL:
			case EXPLOSIVE_MINECART:
				return GRAVITY_UNRENEWABLE;
			case LAVA_BUCKET:
				return LAVA_UNRENEWABLE;
			case COMMAND_MINECART:
			case MONSTER_EGG:
			case MONSTER_EGGS:
				return UNGET_UNRENEWABLE;
			case GOLDEN_APPLE:
				return dataValue == 1;
			case SKULL:
				return dataValue == 5;
			default:
				return isUnrenewableBlock(item.getType(), dataValue);
		}	
	}

	public static boolean isUnrenewable(BlockState block){
		return isUnrenewableBlock(block.getType(), block.getRawData());
	}
	public static boolean isUnrenewableBlock(Material mat, byte dataValue){
		//Note: (Somewhat) Sorted by ID, from least to greatest

		switch(mat){
			case STONE:
				return dataValue != 0;
			case GRASS:
			case DIRT:
			case SPONGE:
//			case GLASS://Note: glass is renewable! (Villagers)
			case ENCHANTMENT_TABLE:
			case JUKEBOX:
			case FLOWER_POT:
			case FLOWER_POT_ITEM://TODO: Check if this is what I think (pot w/ item)
			case DIAMOND_BLOCK:
			case REDSTONE_COMPARATOR:
			case REDSTONE_COMPARATOR_ON:
			case REDSTONE_COMPARATOR_OFF:
			case OBSERVER:
			case DAYLIGHT_DETECTOR:
			case DAYLIGHT_DETECTOR_INVERTED:
			case WEB:
			case DEAD_BUSH:
//			case MOSSY_COBBLESTONE://Note: renewable! (Vines)
			case CLAY_BRICK:
			case BRICK_STAIRS://Note: Same (red) brick type as above, just as stairs
			case WATER_LILY://Note: renewable (fishing)//TODO: This is only set to unrenewable for Eventials
			case CLAY:
			case NETHERRACK:
			case SOUL_SAND:
			case MYCEL://Note: Since dirt is unrenewable, this is as well.
			case NETHER_BRICK:
			case NETHER_BRICK_STAIRS:
			case RED_NETHER_BRICK:
//			case ENDSTONE://Note: renewable! :o (When dragon is respawned, endstone under platform)
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
			case PACKED_ICE:
//			case SUNFLOWER://Note: renewable (Bonemeal on grass)
//			case LILAC:
//			case LARGE_FERN:
//			case ROSE_BUSH:
//			case PEONY:
//			case MAGMA_BLOCK://Note: renewable! (4 magma cream)
				return true;
			case SAND://Note: Sand and Red Sand are considered unrenewable.
			case GRAVEL:
			case DRAGON_EGG:
			case SANDSTONE:
			case RED_SANDSTONE:
			case SANDSTONE_STAIRS:
			case RED_SANDSTONE_STAIRS:
			case CONCRETE_POWDER:
			case CONCRETE:
			case TNT:
				return GRAVITY_UNRENEWABLE;
			case LAVA://Flowing lava is renewable
			case STATIONARY_LAVA:
				return LAVA_UNRENEWABLE && dataValue == 0;
			case STEP:
				return dataValue == 4 || dataValue == 6 || dataValue == 7
						|| (dataValue == 1 && GRAVITY_UNRENEWABLE);
			case STONE_SLAB2:
				return dataValue == 0 && GRAVITY_UNRENEWABLE;
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
			case BEACON:
				return MOB_UNRENEWABLE;
			case BEDROCK:
			case MOB_SPAWNER:
			case COMMAND:
			case COMMAND_CHAIN:
			case COMMAND_REPEATING:
			case ENDER_PORTAL:
			case ENDER_PORTAL_FRAME:
			case BARRIER:
			case STRUCTURE_BLOCK:
			case STRUCTURE_VOID:
				return UNGET_UNRENEWABLE;
			default:
				return isOre(mat);
		}
	}

	public static boolean isUnrenewableBlockThatDropsEquivalentType(Material mat){
		//Note: Sorted by ID, from least to greatest (somewhat)
		switch(mat){
			case GRASS:
			case MYCEL:
			case SOIL:
			case GRASS_PATH:
			case CLAY:
			case CLAY_BALL:
			case FLOWER_POT_ITEM://TODO: Test this theory
			case REDSTONE_COMPARATOR_ON:
			case REDSTONE_COMPARATOR_OFF://Test these as well
				return true;
			default:
				return false;
		}
	}

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

	public static ItemStack getUnewnewableItemForm(BlockState block){
		byte dataValue = block.getData().getData();
		switch(block.getType()){
			case LAVA:
			case STATIONARY_LAVA:
				return new ItemStack(Material.LAVA_BUCKET);
			case GLOWING_REDSTONE_ORE:
				return new ItemStack(Material.REDSTONE_ORE);
			case REDSTONE_COMPARATOR_ON:
			case REDSTONE_COMPARATOR_OFF:
				return new ItemStack(Material.REDSTONE_COMPARATOR);
			case DAYLIGHT_DETECTOR_INVERTED:
				return new ItemStack(Material.DAYLIGHT_DETECTOR);
			case MOB_SPAWNER:
				ItemStack item = new ItemStack(Material.MOB_SPAWNER);
				BlockStateMeta meta = (BlockStateMeta) item.getItemMeta();
				meta.setBlockState(block);
				String name = Evil_Code_DropHeads.Utils.getNormalizedName(
						((CreatureSpawner)block).getSpawnedType());
				meta.setDisplayName(ChatColor.WHITE+name+" Spawner");
				item.setItemMeta(meta);
				return item;
			default:
				return new ItemStack(block.getType(), 1, dataValue);
		}
	}

	public static ItemStack standardize(ItemStack item){
		if(item.hasItemMeta()) return item;
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
			case NETHER_BRICK:
			case NETHER_FENCE:
				return new ItemStack(Material.NETHERRACK, item.getAmount()*4);
			case NETHER_BRICK_ITEM:
				return new ItemStack(Material.NETHERRACK, item.getAmount());
			//TODO: Find a way to convert these (dia->dia_ore & andesite also)!!
			case NETHER_BRICK_STAIRS:
				if((item.getAmount()*3)%2 == 0) return new ItemStack(Material.NETHERRACK, item.getAmount()*3/2);
				else return item;
			case RED_NETHER_BRICK:
				if(item.getAmount() % 2 == 0) return new ItemStack(Material.NETHERRACK, item.getAmount()/2);
				else return item;
			case STEP:
				if(item.getData().getData() == 6)
					return new ItemStack(Material.QUARTZ, item.getAmount()*2);
//				if(item.getData().getData() == 7)
//					return new ItemStack(Material.NETHERRACK, item.getAmount()/2);
			case SPONGE:
			case CONCRETE:
			case CONCRETE_POWDER:
				return new ItemStack(item.getType(), item.getAmount(), (byte)0);
			case GRASS:
			case GRASS_PATH:
			case SOIL:
//				case DIRT://Dirt, Coarse-dirt, and Podzol are distinct and not interchangeable.
				//Note: Coarse Dirt (dirt:1) and Dirt are distinct (Coarse dirt is crafted with gravel)
				return new ItemStack(Material.DIRT, item.getAmount());
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
			case DIAMOND_BLOCK:
				return new ItemStack(Material.DIAMOND, item.getAmount()*9);
			case NETHER_STAR:
			case BEACON:
				return new ItemStack(Material.SOUL_SAND, item.getAmount()*4);
			case QUARTZ_BLOCK:
				return new ItemStack(Material.QUARTZ, item.getAmount()*4);
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
			case STONE:
				if(data == 1 || data == 2){
					return new ItemStack(Material.QUARTZ, item.getAmount()*2);
				}
				if(data == 3 || data == 4){
					return new ItemStack(Material.QUARTZ, item.getAmount());
				}
				if(/*data == 5 || */data == 6){
					if(item.getAmount() % 2 == 0) return new ItemStack(Material.QUARTZ, item.getAmount()/2);
					else return new ItemStack(item.getType(), item.getAmount(), (byte)5);
				}
			default:
				return item;
		}
	}

	/*public static ItemStack setLastPlayerInContact(ItemStack item, UUID player){
//		ItemMeta meta = item.getItemMeta();
//		meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
//		meta.setDisplayName("Item");
//		item.setItemMeta(meta);
		
		net.minecraft.server.v1_11_R1.ItemStack nmsItem = CraftItemStack.asNMSCopy(item.clone());
		if(nmsItem.getTag() == null) nmsItem.setTag(new NBTTagCompound());
		nmsItem.getTag().setString("UUID", player.toString());
		
		return CraftItemStack.asCraftMirror(nmsItem);
	}*/
	
	/*public static ItemStack unflag(ItemStack item){
		net.minecraft.server.v1_11_R1.ItemStack nmsItem = CraftItemStack.asNMSCopy(item.clone());
		nmsItem.setTag(null);
		return CraftItemStack.asCraftMirror(nmsItem);
	}*/
	
/*	public static UUID getLastPlayerInContact(ItemStack item){
		if(item == null// || !item.hasItemMeta() || !item.getItemMeta().hasLore()
			) return null;
		
		net.minecraft.server.v1_11_R1.ItemStack nmsItem = CraftItemStack.asNMSCopy(item);
		
		return (nmsItem != null && nmsItem.hasTag() && nmsItem.getTag().hasKey("UUID"))
				? UUID.fromString(nmsItem.getTag().getString("UUID")) : null;
	}*/
	
/*	public static void setLastPlayerInContact(Block block, UUID player){
		if(block == null || block.getType() == Material.AIR) return;
		block.setMetadata("UUID", new FixedMetadataValue(Renewable.getPlugin(), player.toString()));
		block.setMetadata("timestamp", new FixedMetadataValue(Renewable.getPlugin(), new Date().getTime()));
	}*/
	
/*	public static UUID getLastPlayerInContact(Block block){
		if(block == null || block.getType() == Material.AIR || !block.hasMetadata("UUID")) return null;
		
		return UUID.fromString(block.getMetadata("UUID").get(0).asString());
	}*/
	
/*	public static long getLastContactTimestamp(Block block){
		if(block == null || block.getType() == Material.AIR || !block.hasMetadata("timestamp")) return 0;
		
		return block.getMetadata("timestamp").get(0).asLong();
	}*/

	public static Player getNearbyPlayer(Location loc, int range){
		range = range*range;
		for(Player p : Bukkit.getServer().getOnlinePlayers()){
			if(p.getWorld().getName().equals(loc.getWorld().getName()) && p.getLocation().distanceSquared(loc) > range)
				return p;
		}
		return null;
	}
}