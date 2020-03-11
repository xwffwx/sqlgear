package com.thant.sqlgear;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.thant.common.map.SQLMap;

/**
 * @ClassName SQL
 * @Description SQL定义对象
 * @author 肖文峰
 * @date 2019年5月10日 上午11:31:05
 */
public class SQL {
	/**
	 * variantMark 变量占位符，替换dbutils中使用的?，以防和SQL语句中正常出现的?发生冲突
	 */
	private static String variantMark = "?"; //"-+<(VAR)>+-";
	/**
	 * _emptylst 空列表，常量引用
	 */
	private static final List<Object> _emptylst = new ArrayList<Object>();
	
	/**
	 * _sqlsegs SQL片段
	 */
	private List<Object> _sqlsegs = new ArrayList<Object>();

	/**
	 * sql 字符串，生成的SQL语句 
	 */
	private String sql = null;
	
	/**
	 * args 生成SQL语句对应的参数数组
	 */
	private Object[] args = null;

	/**
	 * @Title SQL
	 * @Description 构造函数
	 * @throws
	 */
	public SQL() {
	}
	
	/**
	 * @Title SQL
	 * @Description 构造函数 
	 * @param 组成SQL语句的变长参数
	 */
	public SQL(Object... segs) {
		append(segs);
	}
	
	/**
	 * @Title 添加SQL片段
	 * @param segs SQL片段，变长参数
	 * @return SQL this
	 * @throws
	 */
	public SQL append(Object... segs) {
		_sqlsegs.addAll(Arrays.asList(segs));
		sql = null;
		args = null;
		return this;
	}
	
	/**
	 * @Title 清除存在的SQL片段
	 * @Description 
	 * @return SQL this
	 * @throws
	 */
	public SQL clear() {
		_sqlsegs.clear();
		sql = null;
		args = null;
		return this;
	}
	
	/**
	 * @Title 获取生成的SQL参数数组
	 * @Description 对存在的SQL片段生成SQL语句和参数，并返回
	 * @return Object[] SQL参数数组
	 * @throws
	 */
	public Object[] getArgs() {
		if (null == sql) {
			buildSql();
		}
		return args;
	}

	/**
	 * @Title 获取生成的SQL语句
	 * @Description 对存在的SQL片段生成SQL语句和参数，并返回
	 * @return String SQL语句
	 * @throws
	 */
	public String getSql() {
		if (null == sql) {
			buildSql();
		}
		return sql;
	}

	/**
	 * @Title 判断字符串是否为空串或NULL
	 * @Description 
	 * @param s 判断的字符串
	 * @return boolean
	 * @throws
	 */
	private static boolean isEmpty(String s) {
		return null==s || "".equals(s);
	}
	
	/**
	 * @Title 统计SQL片段中是否包含指定字符，并返回指定字符的数量
	 * @Description 
	 * @param s SQL片段的文本
	 * @return int 找到的数量
	 * @throws
	 */
	private static Object[] countVariant(String s) {
		int len = variantMark.length();
		int num = 0, begin = 0, pos;
		StringBuilder sb = new StringBuilder();
		while ((pos = s.indexOf(variantMark, begin))>=0) {
			sb.append(s.substring(begin, pos)).append('?');
			begin = pos+len;
			num++;
		}
		if (num>0) {
			sb.append(s.substring(begin));
			return new Object[]{(Integer)num, sb.toString()};
		} else {
			//此分支用上面代码执行也没有问题，但是避免了无意义的字符串拷贝
			return new Object[]{(Integer)0, s};
		}
	}
	
	/**
	 * @Title 根据SQL片段生成SQL语句和参数数组
	 * @Description 
	 * @return void
	 * @throws
	 */
	private void buildSql() {
		List<Object> argsLst = new ArrayList<Object>(); 
		Object[] retA = buildSql(argsLst, 0, _sqlsegs.toArray());

		sql = (String)retA[1];
		args = argsLst.toArray();

		if ((int)retA[0] != 0) {
			throw new RuntimeException("缺少"+(int)retA[0]+"个参数");
		}
	}
	
