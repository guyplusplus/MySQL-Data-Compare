package com.geckotechnology.mySqlDataCompare;

import java.util.ArrayList;

/**
 * select * from information_schema.tables where table_type='BASE TABLE';
 * https://dev.mysql.com/doc/refman/8.0/en/tables-table.html
 * @author 293225
 *
 */
public class Table {
	
	private static final String COLUMN_SEPARATOR = ",";
	private static final String NULL_VALUE = "nU1L";

	private ArrayList<Column> columnsSortedByColumnName = new ArrayList<Column>();
	private String tableName;
	private PrimaryKey primaryKey;
	private boolean readyToBeDataAnalyzed = true;
	
	public boolean hasPrimaryKey() {
		if(primaryKey == null)
			return false;
		if(primaryKey.getColumnsSortedByPrimaryKeyOrdinalPosition().size() == 0)
			return false;
		return true;
	}

	public boolean isReadyToBeDataAnalyzed() {
		return readyToBeDataAnalyzed;
	}

	public void disableDataAnalysis() {
		this.readyToBeDataAnalyzed = false;
	}

	public PrimaryKey getPrimaryKey() {
		return primaryKey;
	}

	public void setPrimaryKey(PrimaryKey primaryKey) {
		this.primaryKey = primaryKey;
	}

	public ArrayList<Column> getColumnsSortedByColumnName() {
		return columnsSortedByColumnName;
	}

	public String getTableName() {
		return tableName;
	}

	public void setTableName(String tableName) {
		this.tableName = tableName;
	}
	
	public void addColumn(Column column) {
		columnsSortedByColumnName.add(column);
	}
	
	public Column getColumnByColumnName(String columnName) {
		for(Column column:columnsSortedByColumnName) {
			if(column.getColumnName().equals(columnName))
				return column;
		}
		return null;
	}

	public void printDetails() {
		System.out.println("TABLE tableName:" + tableName);
		for(Column column:columnsSortedByColumnName) {
			column.printDetails();
		}
	}
	
	/**
	 * 
	 * @return for example (id), or (col1, col2)
	 */
	public StringBuilder createPKColumnsTuple() {
		return buildSequence(primaryKey.getColumnsSortedByPrimaryKeyOrdinalPosition(),
				"(", ")",
				"", "",
				COLUMN_SEPARATOR);
	}
	
	public StringBuilder createSQLToGetAllRows() {
		StringBuilder sb = new StringBuilder();
		sb.append("select ");
		sb.append(buildSequence(primaryKey.getColumnsSortedByPrimaryKeyOrdinalPosition(),
				"concat(", ")",
				"", "", // no need coalesce since it is PK
				", '" + COLUMN_SEPARATOR + "', "));
		sb.append(" PK ,");
		sb.append(buildSequence(getColumnsSortedByColumnName(),
				"md5(concat(", "))",
				"coalesce(", ",'" + NULL_VALUE + "')",
				", '" + COLUMN_SEPARATOR + "', "));
		sb.append(" MD5 from ");
		sb.append(getTableName());
		sb.append(" order by ");
		sb.append(buildSequence(primaryKey.getColumnsSortedByPrimaryKeyOrdinalPosition(),
				"", "",
				"", "",
				", ")); //column is mandated by SQL		
		sb.append(";");
		return sb;
	}
	
	private static StringBuilder buildSequence(ArrayList<Column> columns,
			String globalPrefix, String globalSuffix,
			String columnPrefix, String columnSuffix,
			String separatorAtferFirstColumn) {
		StringBuilder sb = new StringBuilder();
		sb.append(globalPrefix);
		boolean isFirstColumn = true;
		for(Column column:columns) {
			if(!isFirstColumn)
				sb.append(separatorAtferFirstColumn);
			sb.append(columnPrefix);
			sb.append(column.getColumnName());
			sb.append(columnSuffix);
			isFirstColumn = false;
		}
		sb.append(globalSuffix);
		return sb;
	}

}
