package com.geckotechnology.mySqlDataCompare;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import com.geckotechnology.mySqlDataCompare.SchemaDifference.Criticality;
import com.geckotechnology.mySqlDataCompare.SchemaDifference.DifferenceType;

public class MySQLTableDataComparer {
	
	private static final int MAX_DIFFERENCES_PER_TABLE = 100;
	
	private ArrayList<SchemaDifference> dataDifferences = new ArrayList<SchemaDifference>();
	private MySQLSchemaRetriever masterSchemaReader;
	private MySQLSchemaRetriever slaveSchemaReader;
	private int masterTotalRetrievedRows;
	private int slaveTotalRetrievedRows;
	
	public MySQLTableDataComparer(MySQLSchemaRetriever masterSchemaReader, MySQLSchemaRetriever slaveSchemaReader) {
		this.masterSchemaReader = masterSchemaReader;
		this.slaveSchemaReader = slaveSchemaReader;
	}
	
	public int getMasterTotalRetrievedRows() {
		return masterTotalRetrievedRows;
	}

	public int getSlaveTotalRetrievedRows() {
		return slaveTotalRetrievedRows;
	}

	public void compareTable(Table table) throws Exception {
		//note that only 1 SQL statement is used for both tables, even if column or PK columns are not in the same order
		StringBuilder selectSQL = table.createSQLToGetAllRows();
		String pkColumnsTuple = table.createPKColumnsTuple().toString();
		Statement masterStatement = masterSchemaReader.createStatement();
		ResultSet masterResultSet = masterStatement.executeQuery(selectSQL.toString());
		Statement slaveStatement = slaveSchemaReader.createStatement();
		ResultSet slaveResultSet = slaveStatement.executeQuery(selectSQL.toString());
		ArrayList<OneRow> unmatchedMasterRows = new ArrayList<OneRow>();
		ArrayList<OneRow> unmatchedSlaveRows = new ArrayList<OneRow>();
		boolean hasMasterResultSetNext = true;
		boolean hasSlaveResultSetNext = true;
		int dataDifferencesSizeAtStart = dataDifferences.size();
		
		while(hasMasterResultSetNext && hasSlaveResultSetNext) {
			//Get 1 row from master DB
			if(hasMasterResultSetNext && masterResultSet.next()) {
				masterTotalRetrievedRows++;
				processOneRow(table, pkColumnsTuple, true, masterResultSet, unmatchedMasterRows, unmatchedSlaveRows);
			}
			else
				hasMasterResultSetNext = false;
			//Get 1 row from slave DB
			if(hasSlaveResultSetNext && slaveResultSet.next()) {
				slaveTotalRetrievedRows++;
				processOneRow(table, pkColumnsTuple, false, slaveResultSet, unmatchedSlaveRows, unmatchedMasterRows);
			}
			else
				hasSlaveResultSetNext = false;
			//check now there are not too many differences
			if((dataDifferences.size() - dataDifferencesSizeAtStart) > MAX_DIFFERENCES_PER_TABLE) {
				dataDifferences.add(new SchemaDifference(Criticality.ERROR,
						table.getTableName(),
						DifferenceType.DATA_TOO_MANY_DIFFERENCES,
						"max rows:" + MAX_DIFFERENCES_PER_TABLE
						));			
				break;
			}
			if(unmatchedMasterRows.size() > MAX_DIFFERENCES_PER_TABLE ||
					unmatchedSlaveRows.size() > MAX_DIFFERENCES_PER_TABLE) {
				dataDifferences.add(new SchemaDifference(Criticality.ERROR,
						table.getTableName(),
						DifferenceType.DATA_TOO_MANY_UNMATCHED_ROWS,
						"max rows:" + MAX_DIFFERENCES_PER_TABLE
						));
				//clearing the rows as it is not sure they are unmatched
				unmatchedMasterRows.clear();
				unmatchedSlaveRows.clear();
				break;
			}
		}
	
		//rows not found in slave table
		for(OneRow unmatchedMasterOneRow:unmatchedMasterRows) {
			dataDifferences.add(new SchemaDifference(Criticality.ERROR,
					table.getTableName(),
					DifferenceType.DATA_ROW_MISSING_IN_SLAVE_TABLE,
					pkColumnsTuple + "=("+ unmatchedMasterOneRow.getPk() + ")"));			
		}
		//rows not found in master table
		for(OneRow unmatchedSlaveOneRow:unmatchedSlaveRows) {
			dataDifferences.add(new SchemaDifference(Criticality.ERROR,
					table.getTableName(),
					DifferenceType.DATA_ROW_EXCESS_IN_SLAVE_TABLE,
					pkColumnsTuple + "=("+ unmatchedSlaveOneRow.getPk() + ")"));			
		}
		slaveResultSet.close();
		slaveStatement.close();
		masterResultSet.close();
		masterStatement.close();
	}
	
