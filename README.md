# MySQL Data Compare

This simple utility compares 1 or more MySQL schemas (databases) and pinpoints schema and data differences. This tool does not create any remediation SQL statement.

It was developed to compare MySQL replicas, in particular for a MySQL InnoDB cluster (3 or more nodes), and possibly with an offsite DR replica.

## Algorithm

The tool compares md5 of the row, simply speaking `select concat(pk1, pk2) PK, md5(concat(pk1, pk2, coalesce(c1, 'null'), coalesce(c2, 'null'), ...)) MD5 from mytable order by pk1, pk2`. If some primary keys are missing on either master or slave database, or md5 value is not matching, the tool reports the difference, showing the primary keys.

The tool being optimized for tables very similar in content, it retrieves 1 row alternatively from master then slave table. As data is sorted by PK, it keeps a sorted list of differences (pk and md5 value) in memory. As soon as there is a match, any value lower than the match pair is immediately reported and removed from memory. Memory footprint shall then remain small.

## Build and Test

Build only requires Maven and Java 7.

For the automated test suite, install MySQL 5 or 8 on your local machine (127.0.0.1) and execute as admin the setup script [test_schemas.sql](src/test/resources/test_schemas.sql).

## Run

First download `mysql-connector-java-8.0.13.jar` [here](https://dev.mysql.com/downloads/connector/j/) and place this file in the path of MySQLDataCompare jar file. Have also JRE 1.7 or higher in the path.

Simply issue the command `java -jar MySQLDataCompare-1.0.jar masterSchemaURL slaveSchemaURL1 [slaveSchemaURL2]`.

Here is a sample output from the test cases.

```
Loading Database Schema Metadata MASTER
Loading Database Schema Metadata Slave #1
Comparing Database Schema Metadata MASTER with Slave #1
    ERROR object:onlyina, differenceType:TABLE_MISSING_IN_SLAVE_SCHEMA
    ERROR object:onlyinb, differenceType:TABLE_EXCESS_IN_SLAVE_SCHEMA
    ERROR object:nopk1, differenceType:NO_PRIMARY_KEY
    ERROR object:nopk2, differenceType:NO_PRIMARY_KEY
    WARNING object:nopk2.uid, differenceType:COLUMN_DIFFERENT_IS_NULLABLE, note:master:false v.s. slave:true
    ERROR object:nopk2.uid, differenceType:COLUMN_DIFFERENT_PRIMARY_KEY, note:master:true v.s. slave:false
    WARNING object:nopk3.uid, differenceType:COLUMN_DIFFERENT_IS_NULLABLE, note:master:true v.s. slave:false
    ERROR object:nopk3.uid, differenceType:COLUMN_DIFFERENT_PRIMARY_KEY, note:master:false v.s. slave:true
    WARNING object:pkdiff1.sometext, differenceType:COLUMN_DIFFERENT_IS_NULLABLE, note:master:true v.s. slave:false
    ERROR object:pkdiff1.sometext, differenceType:COLUMN_DIFFERENT_PRIMARY_KEY, note:master:false v.s. slave:true
    WARNING object:pkdiff1.uid, differenceType:COLUMN_DIFFERENT_IS_NULLABLE, note:master:false v.s. slave:true
    ERROR object:pkdiff1.uid, differenceType:COLUMN_DIFFERENT_PRIMARY_KEY, note:master:true v.s. slave:false
    WARNING object:pkdiff2.sometext, differenceType:COLUMN_DIFFERENT_IS_NULLABLE, note:master:true v.s. slave:false
    ERROR object:pkdiff2.sometext, differenceType:COLUMN_DIFFERENT_PRIMARY_KEY, note:master:false v.s. slave:true
    WARNING object:pkdiff3.sometext1, differenceType:COLUMN_DIFFERENT_IS_NULLABLE, note:master:false v.s. slave:true
    ERROR object:pkdiff3.sometext1, differenceType:COLUMN_DIFFERENT_PRIMARY_KEY, note:master:true v.s. slave:false
    WARNING object:pkdiff3.sometext2, differenceType:COLUMN_DIFFERENT_IS_NULLABLE, note:master:true v.s. slave:false
    ERROR object:pkdiff3.sometext2, differenceType:COLUMN_DIFFERENT_PRIMARY_KEY, note:master:false v.s. slave:true
    WARNING object:pkdiff4.sometext1, differenceType:COLUMN_DIFFERENT_PRIMARY_KEY_ORDINAL_POSITION, note:master:2 v.s. slave:1
    WARNING object:pkdiff4.uid, differenceType:COLUMN_DIFFERENT_PRIMARY_KEY_ORDINAL_POSITION, note:master:1 v.s. slave:2
    WARNING object:tab1.sometext, differenceType:COLUMN_DIFFERENT_TYPE, note:master:varchar(100) v.s. slave:varchar(99)
    WARNING object:tab3.sometext2, differenceType:COLUMN_DIFFERENT_ORDINAL_POSITION, note:master:3 v.s. slave:1
    WARNING object:tab3.uid, differenceType:COLUMN_DIFFERENT_ORDINAL_POSITION, note:master:1 v.s. slave:3
    ERROR object:tab4.sometext2, differenceType:COLUMN_MISSING_IN_SLAVE_TABLE
    ERROR object:tab4.sometext3, differenceType:COLUMN_EXCESS_IN_SLAVE_TABLE
    WARNING object:tab5.sometext2, differenceType:COLUMN_DIFFERENT_ORDINAL_POSITION, note:master:3 v.s. slave:1
    WARNING object:tab5.uid, differenceType:COLUMN_DIFFERENT_ORDINAL_POSITION, note:master:1 v.s. slave:3
Comparing Database Schema Data MASTER with Slave #1
In scope tables: tab1 tab2 tab3 tab5 
    ERROR object:tab1, differenceType:DATA_ROW_DIFFERENT_MD5, note:(uid)=(3)
    ERROR object:tab1, differenceType:DATA_ROW_MISSING_IN_SLAVE_TABLE, note:(uid)=(5)
    ERROR object:tab1, differenceType:DATA_ROW_DIFFERENT_MD5, note:(uid)=(7)
    ERROR object:tab1, differenceType:DATA_ROW_EXCESS_IN_SLAVE_TABLE, note:(uid)=(2)
    ERROR object:tab2, differenceType:DATA_ROW_MISSING_IN_SLAVE_TABLE, note:(uid)=(5)
    ERROR object:tab2, differenceType:DATA_ROW_EXCESS_IN_SLAVE_TABLE, note:(uid)=(2)
    ERROR object:tab3, differenceType:DATA_ROW_DIFFERENT_MD5, note:(uid,sometext2)=(3,c2)
    ERROR object:tab3, differenceType:DATA_ROW_DIFFERENT_MD5, note:(uid,sometext2)=(5,d2)
    ERROR object:tab3, differenceType:DATA_ROW_MISSING_IN_SLAVE_TABLE, note:(uid,sometext2)=(4,d2)
    ERROR object:tab3, differenceType:DATA_ROW_EXCESS_IN_SLAVE_TABLE, note:(uid,sometext2)=(4,d3)
    ERROR object:tab3, differenceType:DATA_ROW_EXCESS_IN_SLAVE_TABLE, note:(uid,sometext2)=(6,e2)
Comparing Database Schema MASTER with Slave #1 - Complete. Duration(ms): 56
Job Done. Count Summary warningCount:14, errorCount:24
```