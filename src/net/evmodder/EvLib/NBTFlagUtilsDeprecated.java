package net.evmodder.EvLib;

import java.util.Date;
import java.util.UUID;
import org.bukkit.block.BlockState;
import org.bukkit.metadata.FixedMetadataValue;
import net.evmodder.Renewable.Renewable;
import org.bukkit.craftbukkit.v1_13_R2.inventory.CraftItemStack;
import net.minecraft.server.v1_13_R2.ItemStack;
import net.minecraft.server.v1_13_R2.NBTTagCompound;

public class NBTFlagUtilsDeprecated{
	public static org.bukkit.inventory.ItemStack setLastPlayerInContact(
			org.bukkit.inventory.ItemStack item, UUID player){
		ItemStack nmsItem = CraftItemStack.asNMSCopy(item.clone());
		if(nmsItem.getTag() == null) nmsItem.setTag(new NBTTagCompound());
		nmsItem.getTag().setString("UUID", player.toString());
		nmsItem.getTag().setString("timestamp", ""+new Date().getTime());

		return CraftItemStack.asCraftMirror(nmsItem);
	}

	public static org.bukkit.inventory.ItemStack unflag(org.bukkit.inventory.ItemStack item){
		ItemStack nmsItem = CraftItemStack.asNMSCopy(item.clone());
		nmsItem.getTag().remove("UUID");
		nmsItem.getTag().remove("timestamp");
		if(nmsItem.getTag().isEmpty()) nmsItem.setTag(null);
		return CraftItemStack.asCraftMirror(nmsItem);
	}

	public static UUID getLastPlayerInContact(org.bukkit.inventory.ItemStack item){
		if(item == null// || !item.hasItemMeta() ||
						// !item.getItemMeta().hasLore()
		) return null;

		ItemStack nmsItem = CraftItemStack.asNMSCopy(item);

		return (nmsItem != null && nmsItem.hasTag() && nmsItem.getTag().hasKey("UUID")) ?
				UUID.fromString(nmsItem.getTag().getString("UUID")) : null;
	}

	public static void setLastPlayerInContact(BlockState block, UUID player){
		if(block == null) return;
		block.setMetadata("UUID", new FixedMetadataValue(Renewable.getPlugin(), player.toString()));
		block.setMetadata("timestamp", new FixedMetadataValue(Renewable.getPlugin(), new Date().getTime()));
		block.update();
	}

	public static UUID getLastPlayerInContact(BlockState block){
		if(block == null || !block.hasMetadata("UUID")) return null;
		return UUID.fromString(block.getMetadata("UUID").get(0).asString());
	}

	public static long getLastContactTimestamp(BlockState block){
		if(block == null || !block.hasMetadata("timestamp")) return 0;
		return block.getMetadata("timestamp").get(0).asLong();
	}
}