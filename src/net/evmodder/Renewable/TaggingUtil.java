package net.evmodder.Renewable;

import java.util.Date;
import java.util.UUID;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Item;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.Metadatable;
import net.evmodder.EvLib.bukkit.NBTTagUtils;

public class TaggingUtil{
	public static Metadatable setLastPlayerInContact(Metadatable meta, UUID uuid){
		if(meta == null) return meta;
//		if(meta instanceof Item){ // This tag is already added automatically
//			final RefNBTTagCompound tag = NBTTagUtils.getTag((Item)meta);
//			tag.setIntArray("Thrower", new int[]{
//					(int)(uuid.getMostSignificantBits()>>32), (int)uuid.getMostSignificantBits(),
//					(int)(uuid.getLeastSignificantBits()>>32), (int)uuid.getLeastSignificantBits()
//			});
//			meta = NBTTagUtils.setTag((Item)meta, tag);
//		}
		meta.setMetadata("r_UUID", new FixedMetadataValue(Renewable.getPlugin(), uuid.toString()));
		meta.setMetadata("r_ts", new FixedMetadataValue(Renewable.getPlugin(), new Date().getTime()));
		if(meta instanceof BlockState) ((BlockState)meta).update();
		return meta;
	}

	public static UUID getLastPlayerInContact(Metadatable meta){
		if(meta == null) return null;
		if(meta instanceof Item){
			final int[] is = NBTTagUtils.getTag((Item)meta).getIntArray("Thrower");
			if(is != null && is.length != 0){
				final long moreSig = (long)is[0] << 32 | is[1] & 0XFFFFFFFFL;
				final long lessSig = (long)is[2] << 32 | is[3] & 0XFFFFFFFFL;
				return new UUID(moreSig, lessSig);
			}
			else Renewable.getPlugin().getLogger().info("unable to find Thrower:[] for item: "+((Item)meta).getItemStack().getType());
		}
		if(meta == null || !meta.hasMetadata("r_UUID")) return null;
		return UUID.fromString(meta.getMetadata("r_UUID").get(0).asString());
	}

//	public static long getLastContactTimestamp(BlockState block){
//		if(block == null || !block.hasMetadata("r_ts")) return 0;
//		return block.getMetadata("r_ts").get(0).asLong();
//	}
}