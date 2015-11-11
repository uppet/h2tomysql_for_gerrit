package com.ucweb.gerrit.tools.converter;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

public class MySQLDatabaseSession implements IDatabaseSession {
	static boolean sDbInited = false;
	Connection conn = null;
	private Object uuidOfAdmin;
	
	public MySQLDatabaseSession(String dbName, String dbUser, String dbHost,
			String dbPass) throws Throwable {
		if (!sDbInited) {
			sDbInited = true;
			Class.forName("com.mysql.jdbc.Driver");
		}
		conn = DriverManager.
			    getConnection(String.format("jdbc:mysql://%s/%s?user=%s&password=%s&useUnicode=true&characterEncoding=UTF-8", dbHost, dbName, dbUser, dbPass));
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
			tables.close();
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
		// get admin uuid
		if (tableName.equals("account_groups")){
			PreparedStatement uuid_stmt = conn
					.prepareStatement("select group_uuid from account_groups where name='Administrators'");
			ResultSet uuid_rs = uuid_stmt.executeQuery();
			uuidOfAdmin = null;
			if (uuid_rs.next()) {
				uuidOfAdmin = uuid_rs.getObject(1);
				System.out.println("Fount UUID of admin is "
						+ uuidOfAdmin.toString());
			}
			uuid_rs.close();
			uuid_stmt.close();
		}
		
		// clear old data
		if (!tableName.matches("[a-zA-Z_]*"))
			return;
		PreparedStatement clear_stmt = conn.prepareStatement("delete from " + tableName.toLowerCase());
		clear_stmt.execute();
		clear_stmt.close();
		// get correct column names;
		int columnCount = resultSet.getMetaData().getColumnCount();
		ArrayList<String> cols = new ArrayList<String>();
		ArrayList<String> vals = new ArrayList<String>();
		for (int i = 0; i < columnCount ; i++) {
			cols.add(resultSet.getMetaData().getColumnLabel(i+1).toLowerCase());
			vals.add("?");
		}
		System.out.println("  Feeding data into " + tableName.toLowerCase());
		String sql = String.format("insert into %s (%s) values (%s)", tableName.toLowerCase()
				, StringUtils.join(cols, ","), Util.join(vals, ","));
		PreparedStatement stmt = conn.prepareStatement(sql);
		int batchCount = 0;
		while (resultSet.next()) {
			for (int i = 0; i < columnCount ; i++) {
				stmt.setObject(i+1, resultSet.getObject(i+1));
			}
			batchCount++;
			//stmt.execute();
			stmt.addBatch();
			
			if (batchCount > 100) {
				stmt.executeBatch();
				batchCount = 0;
			}
		}
		stmt.executeBatch();
		stmt.close();
		resultSet.close();
	}

	@Override
	public void fixIncrement() throws SQLException {		
		// fix increments
		Map<String, String[]> incrementGroupsActions = new HashMap<String,String[]>();
		//incrementGroupsActions.put("account_group_id", new String[] {"account_groups","max(group_id) + 1"});
		incrementGroupsActions.put("account_id", new String [] { "accounts", "max(account_id) + 1" });
		incrementGroupsActions.put("change_id", new String [] { "changes", "max(change_id) + 1" });
		incrementGroupsActions.put("change_message_id", new String [] { "change_messages", "count(*) + 1" });
		
		Iterator<Map.Entry<String, String[]>> entries = incrementGroupsActions.entrySet().iterator();
		while (entries.hasNext()) {
		    Map.Entry<String, String[]> entry = entries.next();
		    PreparedStatement clear_action_stmt = conn.prepareStatement("delete from " + entry.getKey());
		    clear_action_stmt.execute();
			
		    System.out.println(" Fixing increment into " + entry.getKey());
		    PreparedStatement action_stmt = conn.prepareStatement(
		    		String.format("insert into %s select %s from %s"
		    				, entry.getKey()
		    				, entry.getValue()[1]
		    				, entry.getValue()[0]));
		    action_stmt.execute();
		    action_stmt.close();
		}
		
		// special fix for account_group_id
		System.out.println(" Special fix for account_group_id");
		Statement fix_action = conn.createStatement();
		fix_action.execute("delete from account_group_id");
		fix_action.execute("insert into account_group_id select group_id from account_group_names");
		
		System.out.println(" Special fix for account_id");
		fix_action.execute("delete from account_id");
		fix_action.execute("insert into account_id select account_id from accounts");
		
		if (uuidOfAdmin != null) {
			System.out.println(" Special fix for uuid of admin");
			fix_action
					.execute(String
							.format("update account_groups set group_uuid='%s', owner_group_uuid='%s' where name='Administrators'",
									uuidOfAdmin, uuidOfAdmin));
			fix_action
				.execute(String
						.format("update account_groups set owner_group_uuid='%s'",
								uuidOfAdmin));
		}
		fix_action.close();
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
