package net.evmodder.EvLib;

import org.bukkit.Material;

public class ItemDescDeprecated{
	Material mat;
	byte data;
	public ItemDescDeprecated(Material m, byte b){mat = m; data = b;}
	public ItemDescDeprecated(Material m){mat = m; data = 0;}
	@Override public boolean equals(Object o){
		return o instanceof ItemDescDeprecated &&
				((ItemDescDeprecated)o).mat == mat && ((ItemDescDeprecated)o).data == data;
	}
	@Override public int hashCode(){
		return mat.ordinal();
	}
}