package net.evmodder.Renewable;

import java.util.Date;
import java.util.UUID;
import org.bukkit.block.BlockState;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import net.evmodder.EvLib.extras.NBTTagUtils;
import net.evmodder.EvLib.extras.NBTTagUtils.RefNBTTag;

public class ItemTaggingUtil{
	public static ItemStack setLastPlayerInContact(ItemStack item, UUID uuid){
		RefNBTTag tag = NBTTagUtils.getTag(item);
		tag.setString("r_UUID", uuid.toString());
		tag.setLong("r_ts", new Date().getTime());
		return NBTTagUtils.setTag(item, tag);
	}

	public static ItemStack unflag(ItemStack item){
		RefNBTTag tag = NBTTagUtils.getTag(item);
		tag.remove("r_UUID");
		tag.remove("r_ts");
		return NBTTagUtils.setTag(item, tag);
	}

	public static UUID getLastPlayerInContact(ItemStack item){
		String uuidStr = NBTTagUtils.getTag(item).getString("r_UUID");
		return uuidStr == null || uuidStr.isEmpty() ? null : UUID.fromString(uuidStr);
	}

	public static void setLastPlayerInContact(BlockState block, UUID uuid){
		if(block == null) return;
		block.setMetadata("r_UUID", new FixedMetadataValue(Renewable.getPlugin(), uuid.toString()));
		block.setMetadata("r_ts", new FixedMetadataValue(Renewable.getPlugin(), new Date().getTime()));
		block.update();
	}

	public static UUID getLastPlayerInContact(BlockState block){
		if(block == null || !block.hasMetadata("r_UUID")) return null;
		return UUID.fromString(block.getMetadata("r_UUID").get(0).asString());
	}

	public static long getLastContactTimestamp(BlockState block){
		if(block == null || !block.hasMetadata("r_ts")) return 0;
		return block.getMetadata("r_ts").get(0).asLong();
	}
}