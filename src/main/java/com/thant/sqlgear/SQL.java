package com.thant.sqlgear;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.thant.common.map.SQLMap;

/**
 * @ClassName: SQL
 * @Description: 
 * @author: 肖文峰
 * @date: 2019年5月10日 上午11:31:05
 */
public class SQL {
	private static final List<Object> _emptylst = new ArrayList<Object>();
	private List<Object> _sqlsegs = new ArrayList<Object>();

	private String sql = null;
	private Object[] args = null;

	/**
	 * @Title: SQL
	 * @Description: 构造函数
	 * @param: 
	 * @throws
	 */
	public SQL() {
	}
	
	/**
	 * @Title: SQL
	 * @Description: 
	 * @param: 
	 */
	public SQL(Object... segs) {
		append(segs);
	}
	
	/**
	 * @Title: append
	 * @Description: TODO
	 * @param: @param segs
	 * @param: @return
	 * @return: SQL
	 * @throws
	 */
	public SQL append(Object... segs) {
		_sqlsegs.addAll(Arrays.asList(segs));
		sql = null;
		args = null;
		return this;
	}
	
	public SQL clear() {
		_sqlsegs.clear();
		sql = null;
		args = null;
		return this;
	}
	
	public Object[] getArgs() {
		if (null == sql) {
			buildSql();
		}
		return args;
	}

	public String getSql() {
		if (null == sql) {
			buildSql();
		}
		return sql;
	}

	private static boolean isEmpty(String s) {
		return null==s || "".equals(s);
	}
	
	private static int countChar(String s, char ch) {
		int num = 0;
		int pos = -1;
		while ((pos = s.indexOf(ch, pos+1))>=0) {
			num++;
		}
		return num;
	}
	
	private void buildSql() {
		List<Object> argsLst = new ArrayList<Object>(); 
		Object[] retA = buildSql(argsLst, 0, _sqlsegs.toArray());

		sql = (String)retA[1];
		args = argsLst.toArray();

		if ((int)retA[0] != 0) {
			throw new RuntimeException("缺少"+(int)retA[0]+"个参数");
		}
	}
	
	@SuppressWarnings("unchecked")
	private Object[] buildSql(List<Object>args, int num, Object... sqlA) {
		StringBuilder sb = new StringBuilder();
		
		for (Object obj : sqlA) {
			if (obj instanceof List) {
				Object[] retA = buildSql(args, num, ((List<Object>)obj).toArray());
				sb.append((String)retA[1]);
				num = (int)retA[0];
			} else {
				if (num>0) {
					args.add(obj);
					num--;
				} else if (obj instanceof String) {
					String seg = (String)obj;
					num = countChar(seg, '?');
					sb.append(seg).append(' ');
				} else {
					throw new RuntimeException(sb.toString()+":"+obj.toString());
				}
			}
		}
		
		return new Object[]{num, sb.toString()};
	}
	
	public static List<Object> IF(boolean condition, Object... args) {
		return IFElse(condition, L(args), null);
	}

	public static List<Object> IFElse(boolean condition, String trueexpr, String falseexpr) {
		if (condition) {
			return isEmpty(trueexpr) ? _emptylst : L(trueexpr);
		} else {
			return isEmpty(falseexpr) ? _emptylst : L(falseexpr);
		}
	}
	
	public static List<Object> IFElse(boolean condition, List<Object> truelst, List<Object> falselst) {
		if (condition) {
			return null==truelst ? _emptylst : truelst;
		} else {
			return null==falselst ? _emptylst : falselst;
		}
	}

	public static List<Object> L(Object... args) {
		return Arrays.asList(args);
	}

	public static List<Object> J(String joiner, Object... args) {
		return segs(null, joiner, null, args);
	}
	
	public static List<Object> VALUES(Object... args) {
		return segs("VALUES (", ",", ")", args);
	}
	
	@SuppressWarnings("unchecked")
	public static List<Object> valueWithMap(Map<String, Object> map) {
		List<Object> conditions = new ArrayList<Object>();

		Map<String, Object> values = (Map<String, Object>)map.get(SQLMap.valueskey);
		if (values != null) {
			for (Entry<String, Object> pair : values.entrySet()) {
				Map<String, Object> valdef = (Map<String, Object>)pair.getValue();
				Object val = valdef.get("val");
				conditions.add(V(val));
			}
		}
		return segs("VALUES(", ",", ")", conditions.toArray());
	}
	
