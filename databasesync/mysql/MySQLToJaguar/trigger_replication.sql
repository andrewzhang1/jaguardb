
Suppose your MySQL table to be replicated is table123.

MySQL table:
create table if exists table123
(
    nid int primary key, 
	addr varchar(64 ),
	phone varchar(16)
);

You can create a table123_trigger_table to capture the changes in table123.

MySQL table:
drop table if exists table123_trigger_table;
create table table123_trigger_table (
    ts_ datetime,
    nid int,
    addr varchar(64),
	phone varchar(16),
    action_ char(1),
	primary key( ts_, nid )
);


Then you can create three triggers to record the changes of table123 into table123_trigger_table:

DELIMITER $$
DROP TRIGGER IF EXISTS after_table123_insert;
CREATE TRIGGER after_table123_insert AFTER  INSERT ON table123 FOR EACH ROW
BEGIN
     INSERT INTO table123_trigger_table 
	 select NOW(), table123.*, 'I' from table123 where nid = NEW.nid;
END$$
DELIMITER ;


DELIMITER $$
DROP TRIGGER IF EXISTS after_table123_update;
CREATE TRIGGER after_table123_update AFTER  UPDATE ON table123 FOR EACH ROW
BEGIN
     INSERT INTO table123_trigger_table 
	 select NOW(), table123.*, 'U' from table123 where nid = NEW.nid;
END$$
DELIMITER ;


DELIMITER $$
DROP TRIGGER IF EXISTS after_table123_delete;
CREATE TRIGGER after_table123_delete AFTER  DELETE ON table123 FOR EACH ROW
BEGIN
     INSERT INTO table123_trigger_table
	     set ts_ = NOW(), nid = OLD.nid, action_='D';
END$$
DELIMITER ;



Finally, you just need to write a Java program to periodically pull the records
from table123_trigger_table and write them into your target database table.
Table table123_trigger_table can be cleaned up periodically too.

