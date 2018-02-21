package Evil_Code_Renewable;

import java.util.Date;
import java.util.UUID;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_11_R1.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import net.minecraft.server.v1_11_R1.NBTTagCompound;

public class NBTFlagUtils{
	public static ItemStack setLastPlayerInContact(ItemStack item, UUID player){
		// ItemMeta meta = item.getItemMeta();
		// meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
		// meta.setDisplayName("Item");
		// item.setItemMeta(meta);

		net.minecraft.server.v1_11_R1.ItemStack nmsItem = CraftItemStack.asNMSCopy(item.clone());
		if(nmsItem.getTag() == null) nmsItem.setTag(new NBTTagCompound());
		nmsItem.getTag().setString("UUID", player.toString());

		return CraftItemStack.asCraftMirror(nmsItem);
	}

	public static ItemStack unflag(ItemStack item){
		net.minecraft.server.v1_11_R1.ItemStack nmsItem = CraftItemStack.asNMSCopy(item.clone());
		nmsItem.setTag(null);
		return CraftItemStack.asCraftMirror(nmsItem);
	}

	public static UUID getLastPlayerInContact(ItemStack item){
		if(item == null// || !item.hasItemMeta() ||
						// !item.getItemMeta().hasLore()
		) return null;

		net.minecraft.server.v1_11_R1.ItemStack nmsItem = CraftItemStack.asNMSCopy(item);

		return (nmsItem != null && nmsItem.hasTag() && nmsItem.getTag().hasKey("UUID")) ?
				UUID.fromString(nmsItem.getTag().getString("UUID")) : null;
	}

	public static void setLastPlayerInContact(Block block, UUID player){
		if(block == null || block.getType() == Material.AIR) return;
		block.setMetadata("UUID", new FixedMetadataValue(Renewable.getPlugin(), player.toString()));
		block.setMetadata("timestamp", new FixedMetadataValue(Renewable.getPlugin(), new Date().getTime()));
	}

	public static UUID getLastPlayerInContact(Block block){
		if(block == null || block.getType() == Material.AIR || !block.hasMetadata("UUID")) return null;

		return UUID.fromString(block.getMetadata("UUID").get(0).asString());
	}

	public static long getLastContactTimestamp(Block block){
		if(block == null || block.getType() == Material.AIR || !block.hasMetadata("timestamp")) return 0;

		return block.getMetadata("timestamp").get(0).asLong();
	}
}
