package net.evmodder.EvLib;

import java.util.HashMap;
import org.bukkit.inventory.ItemStack;
import net.evmodder.EvLib.ReflectionUtils;
import net.evmodder.EvLib.ReflectionUtils.RefClass;
import net.evmodder.EvLib.ReflectionUtils.RefConstructor;
import net.evmodder.EvLib.ReflectionUtils.RefMethod;

public class RefNBTTag{// version = X1.0
	static final RefClass cItemStack = ReflectionUtils.getRefClass("{nms}.ItemStack");
	static final RefClass cNBTTagCompound = ReflectionUtils.getRefClass("{nms}.NBTTagCompound");
	static final RefClass classCraftItemStack = ReflectionUtils.getRefClass("{cb}.inventory.CraftItemStack");
	static final RefMethod methodAsNMSCopy = classCraftItemStack.getMethod("asNMSCopy", ItemStack.class);
	static final RefMethod methodAsCraftMirror = classCraftItemStack.getMethod("asCraftMirror", cItemStack);
	static final RefMethod methodGetTag = cItemStack.getMethod("getTag");
	static final RefMethod methodSetTag = cItemStack.getMethod("setTag", cNBTTagCompound);
	static final RefMethod methodTagRemove = cNBTTagCompound.getMethod("remove", String.class);
	static final RefMethod methodTagIsEmpty = cNBTTagCompound.getMethod("isEmpty");
	static final HashMap<Class<?>, RefMethod> tagSetters = new HashMap<Class<?>, RefMethod>();
	static final HashMap<Class<?>, RefMethod> tagGetters = new HashMap<Class<?>, RefMethod>();
	static final Class<?> realNBTTagClass = cNBTTagCompound.getRealClass();
	static{
		tagSetters.put(realNBTTagClass,	cNBTTagCompound.getMethod("set",			String.class, Object.class));
		tagSetters.put(boolean.class,	cNBTTagCompound.getMethod("setBoolean",		String.class, boolean.class));
		tagSetters.put(byte.class,		cNBTTagCompound.getMethod("setByte",		String.class, byte.class));
		tagSetters.put(byte[].class,	cNBTTagCompound.getMethod("setByteArray",	String.class, byte[].class));
		tagSetters.put(double.class,	cNBTTagCompound.getMethod("setDouble",		String.class, double.class));
		tagSetters.put(float.class,		cNBTTagCompound.getMethod("setFloat",		String.class, float.class));
		tagSetters.put(int.class,		cNBTTagCompound.getMethod("setInt",			String.class, int.class));
		tagSetters.put(int[].class,		cNBTTagCompound.getMethod("setIntArray",	String.class, int[].class));
		tagSetters.put(long.class,		cNBTTagCompound.getMethod("setLong",		String.class, long.class));
		tagSetters.put(short.class,		cNBTTagCompound.getMethod("setShort",		String.class, short.class));
		tagSetters.put(String.class,	cNBTTagCompound.getMethod("setString",		String.class, String.class));
	}
	static{
		tagGetters.put(realNBTTagClass,	cNBTTagCompound.getMethod("get",			String.class));
		tagGetters.put(boolean.class,	cNBTTagCompound.getMethod("getBoolean",		String.class));
		tagGetters.put(byte.class,		cNBTTagCompound.getMethod("getByte",		String.class));
		tagGetters.put(byte[].class,	cNBTTagCompound.getMethod("getByteArray",	String.class));
		tagGetters.put(double.class,	cNBTTagCompound.getMethod("getDouble",		String.class));
		tagGetters.put(float.class,		cNBTTagCompound.getMethod("getFloat",		String.class));
		tagGetters.put(int.class,		cNBTTagCompound.getMethod("getInt",			String.class));
		tagGetters.put(int[].class,		cNBTTagCompound.getMethod("getIntArray",	String.class));
		tagGetters.put(long.class,		cNBTTagCompound.getMethod("getLong",		String.class));
		tagGetters.put(short.class,		cNBTTagCompound.getMethod("getShort",		String.class));
		tagGetters.put(String.class,	cNBTTagCompound.getMethod("getString",		String.class));
	}
	
	static final RefConstructor cnstrNBTTagCompound = cNBTTagCompound.findConstructor(0);
	public static Object newNBTTag(){return cnstrNBTTagCompound.create();}

	Object nmsTag;
	public RefNBTTag(){nmsTag = cnstrNBTTagCompound.create();}
	public RefNBTTag(RefNBTTag base){nmsTag = base;};
	private RefNBTTag(Object nmsTag){this.nmsTag = nmsTag;}
	private void addToTag(String key, Object value, Class<?> type) {tagSetters.get(type).of(nmsTag).call(key, value);}
	private Object getFromTag(String key, Class<?> type) {return tagSetters.get(type).of(nmsTag).call(key);}

	public void set(String key, RefNBTTag value){addToTag(key, value.nmsTag, realNBTTagClass);}
	public void setBoolean	(String key, boolean		value){addToTag(key, value, boolean.class);}
	public void setByte		(String key, byte			value){addToTag(key, value, byte.class);}
	public void setByteArray(String key, byte[]			value){addToTag(key, value, byte[].class);}
	public void setDouble	(String key, double			value){addToTag(key, value, double.class);}
	public void setFloat	(String key, float			value){addToTag(key, value, float.class);}
	public void setInt		(String key, int			value){addToTag(key, value, int.class);}
	public void setIntArray	(String key, int[]			value){addToTag(key, value, int[].class);}
	public void setLong		(String key, long			value){addToTag(key, value, long.class);}
	public void setShort	(String key, short			value){addToTag(key, value, short.class);}
	public void setString	(String key, String			value){addToTag(key, value, String.class);}
	//
	public RefNBTTag get(String key){return new RefNBTTag(getFromTag(key, realNBTTagClass));}
	public boolean getBoolean	(String key){return (boolean)	getFromTag(key, boolean.class);}
	public byte getByte			(String key){return (byte)		getFromTag(key, byte.class);}
	public byte[] getByteArray	(String key){return (byte[])	getFromTag(key, byte[].class);}
	public double getDouble		(String key){return (double)	getFromTag(key, double.class);}
	public float getFloat		(String key){return (float)		getFromTag(key, float.class);}
	public int getInt			(String key){return (int)		getFromTag(key, int.class);}
	public int[] getIntArray	(String key){return (int[])		getFromTag(key, int[].class);}
	public long getLong			(String key){return (long)		getFromTag(key, long.class);}
	public short getShort		(String key){return (short)		getFromTag(key, short.class);}
	public String getString		(String key){return (String)	getFromTag(key, String.class);}
	//
	public void remove(String key){methodTagRemove.of(nmsTag).call(key);}

	// For ItemStacks ----------------------------------------------------
	public static ItemStack setTag(ItemStack item, RefNBTTag tag){
		Object nmsTag = tag.nmsTag;
		if(methodTagIsEmpty.of(tag.nmsTag).call().equals(true)) nmsTag = null;
		Object nmsItem = methodAsNMSCopy.of(null).call(item);
		methodSetTag.of(nmsItem).call(nmsTag);
		item = (ItemStack) methodAsCraftMirror.of(null).call(nmsItem);
		return item;
	}
	public static RefNBTTag getTag(ItemStack item){
		Object nmsItem = methodAsNMSCopy.of(null).call(item);
		Object nmsTag = methodGetTag.of(nmsItem).call();
		return nmsTag == null ? new RefNBTTag() : new RefNBTTag(nmsTag);
	};
}