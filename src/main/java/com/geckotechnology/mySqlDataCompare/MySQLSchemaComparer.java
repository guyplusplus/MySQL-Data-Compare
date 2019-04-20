package com.geckotechnology.mySqlDataCompare;

import java.util.ArrayList;

public class MySQLSchemaComparer {
	
	private ArrayList<SchemaDifference> schemaDifferences = new ArrayList<SchemaDifference>();
	private Schema masterSchema;
	private Schema slaveSchema;
	private ArrayList<String> masterOnlyTableNames;
	private ArrayList<String> slaveOnlyTableNames;
	private ArrayList<String> sharedTableNames;
	
	public MySQLSchemaComparer(Schema masterSchema, Schema slaveSchema) {
		this.masterSchema = masterSchema;
		this.slaveSchema = slaveSchema;
	}
	
	public int calculateDifferencesCount(SchemaDifference.DifferenceType differenceType) {
		int count = 0;
		for(SchemaDifference schemaDifference:schemaDifferences)
			if(schemaDifference.getDifferenceType() == differenceType)
				count++;
		return count;
	}
	
	public int calculateCriticalityCount(SchemaDifference.Criticality criticality) {
		int count = 0;
		for(SchemaDifference schemaDifference:schemaDifferences)
			if(schemaDifference.getCriticality() == criticality)
				count++;
		return count;
	}
	
	public void compareMetaData() {
		compareSchemas();
		compareTables();		
		compareColumns();
	}
	
	private void compareSchemas() {
		if(!masterSchema.getDatabaseProductVersion().equals(slaveSchema.getDatabaseProductVersion())) {
			schemaDifferences.add(new SchemaDifference(
					SchemaDifference.Criticality.WARNING,
					"",
					SchemaDifference.DifferenceType.INSTANCE_DIFFERENT_VERSION,
					"master:" + masterSchema.getDatabaseProductVersion() + " v.s. slave:" + slaveSchema.getDatabaseProductVersion()));
		}		
	}
	
	private void compareTables() {
		masterOnlyTableNames = new ArrayList<String>();
		slaveOnlyTableNames = new ArrayList<String>();
		sharedTableNames = new ArrayList<String>();
		for(Table masterTable:masterSchema.getTablesSortedByTableName())
			masterOnlyTableNames.add(masterTable.getTableName());
		for(Table slaveTable:slaveSchema.getTablesSortedByTableName()) {
			if(masterOnlyTableNames.contains(slaveTable.getTableName())) {
				masterOnlyTableNames.remove(slaveTable.getTableName());
				sharedTableNames.add(slaveTable.getTableName());
			}
			else
				slaveOnlyTableNames.add(slaveTable.getTableName());
		}
		for(String masterOnlyTableName:masterOnlyTableNames) {
			schemaDifferences.add(new SchemaDifference(
					SchemaDifference.Criticality.ERROR,
					masterOnlyTableName,
					SchemaDifference.DifferenceType.TABLE_MISSING_IN_SLAVE_SCHEMA));
			masterSchema.getTableByTableName(masterOnlyTableName).disableDataAnalysis();
		}
		for(String slaveOnlyTableName:slaveOnlyTableNames) {
			schemaDifferences.add(new SchemaDifference(
					SchemaDifference.Criticality.ERROR,
					slaveOnlyTableName,
					SchemaDifference.DifferenceType.TABLE_EXCESS_IN_SLAVE_SCHEMA));
			slaveSchema.getTableByTableName(slaveOnlyTableName).disableDataAnalysis();
		}
	}
	