	/**
	 * @Title 根据SQL片段生成SQL语句和参数数组，内部迭代
	 * @Description 
	 * @param args 当前参数列表
	 * @param num 当前需要匹配的参数个数
	 * @param sqlA 当前待解析的SQL片段
	 * @return Object[] 返回值是一个长度为2的数组，第一个是本轮解析之后剩余需匹配的参数数量，第二个是解析生成的SQL语句片段
	 * @throws
	 */
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
					Object[] ret = countVariant(seg);
					num = (int)ret[0];
					sb.append((String)ret[1]).append(' ');
				} else {
					throw new RuntimeException(sb.toString()+":"+obj.toString());
				}
			}
		}
		
		return new Object[]{num, sb.toString()};
	}
	
	/**
	 * @Title 构造IF判断片段
	 * @Description 
	 * @param condition 条件表达式
	 * @param args 如果条件表达式为TRUE加入的SQL片段，变长数组
	 * @return List<Object> 一个SQL片段
	 * @throws
	 */
	public static List<Object> IF(boolean condition, Object... args) {
		return IFElse(condition, L(args), (List<Object>)null);
	}

	/**
	 * @Title 构造IFElse判断片段
	 * @Description 
	 * @param condition 条件表达式
	 * @param trueexpr 如果条件表达式为TRUE加入的SQL片段，字符串或SQL片段
	 * @param falseexpr 如果条件表达式为FALSE加入的SQL片段，字符串或SQL片段
	 * @return List<Object> 一个SQL片段
	 * @throws
	 */
	public static List<Object> IFElse(boolean condition, String trueexpr, String falseexpr) {
		if (condition) {
			return isEmpty(trueexpr) ? _emptylst : L(trueexpr);
		} else {
			return isEmpty(falseexpr) ? _emptylst : L(falseexpr);
		}
	}
	public static List<Object> IFElse(boolean condition, String truelst, List<Object> falselst) {
		if (condition) {
			return null==truelst ? _emptylst : L(truelst);
		} else {
			return null==falselst ? _emptylst : falselst;
		}
	}
	public static List<Object> IFElse(boolean condition, List<Object> truelst, String falselst) {
		if (condition) {
			return null==truelst ? _emptylst : truelst;
		} else {
			return null==falselst ? _emptylst : L(falselst);
		}
	}
	public static List<Object> IFElse(boolean condition, List<Object> truelst, List<Object> falselst) {
		if (condition) {
			return null==truelst ? _emptylst : truelst;
		} else {
			return null==falselst ? _emptylst : falselst;
		}
	}

	/**
	 * @Title 将多个SQL片段连接成一个SQL片段
	 * @Description 
	 * @param args 多个SQL片段，变长数组
	 * @return List<Object> 一个SQL片段
	 * @throws
	 */
	public static List<Object> L(Object... args) {
		return Arrays.asList(args);
	}

	/**
	 * @Title 将多个SQL片段连接成一个SQL片段，中间用指定字符串间隔
	 * @Description 
	 * @param joiner 间隔字符串
	 * @param args 多个SQL片段，变长数组
	 * @return List<Object> 一个SQL片段
	 * @throws
	 */
	public static List<Object> J(String joiner, Object... args) {
		return segs(null, joiner, null, args);
	}
	
	/**
	 * @Title 将外部参数包装成一个SQL片段
	 * @Description 
	 * @param value 参数
	 * @return List<Object> 一个SQL片段
	 * @throws
	 */
	public static List<Object> V(Object value) {
		if (value != null && value.getClass().isArray()) {
			List<Object> lst = new ArrayList<Object>();
			Object[] objA = (Object[])value;
			lst.add("(");
			boolean first = true;
			for (Object item : objA) {
				if (first) {
					first = false;
					lst.add(variantMark);
				} else {
					lst.add(","+variantMark);
				}
				lst.add(item);
			}
			lst.add(")");
			return lst;
		}
		return Arrays.asList(new Object[]{variantMark, value});
	}
	
	/**
	 * @Title 构造INSERT语句的VALUES片段
	 * @Description 
	 * @param args VALUES的片段，变长数组
	 * @return List<Object> 一个SQL片段
	 * @throws
	 */
	public static List<Object> VALUES(Object... args) {
		if (args.length<=0) {
			return null;
		}
		if (args[0] instanceof String) {
			int len = args.length/2;
			Object[] fields = new Object[len];
			Object[] values = new Object[len];
			for (int i = 0,j = 0; i<len; ++i, j+=2) {
				fields[i] = args[j];
				values[i] = V(args[j+1]);
			}
			return L(
				segs("(", ",", ")", fields),
				segs("VALUES (", ",", ")", values)
			);
		} else {
			return segs("VALUES (", ",", ")", args);
		}
	}
	
	/**
	 * @Title 构造INSERT语句的VALUES片段
	 * @Description 
	 * @param map 用SQLMap定义的VALUES内容，@see SQLMap
	 * @return List<Object> 一个SQL片段
	 * @throws
	 */
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
	
	/**
	 * @Title 构造UPDATE语句的更新列
	 * @Description 
	 * @param map 用SQLMap定义的选择列内容，@see SQLMap
	 * @return List<Object> 一个SQL片段
	 * @throws
	 */
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

	/**
	 * @Title 构造UPDATE语句的更新列
	 * @Description 
	 * @param args 要更新的列数组
	 * @return List<Object> 一个SQL片段
	 * @throws
	 */
	/*public static List<Object> FIELDS(Object... args) {
		return segs("(", ",", ")", args);
	}*/
	
	/**
	 * @Title 构造UPDATE语句的SET片段
	 * @Description 
	 * @param args SET的SQL片段，变长数组
	 * @return List<Object> 一个SQL片段
	 * @throws
	 */
	public static List<Object> SET(Object... args) {
		if (args.length<=0) {
			return null;
		}
		if (args[0] instanceof String) {
			Object[] newargs = new Object[args.length/2];
			for (int i = 0,j = 0; i<newargs.length; ++i, j+=2) {
				newargs[i] = L(args[j], "=", V(args[j+1]));
			}
			return segs("SET", ",", null, newargs);
		} else {
			return segs("SET", ",", null, args);
		}
	}

	/**
	 * @Title 构造UPDATE语句的SET片段
	 * @Description 
	 * @param args SET的SQL片段，变长数组
	 * @return List<Object> 一个SQL片段
	 * @throws
	 */
	public static List<Object> OR(Object... args) {
		return segs("((", ") OR (", "))", args);
	}
	
	/**
	 * @Title 构造UPDATE语句的SET片段
	 * @Description 
	 * @param args SET的SQL片段，变长数组
	 * @return List<Object> 一个SQL片段
	 * @throws
	 */
	public static List<Object> AND(Object... args) {
		return segs("((", ") AND (", "))", args);
	}
	
	/**
	 * @Title 构造UPDATE语句的SET片段
	 * @Description 
	 * @param map 用SQLMap定义的SET内容，@see SQLMap
	 * @return List<Object> 一个SQL片段
	 * @throws
	 */
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
				condition.add(key+"="+variantMark);
				condition.add(val);
				conditions.add(condition);
			}
		}
		return segs("SET", ",", null, conditions.toArray());
	}
	
	/**
	 * @Title 构造SELECT/UPDATE语句的WHERE片段
	 * @Description 
	 * @param args WHERE条件，变长数组，用AND连接条件
	 * @return List<Object> 一个SQL片段
	 * @throws
	 */
	public static List<Object> WHERE(Object... args) {
		if (args.length<=0) {
			return null;
		}
		if (args[0] instanceof String) {
			Object[] newargs = new Object[args.length/3];
			for (int i = 0,j = 0; i<newargs.length; ++i, j+=3) {
				newargs[i] = L(args[j], args[j+1], V(args[j+2]));
			}
			return segs("WHERE", "AND", null, newargs);
		} else {
			return segs("WHERE", "AND", null, args);
		}
	}

	/**
	 * @Title 构造SELECT/UPDATE语句的WHERE片段
	 * @Description 
	 * @param args WHERE条件，变长数组，用OR连接条件
	 * @return List<Object> 一个SQL片段
	 * @throws
	 */
	public static List<Object> WHERE_OR(Object... args) {
		if (args.length<=0) {
			return null;
		}
		if (args[0] instanceof String) {
			Object[] newargs = new Object[args.length/3];
			for (int i = 0,j = 0; i<newargs.length; ++i, j+=3) {
				newargs[i] = L(args[j], args[j+1], V(args[j+2]));
			}
			return segs("WHERE", "OR", null, newargs);
		} else {
			return segs("WHERE", "OR", null, args);
		}
	}

	/**
	 * @Title 构造SELECT/UPDATE语句的WHERE片段
	 * @Description 
	 * @param map 用SQLMap定义的WHERE条件内容，@see SQLMap
	 * @return List<Object> 一个SQL片段
	 * @throws
	 */
	@SuppressWarnings("unchecked")
	public static List<Object> whereWithMap(Map<String, Object> map) {
		String joiner = Boolean.TRUE.equals(map.get(SQLMap.joinwithorkey)) ? "OR" : "AND";
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
		return segs("WHERE", joiner, null, conditions.toArray());
	}

	/**
	 * @Title 通用的拼接SQL片段
	 * @Description 
	 * @param prefix 前缀部分，如果后续没有内容则不生成前缀
	 * @param joiner WHERE条件之间的连接串 AND | OR
	 * @param suffix 后缀部分
	 * @param args WHERE条件，变长数组
	 * @return List<Object> 一个SQL片段
	 * @throws
	 */
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

	/**
	 * get variantMark
	 */
	public static String getVariantMark() {
		return variantMark;
	}

	/**
	 * set variantMark
	 * 不要使用?，当SQL语句中有包含?的字符串常量时会出错
	 */
	public static void setVariantMark(String mark) {
		variantMark = mark;
	}
}
