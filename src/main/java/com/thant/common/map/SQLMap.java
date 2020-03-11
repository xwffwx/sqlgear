package com.thant.common.map;

import java.sql.Blob;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.thant.sqlgear.SQLRunner;

/**
 * @author thant
 * select()
 * from()
 * where()
 * order()
 * 
 * update()
 * values()
 * where()
 */
public class SQLMap extends CommonMap {
	public static final String objectskey = "__objects__";
	public static final String fieldskey = "__fields__";
	public static final String whereskey = "__wheres__";
	public static final String valueskey = "__values__";
	public static final String orderbykey = "__orderby__";
	public static final String pagebeginkey = "__beginrow__";
	public static final String pageendkey = "__endrow__";
	
	public static final String joinwithorkey = "__joinwithor__";
	public static final String insertwithlistkey = "__insertwithlist__";
	public static final String updatetypekey = "__updatetype__";

	public SQLMap() {
	}
	
	public SQLMap(Map<String, Object> params) {
		super(params);
	}

	/*
	public SQLMap(String type, Object... initA) {
		_map = new LinkedHashMap<String, Object>(initA.length/2);
		SQLMap wrap = new SQLMap(_map);
		if (valueskey.equals(type)) {
			for (int i=0; i<initA.length-1; i+=2) {
				String key = (String)initA[i];
				Object val = initA[i+1];
				wrap.valuesPut(key, val);
			}
		} else if (whereskey.equals(type)) {
			for (int i=0; i<initA.length-1; i+=3) {
				String key = (String)initA[i];
				String op = (String)initA[i+1];
				Object val = initA[i+2];
				wrap.wheresPut(key, op, val);
			}
		}
	}*/
	
	public boolean isJoinWithOR() {
		return (true == (Boolean)get(joinwithorkey));
	}

	private void setJoinWithOR(boolean joinWithOR) {
		put(joinwithorkey, joinWithOR);
	}

	/**
	 * 
	 * 功能描述:值比较 例如 id=1
	 * <pre></pre> 
	 * @param field
	 * @param op
	 * @param val
	 * @return
	 */
	public Object wheresPut(String field, String op, Object val) {
		StringBuilder sb = new StringBuilder(whereskey).append(getSpliter()).append(field);
		if (null == val) {
			op = op.toLowerCase();
			op = ("=".equals(op) || "is".equals(op)) ? "is" : "is not";
		}
		super.directPut(sb.toString()+getSpliter()+"op", op);
		return super.directPut(sb.toString()+getSpliter()+"val", val);
	}
	
	private boolean isEmpty(String s) {
		return null==s || "".equals(s);
	}

