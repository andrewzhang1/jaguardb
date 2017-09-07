
Suppose your table to be replicated is table123 with columns uid and addr.
You can create a table123_trigger_table to capture the changes in table123.

drop table if exists table123_trigger_table;
create table table123_trigger_table (
    ts datetime,
    uid int,
    addr varchar(64),
    action char(1),
	primary key( ts, uid )
);


Then you can create three triggers to record the changes of table123 into table123_trigger_table:

DELIMITER $$
DROP TRIGGER IF EXISTS after_table123_insert;
CREATE TRIGGER after_table123_insert AFTER  INSERT ON table123 FOR EACH ROW
BEGIN
     INSERT INTO table123_trigger_table select NOW(), table123.*, 'I' from table123 where uid = NEW.uid;
END$$
DELIMITER ;


DELIMITER $$
DROP TRIGGER IF EXISTS after_table123_update;
CREATE TRIGGER after_table123_update AFTER  UPDATE ON table123 FOR EACH ROW
BEGIN
     INSERT INTO table123_trigger_table select NOW(), table123.*, 'U' from table123 where uid = NEW.uid;
END$$
DELIMITER ;


DELIMITER $$
DROP TRIGGER IF EXISTS after_table123_delete;
CREATE TRIGGER after_table123_delete AFTER  DELETE ON table123 FOR EACH ROW
BEGIN
     INSERT INTO table123_trigger_table
	     set ts = NOW(), uid = OLD.uid, 
		     action='D';
END$$
DELIMITER ;



Finally, you just need to write a Java program to periodically pull the records
from table123_trigger_table and write them into your target database table.
Table table123_trigger_table can be cleaned up periodically too.

