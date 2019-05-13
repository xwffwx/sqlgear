package com.thant.sqlgear;

import java.sql.Array;
import java.sql.Blob;
import java.sql.CallableStatement;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.NClob;
import java.sql.PreparedStatement;
import java.sql.SQLClientInfoException;
import java.sql.SQLWarning;
import java.sql.SQLXML;
import java.sql.Savepoint;
import java.sql.Statement;
import java.sql.Struct;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Executor;

public class SilentConnection implements Connection {
	private Connection _connect = null;
	private Exception error = null;
	
	public SilentConnection(Connection cnt) {
		if (cnt != null) {
			if (cnt instanceof SilentConnection) {
				_connect = ((SilentConnection)cnt).getConnection(); 
			} else {
				_connect = cnt;
			}
		}
	}
	
	@Override
	public <T> T unwrap(Class<T> paramClass) {
		try {
			error = null;
			return _connect.unwrap(paramClass);
		} catch (Exception e) {
			error = e;
			throw new RuntimeException(e.getMessage());
		}
	}

	@Override
	public boolean isWrapperFor(Class<?> paramClass) {
		try {
			error = null;
			return _connect.isWrapperFor(paramClass);
		} catch (Exception e) {
			error = e;
			throw new RuntimeException(e.getMessage());
		}
	}

	@Override
	public Statement createStatement() {
		try {
			error = null;
			return _connect.createStatement(); 
		} catch (Exception e) {
			error = e;
			throw new RuntimeException(e.getMessage());
		}
	}

	@Override
	public PreparedStatement prepareStatement(String paramString) {
		try {
			error = null;
			return _connect.prepareStatement(paramString);
		} catch (Exception e) {
			error = e;
			throw new RuntimeException(e.getMessage());
		}
	}

	@Override
	public CallableStatement prepareCall(String paramString) {
		try {
			error = null;
			return _connect.prepareCall(paramString);
		} catch (Exception e) {
			error = e;
			throw new RuntimeException(e.getMessage());
		}
	}

	@Override
	public String nativeSQL(String paramString) {
		try {
			error = null;
			return _connect.nativeSQL(paramString);
		} catch (Exception e) {
			error = e;
			throw new RuntimeException(e.getMessage());
		}
	}

	@Override
	public void setAutoCommit(boolean paramBoolean) {
		try {
			error = null;
			_connect.setAutoCommit(paramBoolean);
		} catch (Exception e) {
			error = e;
			throw new RuntimeException(e.getMessage());
		}
	}

	@Override
	public boolean getAutoCommit() {
		try {
			error = null;
			return _connect.getAutoCommit();
		} catch (Exception e) {
			error = e;
			throw new RuntimeException(e.getMessage());
		}
	}

	@Override
	public void commit() {
		try {
			error = null;
			_connect.commit();
		} catch (Exception e) {
			error = e;
			throw new RuntimeException(e.getMessage());
		}
	}

	@Override
	public void rollback() {
		try {
			error = null;
			_connect.rollback();
		} catch (Exception e) {
			error = e;
			throw new RuntimeException(e.getMessage());
		}
	}

	@Override
	public void close() {
		try {
			error = null;
			_connect.close();
		} catch (Exception e) {
			error = e;
			throw new RuntimeException(e.getMessage());
		}
	}

	@Override
	public boolean isClosed() {
		try {
			error = null;
			return _connect.isClosed();
		} catch (Exception e) {
			error = e;
			throw new RuntimeException(e.getMessage());
		}
	}

	@Override
	public DatabaseMetaData getMetaData() {
		try {
			error = null;
			return _connect.getMetaData();
		} catch (Exception e) {
			error = e;
			throw new RuntimeException(e.getMessage());
		}
	}

	@Override
	public void setReadOnly(boolean paramBoolean) {
		try {
			error = null;
			_connect.setReadOnly(paramBoolean);
		} catch (Exception e) {
			error = e;
			throw new RuntimeException(e.getMessage());
		}
	}

	@Override
	public boolean isReadOnly() {
		try {
			error = null;
			return _connect.isReadOnly();
		} catch (Exception e) {
			error = e;
			throw new RuntimeException(e.getMessage());
		}
	}

	@Override
	public void setCatalog(String paramString) {
		try {
			error = null;
			_connect.setCatalog(paramString);
		} catch (Exception e) {
			error = e;
			throw new RuntimeException(e.getMessage());
		}
	}

	@Override
	public String getCatalog() {
		try {
			error = null;
			return _connect.getCatalog();
		} catch (Exception e) {
			error = e;
			throw new RuntimeException(e.getMessage());
		}
	}

	@Override
	public void setTransactionIsolation(int paramInt) {
		try {
			error = null;
			_connect.setTransactionIsolation(paramInt);
		} catch (Exception e) {
			error = e;
			throw new RuntimeException(e.getMessage());
		}
	}

