package com.geckotechnology.mySqlDataCompare;

import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;

import com.geckotechnology.mySqlDataCompare.SchemaDifference.Criticality;
import com.geckotechnology.mySqlDataCompare.SchemaDifference.DifferenceType;

public class MySQLTableDataComparer {
	
	private ArrayList<SchemaDifference> dataDifferences = new ArrayList<SchemaDifference>();
	private MySQLSchemaRetriever masterSchemaReader;
	private MySQLSchemaRetriever slaveSchemaReader;
	
	public MySQLTableDataComparer(MySQLSchemaRetriever masterSchemaReader, MySQLSchemaRetriever slaveSchemaReader) {
		this.masterSchemaReader = masterSchemaReader;
		this.slaveSchemaReader = slaveSchemaReader;
	}
	
	public void compareTable(Table table) throws Exception {
		StringBuilder selectSQL = table.createSQLToGetAllRows();
		String pkTupple = table.createPKTupple().toString();
		Statement masterStatement = masterSchemaReader.getStatement();
		ResultSet masterResultSet = masterStatement.executeQuery(selectSQL.toString());
		Statement slaveStatement = slaveSchemaReader.getStatement();
		ResultSet slaveResultSet = slaveStatement.executeQuery(selectSQL.toString());
		ArrayList<OneRow> masterRows = new ArrayList<OneRow>();
		ArrayList<OneRow> slaveRows = new ArrayList<OneRow>();
		boolean hasMasterResultSetNext = true;
		boolean hasSlaveResultSetNext = true;
		while(hasMasterResultSetNext && hasSlaveResultSetNext) {
			
			//Get 1 row from master DB
			if(hasMasterResultSetNext && masterResultSet.next()) {
				OneRow masterOneRow = new OneRow(masterResultSet.getString(1), masterResultSet.getString(2));
				int slaveRowIndex = slaveRows.indexOf(masterOneRow);
				if(slaveRowIndex != -1) {
					//slave row matching PK has been found
					OneRow slaveOneRow = slaveRows.get(slaveRowIndex);
					if(!slaveOneRow.getMd5().equals(masterOneRow.getMd5())) {
						//PK are same but the MD5 are different
						dataDifferences.add(new SchemaDifference(Criticality.ERROR,
								table.getTableName(),
								DifferenceType.DATA_ROW_DIFFERENT_MD5,
								pkTupple + "=("+ masterOneRow.getPk() + ")"));
					}
					slaveRows.remove(slaveRowIndex);
					if(slaveRowIndex != 0) {
						//we found older records, meaning it was not matched
						for(int i = 0; i<slaveRowIndex; i++) {
							OneRow removedSlaveOneRaw = slaveRows.remove(0);
							dataDifferences.add(new SchemaDifference(Criticality.ERROR,
									table.getTableName(),
									DifferenceType.DATA_ROW_IN_EXCESS,
									pkTupple + "=("+ removedSlaveOneRaw.getPk() + ")"));
						}
					}
				}
				else {
					//no matching PK, so add to masterRows
					masterRows.add(masterOneRow);
				}
			}
			else
				hasMasterResultSetNext = false;
			
			//Get 1 row from slave DB
			if(hasSlaveResultSetNext && slaveResultSet.next()) {
				OneRow slaveOneRow = new OneRow(slaveResultSet.getString(1), slaveResultSet.getString(2));
				int masterRowIndex = masterRows.indexOf(slaveOneRow);
				if(masterRowIndex != -1) {
					//slave row matching PK has been found
					OneRow masterOneRow = masterRows.get(masterRowIndex);
					if(!slaveOneRow.getMd5().equals(masterOneRow.getMd5())) {
						//PK are same but the MD5 are different
						dataDifferences.add(new SchemaDifference(Criticality.ERROR,
								table.getTableName(),
								DifferenceType.DATA_ROW_DIFFERENT_MD5,
								pkTupple + "=("+ masterOneRow.getPk() + ")"));
					}
					masterRows.remove(masterRowIndex);
					if(masterRowIndex != 0) {
						//we found older records, meaning it was not matched
						for(int i = 0; i<masterRowIndex; i++) {
							OneRow removedMasterOneRaw = masterRows.remove(0);
							dataDifferences.add(new SchemaDifference(Criticality.ERROR,
									table.getTableName(),
									DifferenceType.DATA_MISSING_ROW,
									pkTupple + "=("+ removedMasterOneRaw.getPk() + ")"));
						}
					}
				}
				else {
					//no matching PK, so add to slaveRows
					slaveRows.add(slaveOneRow);
				}
			}
			else
				hasSlaveResultSetNext = false;

		}
	
		for(OneRow masterOneRow:masterRows) {
			dataDifferences.add(new SchemaDifference(Criticality.ERROR,
					table.getTableName(),
					DifferenceType.DATA_MISSING_ROW,
					pkTupple + "=("+ masterOneRow.getPk() + ")"));			
		}
		for(OneRow slaveOneRow:slaveRows) {
			dataDifferences.add(new SchemaDifference(Criticality.ERROR,
					table.getTableName(),
					DifferenceType.DATA_ROW_IN_EXCESS,
					pkTupple + "=("+ slaveOneRow.getPk() + ")"));			
		}
		slaveResultSet.close();
		slaveStatement.close();
		masterResultSet.close();
		masterStatement.close();		
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
