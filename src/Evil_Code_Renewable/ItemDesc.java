package Evil_Code_Renewable;

import org.bukkit.Material;

public class ItemDesc{
	Material mat;
	byte data;
	public ItemDesc(Material m, byte b){mat = m; data = b;}
	public ItemDesc(Material m){mat = m; data = 0;}
}