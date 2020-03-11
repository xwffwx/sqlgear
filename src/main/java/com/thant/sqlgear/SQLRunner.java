package com.thant.sqlgear;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Savepoint;
import java.util.List;
import java.util.Map;
import javax.sql.DataSource;

import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.MapListHandler;

import com.thant.common.map.CommonMap;
import com.thant.common.map.SQLMap;
import static com.thant.sqlgear.SQL.*;

/**
 * @ClassName: SQLRunner
 * @Description: SQL语句执行工具
 * @author: 肖文峰
 * @date: 2019年5月30日 下午3:33:27
 */
public class SQLRunner {
	private DataSource _ds = null;
	private SilentConnection connect = null;
	private QueryRunner runner = new QueryRunner();
	
	private String lastSql = null;
	private Object[] lastArgs = {}; 

	public SQLRunner() {
	}

	/**
	 * @Title 构造函数
	 * @Description 根据连接对象创建
	 * @param cnt 连接对象
	 */
	public SQLRunner(Connection cnt) {
		if (cnt != null) {
			connect = new SilentConnection(cnt);
			//connect.setAutoCommit(false); 对外部给的连接，不应该有默认动作(不挖坑)，所以注释掉
		}
	}

	/**
	 * @Title isEmpty
	 * @Description 
	 * @param s
	 * @return boolean
	 * @throws
	 */
	private static boolean isEmpty(String s) {
		return null==s || "".equals(s);
	}
	