	public static List<Object> SET(Object... args) {
		return segs("SET", ",", null, args);
	}
	
	@SuppressWarnings("unchecked")
	public static List<Object> fieldWithMap(Map<String, Object> map) {
		List<Object> conditions = new ArrayList<Object>();

		Map<String, Object> values = (Map<String, Object>)map.get(SQLMap.valueskey);
		if (values != null) {
			for (Entry<String, Object> pair : values.entrySet()) {
				String key = pair.getKey();
				conditions.add(key);
			}
		}
		return segs("(", ",", ")", conditions.toArray());
	}
	
	@SuppressWarnings("unchecked")
	public static List<Object> setWithMap(Map<String, Object> map) {
		List<Object> conditions = new ArrayList<Object>();

		Map<String, Object> values = (Map<String, Object>)map.get(SQLMap.valueskey);
		if (values != null) {
			for (Entry<String, Object> pair : values.entrySet()) {
				String key = pair.getKey();
				Map<String, Object> valdef = (Map<String, Object>)pair.getValue();
				Object val = valdef.get("val");
				List<Object> condition = new ArrayList<Object>(2);
				condition.add(key+"=?");
				condition.add(val);
				conditions.add(condition);
			}
		}
		return segs("SET", ",", null, conditions.toArray());
	}
	
	public static List<Object> WHERE(String joiner, Object... args) {
		return segs("WHERE", joiner, null, args);
	}

	public static List<Object> whereWithMap(Map<String, Object> map) {
		return WHERE("WHERE", Boolean.TRUE.equals(map.get(SQLMap.joinwithorkey)) ? "OR" : "AND", map);
	}
	
	public static List<Object> V(Object value) {
		if (value != null && value.getClass().isArray()) {
			List<Object> lst = new ArrayList<Object>();
			Object[] objA = (Object[])value;
			lst.add("(");
			boolean first = true;
			for (Object item : objA) {
				if (first) {
					first = false;
					lst.add("?");
				} else {
					lst.add(",?");
				}
				lst.add(item);
			}
			lst.add(")");
			return lst;
		}
		return Arrays.asList(new Object[]{"?", value});
	}

	@SuppressWarnings("unchecked")
	private static List<Object> WHERE(String prefix, String joiner, Map<String, Object> map) {
		List<Object> conditions = new ArrayList<Object>();

		Map<String, Object> wheres = (Map<String, Object>)map.get(SQLMap.whereskey);
		if (wheres != null) {
			for (Entry<String, Object> pair : wheres.entrySet()) {
				String key = pair.getKey();
				Map<String, Object> valdef = (Map<String, Object>)pair.getValue();
				String op  = (String)valdef.get("op");
				Object val = valdef.get("val");
				if ("raw".equals(op)) {
					conditions.add(val);
				} else {
					List<Object> condition = new ArrayList<Object>(3);
					condition.add(key);
					if (null==val) {
						if ("=".equals(op)) {
							condition.add("IS NULL");
							continue;
						} else if (("<>".equals(op))) {
							condition.add("IS NOT NULL");
							continue;
						}
					}
					condition.add(op);
					condition.add(V(val));
					conditions.add(condition);
				}
			}
		}
		return segs(prefix, joiner, null, conditions.toArray());
	}

	public static List<Object> segs(String prefix, String joiner, String suffix, Object... args) {
		int num = 0;
		List<Object> lst = new ArrayList<Object>();
		if (args.length>0) {
			String j = prefix;
			for (Object obj : args) {
				if (obj instanceof List && obj!=null && ((List<?>)obj).size()<=0) {
					continue;
				}
				if (!isEmpty(j)) lst.add(j);
				lst.add(obj);
				j = joiner;
				++num;
			}
			if (!isEmpty(suffix)) {
				lst.add(suffix);
			}
		}
		if (num<=0) {
			lst.clear();
		}
		return lst;
	}
}