	/**
	 * 
	 * 功能描述: 原生where条件，用于字段之间的比较 例如 a.id = b.id
	 * <pre></pre> 
	 * @param field
	 * @param rawval
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public Object wheresPut(String rawstr) {
		if (isEmpty(rawstr)) return null;
		
		Map<String, Object> wheres = (Map<String, Object>)get(whereskey);
		int id = (null == wheres) ? 1 : wheres.size()+1;
		String field = String.valueOf(id);
		StringBuilder sb = new StringBuilder(whereskey).append(getSpliter()).append(field);
		super.directPut(sb.toString()+getSpliter()+"op", "raw");
		return super.directPut(sb.toString()+getSpliter()+"val", rawstr);
	}

	public Object valuesPut(String field, Object val) {
		StringBuilder sb = new StringBuilder(valueskey).append(getSpliter()).append(field);
		if (val instanceof String) {
			super.directPut(sb.toString()+getSpliter()+"type", "VARCHAR");
		} else if (val instanceof Integer) {
			super.directPut(sb.toString()+getSpliter()+"type", "INTEGER");
		} else if (val instanceof java.util.Date) {
			super.directPut(sb.toString()+getSpliter()+"type", "TIMESTAMP");
		} else if (val instanceof Blob) {
			super.directPut(sb.toString()+getSpliter()+"type", "BLOB");
		} else if (val instanceof Timestamp) {
			super.directPut(sb.toString()+getSpliter()+"type", "TIMESTAMP");
		} else if (val instanceof Float) {
			super.directPut(sb.toString()+getSpliter()+"type", "REAL");
		} else if (val instanceof Double) {
			super.directPut(sb.toString()+getSpliter()+"type", "DOUBLE");
		} else if (val instanceof Long) {
			super.directPut(sb.toString()+getSpliter()+"type", "BIGINT");
		} else if (val instanceof java.sql.Date) {
			super.directPut(sb.toString()+getSpliter()+"type", "DATE");
		} else if (val instanceof Time) {
			super.directPut(sb.toString()+getSpliter()+"type", "TIME");
		} else if (val instanceof Clob) {
			super.directPut(sb.toString()+getSpliter()+"type", "CLOB");
		} else if (val instanceof byte[]) {
			super.directPut(sb.toString()+getSpliter()+"type", "BINARY");
		}
		return super.directPut(sb.toString()+getSpliter()+"val", val);
	}
	
	@SuppressWarnings("unchecked")
	public Map<String, Object> getObjValues() {
		Map<String, Object> model = new HashMap<String, Object>(); 
		
		Map<String, Object> ws = (Map<String, Object>)_map.get(whereskey);
		if (ws != null) {
			for (Entry<String, Object> entry : ws.entrySet()) {
				model.put(entry.getKey(),
					((Map<String, Object>)entry.getValue()).get("val"));
			}
		}

		Map<String, Object> vs = (Map<String, Object>)_map.get(valueskey);
		if (vs != null) {
			for (Entry<String, Object> entry : vs.entrySet()) {
				model.put(entry.getKey(),
					((Map<String, Object>)entry.getValue()).get("val"));
			}
		}

		return model;
	}
	
	public static boolean isSysKey(String key) {
		return key.startsWith("__") && key.endsWith("__"); 
	}
	
	public SQLMap SELECT(String objects) {
		put(fieldskey, objects);
		return this;
	}

	public SQLMap FROM(String objects) {
		put(objectskey, objects);
		return this;
	}

	public SQLMap WHERE_OR(Object... args) {
		setJoinWithOR(true);
		return _WHERE(args);
	}

	public SQLMap WHERE(Object... args) {
		setJoinWithOR(false);
		return _WHERE(args);
	}
	
	private SQLMap _WHERE(Object... args) {
		for (int i=0; i<args.length; i+=3) {
			String key = (String)args[i];
			String op = (String)args[i+1];
			Object val = args[i+2];
			wheresPut(key, op, val);
		}
		return this;
	}

	public SQLMap ORDERBY(String orderby) {
		put(orderbykey, orderby);
		return this;
	}
	
	@SuppressWarnings("unchecked")
	public boolean containsWhere(String field) {
		Map<String, Object> map = (Map<String, Object>)get(whereskey);
		if (map != null && map.containsKey(field)) {
			return true;
		}
		return false;
	}
	
	public String getWhereValue(String field) {
		StringBuilder sb = new StringBuilder(whereskey)
			.append(getSpliter()).append(field).append("val");
		return (String)directGet(sb.toString());
	}
	
	public String getWhereOP(String field) {
		StringBuilder sb = new StringBuilder(whereskey)
			.append(getSpliter()).append(field).append("op");
		return (String)directGet(sb.toString());
	}
	
	public SQLMap UPDATE(String table) {
		put(updatetypekey, "UPDATE");
		put(objectskey, table);
		return this;
	}

	public SQLMap INSERT(String table) {
		put(updatetypekey, "INSERT");
		put(objectskey, table);
		return this;
	}
	
	public SQLMap REPLACE(String table) {
		put(updatetypekey, "REPLACE");
		put(objectskey, table);
		return this;
	}

	private void clearValues() {
		@SuppressWarnings("unchecked")
		Map<String, Object> map = (Map<String, Object>)get(valueskey);
		if (map != null) map.clear();
	}
	
	public SQLMap VALUES(Object... args) {
		clearValues();
		put(insertwithlistkey, false);
		for (int i=0; i<args.length; i+=2) {
			String key = (String)args[i];
			Object val = args[i+1];
			valuesPut(key, val);
		}
		return this;
	}

	public SQLMap VALUESLIST(Object... args) {
		clearValues();
		put(insertwithlistkey, true);
		for (int i=0; i<args.length; ++i) {
			Object val = args[i];
			valuesPut(insertwithlistkey+i, val);
		}
		return this;
	}

	public SQLMap VALUESMAP(Map<String, Object> map) {
		clearValues();
		put(insertwithlistkey, false);
		for (Entry<String, Object> item : map.entrySet()) {
			valuesPut(item.getKey(), item.getValue());
		}
		return this;
	}
	
	public List<Map<String, Object>> query(SQLRunner runner) {
		return query(runner, null);
	}
	
	public List<Map<String, Object>> query(SQLRunner runner, Connection cont) {
		List<Map<String, Object>> ret = runner.query(cont, this);
		clear();
		return ret;
	}
	
	public Map<String, Object> queryOne(SQLRunner runner) {
		return queryOne(runner, null);
	}
	
	public Map<String, Object> queryOne(SQLRunner runner, Connection cont) {
		Map<String, Object> ret = runner.queryOne(cont, this);
		clear();
		return ret;
	}
	
	public Object queryValue(SQLRunner runner) {
		return queryValue(runner, null);
	}
	
	public Object queryValue(SQLRunner runner, Connection cont) {
		Object ret = runner.queryValue(cont, this);
		clear();
		return ret;
	}

	public int update(SQLRunner runner, Connection cont) {
		int ret = runner.update(cont, this);
		clear();
		return ret;
	}
}
