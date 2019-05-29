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

public class SQLRunner {
	private SilentConnection connect = null;
	private QueryRunner runner = new QueryRunner();

	public SQLRunner() {
	}

	public SQLRunner(Connection cnt) {
		if (cnt != null) {
			connect = new SilentConnection(cnt);
			connect.setAutoCommit(false);
		}
	}

	private static boolean isEmpty(String s) {
		return null==s || "".equals(s);
	}
	
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
		}
	}
	
	protected void finalize() {
		close();
	}
	
	public void close() {
		if (connect != null) {
			try {
				//connect.rollback();
				connect.close();
			} catch (Exception e) {} finally {
				connect = null;
			}
		}
	}

	@SuppressWarnings("unchecked")
	public int update(Connection cnt, Map<String, Object> map) {
		boolean isUpdate = false;
		if (map.get(SQLMap.whereskey) != null
			&& ((Map<String, Object>)map.get(SQLMap.whereskey)).size()>0) {
			//查询条件不为空才做原记录查询，避免发生全表查询
			Object[] bakA = {map.get(SQLMap.fieldskey), map.get(SQLMap.orderbykey)};
			map.put(SQLMap.fieldskey, "COUNT(*)");
			map.remove(SQLMap.orderbykey);
			long num = 0;
			try {
				num = (long)CommonMap.getValue(query(null, map));
			} catch (Exception e) {
				throw new RuntimeException(e.toString());
			} finally {
				map.put(SQLMap.fieldskey, bakA[0]);
				map.put(SQLMap.orderbykey, bakA[1]);
			}
			if (1 == num) {
				//查到有原记录，可做更新
				isUpdate = true;
			}
		}
		if (isUpdate) {
			return update(cnt, "UPDATE", map.get(SQLMap.objectskey)
				,SQL.setWithMap(map)
				,SQL.whereWithMap(map));
		} else {
			return update(cnt, "INSERT INTO", map.get(SQLMap.objectskey)
				,SQL.fieldWithMap(map)
				,SQL.valueWithMap(map));
		}
	}

	@SuppressWarnings("unchecked")
	public int delete(Connection cnt, Map<String, Object> map) {
		if (map.get(SQLMap.whereskey) != null
			&& ((Map<String, Object>)map.get(SQLMap.whereskey)).size()>0) {
			//查询条件不为空才做原记录查询，避免发生全表删除
			int rows = update(cnt, "DELETE FROM", map.get(SQLMap.objectskey)
				,SQL.whereWithMap(map));
			if (rows>1) {
				throw new RuntimeException("不能删除多条记录");
			} else if (1 == rows) {
				return 1; 
			}
		}
		return 0;
	}

	public int update(Connection cnt, Object... sqlA) {
		return update(cnt, new SQL(sqlA));
	}
	
	public int update(Connection cnt, SQL sql) {
		if (null == cnt && null == connect) return 0;

		int rows = 0;
		try {
			rows = runner.update(null==cnt ? connect.getConnection() : cnt, sql.getSql(), sql.getArgs());
		} catch (SQLException e) {
			throw new RuntimeException(e.getMessage());
		}
		return rows;
	}

	public List<Map<String, Object>> query(Connection cnt, Map<String, Object> map) {
		String fields = (String)map.get(SQLMap.fieldskey);
		if (isEmpty(fields)) fields = "*";
		
		if (map.containsKey(SQLMap.orderbykey)) {
			return query(null==cnt ? connect : cnt,
				"SELECT", fields, "FROM", map.get(SQLMap.objectskey),
				SQL.whereWithMap(map),
				"ORDER BY", map.get(SQLMap.orderbykey));
		} else {
			return query(null==cnt ? connect : cnt,
				"SELECT", fields, "FROM", map.get(SQLMap.objectskey),
				SQL.whereWithMap(map));
		}
	}

	public List<Map<String, Object>> query(Connection cnt, Object... sqlA) {
		return query(cnt, new SQL(sqlA));
	}
	
	public List<Map<String, Object>> query(Connection cnt, SQL sql) {
		if (null == cnt && null == connect || null == sql) return null;
		
		List<Map<String, Object>> ret = null;
		try {
			ret = runner.query(null==cnt ? connect.getConnection() : cnt, sql.getSql(), new MapListHandler(), sql.getArgs());
		} catch (SQLException e) {
			throw new RuntimeException(e.getMessage());
		}
		return ret;
	}

	public Object queryValue(Connection cnt, Object... sqlA) {
		return CommonMap.getValue(query(cnt, sqlA));
	}

	public Map<String, Object> queryOne(Connection cnt, Object... sqlA) {
		return CommonMap.getOne(query(cnt, sqlA));
	}

	public SilentConnection getConnect() {
		return connect;
	}
	
	public void commit() {
		if (connect != null) {
			connect.commit();
		}
	}
	
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
