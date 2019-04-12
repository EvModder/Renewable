package Evil_Code_Renewable;

import java.util.Date;
import java.util.UUID;
import org.bukkit.block.BlockState;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import EvLib.NBTTagUtils;

public class ItemTrackingUtils{
	public static ItemStack setLastPlayerInContact(ItemStack item, UUID uuid){
		item = NBTTagUtils.addNBTTag(item, "r_UUID", uuid.toString());
		item = NBTTagUtils.addNBTTag(item, "r_ts", new Date().getTime());
		return item;
	}

	public static ItemStack unflag(ItemStack item){
		item = NBTTagUtils.removeNBTTag(item, "r_UUID");
		item = NBTTagUtils.removeNBTTag(item, "r_ts");
		return item;
	}

	public static UUID getLastPlayerInContact(ItemStack item){
		String uuidStr = NBTTagUtils.getTagString(item, "r_UUID");
		return uuidStr == null ? null : UUID.fromString(uuidStr);
	}

	public static void setLastPlayerInContact(BlockState block, UUID player){
		if(block == null) return;
		block.setMetadata("r_UUID", new FixedMetadataValue(Renewable.getPlugin(), player.toString()));
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