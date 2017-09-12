
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
    id_ int auto_increment primary key,
    ts_ timestamp,
    action_ char(1),
    status_ char(1),
    nid int,
    addr varchar(64),
	phone varchar(16)
);


Then you can create three triggers to record the changes of table123 into table123_trigger_table:

DELIMITER $$
DROP TRIGGER IF EXISTS after_table123_insert;
CREATE TRIGGER after_table123_insert AFTER  INSERT ON table123 FOR EACH ROW
BEGIN
     INSERT INTO table123_trigger_table (ts_, action_, status_, nid, addr, phone) 
	 values( NOW(), 'I', 'I', new.nid, new.addr, new.phone);
END$$
DELIMITER ;


DELIMITER $$
DROP TRIGGER IF EXISTS after_table123_update;
CREATE TRIGGER after_table123_update AFTER  UPDATE ON table123 FOR EACH ROW
BEGIN
     INSERT INTO table123_trigger_table (ts_, action_, status_, nid, addr, phone) 
	values ( NOW(), 'U', 'I', new.nid, new.addr, new.phone); 
END$$
DELIMITER ;


DELIMITER $$
DROP TRIGGER IF EXISTS before_table123_delete;
CREATE TRIGGER before_table123_delete AFTER  DELETE ON table123 FOR EACH ROW
BEGIN
     INSERT INTO table123_trigger_table (ts_, action_, status_, nid, addr, phone)
	values ( NOW(), 'D', 'I', old.nid, old.addr, old.phone);
END$$
DELIMITER ;



Finally, you just need to write a Java program to periodically pull the records
from table123_trigger_table and write them into your target database table.
Table table123_trigger_table can be cleaned up periodically too.

