# Automation1 for ArrayDB Acceptance Test 
# Create on: 06/11/2015
# Updated on: 08/28/2015
# Created by: Andrew Zhang
# Purpose: Minimum test cases covers all the basic SQL syntax for jaguar.
#
# Command Line:
#  jag -u user_myArrayDB1 -p password -d myArrayDB1 -h 127.0.0.1:8888 -v yes < mycommands.sql > testBatchSQL.log
# Updated on: 08/s4/2015

# Test 1-1. Create tables
#########################
drop table jaguarFunc1 ;
drop table jaguarFunc2 ;
drop table jaguarFunc3 ;
drop table jaguarFunc4:
drop table jaguarFunc5;

create table jaguarFunc1  ( key: uid char(32), value: v1 char(16), v2 char(16), v3 char(16) );
create table jaguarFunc2  ( key: uid char(32), value: v1 char(16), v2 char(16), v3 char(16) );
create table jaguarFunc3  ( key: uid char(32), value: v1 char(16), v2 char(16), v3 char(16) );
create table jaguarFunc4  ( key: uid char(32), value: v1 char(16), v2 char(16), v3 char(16) );
create table jaguarFunc5  ( key: uid char(32), value: v1 char(16), v2 char(16), v3 char(16) );

show tables;

# Test 1-2.  Delete table
###########################
desc jaguarFunc1;
show tables;
drop table jaguarFunc1;
show tables;

# Test 1-3. Check table schema
desc jaguarFunc1;

# Test 2-1: Load a 10k row text file in foramt H:
#################################################
load $HOME/jaguarTestData/10kLine.txt into jaguarFunc2;

# Test 2-2: Basic query 
##########################
select * from jaguarFunc2 where uid = FQfbHNKNcgPORWNs;
select * from jaguarFunc2 where v2=c3BRPlD1BsNsnAT3;

# Test 3: Insert data and select
#################################
select count(*) from jaguarFunc2;
insert into jaguarFunc2 (uid, v1, v2, v3) values (4, 'xxx', 'yyyy', 'zzz');
select count(*) from jaguarFunc2;
## Expect 10006 rows found

select * from jaguarFunc2 where v2=yyyy;
select * from jaguarFunc2 where v3=zzz;
select * from jaguarFunc2 where uid=4;

######################
# Test 4: Update data
######################

update jaguarFunc2 set v2='v2 is From yyyy to y2y2y2' where uid=4;
select * from jaguarFunc2 where  uid=4;

create table test_user ( key: fname char(10), lname char(12), value: age int(3), address char(128) );
insert into test_user ( fname, lname, age, address) values ( 'Larry', 'Patterson', 40, 'lpat@yahoo.com' );
insert into test_user ( fname, lname, age, address) values ( 'Andrew', 'Zhang', 500, 'agz1117@hotmail.com' );
insert into test_user ( fname, lname, age, address) values ( 'Jason', 'Zhang', 18, 'agz1117@hotmail.com' );

update test_user set address = jason.zhang@gmail.com where  fname = Jason and lname = Zhang;
select * from test_user;

update test_user set age  = 50  where  fname = Andrew and lname = Zhang;
select * from test_user;

update test_user set fname  = Andy where  fname = Andrew and lname = Zhang;
select * from test_user;
select count(*) from test_user;

########################################
# Test 5-1: Delete data from table user
#######################################
delete from test_user;
select count(*) from test_user;
select * from test_user;

show tables;
drop table test_user;
show tables;

#################################################
# Test 5-2: Delete data from table jaguarFunc2
#################################################
select count(*) from jaguarFunc2
delete from jaguarFunc2 
select count(*) from jaguarFunc2

##############
# Test join
##############
insert into jaguarFunc3 (uid, v1, v2, v3) values (1, 't3_v1_L1', 't3_v2_L1', 't3_v3_L1');
insert into jaguarFunc3 (uid, v1, v2, v3) values (2, 't3_v1_L2', 't3_v2_L2', 't3_v3_L2');
insert into jaguarFunc3 (uid, v1, v2, v3) values (3, 't3_v1_L3', 't3_v2_L3', 't3_v3_L3');


insert into jaguarFunc4 (uid, v1, v2, v3) values (1, 't4_v1_L1', 't4_v2_L1', 't4_v3_L1');
insert into jaguarFunc4 (uid, v1, v2, v3) values (2, 't4_v1_L2', 't4_v2_L2', 't4_v3_L2');
insert into jaguarFunc4 (uid, v1, v2, v3) values (3, 't4_v1_L3', 't4_v2_L3', 't4_v3_L3');


insert into jaguarFunc5 (uid, v1, v2, v3) values (1, 't5_v1_L1', 't5_v2_L1', 't5_v3_L1');
insert into jaguarFunc5 (uid, v1, v2, v3) values (2, 't5_v1_L2', 't5_v2_L2', 't5_v3_L2');
insert into jaguarFunc5 (uid, v1, v2, v3) values (3, 't5_v1_L3', 't5_v2_L3', 't5_v3_L3');

select * from jaguarFunc3;
select * from jaguarFunc4;
select * from jaguarFunc5;

quit;


