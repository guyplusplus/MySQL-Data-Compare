# MYSQLDataCompare

This simple utility compares 1 or more MySQL schemas (databases) and pinpoints schema and data differences. This tool does not create any remediation SQL statement.

It was developped to compare MySQL replicas, either within a MySQL InnoDB cluster, or with an offsite DR replica.

# Build

Install MySQL 5 or 8 on your local machine and execute as admin the setup script [test_schemas.sql](src/test/resources/test_schemas.sql).

# Run

First download `mysql-connector-java-8.0.13.jar` and place this file in the path of MySQLDataCompare jar file. Have also jre 1.7 or higher in the path.

Simply issue the command `java -jar MySQLDataCompare-1.0.jar masterSchemaURL slaveSchemaURL1 [slaveSchemaURL2]`.

Here is a sample output from the test cases.