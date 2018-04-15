package Evil_Code_Renewable;

import org.bukkit.Material;

public class ItemDesc{
	Material mat;
	byte data;
	public ItemDesc(Material m, byte b){mat = m; data = b;}
	public ItemDesc(Material m){mat = m; data = 0;}
	@Override public boolean equals(Object o){
		return o instanceof ItemDesc && ((ItemDesc)o).mat == mat && ((ItemDesc)o).data == data;
	}
	@Override public int hashCode(){
		return mat.ordinal();
	}
}