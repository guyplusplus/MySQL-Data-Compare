package com.geckotechnology.mySqlDataCompare;

import java.util.ArrayList;

public class Schema {

	private String schemaName;
	private String databaseProductVersion;
	private ArrayList<Table> tablesSortedByTableName = new ArrayList<Table>();

	public ArrayList<Table> getTablesSortedByTableName() {
		return tablesSortedByTableName;
	}

	public String getSchemaName() {
		return schemaName;
	}

	public void setSchemaName(String schemaName) {
		this.schemaName = schemaName;
	}

	public String getDatabaseProductVersion() {
		return databaseProductVersion;
	}

	public void setDatabaseProductVersion(String databaseProductVersion) {
		this.databaseProductVersion = databaseProductVersion;
	}
	
	public void addTable(Table table) {
		tablesSortedByTableName.add(table);
	}
	
	public Table getTableByTableName(String tableName) {
		for(Table table:tablesSortedByTableName) {
			if(table.getTableName().equals(tableName))
				return table;
		}
		return null;
	}
	
	public void printDetails() {
		System.out.println("SCHEMA schemaName:" + schemaName + ", databaseProductVersion:" + databaseProductVersion);
		for(Table table:tablesSortedByTableName) {
			table.printDetails();
		}
	}
}
