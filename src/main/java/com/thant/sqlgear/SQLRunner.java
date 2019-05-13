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

	public int update(Connection cnt, Map<String, Object> map) {
		List<Map<String, Object>> lst = query(cnt, map);
		if (lst != null && lst.size()>0) {
			return update(cnt, "UPDATE", map.get(SQLMap.objectskey)
				,SQL.setWithMap(map)
				,SQL.whereWithMap(map));
		} else {
			return update(cnt, "INSERT INTO", map.get(SQLMap.objectskey)
				,SQL.fieldWithMap(map)
				,SQL.valueWithMap(map));
		}
	}

	public int update(Connection cnt, Object... sqlA) {
		return update(cnt, new SQL(sqlA));
	}
	
	public int update(Connection cnt, SQL sql) {
		if (null == cnt && null == connect) return 0;

		int rows = 0;
		try {
			//long t = System.currentTimeMillis();
			rows = runner.update(null==cnt ? connect.getConnection() : cnt, sql.getSql(), sql.getArgs());
			//t = System.currentTimeMillis() - t;
			//System.out.println(String.format("SQL:%s%ntime:%dms%neffectrows:%d", sql, t, rows));
		} catch (SQLException e) {
			throw new RuntimeException(e.getMessage());
		}
		return rows;
	}

	public List<Map<String, Object>> query(Connection cnt, Map<String, Object> map) {
		String fields = (String)map.get(SQLMap.fieldskey);
		if (isEmpty(fields)) fields = "*";
		
		return query(null==cnt ? connect : cnt,
			"select", fields, "from", map.get(SQLMap.objectskey), SQL.whereWithMap(map));
	}

	public List<Map<String, Object>> query(Connection cnt, Object... sqlA) {
		return query(cnt, new SQL(sqlA));
	}
	
	public List<Map<String, Object>> query(Connection cnt, SQL sql) {
		if (null == cnt && null == connect || null == sql) return null;
		
		List<Map<String, Object>> ret = null;
		try {
			//long t = System.currentTimeMillis();
			ret = runner.query(null==cnt ? connect.getConnection() : cnt, sql.getSql(), new MapListHandler(), sql.getArgs());
			//t = System.currentTimeMillis() - t;
			//System.out.println(String.format("SQL:%s%ntime:%dms%n", sql, t));
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