	@Override
	public int getTransactionIsolation() {
		try {
			error = null;
			return _connect.getTransactionIsolation();
		} catch (Exception e) {
			error = e;
			throw new RuntimeException(e.getMessage());
		}
	}

	@Override
	public SQLWarning getWarnings() {
		try {
			error = null;
			return _connect.getWarnings();
		} catch (Exception e) {
			error = e;
			throw new RuntimeException(e.getMessage());
		}
	}

	@Override
	public void clearWarnings() {
		try {
			error = null;
			_connect.clearWarnings();
		} catch (Exception e) {
			error = e;
			throw new RuntimeException(e.getMessage());
		}
	}

	@Override
	public Statement createStatement(int paramInt1, int paramInt2) {
		try {
			error = null;
			return _connect.createStatement(paramInt1, paramInt2);
		} catch (Exception e) {
			error = e;
			throw new RuntimeException(e.getMessage());
		}
	}

	@Override
	public PreparedStatement prepareStatement(String paramString, int paramInt1, int paramInt2) {
		try {
			error = null;
			return _connect.prepareStatement(paramString, paramInt1, paramInt2);
		} catch (Exception e) {
			error = e;
			throw new RuntimeException(e.getMessage());
		}
	}

	@Override
	public CallableStatement prepareCall(String paramString, int paramInt1, int paramInt2) {
		try {
			error = null;
			return _connect.prepareCall(paramString, paramInt1, paramInt2);
		} catch (Exception e) {
			error = e;
			throw new RuntimeException(e.getMessage());
		}
	}

	@Override
	public Map<String, Class<?>> getTypeMap() {
		try {
			error = null;
			return _connect.getTypeMap();
		} catch (Exception e) {
			error = e;
			throw new RuntimeException(e.getMessage());
		}
	}

	@Override
	public void setTypeMap(Map<String, Class<?>> paramMap) {
		try {
			error = null;
			_connect.setTypeMap(paramMap);
		} catch (Exception e) {
			error = e;
			throw new RuntimeException(e.getMessage());
		}
	}

	@Override
	public void setHoldability(int paramInt) {
		try {
			error = null;
			_connect.setHoldability(paramInt);
		} catch (Exception e) {
			error = e;
			throw new RuntimeException(e.getMessage());
		}
	}

	@Override
	public int getHoldability() {
		try {
			error = null;
			return _connect.getHoldability();
		} catch (Exception e) {
			error = e;
			throw new RuntimeException(e.getMessage());
		}
	}

	@Override
	public Savepoint setSavepoint() {
		try {
			error = null;
			return _connect.setSavepoint();
		} catch (Exception e) {
			error = e;
			throw new RuntimeException(e.getMessage());
		}
	}

	@Override
	public Savepoint setSavepoint(String paramString) {
		try {
			error = null;
			return _connect.setSavepoint(paramString);
		} catch (Exception e) {
			error = e;
			throw new RuntimeException(e.getMessage());
		}
	}

	@Override
	public void rollback(Savepoint paramSavepoint) {
		try {
			error = null;
			_connect.rollback();
		} catch (Exception e) {
			error = e;
			throw new RuntimeException(e.getMessage());
		}
	}

	@Override
	public void releaseSavepoint(Savepoint paramSavepoint) {
		try {
			error = null;
			_connect.releaseSavepoint(paramSavepoint);
		} catch (Exception e) {
			error = e;
			throw new RuntimeException(e.getMessage());
		}
	}

	@Override
	public Statement createStatement(int paramInt1, int paramInt2, int paramInt3) {
		try {
			error = null;
			return _connect.createStatement(paramInt1, paramInt2, paramInt3);
		} catch (Exception e) {
			error = e;
			throw new RuntimeException(e.getMessage());
		}
	}

	@Override
	public PreparedStatement prepareStatement(String paramString, int paramInt1, int paramInt2, int paramInt3) {
		try {
			error = null;
			return _connect.prepareStatement(paramString, paramInt1, paramInt2, paramInt3);
		} catch (Exception e) {
			error = e;
			throw new RuntimeException(e.getMessage());
		}
	}

	@Override
	public CallableStatement prepareCall(String paramString, int paramInt1, int paramInt2, int paramInt3) {
		try {
			error = null;
			return _connect.prepareCall(paramString, paramInt1, paramInt2, paramInt3);
		} catch (Exception e) {
			error = e;
			throw new RuntimeException(e.getMessage());
		}
	}

	@Override
	public PreparedStatement prepareStatement(String paramString, int paramInt) {
		try {
			error = null;
			return _connect.prepareStatement(paramString, paramInt);
		} catch (Exception e) {
			error = e;
			throw new RuntimeException(e.getMessage());
		}
	}

