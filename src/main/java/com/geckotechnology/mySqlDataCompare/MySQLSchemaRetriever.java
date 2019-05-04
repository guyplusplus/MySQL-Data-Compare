package com.geckotechnology.mySqlDataCompare;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Properties;

public class MySQLSchemaRetriever {
	
	private static final int SELECT_FETCH_SIZE = 1024;
	private static final String SQL_SELECT_SCHEMA = "select schema()";
	private static final String SQL_SELECT_ALL_TABLES = "select columns.table_name, columns.column_name, columns.column_type, columns.ordinal_position, columns.column_key, columns.is_nullable from information_schema.columns, information_schema.tables where tables.table_schema=schema() and columns.table_name = tables.table_name and columns.table_schema = tables.table_schema and tables.table_type='BASE TABLE' order by columns.table_name, columns.column_name";
	private static final String SQL_SELECT_ALL_PKS = "select key_column_usage.table_name, key_column_usage.column_name, key_column_usage.ordinal_position from information_schema.key_column_usage where key_column_usage.table_schema=schema() and key_column_usage.constraint_name = 'PRIMARY' order by key_column_usage.table_name, key_column_usage.ordinal_position";
	
	private String url;
	private Connection conn;
	
	public MySQLSchemaRetriever(String url) {
		this.url = url;
	}
	
	public void openConnection() throws Exception {
		Properties info = new Properties();
		info.put ("defaultFetchSize", Integer.toString(SELECT_FETCH_SIZE));
		info.put ("useCursorFetch", "true");
		conn = DriverManager.getConnection(url, info);
		conn.setAutoCommit(true);
	}
	
	public void closeConnection() {
		try {
			conn.close();
		} catch (Exception e) {
			//do nothing
		}
	}
	
	public Statement createStatement() throws Exception {
		Statement stmt = conn.createStatement(
	    		ResultSet.TYPE_FORWARD_ONLY,
	    	    ResultSet.CONCUR_READ_ONLY,
	    	    ResultSet.CLOSE_CURSORS_AT_COMMIT);
	    return stmt;
	}
	
	public Schema retrieveMetaData() throws Exception {
		Schema schema = new Schema();
		schema.setDatabaseProductVersion(conn.getMetaData().getDatabaseProductVersion());
		
		Statement stmt = null;
		ResultSet rs = null;
	    //-----------------------------------------------------------
		//check that current schema is not null
	    stmt = createStatement();
	    rs = stmt.executeQuery(SQL_SELECT_SCHEMA);
	    while(rs.next()) {
	    	schema.setSchemaName(rs.getString(1));
	    	if(schema.getSchemaName() == null) {
	    		System.err.println("You must specify the schema in the URL");
	    		throw new Exception("Schema not defined in URL");
	    	}
	    }
	    rs.close();
	    stmt.close();
	    //-----------------------------------------------------------
		//get all tables
	    stmt = createStatement();
	    rs = stmt.executeQuery(SQL_SELECT_ALL_TABLES);
	    String currentTableName = null;
	    Table currentTable = null;
	    while(rs.next()) {
	    	String tableName = rs.getString(1);
	    	if(!tableName.equals(currentTableName)) {
	    		if(currentTable != null)
	    			schema.addTable(currentTable);
	    		currentTableName = tableName;
	    		currentTable = new Table();
	    		currentTable.setTableName(tableName);
	    	}
	    	Column column = new Column();
	    	column.setColumnName(rs.getString(2));
	    	column.setColumnType(rs.getString(3));
	    	column.setOrdinalPosition(rs.getInt(4));
	    	column.setPrimaryKey("PRI".equals(rs.getString(5)));
	    	column.setNullable("YES".equals(rs.getString(6)));
	    	currentTable.addColumn(column);
	    }
		if(currentTable != null)
			schema.addTable(currentTable);
	    rs.close();
	    stmt.close();
	    //-----------------------------------------------------------
		//get all PKs
	    stmt = createStatement();
	    rs = stmt.executeQuery(SQL_SELECT_ALL_PKS);
	    currentTable = null;
	    while(rs.next()) {
	    	String tableName = rs.getString(1);
	    	if(currentTable == null || !tableName.equals(currentTable.getTableName())) {
	    		currentTable = schema.getTableByTableName(tableName);
	    		currentTable.setPrimaryKey(new PrimaryKey());
	    	}
	    	Column column = currentTable.getColumnByColumnName(rs.getString(2));
	    	if(!column.isPrimaryKey())
	    		throw new Exception("Column is PRI but not primary key");
	    	column.setPrimaryKeyOrdinalPosition(rs.getInt(3));
	    	currentTable.getPrimaryKey().addColumn(column);
	    }
	    rs.close();
	    stmt.close();	    
	    //-----------------------------------------------------------
	    //debug
	    //schema.printDetails();
	    //-----------------------------------------------------------
	    //job done
	    return schema;
	}

}
