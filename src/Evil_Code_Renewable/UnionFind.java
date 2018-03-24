package Evil_Code_Renewable;

import java.util.HashMap;
//import java.util.HashSet;

public class UnionFind<T>{
//	HashMap<T, HashSet<T>> map;
	HashMap<T, T> parent;

	public UnionFind(){
//		map = new HashMap<T, HashSet<T>>();
		parent = new HashMap<T, T>();
	}

//	@SuppressWarnings("serial")
	public void add(final T t){
//		map.put(t, new HashSet<T>() {{ add(t); }});
		parent.put(t, t);
	}

	public T find(T t){
		T p = parent.get(t);
		if(!t.equals(p) && p != null && !p.equals(p=find(p))) parent.put(t, p);
		return p;
	}

	public void addToSet(T t, T p){
//		map.get(find(p)).add(t);
		parent.put(t, find(p));
	}

	public void mergeSets(T u, T v){
//		HashSet<T> uset = map.get(find(u)), vset = map.get(find(v));
//		if(uset.size() > vset.size()){
//			uset.addAll(vset);
//			parent.put(v, parent.get(u));
//		}
//		else{
//			vset.addAll(uset);
//			parent.put(u, parent.get(v));
//		}
		parent.put(u, parent.get(v));
	}

//	public HashSet<T> getSet(T t){
//		return map.get(find(t));
//	}

	public boolean sameSet(T u, T v){
		return u != null && v != null && find(u) != null && find(u).equals(find(v));
	}
}