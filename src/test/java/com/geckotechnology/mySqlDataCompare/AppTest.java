package com.geckotechnology.mySqlDataCompare;

import java.util.ArrayList;

import com.geckotechnology.mySqlDataCompare.SchemaDifference.Criticality;
import com.geckotechnology.mySqlDataCompare.SchemaDifference.DifferenceType;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class AppTest extends TestCase
{
	private static final String MASTER_URL = "jdbc:mysql://localhost:3306/schema_a?user=usertestA&password=password";
	private static final String SLAVE_URL = "jdbc:mysql://localhost:3306/schema_b?user=usertestB&password=password";

	private MySQLSchemaRetriever masterSchemaReader = null;
	private Schema masterSchema = null;
	private MySQLSchemaRetriever slaveSchemaReader = null;
	private Schema slaveSchema = null;

    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public AppTest( String testName )
    {
        super( testName );
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite()
    {
        return new TestSuite( AppTest.class );
    }
    
    protected void setUp() throws Exception {
		masterSchemaReader = new MySQLSchemaRetriever(MASTER_URL);
		masterSchemaReader.openConnection();
		masterSchema = masterSchemaReader.retrieveMetaData();
		slaveSchemaReader = new MySQLSchemaRetriever(SLAVE_URL);
		slaveSchemaReader.openConnection();
		slaveSchema = slaveSchemaReader.retrieveMetaData();
    }

    /**
     * @throws Exception 
     */
    public void testCompareSchemaAndData() throws Exception
    {
		MySQLSchemaComparer mySQLSchemaComparer = new MySQLSchemaComparer(masterSchema, slaveSchema);
		mySQLSchemaComparer.compareMetaData();
		ArrayList<SchemaDifference> schemaDifferences = mySQLSchemaComparer.getSchemaDifferences();
		assertTrue(schemaDifferences.indexOf(new SchemaDifference(Criticality.ERROR, "onlyina", DifferenceType.TABLE_MISSING_IN_SLAVE_SCHEMA)) >= 0);
		assertTrue(schemaDifferences.indexOf(new SchemaDifference(Criticality.ERROR, "onlyinb", DifferenceType.TABLE_EXCESS_IN_SLAVE_SCHEMA)) >= 0);
		assertTrue(schemaDifferences.indexOf(new SchemaDifference(Criticality.ERROR, "nopk1", DifferenceType.NO_PRIMARY_KEY)) >= 0);
		assertTrue(schemaDifferences.indexOf(new SchemaDifference(Criticality.ERROR, "nopk2", DifferenceType.NO_PRIMARY_KEY)) >= 0);
		assertTrue(schemaDifferences.indexOf(new SchemaDifference(Criticality.ERROR, "nopk2.uid", DifferenceType.COLUMN_DIFFERENT_PRIMARY_KEY, "master:true v.s. slave:false")) >= 0);
		assertTrue(schemaDifferences.indexOf(new SchemaDifference(Criticality.ERROR, "nopk3.uid", DifferenceType.COLUMN_DIFFERENT_PRIMARY_KEY, "master:false v.s. slave:true")) >= 0);
		assertTrue(schemaDifferences.indexOf(new SchemaDifference(Criticality.ERROR, "pkdiff1.sometext", DifferenceType.COLUMN_DIFFERENT_PRIMARY_KEY, "master:false v.s. slave:true")) >= 0);
		assertTrue(schemaDifferences.indexOf(new SchemaDifference(Criticality.ERROR, "pkdiff1.uid", DifferenceType.COLUMN_DIFFERENT_PRIMARY_KEY, "master:true v.s. slave:false")) >= 0);
		assertTrue(schemaDifferences.indexOf(new SchemaDifference(Criticality.ERROR, "pkdiff2.sometext", DifferenceType.COLUMN_DIFFERENT_PRIMARY_KEY, "master:false v.s. slave:true")) >= 0);
		assertTrue(schemaDifferences.indexOf(new SchemaDifference(Criticality.ERROR, "pkdiff3.sometext1", DifferenceType.COLUMN_DIFFERENT_PRIMARY_KEY, "master:true v.s. slave:false")) >= 0);
		assertTrue(schemaDifferences.indexOf(new SchemaDifference(Criticality.ERROR, "pkdiff3.sometext2", DifferenceType.COLUMN_DIFFERENT_PRIMARY_KEY, "master:false v.s. slave:true")) >= 0);
		assertTrue(schemaDifferences.indexOf(new SchemaDifference(Criticality.WARNING, "pkdiff4.sometext1", DifferenceType.COLUMN_DIFFERENT_PRIMARY_KEY_ORDINAL_POSITION, "master:2 v.s. slave:1")) >= 0);
		assertTrue(schemaDifferences.indexOf(new SchemaDifference(Criticality.WARNING, "pkdiff4.uid", DifferenceType.COLUMN_DIFFERENT_PRIMARY_KEY_ORDINAL_POSITION, "master:1 v.s. slave:2")) >= 0);
		assertTrue(schemaDifferences.indexOf(new SchemaDifference(Criticality.WARNING, "tab1.sometext", DifferenceType.COLUMN_DIFFERENT_TYPE, "master:varchar(100) v.s. slave:varchar(99)")) >= 0);
		assertTrue(schemaDifferences.indexOf(new SchemaDifference(Criticality.WARNING, "tab3.sometext2", DifferenceType.COLUMN_DIFFERENT_ORDINAL_POSITION, "master:3 v.s. slave:1")) >= 0);
		assertTrue(schemaDifferences.indexOf(new SchemaDifference(Criticality.WARNING, "tab3.uid", DifferenceType.COLUMN_DIFFERENT_ORDINAL_POSITION, "master:1 v.s. slave:3")) >= 0);
		assertTrue(schemaDifferences.indexOf(new SchemaDifference(Criticality.ERROR, "tab4.sometext2", DifferenceType.COLUMN_MISSING_IN_SLAVE_TABLE)) >= 0);
		assertTrue(schemaDifferences.indexOf(new SchemaDifference(Criticality.ERROR, "tab4.sometext3", DifferenceType.COLUMN_EXCESS_IN_SLAVE_TABLE)) >= 0);
		assertTrue(schemaDifferences.indexOf(new SchemaDifference(Criticality.WARNING, "tab5.sometext2", DifferenceType.COLUMN_DIFFERENT_ORDINAL_POSITION, "master:3 v.s. slave:1")) >= 0);
		assertTrue(schemaDifferences.indexOf(new SchemaDifference(Criticality.WARNING, "tab5.uid", DifferenceType.COLUMN_DIFFERENT_ORDINAL_POSITION, "master:1 v.s. slave:3")) >= 0);
		assertTrue(schemaDifferences.indexOf(new SchemaDifference(Criticality.WARNING, "nopk2.uid", DifferenceType.COLUMN_DIFFERENT_IS_NULLABLE, "master:false v.s. slave:true")) >= 0);
		assertTrue(schemaDifferences.indexOf(new SchemaDifference(Criticality.WARNING, "nopk3.uid", DifferenceType.COLUMN_DIFFERENT_IS_NULLABLE, "master:true v.s. slave:false")) >= 0);
		assertTrue(schemaDifferences.indexOf(new SchemaDifference(Criticality.WARNING, "pkdiff1.sometext", DifferenceType.COLUMN_DIFFERENT_IS_NULLABLE, "master:true v.s. slave:false")) >= 0);
		assertTrue(schemaDifferences.indexOf(new SchemaDifference(Criticality.WARNING, "pkdiff1.uid", DifferenceType.COLUMN_DIFFERENT_IS_NULLABLE, "master:false v.s. slave:true")) >= 0);
		assertTrue(schemaDifferences.indexOf(new SchemaDifference(Criticality.WARNING, "pkdiff2.sometext", DifferenceType.COLUMN_DIFFERENT_IS_NULLABLE, "master:true v.s. slave:false")) >= 0);
		assertTrue(schemaDifferences.indexOf(new SchemaDifference(Criticality.WARNING, "pkdiff3.sometext1", DifferenceType.COLUMN_DIFFERENT_IS_NULLABLE, "master:false v.s. slave:true")) >= 0);
		assertTrue(schemaDifferences.indexOf(new SchemaDifference(Criticality.WARNING, "pkdiff3.sometext2", DifferenceType.COLUMN_DIFFERENT_IS_NULLABLE, "master:true v.s. slave:false")) >= 0);
		assertEquals(schemaDifferences.size(), 27);
        
		ArrayList<Table> tablesReadyToBeDataAnalyzed = mySQLSchemaComparer.getMasterTablesReadyToBeDataAnalyzed();
		MySQLTableDataComparer tableDataComparer = new MySQLTableDataComparer(masterSchemaReader, slaveSchemaReader);
		for(Table table:tablesReadyToBeDataAnalyzed)
			tableDataComparer.compareTable(table);
		ArrayList<SchemaDifference> dataDifferences = tableDataComparer.getDataDifferences();
		assertTrue(dataDifferences.indexOf(new SchemaDifference(Criticality.ERROR, "tab1", DifferenceType.DATA_ROW_DIFFERENT_MD5, "(uid)=(3)")) >= 0);
		assertTrue(dataDifferences.indexOf(new SchemaDifference(Criticality.ERROR, "tab1", DifferenceType.DATA_ROW_MISSING_IN_SLAVE_TABLE, "(uid)=(5)")) >= 0);
		assertTrue(dataDifferences.indexOf(new SchemaDifference(Criticality.ERROR, "tab1", DifferenceType.DATA_ROW_DIFFERENT_MD5, "(uid)=(7)")) >= 0);
		assertTrue(dataDifferences.indexOf(new SchemaDifference(Criticality.ERROR, "tab1", DifferenceType.DATA_ROW_EXCESS_IN_SLAVE_TABLE, "(uid)=(2)")) >= 0);
		assertTrue(dataDifferences.indexOf(new SchemaDifference(Criticality.ERROR, "tab2", DifferenceType.DATA_ROW_MISSING_IN_SLAVE_TABLE, "(uid)=(5)")) >= 0);
		assertTrue(dataDifferences.indexOf(new SchemaDifference(Criticality.ERROR, "tab2", DifferenceType.DATA_ROW_EXCESS_IN_SLAVE_TABLE, "(uid)=(2)")) >= 0);
		assertTrue(dataDifferences.indexOf(new SchemaDifference(Criticality.ERROR, "tab3", DifferenceType.DATA_ROW_DIFFERENT_MD5, "(uid,sometext2)=(3,c2)")) >= 0);
		assertTrue(dataDifferences.indexOf(new SchemaDifference(Criticality.ERROR, "tab3", DifferenceType.DATA_ROW_DIFFERENT_MD5, "(uid,sometext2)=(5,d2)")) >= 0);
		assertTrue(dataDifferences.indexOf(new SchemaDifference(Criticality.ERROR, "tab3", DifferenceType.DATA_ROW_MISSING_IN_SLAVE_TABLE, "(uid,sometext2)=(4,d2)")) >= 0);
		assertTrue(dataDifferences.indexOf(new SchemaDifference(Criticality.ERROR, "tab3", DifferenceType.DATA_ROW_EXCESS_IN_SLAVE_TABLE, "(uid,sometext2)=(4,d3)")) >= 0);
		assertTrue(dataDifferences.indexOf(new SchemaDifference(Criticality.ERROR, "tab3", DifferenceType.DATA_ROW_EXCESS_IN_SLAVE_TABLE, "(uid,sometext2)=(6,e2)")) >= 0);
		assertEquals(dataDifferences.size(), 11);
        assertEquals(tableDataComparer.getMasterTotalRetrievedRows(), 19);
        assertEquals(tableDataComparer.getSlaveTotalRetrievedRows(), 20);
    }
}
