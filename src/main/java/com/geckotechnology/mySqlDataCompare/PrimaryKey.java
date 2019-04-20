package com.geckotechnology.mySqlDataCompare;

import java.util.ArrayList;

/**
 * select * from information_schema.key_column_usage where constraint_name = 'PRIMARY';
 * https://dev.mysql.com/doc/refman/8.0/en/key-column-usage-table.html
 * @author 293225
 *
 */
public class PrimaryKey {

	private ArrayList<Column> columnsSortedByPrimaryKeyOrdinalPosition = new ArrayList<Column>();

	public ArrayList<Column> getColumnsSortedByPrimaryKeyOrdinalPosition() {
		return columnsSortedByPrimaryKeyOrdinalPosition;
	}
	
	public void addColumn(Column column) {
		columnsSortedByPrimaryKeyOrdinalPosition.add(column);
	}

	public void printDetails() {
		System.out.println("    PK Start----");
		for(Column column:columnsSortedByPrimaryKeyOrdinalPosition) {
			column.printDetails();
		}
		System.out.println("    PK End----");
	}
}
