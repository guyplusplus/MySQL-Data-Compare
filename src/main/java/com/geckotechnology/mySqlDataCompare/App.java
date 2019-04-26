package com.geckotechnology.mySqlDataCompare;

import java.util.ArrayList;

public class App 
{
    public static void main(String[] args)
    {
		if(args.length < 2) {
			System.out.println("Command: java -jar aaa.jar masterDB.URL slaveDB1.url [slaveDB2.url] [slaveDB3.url]...");
			System.out.println("The tool will compare the master database (schema) with 1 or many slaves databases. It will highlight in particular:");
			System.out.println("- versions are different");
			System.out.println("- tables that are missing on either side");
			System.out.println("- columns that are missing on either side");
			System.out.println("- column types that are different");
			System.out.println("- column types that are different");
			System.out.println("- data that is different");
			System.out.println("Example: java -jar aaa.jar \"jdbc:mysql://localhost:3306/schema_a?user=john&password=pwd\" \"jdbc:mysql://localhost:3306/schema_b?user=paul&password=pwd\"");
			System.exit(0);
		}
		MySQLSchemaRetriever masterSchemaReader = null;
		Schema masterSchema = null;
		try {
			System.out.println("Loading Database Schema Metadata MASTER");
			masterSchemaReader = new MySQLSchemaRetriever(args[0]);
			masterSchemaReader.openConnection();
			masterSchema = masterSchemaReader.retrieveMetaData();
		}
		catch(Exception e) {
			System.err.println("Failed to load Database Schema Metadata MASTER");
			e.printStackTrace();
			System.exit(1);
		}

		int warningCount = 0;
		int errorCount = 0;

		for(int i = 1; i<args.length; i++) {
			MySQLSchemaRetriever slaveSchemaReader = null;
			Schema slaveSchema = null;
			MySQLTableDataComparer tableDataComparer = null;
			long startTime = System.currentTimeMillis();
			try {
				System.out.println();
				System.out.println("Loading Database Schema Metadata Slave #" + i);
				slaveSchemaReader = new MySQLSchemaRetriever(args[i]);
				slaveSchemaReader.openConnection();
				slaveSchema = slaveSchemaReader.retrieveMetaData();
				System.out.println("Comparing Database Schema Metadata MASTER with Slave #" + i);
				MySQLSchemaComparer mySQLSchemaComparer = new MySQLSchemaComparer(masterSchema, slaveSchema);
				mySQLSchemaComparer.compareMetaData();
				mySQLSchemaComparer.printSchemaDifferenceDetails();
				//mySQLSchemaComparer.printTablesForDataAnalysis();
				warningCount += mySQLSchemaComparer.calculateCriticalityCount(SchemaDifference.Criticality.WARNING);
				errorCount += mySQLSchemaComparer.calculateCriticalityCount(SchemaDifference.Criticality.ERROR);
				System.out.println("Comparing Database Schema Data MASTER with Slave #" + i);
				ArrayList<Table> tablesReadyToBeDataAnalyzed = mySQLSchemaComparer.getMasterTablesReadyToBeDataAnalyzed();
				//printing table names to be analyzed
				System.out.print("In-scope tables for comparison: " + tablesReadyToBeDataAnalyzed.size() + ". Table names: ");
				for(Table table:tablesReadyToBeDataAnalyzed) {
					System.out.print(table.getTableName());		
					System.out.print(" ");
				}
				System.out.println();
				//start comparison
				tableDataComparer = new MySQLTableDataComparer(masterSchemaReader, slaveSchemaReader);
				for(Table table:tablesReadyToBeDataAnalyzed)
					tableDataComparer.compareTable(table);
				tableDataComparer.printDataDifferenceDetails();
				warningCount += tableDataComparer.calculateCriticalityCount(SchemaDifference.Criticality.WARNING);
				errorCount += tableDataComparer.calculateCriticalityCount(SchemaDifference.Criticality.ERROR);
			}
			catch(Exception e) {
				System.err.println("Failed to process Slave #" + i);
				e.printStackTrace();
				errorCount++;
			}
			finally {
				if(slaveSchemaReader != null)
					slaveSchemaReader.closeConnection();	
				long endTime = System.currentTimeMillis();
				System.out.println("Comparing Database Schema MASTER with Slave #" + i + " complete");
				System.out.print("Duration(ms): " + (endTime - startTime));
				if(tableDataComparer != null)
					System.out.print(". Total rows retrieved from master: " + tableDataComparer.getMasterTotalRetrievedRows() +
							", slave: " + tableDataComparer.getSlaveTotalRetrievedRows());
				System.out.println();
			}
		}
		
		try {
			if(masterSchemaReader != null)
				masterSchemaReader.closeConnection();			
		}
		catch(Exception e) {
			
		}
		
		System.out.println();
		System.out.println("Job Done. Count Summary warningCount:" + warningCount + ", errorCount:" + errorCount);
		if(errorCount>0)
			System.exit(1);
    }
}
