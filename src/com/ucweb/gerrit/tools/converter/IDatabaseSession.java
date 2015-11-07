package com.ucweb.gerrit.tools.converter;

import java.sql.ResultSet;
import java.sql.SQLException;

public interface IDatabaseSession {
	void debugPrintTables();

	void cleanup();
	
	ResultSet getTableContent(String tableName)  throws SQLException;
	
	void feedResultsToTable(ResultSet resultSet, String tableName)  throws SQLException;
	
	void fixIncrement() throws SQLException;
}
