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