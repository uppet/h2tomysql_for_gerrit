package com.ucweb.gerrit.tools.converter;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class MySQLDatabaseSession implements IDatabaseSession {
	static boolean sDbInited = false;
	Connection conn = null;
	
	public MySQLDatabaseSession(String dbName, String dbUser, String dbHost,
			String dbPass) throws Throwable {
		if (!sDbInited) {
			sDbInited = true;
			Class.forName("com.mysql.jdbc.Driver");
		}
		conn = DriverManager.
			    getConnection(String.format("jdbc:mysql://%s/%s?user=%s&password=%s", dbHost, dbName, dbUser, dbPass));
	}

	@Override
	public void debugPrintTables() {
		if (conn == null)
			return;
		try {
			DatabaseMetaData m = conn.getMetaData();
			ResultSet tables = m.getTables(conn.getCatalog(), null, "%", null);
			while (tables.next()) {
				System.out.println("table = " + tables.getString(3));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	@Override
	public ResultSet getTableContent(String tableName)  throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void feedResultsToTable(ResultSet resultSet, String tableName)  throws SQLException {
		// clear old data
		if (!tableName.matches("[a-zA-Z_]*"))
			return;
		PreparedStatement clear_stmt = conn.prepareStatement("delete from " + tableName.toLowerCase());
		clear_stmt.execute();
		// get correct column names;
		int columnCount = resultSet.getMetaData().getColumnCount();
		ArrayList<String> cols = new ArrayList<String>();
		ArrayList<String> vals = new ArrayList<String>();
		for (int i = 0; i < columnCount ; i++) {
			cols.add(resultSet.getMetaData().getColumnLabel(i+1).toLowerCase());
			vals.add("?");
		}
		String sql = String.format("insert into %s (%s) values (%s)", tableName.toLowerCase()
				, Util.join(cols, ","), Util.join(vals, ","));
		PreparedStatement stmt = conn.prepareStatement(sql);
		while (resultSet.next()) {
			for (int i = 0; i < columnCount ; i++) {
				stmt.setObject(i+1, resultSet.getObject(i+1));
			}
			stmt.execute();
		}
		
		// fix increments
//		String incrementGroups = new String
//                "ACCOUNT_GROUP_ID" => ["ACCOUNT_GROUPS",
//                                       "MAX(GROUP_ID)"],
//                "ACCOUNT_ID" => ["ACCOUNTS",
//                                 "MAX(ACCOUNT_ID)"],
//                "CHANGE_ID" => ["CHANGES",
//                                "MAX(CHANGE_ID)"],
//                "CHANGE_MESSAGE_ID" => ["CHANGE_MESSAGES",
//                                        "COUNT(*)"]
//            );
	}

	@Override
	public void cleanup() {
		if (conn != null) {
			try {
				conn.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}
}
