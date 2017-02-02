package me.codercloud.installer.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;

import org.json.simple.JSONValue;

public abstract class Data {
	
	public static Data readData(String s) {
		Object o = JSONValue.parse(s);
		if(o instanceof Map)
			return new DataMap((Map<?,?>) o);
		else if(o instanceof List) 
			return new DataList((List<?>) o);
		throw new IllegalArgumentException();
	}
	
	public boolean isList() {
		return this instanceof DataList;
	}
	
	public DataList asList() {
		return (DataList) this;
	}
	
	public boolean isMap() {
		return this instanceof DataMap;
	}
	
	public DataMap asMap() {
		return (DataMap) this;
	}
	
	public String toJsonString() {
		return JSONValue.toJSONString(this);
	}
	
	public static class DataList extends Data implements List<Object> {
		
		private List<Object> data;
		
		public DataList() {
			data=new ArrayList<Object>();
		}
		
		private DataList(List<?> data) {
			this();
			addAll(data);
		}
		
		public int size() {
			return data.size();
		}

		public boolean isEmpty() {
			return data.isEmpty();
		}

		public boolean contains(Object o) {
			return data.contains(o);
		}
		
		public Iterator<Object> iterator() {
			return data.iterator();
		}

		public Object[] toArray() {
			return data.toArray();
		}

		public <T> T[] toArray(T[] a) {
			return (T[]) data.toArray(a);
		}

		public boolean add(Object e) {
			return data.add(asAdd(e));
		}

		public boolean remove(Object o) {
			return data.remove(o);
		}

		public boolean containsAll(Collection<?> c) {
			return data.containsAll(c);
		}

		public boolean addAll(Collection<? extends Object> c) {
			for(Object o : c)
				add(o);
			return true;
		}

		public boolean addAll(int index, Collection<? extends Object> c) {
			for(Object o : c)
				add(index++, o);
			return true;
		}

		public boolean removeAll(Collection<?> c) {
			return data.removeAll(c);
		}

		public boolean retainAll(Collection<?> c) {
			return data.retainAll(c);
		}

		public void clear() {
			data.clear();
		}

		public Object get(int index) {
			return data.get(index);
		}

		public Object set(int index, Object element) {
			return data.set(index, asAdd(element));
		}

		public void add(int index, Object element) {
			data.add(index, asAdd(element));
		}

		public Object remove(int index) {
			return data.remove(index);
		}

		public int indexOf(Object o) {
			return data.indexOf(o);
		}

		public int lastIndexOf(Object o) {
			return data.lastIndexOf(o);
		}

		public ListIterator<Object> listIterator() {
			return data.listIterator();
		}

		public ListIterator<Object> listIterator(int index) {
			return data.listIterator();
		}

		public List<Object> subList(int fromIndex, int toIndex) {
			return data.subList(fromIndex, toIndex);
		}
		
		private Object asAdd(Object o) {
			if(o instanceof Map)
				return new DataMap((Map<?, ?>) o);
			if(o instanceof List)
				return new DataList((List<?>) o);
			return o;
		}
		
	}
	
	public static class DataMap extends Data implements Map<String, Object> {
		
		private Map<String, Object> data;
		
		public DataMap() {
			data = new HashMap<String, Object>();
		}
		
		@SuppressWarnings("unchecked")
		private DataMap(Map<?, ?> data) {
			this();
			putAll((Map<String, ?>)data);
		}
		
		public int size() {
			return 0;
		}

		public boolean isEmpty() {
			return data.isEmpty();
		}

		public boolean containsKey(Object key) {
			return data.containsKey(key);
		}

		public boolean containsValue(Object value) {
			return data.containsKey(value);
		}

		public Object get(Object key) {
			return data.get(key);
		}
		
		public String getAsString(Object key) {
			Object o = get(key);
			return o==null?null:String.valueOf(o);
		}

		public Object put(String key, Object value) {
			return data.put(key, asAdd(value));
		}

		public Object remove(Object key) {
			return data.remove(key);
		}

		public void putAll(Map<? extends String, ? extends Object> m) {
			for(Entry<?, ?> e : m.entrySet())
				data.put(e.getKey().toString(), e.getValue());
		}

		public void clear() {
			data.clear();
		}

		public Set<String> keySet() {
			return data.keySet();
		}

		public Collection<Object> values() {
			return data.values();
		}

		public Set<Entry<String, Object>> entrySet() {
			
			return data.entrySet();
		}
		
		private Object asAdd(Object o) {
			if(o instanceof Map)
				return new DataMap((Map<?, ?>) o);
			if(o instanceof List)
				return new DataList((List<?>) o);
			return o;
		}
		
	}
	
}
