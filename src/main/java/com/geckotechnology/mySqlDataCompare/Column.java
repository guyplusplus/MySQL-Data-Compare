package com.geckotechnology.mySqlDataCompare;

/**
 * select * from information_schema.columns;
 * https://dev.mysql.com/doc/refman/8.0/en/columns-table.html
 * @author 293225
 *
 */
public class Column {

	private String columnName;
	private String columnType; //i.e. varchar(10)
	private int ordinalPosition; //start at 1
	private int primaryKeyOrdinalPosition; //start at 1
	private boolean primaryKey;
	private boolean isNullable;

	public boolean isNullable() {
		return isNullable;
	}
	public void setNullable(boolean isNullable) {
		this.isNullable = isNullable;
	}
	public String getColumnName() {
		return columnName;
	}
	public int getOrdinalPosition() {
		return ordinalPosition;
	}
	public String getColumnType() {
		return columnType;
	}
	public boolean isPrimaryKey() {
		return primaryKey;
	}
	public int getPrimaryKeyOrdinalPosition() {
		return primaryKeyOrdinalPosition;
	}
	public void setColumnName(String columnName) {
		this.columnName = columnName;
	}
	public void setColumnType(String columnType) {
		this.columnType = columnType;
	}
	public void setOrdinalPosition(int ordinalPosition) {
		this.ordinalPosition = ordinalPosition;
	}
	public void setPrimaryKeyOrdinalPosition(int primaryKeyOrdinalPosition) {
		this.primaryKeyOrdinalPosition = primaryKeyOrdinalPosition;
	}
	public void setPrimaryKey(boolean primaryKey) {
		this.primaryKey = primaryKey;
	}

	
	public void printDetails() {
		System.out.println("      COLUMN columnName:" + columnName +
				", columnType:" + columnType + 
				", ordinalPosition:" + ordinalPosition + 
				", primaryKeyOrdinalPosition:" + primaryKeyOrdinalPosition + 
				", primaryKey:" + primaryKey +
				", isNullable:" + isNullable
				);
	}
}
