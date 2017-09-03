
Suppose your table to be replicated is table123 with columns uid and addr.
You can create a table123_trigger_table to capture the changes in table123.

create table table123_trigger_table (
    ts datetime primary key,
    uid int,
    addr varchar(64),
    action char(1)
);


Then you can create three triggers to record the changes of table123 into table123_trigger_table:

DELIMITER $$
CREATE TRIGGER after_table123_insert AFTER  INSERT ON table123 FOR EACH ROW
BEGIN
     INSERT INTO table123_trigger_table
     SET action = 'I',
        uid = NEW.uid,
        addr = NEW.addr,
        ts = NOW();
END$$
DELIMITER ;


DELIMITER $$
CREATE TRIGGER after_table123_update AFTER  UPDATE ON table123 FOR EACH ROW
BEGIN
     INSERT INTO table123_trigger_table
     SET action = 'U',
        uid = NEW.uid,
        addr = NEW.addr,
        ts = NOW();
END$$
DELIMITER ;


DELIMITER $$
CREATE TRIGGER after_table123_delete AFTER  DELETE ON table123 FOR EACH ROW
BEGIN
     INSERT INTO table123_trigger_table
     SET action = 'D',
        uid = OLD.uid,
        addr = OLD.addr,
        ts = NOW();
END$$
DELIMITER ;


Finally, you just need to write a Java program to periodically pull the records
from table123_trigger_table and write them into your target database table.
Table table123_trigger_table can be cleaned up periodically too.

