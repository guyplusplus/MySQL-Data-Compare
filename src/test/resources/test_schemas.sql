drop user 'usertestA'@'localhost';
drop user 'usertestB'@'localhost';
drop schema schema_A;
drop schema schema_B;

create schema schema_A;
create schema schema_B;
create user 'usertestA'@'localhost' identified by 'password';
grant select on schema_A.* to 'usertestA'@'localhost';
create user 'usertestB'@'localhost' identified by 'password';
grant select on schema_B.* to 'usertestB'@'localhost';

#######
# Table missing in either schema

create table schema_A.onlyInA(
   uid INT,
   PRIMARY KEY ( uid )
);

create table schema_B.onlyInB(
   uid INT,
   PRIMARY KEY ( uid )
);

#######
# Table with no PK

create table schema_A.nopk1(
   uid INT
);

create table schema_B.nopk1(
   uid INT
);

#######
# Table with no PK

create table schema_A.nopk2(
   uid INT,
   PRIMARY KEY ( uid )
);

create table schema_B.nopk2(
   uid INT
);

#######
# Table with no PK

create table schema_A.nopk3(
   uid INT
);

create table schema_B.nopk3(
   uid INT,
   PRIMARY KEY ( uid )
);

#######
# Tables with different PKs

create table schema_A.pkdiff1(
   uid INT,
   sometext VARCHAR(100),
   PRIMARY KEY ( uid )
);

create table schema_B.pkdiff1(
   uid INT,
   sometext VARCHAR(100),
   PRIMARY KEY ( sometext )
);

#######
# Tables with different PKs, but first column is same, different number of pk columns

create table schema_A.pkdiff2(
   uid INT,
   sometext VARCHAR(100),
   PRIMARY KEY ( uid )
);

create table schema_B.pkdiff2(
   uid INT,
   sometext VARCHAR(100),
   PRIMARY KEY ( uid, sometext )
);

#######
# Tables with different PKs, but first column is same, same number of pk columns

create table schema_A.pkdiff3(
   uid INT,
   sometext1 VARCHAR(100),
   sometext2 VARCHAR(100),
   PRIMARY KEY ( uid, sometext1 )
);

create table schema_B.pkdiff3(
   uid INT,
   sometext1 VARCHAR(100),
   sometext2 VARCHAR(100),
   PRIMARY KEY ( uid, sometext2 )
);

#######
# Tables with different PKs, pk column order is different

create table schema_A.pkdiff4(
   uid INT,
   sometext1 VARCHAR(100),
   sometext2 VARCHAR(100),
   PRIMARY KEY ( uid, sometext1 )
);

create table schema_B.pkdiff4(
   uid INT,
   sometext1 VARCHAR(100),
   sometext2 VARCHAR(100),
   PRIMARY KEY ( sometext1, uid )
);

#######
# Simple table

create table schema_A.tab1(
   uid INT,
   sometext VARCHAR(100),
   PRIMARY KEY ( uid )
);

create table schema_B.tab1(
   uid INT,
   sometext VARCHAR(99),
   PRIMARY KEY ( uid )
);

#schema_a uid 2 missing
#schema_b uid 5 missing
#schema_a/b uid 3 sometext different
#schema_a/b uid 7 sometext different with a NULL

insert into schema_a.tab1 (uid,sometext) values (1, 'a');
insert into schema_a.tab1 (uid,sometext) values (3, 'c');
insert into schema_a.tab1 (uid,sometext) values (4, NULL);
insert into schema_a.tab1 (uid,sometext) values (5, 'e');
insert into schema_a.tab1 (uid,sometext) values (6, 'f');
insert into schema_a.tab1 (uid,sometext) values (7, 'g');

insert into schema_b.tab1 (uid,sometext) values (1, 'a');
insert into schema_b.tab1 (uid,sometext) values (2, 'b');
insert into schema_b.tab1 (uid,sometext) values (3, 'cc');
insert into schema_b.tab1 (uid,sometext) values (4, NULL);
insert into schema_b.tab1 (uid,sometext) values (6, 'f');
insert into schema_b.tab1 (uid,sometext) values (7, NULL);

#######
# Table with only PK

create table schema_A.tab2(
   uid INT,
   PRIMARY KEY ( uid )
);

create table schema_B.tab2(
   uid INT,
   PRIMARY KEY ( uid )
);

#schema_a uid 2 missing
#schema_b uid 5 missing

insert into schema_a.tab2 (uid) values (1);
insert into schema_a.tab2 (uid) values (3);
insert into schema_a.tab2 (uid) values (4);
insert into schema_a.tab2 (uid) values (5);
insert into schema_a.tab2 (uid) values (6);
insert into schema_a.tab2 (uid) values (7);

insert into schema_b.tab2 (uid) values (1);
insert into schema_b.tab2 (uid) values (2);
insert into schema_b.tab2 (uid) values (3);
insert into schema_b.tab2 (uid) values (4);
insert into schema_b.tab2 (uid) values (6);
insert into schema_b.tab2 (uid) values (7);

#######
# Simple table with columns created in different order, inserted in different order of PK

create table schema_A.tab3(
   uid INT,
   sometext1 VARCHAR(100),
   sometext2 VARCHAR(100),
   PRIMARY KEY ( uid, sometext2 )
);

create table schema_B.tab3(
   sometext2 VARCHAR(100),
   sometext1 VARCHAR(100),
   uid INT,
   PRIMARY KEY ( uid, sometext2 )
);