	private void compareColumns() {
		for(String sharedTableName:sharedTableNames) {
			Table masterTable = masterSchema.getTableByTableName(sharedTableName);
			Table slaveTable = slaveSchema.getTableByTableName(sharedTableName);
			//-------------------
			//for each table compare the columns are on both sides
			ArrayList<String> masterOnlyColumnNames = new ArrayList<String>();
			ArrayList<String> slaveOnlyColumnNames = new ArrayList<String>();
			ArrayList<String> sharedColumnNames = new ArrayList<String>();
			for(Column masterColumn:masterTable.getColumnsSortedByColumnName())
				masterOnlyColumnNames.add(masterColumn.getColumnName());
			for(Column slaveColumn:slaveTable.getColumnsSortedByColumnName()) {
				if(masterOnlyColumnNames.contains(slaveColumn.getColumnName())) {
					masterOnlyColumnNames.remove(slaveColumn.getColumnName());
					sharedColumnNames.add(slaveColumn.getColumnName());
				}
				else
					slaveOnlyColumnNames.add(slaveColumn.getColumnName());
			}
			for(String masterOnlyColumnName:masterOnlyColumnNames) {
				schemaDifferences.add(new SchemaDifference(
						SchemaDifference.Criticality.ERROR,
						masterTable.getTableName() + "." + masterOnlyColumnName,
						SchemaDifference.DifferenceType.COLUMN_MISSING_IN_SLAVE_TABLE));
				masterTable.disableDataAnalysis();
				slaveTable.disableDataAnalysis();
			}
			for(String slaveOnlyColumnName:slaveOnlyColumnNames) {
				schemaDifferences.add(new SchemaDifference(
						SchemaDifference.Criticality.ERROR,
						slaveTable.getTableName() + "." + slaveOnlyColumnName,
						SchemaDifference.DifferenceType.COLUMN_EXCESS_IN_SLAVE_TABLE));
				masterTable.disableDataAnalysis();
				slaveTable.disableDataAnalysis();
			}
			//-------------------
			//for each table each shared columns check if there is PK
			if(!slaveTable.hasPrimaryKey()) {
				schemaDifferences.add(new SchemaDifference(
						SchemaDifference.Criticality.ERROR,
						slaveTable.getTableName(),
						SchemaDifference.DifferenceType.NO_PRIMARY_KEY));
				masterTable.disableDataAnalysis();
				slaveTable.disableDataAnalysis();
			}
			//-------------------
			//for each table each shared columns compare types and order
			for(String sharedColumnName:sharedColumnNames) {
				Column masterColumn = masterTable.getColumnByColumnName(sharedColumnName);
				Column slaveColumn = slaveTable.getColumnByColumnName(sharedColumnName);
				if(!masterColumn.getColumnType().equals(slaveColumn.getColumnType()))
					schemaDifferences.add(new SchemaDifference(
							SchemaDifference.Criticality.WARNING,
							slaveTable.getTableName() + "." + slaveColumn.getColumnName(),
							SchemaDifference.DifferenceType.COLUMN_DIFFERENT_TYPE,
							"master:" + masterColumn.getColumnType() + " v.s. slave:" + slaveColumn.getColumnType()));
				if(masterColumn.isNullable() != slaveColumn.isNullable())
					schemaDifferences.add(new SchemaDifference(
							SchemaDifference.Criticality.WARNING,
							slaveTable.getTableName() + "." + slaveColumn.getColumnName(),
							SchemaDifference.DifferenceType.COLUMN_DIFFERENT_IS_NULLABLE,
							"master:" + masterColumn.isNullable() + " v.s. slave:" + slaveColumn.isNullable()));
				if(masterColumn.getOrdinalPosition() != slaveColumn.getOrdinalPosition())
					schemaDifferences.add(new SchemaDifference(
							SchemaDifference.Criticality.WARNING,
							slaveTable.getTableName() + "." + slaveColumn.getColumnName(),
							SchemaDifference.DifferenceType.COLUMN_DIFFERENT_ORDINAL_POSITION,
							"master:" + masterColumn.getOrdinalPosition() + " v.s. slave:" + slaveColumn.getOrdinalPosition()));
				if(masterColumn.isPrimaryKey() != slaveColumn.isPrimaryKey()) {
					schemaDifferences.add(new SchemaDifference(
							SchemaDifference.Criticality.ERROR,
							slaveTable.getTableName() + "." + slaveColumn.getColumnName(),
							SchemaDifference.DifferenceType.COLUMN_DIFFERENT_PRIMARY_KEY,
							"master:" + masterColumn.isPrimaryKey() + " v.s. slave:" + slaveColumn.isPrimaryKey()));
					masterTable.disableDataAnalysis();
					slaveTable.disableDataAnalysis();
				}
				else if(masterColumn.isPrimaryKey() &&
						masterColumn.getPrimaryKeyOrdinalPosition() != slaveColumn.getPrimaryKeyOrdinalPosition()) {
					schemaDifferences.add(new SchemaDifference(
							SchemaDifference.Criticality.ERROR,
							slaveTable.getTableName() + "." + slaveColumn.getColumnName(),
							SchemaDifference.DifferenceType.COLUMN_DIFFERENT_PRIMARY_KEY_ORDINAL_POSITION,
							"master:" + masterColumn.getPrimaryKeyOrdinalPosition() + " v.s. slave:" + slaveColumn.getPrimaryKeyOrdinalPosition()));
					masterTable.disableDataAnalysis();
					slaveTable.disableDataAnalysis();
				}
			}
		}
	}
	
	public ArrayList<Table> getTablesReadyToBeDataAnalyzed() {
		ArrayList<Table> tables = new ArrayList<Table>();
		for(Table masterTable:masterSchema.getTablesSortedByTableName()) {
			if(masterTable.isReadyToBeDataAnalyzed()) {
				tables.add(masterTable);
			}
		}
		return tables;
	}

	public ArrayList<SchemaDifference> getSchemaDifferences() {
		return schemaDifferences;
	}
	
	public void printTablesForDataAnalysis() {
		System.out.println("Tables ready for data analysis:");
		for(Table masterTable:masterSchema.getTablesSortedByTableName()) {
			if(masterTable.isReadyToBeDataAnalyzed()) {
				System.out.println("  " + masterTable.getTableName());
				masterTable.getPrimaryKey().printDetails();
				System.out.println(masterTable.createSQLToGetAllRows());
			}
		}		
	}
	
	public void printSchemaDifferenceDetails() {
		for(SchemaDifference schemaDifference:schemaDifferences) {
			schemaDifference.printDetails();
		}
	}

}
