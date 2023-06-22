package net.evmodder.Renewable;

import java.util.Date;
import java.util.UUID;
import org.bukkit.block.BlockState;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.Metadatable;
import net.evmodder.EvLib.extras.NBTTagUtils;
import net.evmodder.EvLib.extras.NBTTagUtils.RefNBTTagCompound;

public class TaggingUtil{
	public static ItemStack setLastPlayerInContact(ItemStack item, UUID uuid){
		RefNBTTagCompound tag = NBTTagUtils.getTag(item);
		tag.setString("r_UUID", uuid.toString());
		//tag.setLong("r_ts", new Date().getTime());
		return NBTTagUtils.setTag(item, tag);
	}

	public static ItemStack unflag(ItemStack item){
		RefNBTTagCompound tag = NBTTagUtils.getTag(item);
		tag.remove("r_UUID");
		//tag.remove("r_ts");
		return NBTTagUtils.setTag(item, tag);
	}

	public static UUID getLastPlayerInContact(ItemStack item){
		String uuidStr = NBTTagUtils.getTag(item).getString("r_UUID");
		return uuidStr == null || uuidStr.isEmpty() ? null : UUID.fromString(uuidStr);
	}

	public static void setLastPlayerInContact(Metadatable meta, UUID uuid){
		if(meta == null) return;
		meta.setMetadata("r_UUID", new FixedMetadataValue(Renewable.getPlugin(), uuid.toString()));
		meta.setMetadata("r_ts", new FixedMetadataValue(Renewable.getPlugin(), new Date().getTime()));
		if(meta instanceof BlockState) ((BlockState)meta).update();
	}

	public static UUID getLastPlayerInContact(Metadatable meta){
		if(meta == null || !meta.hasMetadata("r_UUID")) return null;
		return UUID.fromString(meta.getMetadata("r_UUID").get(0).asString());
	}

//	public static long getLastContactTimestamp(BlockState block){
//		if(block == null || !block.hasMetadata("r_ts")) return 0;
//		return block.getMetadata("r_ts").get(0).asLong();
//	}
}