#schema_a uid 4,d3 missing
#schema_a uid 6,d2 missing
#schema_b uid 4,d2 missing
#schema_a/b uid 3,c2 sometext1 different
#schema_a/b uid 5,d2 sometext1 different with a NULL

insert into schema_a.tab3 (uid,sometext1,sometext2) values (1, 'a1', 'a2');
insert into schema_a.tab3 (uid,sometext1,sometext2) values (2, 'b1', 'b2');
insert into schema_a.tab3 (uid,sometext1,sometext2) values (3, 'c1', 'c2');
insert into schema_a.tab3 (uid,sometext1,sometext2) values (4, 'd1', 'd2');
insert into schema_a.tab3 (uid,sometext1,sometext2) values (5, null, 'd2');

insert into schema_b.tab3 (uid,sometext1,sometext2) values (2, 'b1', 'b2');
insert into schema_b.tab3 (uid,sometext1,sometext2) values (1, 'a1', 'a2');
insert into schema_b.tab3 (uid,sometext1,sometext2) values (3, 'c0', 'c2');
insert into schema_b.tab3 (uid,sometext1,sometext2) values (4, 'd1', 'd3');
insert into schema_b.tab3 (uid,sometext1,sometext2) values (5, 'e1', 'd2');
insert into schema_b.tab3 (uid,sometext1,sometext2) values (6, 'e1', 'e2');

#######
# Simple table with columns created in different order, inserted in different order of PK

create table schema_A.tab4(
   uid INT,
   sometext1 VARCHAR(100),
   sometext2 VARCHAR(100),
   PRIMARY KEY ( uid )
);

create table schema_B.tab4(
   uid INT,
   sometext1 VARCHAR(100),
   sometext3 VARCHAR(100),
   PRIMARY KEY ( uid )
);

#######
# Simple table with columns created in different order, inserted in different order of PK

create table schema_A.tab5(
   uid INT,
   sometext1 VARCHAR(100),
   sometext2 VARCHAR(100),
   PRIMARY KEY ( uid, sometext1 )
);

create table schema_B.tab5(
   sometext2 VARCHAR(100),
   sometext1 VARCHAR(100),
   uid INT,
   PRIMARY KEY ( uid, sometext1 )
);

insert into schema_a.tab5 (uid,sometext1,sometext2) values (1, 'a1', 'a2');
insert into schema_a.tab5 (uid,sometext1,sometext2) values (2, 'b1', 'b2');

insert into schema_b.tab5 (uid,sometext1,sometext2) values (2, 'b1', 'b2');
insert into schema_b.tab5 (uid,sometext1,sometext2) values (1, 'a1', 'a2');

#######
# Big Table with 1,000,000 rows

create table schema_A.bigtable(
   uid INT,
   rndint INT,
   PRIMARY KEY ( uid )
);

INSERT IGNORE INTO schema_A.bigtable ( uid, rndint )
SELECT 2 * round(rand() * 1073741823), 2 * round(rand() * 1073741823) 
FROM 
(select 0 as i union select 1 union select 2 union select 3 union select 4 union select 5 union select 6 union select 7 union select 8 union select 9) as t1,
(select 0 as i union select 1 union select 2 union select 3 union select 4 union select 5 union select 6 union select 7 union select 8 union select 9) as t2,
(select 0 as i union select 1 union select 2 union select 3 union select 4 union select 5 union select 6 union select 7 union select 8 union select 9) as t3,
(select 0 as i union select 1 union select 2 union select 3 union select 4 union select 5 union select 6 union select 7 union select 8 union select 9) as t4,
(select 0 as i union select 1 union select 2 union select 3 union select 4 union select 5 union select 6 union select 7 union select 8 union select 9) as t5,
(select 0 as i union select 1 union select 2 union select 3 union select 4 union select 5 union select 6 union select 7 union select 8 union select 9) as t6;

create table schema_B.bigtable(
   uid INT,
   rndint INT,
   PRIMARY KEY ( uid )
);

INSERT IGNORE INTO schema_B.bigtable ( uid, rndint ) select sql_no_cache uid, rndint from schema_A.bigtable;

INSERT INTO schema_A.bigtable ( uid, rndint ) VALUES ( 500000001, 1234);
INSERT INTO schema_B.bigtable ( uid, rndint ) VALUES ( 500000001, 4567);
INSERT INTO schema_A.bigtable ( uid, rndint ) VALUES ( 700000001, 1234);
INSERT INTO schema_B.bigtable ( uid, rndint ) VALUES ( 300000001, 4567);

#######
# Big diff Table with 1,000 rows

create table schema_A.bigdiff(
   uid INT,
   rndint INT,
   PRIMARY KEY ( uid )
);

INSERT IGNORE INTO schema_A.bigdiff ( uid, rndint )
SELECT 2 * round(rand() * 1073741823), 2 * round(rand() * 1073741823) 
FROM 
(select 0 as i union select 1 union select 2 union select 3 union select 4 union select 5 union select 6 union select 7 union select 8 union select 9) as t1,
(select 0 as i union select 1 union select 2 union select 3 union select 4 union select 5 union select 6 union select 7 union select 8 union select 9) as t2,
(select 0 as i union select 1 union select 2 union select 3 union select 4 union select 5 union select 6 union select 7 union select 8 union select 9) as t3;

create table schema_B.bigdiff(
   uid INT,
   rndint INT,
   PRIMARY KEY ( uid )
);

#######
# View to make sure it has no impact

create view schema_a.myview1 as select * from schema_a.tab3;
create view schema_b.myview2 as select * from schema_b.tab3;

commit;