	private void processOneRow(Table table, String pkColumnsTuple, boolean isSourceMaster,
			ResultSet sourceResultSet, ArrayList<OneRow> unmatchedSourceRows, ArrayList<OneRow> unmatchedTargetRows) throws SQLException {
		OneRow sourceOneRow = new OneRow(sourceResultSet.getString(1), sourceResultSet.getString(2));
		int targetRowIndex = unmatchedTargetRows.indexOf(sourceOneRow);
		if(targetRowIndex != -1) {
			//target row matching PK has been found, it can be removed immediately from targetRows
			OneRow targetOneRow = unmatchedTargetRows.remove(targetRowIndex);
			if(!targetOneRow.getMd5().equals(sourceOneRow.getMd5())) {
				//PK are same but the MD5 are different
				dataDifferences.add(new SchemaDifference(Criticality.ERROR,
						table.getTableName(),
						DifferenceType.DATA_ROW_DIFFERENT_MD5,
						pkColumnsTuple + "=("+ sourceOneRow.getPk() + ")"));
			}
			if(targetRowIndex != 0) {
				//we found older records, meaning it was not matched
				for(int i = 0; i<targetRowIndex; i++) {
					OneRow removedTargetOneRaw = unmatchedTargetRows.remove(0);
					dataDifferences.add(new SchemaDifference(Criticality.ERROR,
							table.getTableName(),
							(isSourceMaster ? DifferenceType.DATA_ROW_EXCESS_IN_SLAVE_TABLE : DifferenceType.DATA_ROW_MISSING_IN_SLAVE_TABLE),
							pkColumnsTuple + "=("+ removedTargetOneRaw.getPk() + ")"));
				}
			}
			//remove all source rows if required
			if(unmatchedSourceRows.size() > 0) {
				for(OneRow unmatchedSourceOneRow:unmatchedSourceRows) {
					dataDifferences.add(new SchemaDifference(Criticality.ERROR,
							table.getTableName(),
							(isSourceMaster ? DifferenceType.DATA_ROW_MISSING_IN_SLAVE_TABLE : DifferenceType.DATA_ROW_EXCESS_IN_SLAVE_TABLE),
							pkColumnsTuple + "=("+ unmatchedSourceOneRow.getPk() + ")"));			
				}
				unmatchedSourceRows.clear();
			}
		}
		else {
			//no matching PK, so add to source rows
			unmatchedSourceRows.add(sourceOneRow);
		}		
	}

	public int calculateCriticalityCount(SchemaDifference.Criticality criticality) {
		int count = 0;
		for(SchemaDifference dataDifference:dataDifferences)
			if(dataDifference.getCriticality() == criticality)
				count++;
		return count;
	}
	
	public ArrayList<SchemaDifference> getDataDifferences() {
		return dataDifferences;
	}
	
	public void printDataDifferenceDetails() {
		for(SchemaDifference schemaDifference:dataDifferences) {
			schemaDifference.printDetails();
		}
	}

}
