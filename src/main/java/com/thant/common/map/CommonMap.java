package com.thant.common.map;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class CommonMap implements Map<String, Object> {
	private String spliter = ".";
	private String spliter_reg = "\\.";
	
	Map<String, Object> _map;

	@Override
	public int size() {
		return _map.size();
	}

	@Override
	public boolean isEmpty() {
		return _map.isEmpty();
	}

	@Override
	public boolean containsKey(Object key) {
		return _map.containsKey(key);
	}

	@Override
	public boolean containsValue(Object value) {
		return _map.containsValue(value);
	}

	@Override
	public Object get(Object key) {
		return _map.get(key);
	}

	public Object get(Object key, Object defaultval) {
		Object val = _map.get(key);
		return null == val ? defaultval : val;
	}
	
	@Override
	public Object put(String key, Object value) {
		return _map.put(key, value);
	}

	@Override
	public Object remove(Object key) {
		return _map.remove(key);
	}

	@Override
	public void putAll(Map<? extends String, ? extends Object> m) {
		_map.putAll(m);
	}

	@Override
	public void clear() {
		_map.clear();
	}

	@Override
	public Set<String> keySet() {
		return _map.keySet();
	}

	@Override
	public Collection<Object> values() {
		return _map.values();
	}

	@Override
	public Set<java.util.Map.Entry<String, Object>> entrySet() {
		return _map.entrySet();
	}

	public CommonMap() {
		_map = new LinkedHashMap<String, Object>();
	}

	/**
	 * 注意是引用源map，类似于attach
	 * @param othermap
	 */
	public CommonMap(Map<String, Object> othermap) {
		if (othermap instanceof CommonMap) {
			_map = ((CommonMap)othermap).getMap();
		} else {
			_map = othermap;
		}
	}
	
	public static CommonMap copy(Map<String, Object> othermap) {
		CommonMap map = new CommonMap();
		map.putAll(othermap);
		return map;
	}

	public CommonMap(boolean direct, Object... initA) {
		_map = new LinkedHashMap<String, Object>(initA.length/2);
		if (direct) {
			CommonMap wrap = new CommonMap(_map);
			for (int i=0; i<initA.length-1; i+=2) {
				String key = (String)initA[i];
				Object val = initA[i+1];
				wrap.directPut(key, val);
			}
		} else {
			for (int i=0; i<initA.length-1; i+=2) {
				String key = (String)initA[i];
				Object val = initA[i+1];
				_map.put(key, val);
			}
		}
	}
	
	public Object directGet(String keys) {
		return directGet(keys, null);
	}
	
	@SuppressWarnings("unchecked")
	public Object directGet(String keys, Object defaultVal) {
		String[] pathA = keys.split(spliter_reg);
		
		Object obj = null;
		Map<String, Object> m = _map;
		for (int i=0; i<pathA.length; ++i) {
			obj = m.get(pathA[i]);
			if (null == obj) break;

			if (i < pathA.length-1) {
				if (obj instanceof Map) {
					m = (Map<String, Object>)obj;
				} else {
					obj = null;
					break;
				}
			}
		}
		return null == obj ? defaultVal : obj;
	}

	@SuppressWarnings("unchecked")
	public Object directPut(String keys, Object val) {
		String[] pathA = keys.split(spliter_reg);
		
		Object obj = null;
		Map<String, Object> m = _map;
		for (int i=0; i<pathA.length-1; ++i) {
			obj = m.get(pathA[i]);
			if (null == obj) {
				Map<String, Object> newmap = new LinkedHashMap<String, Object>();
				m.put(pathA[i], newmap);
				m = newmap;
				continue;
			} else {
				m = (Map<String, Object>)obj;
			}
		}
		return m.put(pathA[pathA.length-1], val);
	}
	
	public Map<String, Object> getMap() {
		return _map;
	}
	
	public String getSpliter() {
		return spliter;
	}
	
	public void putSpliter(String s) {
		spliter = s;
		spliter_reg = s.replace(".", "\\.");
	}

	public static Map<String, Object> getOne(List<Map<String, Object>> rows) {
		if (rows != null && rows.size()>0) {
			return rows.get(0);
		}
		return null;
	}
	
	public static Object getValue(List<Map<String, Object>> rows) {
		if (rows != null && rows.size()>0 && rows.get(0).size()>0) {
			return rows.get(0).values().iterator().next();
		}
		return null;
	}
}
