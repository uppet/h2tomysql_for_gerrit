package com.ucweb.gerrit.tools.converter;

import java.io.File;
import java.sql.ResultSet;

import org.ini4j.Wini;

public class GerritDatabaseConverter {
	public void loadConfig(String oldConf, String newConf) {
		try {
			// make path absolute
			String curDir = System.getProperty("user.dir");
			File oldFile = new File(oldConf);
			File newFile = new File(newConf);
			if (!oldFile.isAbsolute()) {
				oldFile = new File(new File(curDir), oldConf);
			}
			if (!newFile.isAbsolute()) {
				newFile = new File(new File(curDir), newConf);
			}
			String oldBaseDir = new File(oldFile.getParent()).getParent();
			
			Wini oldIni = new Wini(oldFile);
			Wini newIni = new Wini(newFile);
			String oldType = oldIni.get("database", "type");
			String newType = newIni.get("database", "type");
			
			if ("h2".equals(oldType)) {
				String dbPath = oldIni.get("database", "database");
				dbPath = new File(new File(oldBaseDir), dbPath).toString();
				h2Session = createH2DatabaseSession(dbPath);
				h2Session.debugPrintTables();
			}
			
			if ("MYSQL".equals(newType)) {
				String dbName = newIni.get("database", "database");
				String dbUser = newIni.get("database", "username");
				String dbHost = newIni.get("database", "hostname");
				String dbPass = newIni.get("database", "password");
				mysqlSession = createMySQLDatabaseSession(dbName, dbUser, dbHost, dbPass);
				mysqlSession.debugPrintTables();
			}
		} catch (Throwable e) {
			System.out.println("ERROR:Fail when loading gerrit config.");
			e.printStackTrace();
		}
	}
	
	public void transferData() {
		String[] dataTables = new String[] {
				"account_diff_preferences",
				"account_external_ids",
				"account_group_by_id",
				"account_group_by_id_aud",
				"account_group_members",
				"account_group_members_audit",
				"account_group_names",
				"account_groups",
				"account_patch_reviews",
				"account_project_watches",
				"account_ssh_keys",
				"accounts",
				"change_messages",
				"changes",
				"patch_comments",
				"patch_set_ancestors",
				"patch_set_approvals",
				"patch_sets",
				"schema_version",
				"starred_changes",
				"submodule_subscriptions",
				"system_config"
		};
		try {
			for (int i = 0; i < dataTables.length; i++) {
				ResultSet rs = h2Session.getTableContent(dataTables[i]);
				mysqlSession.feedResultsToTable(rs, dataTables[i]);
			}
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}
	
	public void cleanup() {

		if (h2Session != null) {
			h2Session.cleanup();
		}
		if (mysqlSession != null) {
			mysqlSession.cleanup();
		}
	}
	
	private IDatabaseSession createMySQLDatabaseSession(String dbName,
			String dbUser, String dbHost, String dbPass) throws Throwable {
		return new MySQLDatabaseSession(dbName, dbUser, dbHost, dbPass);
	}

	private IDatabaseSession createH2DatabaseSession(String dbPath) throws Throwable {
		return new H2DatabaseSession(dbPath);
	}

	IDatabaseSession h2Session = null;
	IDatabaseSession mysqlSession = null;
}
