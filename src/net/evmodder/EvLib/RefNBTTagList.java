package net.evmodder.EvLib;

import net.evmodder.EvLib.ReflectionUtils.RefClass;
import net.evmodder.EvLib.ReflectionUtils.RefConstructor;
import net.evmodder.EvLib.ReflectionUtils.RefMethod;

public class RefNBTTagList implements RefNBTBase{
	static final RefClass classNBTBase = ReflectionUtils.getRefClass("{nms}.NBTBase"); 
	static final RefClass classNBTTagList = ReflectionUtils.getRefClass("{nms}.NBTTagList");
	static final RefConstructor cnstrNBTTagList = classNBTTagList.findConstructor(0);
	static final Class<?> realNBTBaseClass = classNBTBase.getRealClass();
	static final Class<?> realNBTTagListClass = classNBTTagList.getRealClass();
	static final RefMethod methodAdd = classNBTTagList.getMethod("add", realNBTBaseClass);
	static final RefMethod methodGet = classNBTTagList.getMethod("get", int.class);

	Object nmsTagList;
	public RefNBTTagList(){nmsTagList = cnstrNBTTagList.create();}
	public RefNBTTagList(RefNBTTagList base){nmsTagList = base;};
	RefNBTTagList(Object nmsTagList){this.nmsTagList = nmsTagList;}

	public void add(RefNBTTag tag){methodAdd.of(nmsTagList).call(tag.nmsTag);}
	public void add(RefNBTTagList tagList){methodAdd.of(nmsTagList).call(tagList.nmsTagList);}
	public RefNBTBase get(int i){
		Object value = methodGet.of(nmsTagList).call(i);
		if(value == null) return null;
		return value.getClass().equals(realNBTTagListClass) ? new RefNBTTagList(value) : new RefNBTTag(value);
	}
}