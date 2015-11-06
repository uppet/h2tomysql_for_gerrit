package com.ucweb.gerrit.tools.converter;

public class H2ToMySQLLauncher {

	public static void main(String[] args) {
		if (args.length != 2) {
			printUsage();
			System.exit(-1);
		}
		System.out.println("H2 to MySQL started");
		GerritDatabaseConverter converter = new GerritDatabaseConverter();
		try {
			converter.loadConfig(args[0], args[1]);
			converter.transferData();
		} finally {
			converter.cleanup();
		}
	}

	private static void printUsage() {
		System.out.println("H2 to MySQL database converter for gerrit.\n"
				+ "Usage:\n" 
				+ "Program old_gerrit_config_path new_gerrit_config_path\n"
				+ "Note: program will try to infer gerrit site instance from config file path");
	}

}