	/**
	 * @Title 构造函数
	 * @Description 根据数据源对象创建
	 * @param ds 数据源对象
	 */
	public SQLRunner(DataSource ds) {
		Connection cnt = null;
		try {
			cnt = ds.getConnection();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		if (cnt != null) {
			connect = new SilentConnection(cnt);
			connect.setAutoCommit(false);
			_ds = ds; 
		}
	}
	
	public String getLastSql() {
		return lastSql;
	}

	public Object[] getLastArgs() {
		return lastArgs;
	}

	public void setLastArgs(Object[] lastArgs) {
		this.lastArgs = lastArgs;
	}

	protected void finalize() {
		close();
	}
	
	/**
	 * @Title 关闭连接
	 * @Description 
	 * @return void
	 * @throws
	 */
	public void close() {
		if (connect != null) {
			try {
				if (_ds != null) {
					connect.commit();//自己从数据源获取的连接，自己提交且释放;
					connect.close(); //_ds==null时表示是外部给的连接，由外部释放
				}
			} catch (Exception e) {} finally {
				connect = null;
				_ds = null;
			}
		}
	}

	/**
	 * @Title 插入/更新一条记录
	 * @Description 如果在参数mao中提供了where条件且只匹配一条记录则做更新，否则做插入操作
	 * @param cnt 连接对象，NULL表示使用构造函数关联的连接
	 * @param map 提交的记录，类型 @see SQLMap
	 * @return int 修改的记录条数
	 * @throws
	 */
	@SuppressWarnings("unchecked")
	public int update(Connection cnt, Map<String, Object> map) {
		boolean isUpdate = false;
		if ("REPLACE".equals(map.get(SQLMap.updatetypekey))
			&& map.get(SQLMap.whereskey) != null
			&& ((Map<String, Object>)map.get(SQLMap.whereskey)).size()>0) {
			//执行REPLACE，而且查询条件不为空才做原记录查询，避免发生全表查询
			Object[] bakA = {map.get(SQLMap.fieldskey), map.get(SQLMap.orderbykey)};
			map.put(SQLMap.fieldskey, "COUNT(*)");
			map.remove(SQLMap.orderbykey);
			long num = 0;
			try {
				Object countObj = CommonMap.getValue(query(cnt, map));
				if (countObj instanceof Long) {
					num = (long)countObj;
				} else {
					num = (int)countObj;
				}
			} catch (Exception e) {
				throw new RuntimeException(e.toString());
			} finally {
				if (bakA[0] != null) map.put(SQLMap.fieldskey, bakA[0]);
				if (bakA[1] != null) map.put(SQLMap.orderbykey, bakA[1]);
			}
			if (1 == num) {
				//查到有原记录，可做更新
				isUpdate = true;
			} else if (num>1) {
				throw new RuntimeException("不允许用REPLACE批量更新多条记录，请使用UPDATE");
			}
		}
		if (isUpdate) {
			return update(cnt, "UPDATE", map.get(SQLMap.objectskey)
				,setWithMap(map)
				,whereWithMap(map));
		} else {
			return update(cnt, "INSERT INTO", map.get(SQLMap.objectskey)
				,IF(false == (Boolean)map.get(SQLMap.insertwithlistkey), fieldWithMap(map))
				,valueWithMap(map));
		}
	}

	/**
	 * @Title 删除一条记录
	 * @Description 应用于表的CRUD，只能删除一条记录
	 * @param cnt 连接对象，NULL表示使用构造函数关联的连接
	 * @param map 定义删除对象的SQLmap
	 * @return int 删除的记录数，=1表示成功，0未找到记录
	 * @throws
	 */
	@SuppressWarnings("unchecked")
	public int delete(Connection cnt, Map<String, Object> map) {
		if (map.get(SQLMap.whereskey) != null
			&& ((Map<String, Object>)map.get(SQLMap.whereskey)).size()>0) {
			//查询条件不为空才做原记录查询，避免发生全表删除
			int rows = update(cnt, "DELETE FROM", map.get(SQLMap.objectskey)
				,whereWithMap(map));
			if (rows>1) {
				throw new RuntimeException("不能删除多条记录");
			} else if (1 == rows) {
				return 1; 
			}
		}
		return 0;
	}

	/**
	 * @Title 通用更新
	 * @Description 支持UPDATE/INSERT/DELETE等改变数据的动作
	 * @param cnt 连接对象，NULL表示使用构造函数关联的连接
	 * @param sqlA 构成SQL语句的变长参数
	 * @return int 影响的记录数
	 * @throws
	 */
	public int update(Connection cnt, Object... sqlA) {
		return update(cnt, new SQL(sqlA));
	}
	
	/**
	 * @Title 通用更新
	 * @Description 支持UPDATE/INSERT/DELETE等改变数据的动作
	 * @param cnt 连接对象，NULL表示使用构造函数关联的连接
	 * @param sql 定义的SQL对象 @see SQL
	 * @return int 影响的记录数
	 * @throws
	 */
	public int update(Connection cnt, SQL sql) {
		if (null == cnt && null == connect) return 0;

		lastSql  = sql.getSql();
		lastArgs = sql.getArgs();

		int rows = 0;
		try {
			rows = runner.update(null==cnt ? connect.getConnection() : cnt, lastSql, lastArgs);
		} catch (SQLException e) {
			throw new RuntimeException(e.getMessage());
		}
		return rows;
	}

	/**
	 * @Title Map条件查询
	 * @Description 返回带分页和排序的单/多行数据 
	 * @param cnt 连接对象，NULL表示使用构造函数关联的连接
	 * @param map 查询+分页+排序条件，@see SQLMap
	 * @return List<Map<String,Object>> 返回数据结果集
	 * @throws
	 */
	public List<Map<String, Object>> query(Connection cnt, Map<String, Object> map) {
		String fields = (String)map.get(SQLMap.fieldskey);
		if (isEmpty(fields)) fields = "*";
		
		return query(null==cnt ? connect : cnt,
			"SELECT", fields, "FROM", map.get(SQLMap.objectskey),
			whereWithMap(map),
			IF(!isEmpty((String)map.get(SQLMap.orderbykey)), "ORDER BY", map.get(SQLMap.orderbykey)),
			IF(map.get(SQLMap.pagebeginkey)!=null && map.get(SQLMap.pageendkey)!=null,
				"LIMIT", V(map.get(SQLMap.pagebeginkey)) , ",", V(map.get(SQLMap.pageendkey)))
		);
	}

	/**
	 * @Title 通用查询
	 * @Description   
	 * @param cnt 连接对象，NULL表示使用构造函数关联的连接
	 * @param sqlA 构成SQL语句的变长参数
	 * @return List<Map<String,Object>> 返回数据结果集
	 * @throws
	 */
	public List<Map<String, Object>> query(Connection cnt, Object... sqlA) {
		return query(cnt, new SQL(sqlA));
	}
	
	/**
	 * @Title 通用查询
	 * @Description 
	 * @param cnt 连接对象，NULL表示使用构造函数关联的连接
	 * @param sql 定义的SQL对象 @see SQL
	 * @return List<Map<String,Object>> 返回数据结果集
	 * @throws
	 */
	public List<Map<String, Object>> query(Connection cnt, SQL sql) {
		if (null == cnt && null == connect || null == sql) return null;
		
		lastSql  = sql.getSql();
		lastArgs = sql.getArgs();
		
		List<Map<String, Object>> ret = null;
		try {
			ret = runner.query(null==cnt ? connect.getConnection() : cnt, lastSql, new MapListHandler(), lastArgs);
		} catch (SQLException e) {
			throw new RuntimeException(e.getMessage());
		}
		return ret;
	}

	/**
	 * @Title 查询返回一个值
	 * @Description 把结果中第一行的第一列数据返回，如果没有的话返回NULL
	 * @param cnt 连接对象，NULL表示使用构造函数关联的连接
	 * @param sqlA 构成SQL语句的变长参数
	 * @return Object 查询到的值
	 * @throws
	 */
	public Object queryValue(Connection cnt, Object... sqlA) {
		return CommonMap.getValue(query(cnt, sqlA));
	}

	/**
	 * @Title 查询返回一行数据
	 * @Description 
	 * @param cnt 连接对象，NULL表示使用构造函数关联的连接
	 * @param sqlA 定义的SQL对象 @see SQL
	 * @return Map<String,Object>
	 * @throws
	 */
	public Map<String, Object> queryOne(Connection cnt, Object... sqlA) {
		return CommonMap.getOne(query(cnt, sqlA));
	}

	/**
	 * @Title 返回构造函数关联的连接
	 * @Description 
	 * @return SilentConnection 连接对象 @see SilentConnection
	 * @throws
	 */
	public SilentConnection getConnect() {
		return connect;
	}
	
	/**
	 * @Title 提交连接
	 * @Description 
	 * @return void
	 * @throws
	 */
	public void commit() {
		if (connect != null) {
			connect.commit();
		}
	}
	
	/**
	 * @Title 回滚连接
	 * @Description 
	 * @param sp 要回滚到的保存点，没有保存点可传NULL
	 * @return void
	 * @throws
	 */
	public void rollback(Savepoint sp) {
		if (connect != null) {
			if (null == sp) {
				connect.rollback();
			} else {
				connect.rollback(sp);
			}
		}
	}
}
