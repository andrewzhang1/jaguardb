

drop table table234_trigger_table;
create table table234_trigger_table (
    id_ number primary key,
    ts_ timestamp,
    action_ char(1),
    status_ char(1),
    partno number,
    name varchar(32),
    sku  varchar(32),
    manufacture varchar(32),
    buydate  timestamp,
    buyer   varchar(32),
    ptype   varchar(2),
    supplier varchar(32)
);


drop sequence table234_trigger_table_seq;
create sequence table234_trigger_table_seq start with 1;


CREATE OR REPLACE TRIGGER after_table234_insert AFTER INSERT ON table234 
 FOR EACH ROW
 BEGIN
    INSERT INTO table234_trigger_table values ( 
			    table234_trigger_table_seq.nextval,	
				sysdate,
				'I', 
				'I', 
				:new.partno,
				:new.name,
				:new.sku,
				:new.manufacture,
				:new.buydate,
				:new.buyer,
				:new.ptype,
				:new.supplier
				);
 END;
 /


CREATE OR REPLACE TRIGGER after_table234_update AFTER  UPDATE ON table234 
 FOR EACH ROW
 BEGIN
    INSERT INTO table234_trigger_table values (
				table234_trigger_table_seq.nextval, 
				sysdate,
				'U', 
				'I',
				:new.partno,
				:new.name,
				:new.sku,
				:new.manufacture,
				:new.buydate,
				:new.buyer,
				:new.ptype,
				:new.supplier
				);
 END;
 /

CREATE OR REPLACE TRIGGER before_table234_delete before  DELETE ON table234 
 FOR EACH ROW
 BEGIN
    INSERT INTO table234_trigger_table values (
				table234_trigger_table_seq.nextval, 
				sysdate,
				'D', 
				'I', 
				:old.partno,
				:old.name,
				:old.sku,
				:old.manufacture,
				:old.buydate,
				:old.buyer,
				:old.ptype,
				:old.supplier
				);
 END;
 /