	@Override
	public PreparedStatement prepareStatement(String paramString, int[] paramArrayOfInt) {
		try {
			error = null;
			return _connect.prepareStatement(paramString, paramArrayOfInt);
		} catch (Exception e) {
			error = e;
			throw new RuntimeException(e.getMessage());
		}
	}

	@Override
	public PreparedStatement prepareStatement(String paramString, String[] paramArrayOfString) {
		try {
			error = null;
			return _connect.prepareStatement(paramString, paramArrayOfString);
		} catch (Exception e) {
			error = e;
			throw new RuntimeException(e.getMessage());
		}
	}

	@Override
	public Clob createClob() {
		try {
			error = null;
			return _connect.createClob();
		} catch (Exception e) {
			error = e;
			throw new RuntimeException(e.getMessage());
		}
	}

	@Override
	public Blob createBlob() {
		try {
			error = null;
			return _connect.createBlob();
		} catch (Exception e) {
			error = e;
			throw new RuntimeException(e.getMessage());
		}
	}

	@Override
	public NClob createNClob() {
		try {
			error = null;
			return _connect.createNClob();
		} catch (Exception e) {
			error = e;
			throw new RuntimeException(e.getMessage());
		}
	}

	@Override
	public SQLXML createSQLXML() {
		try {
			error = null;
			return _connect.createSQLXML();
		} catch (Exception e) {
			error = e;
			throw new RuntimeException(e.getMessage());
		}
	}

	@Override
	public boolean isValid(int paramInt) {
		try {
			error = null;
			return _connect.isValid(paramInt);
		} catch (Exception e) {
			error = e;
			throw new RuntimeException(e.getMessage());
		}
	}

	@Override
	public void setClientInfo(String paramString1, String paramString2) throws SQLClientInfoException {
		try {
			error = null;
			_connect.setClientInfo(paramString1, paramString2);
		} catch (Exception e) {
			error = e;
			throw new RuntimeException(e.getMessage());
		}
	}

	@Override
	public void setClientInfo(Properties paramProperties) throws SQLClientInfoException {
		try {
			error = null;
			_connect.setClientInfo(paramProperties);
		} catch (Exception e) {
			error = e;
			throw new RuntimeException(e.getMessage());
		}
	}

	@Override
	public String getClientInfo(String paramString) {
		try {
			error = null;
			return _connect.getClientInfo(paramString);
		} catch (Exception e) {
			error = e;
			throw new RuntimeException(e.getMessage());
		}
	}

	@Override
	public Properties getClientInfo() {
		try {
			error = null;
			return _connect.getClientInfo();
		} catch (Exception e) {
			error = e;
			throw new RuntimeException(e.getMessage());
		}
	}

	@Override
	public Array createArrayOf(String paramString, Object[] paramArrayOfObject) {
		try {
			error = null;
			return _connect.createArrayOf(paramString, paramArrayOfObject);
		} catch (Exception e) {
			error = e;
			throw new RuntimeException(e.getMessage());
		}
	}

	@Override
	public Struct createStruct(String paramString, Object[] paramArrayOfObject) {
		try {
			error = null;
			return _connect.createStruct(paramString, paramArrayOfObject);
		} catch (Exception e) {
			error = e;
			throw new RuntimeException(e.getMessage());
		}
	}

	@Override
	public void setSchema(String paramString) {
		try {
			error = null;
			_connect.setSchema(paramString);
		} catch (Exception e) {
			error = e;
			throw new RuntimeException(e.getMessage());
		}
	}

	@Override
	public String getSchema() {
		try {
			error = null;
			return _connect.getSchema();
		} catch (Exception e) {
			error = e;
			throw new RuntimeException(e.getMessage());
		}
	}

	@Override
	public void abort(Executor paramExecutor) {
		try {
			error = null;
			_connect.abort(paramExecutor);
		} catch (Exception e) {
			error = e;
			throw new RuntimeException(e.getMessage());
		}
	}

	@Override
	public void setNetworkTimeout(Executor paramExecutor, int paramInt) {
		try {
			error = null;
			_connect.setNetworkTimeout(paramExecutor, paramInt);
		} catch (Exception e) {
			error = e;
			throw new RuntimeException(e.getMessage());
		}
	}

	@Override
	public int getNetworkTimeout() {
		try {
			error = null;
			return _connect.getNetworkTimeout();
		} catch (Exception e) {
			error = e;
			throw new RuntimeException(e.getMessage());
		}
	}
	
	public String getError() {
		return null == error ? null : error.getMessage();
	}
	
	public Exception getDetailError() {
		return error;
	}
	
	public Connection getConnection() {
		return _connect;
	}
}
