#!/bin/bash

##########################################################################################
## Create changelog table for a table in MySQL Also creates triggers
## to track DML (insert, update, delete in Oracle table
##
## Usage:   ./mysql_create_changelog_trigger.sh  <DATABASE> <MYSQL_TABLE>  <MUYSQL_CHANGELOG_TABLE>
##
##########################################################################################

mydb=$1
table=$2
changelog=$3


if [[ "x$changelog" = "x" ]]; then
	echo "Usage:     $0  <DATABASE> <TABLE_NAME> <CHANGELOG_TABLE>"
	echo
	echo "Example:   $0  mydb  table123  table123_changelog"
	echo
	echo "Make sure CHANGELOG_TABLE does not exist"
	exit 1
fi

pd=`pwd`
dirn="tmpdir$$"
/bin/mkdir -p $dirn
cd $dirn


cmd="tmpcmd.sql"
log="describe.log"
echo "describe $table;" > $cmd

echo -n "Enter MySQL user name: "
read uid

echo -n "Enter $uid password: "
read -s pass
echo

mysql -s -u$uid -p$pass $mydb < $cmd  > $log 2>&1
/bin/rm -f $cmd
if grep -i error $log; then
    echo "Table $table does not exists in mysql/$db"
	cd $pd
	/bin/rm -rf $dirn
    exit 1
fi

descrc="describe_${table}.txt"
desccolrc="describe_${table}_colname.txt"
cat $log|tr '[:upper:]' '[:lower:]'|grep -vi 'warning'|grep -v 'Default Extra'|sed -e 's/ \+/ /g' -e '/^$/d' > $descrc

/bin/rm -f $log
awk '{print $1}' $descrc > $desccolrc


echo "describe $changelog;" | mysql -s -u$uid -p$pass $mydb > $log 2>&1
((changelogExist=0))
if grep -i error $log; then
	echo "OK, $changelog does not exist, use $changelog as changelog table"
	((changelogExist=0))
else
	((changelogExist=1))
	echo "Table $changelog exists already."
	echo -n "Are you sure you want to drop it and create a new table with the same name? (y|n) "
	read ans
	if [[ "x$ans" != "xy" ]]; then
		echo "quit, please try again with a different changelog table name"
		/bin/rm -f $log
		cd $pd
		/bin/rm -rf $dirn
		exit 1
	fi
fi

/bin/rm -f $log

##################### create changelog table
/bin/rm -f $cmd
if ((changelogExist==1)); then
    echo "drop table if exists $changelog;" >> $cmd
fi
echo "create table $changelog (" >> $cmd
echo "    id_ bigint not null AUTO_INCREMENT primary key," >> $cmd
echo "    ts_ datetime, " >> $cmd
echo "    action_ char(1), " >> $cmd
echo "    status_ char(1), " >> $cmd
numlines=`wc -l $descrc|cut -d' ' -f1`
((n=1))
while read line
do
	col=`echo $line|awk '{print $1}'`
	typ=`echo $line|awk '{print $2}'`
	if ((n<numlines)); then
		echo "    $col $typ," >> $cmd
	else
		echo "    $col $typ" >> $cmd
	fi
	((n=n+1))
done < $descrc
echo  "); " >> $cmd

cmd2="${cmd}.tmprc"
tr '[:upper:]' '[:lower:]' < $cmd > $cmd2
/bin/mv -f $cmd2 $cmd

mysql -s -u$uid -p$pass $mydb < $cmd > /dev/null 2>&1
echo "Created $changelog"
echo "describe $changelog;" | mysql -s -u$uid -p$pass $mydb 2>/dev/null


##################### create mysql insert trigger
echo "drop trigger if exists ${table}_jagtrgins;" > $cmd
echo "DELIMITER \$\$" >> $cmd
echo "CREATE TRIGGER ${table}_jagtrgins AFTER INSERT ON ${table} FOR EACH ROW " >> $cmd
echo "BEGIN" >> $cmd
echo "    INSERT INTO $changelog ( ts_, action_, status_, "  >> $cmd
((n=1))
while read col; do
	if ((n<numlines)); then
		echo "  $col," >> $cmd
	else
		echo "  $col" >> $cmd
	fi
	((n=n+1))
done < $desccolrc
echo "    ) values ( "  >> $cmd
echo "        now()," >> $cmd
echo "        'I'," >> $cmd
echo "        'N'," >> $cmd
((n=1))
while read col; do
	if ((n<numlines)); then
		echo "        new.$col," >> $cmd
	else
		echo "        new.$col" >> $cmd
	fi
	((n=n+1))
done < $desccolrc
echo  "        );" >> $cmd
echo  "END\$\$" >> $cmd
echo  "DELIMITER ;" >> $cmd
mysql -s -u$uid -p$pass $mydb < $cmd > /dev/null 2>&1
echo "Created insert trigger ${table}_jagtrgins"


##################### create mysql update trigger
echo "drop trigger if exists ${table}_jagtrgupd;" > $cmd
echo "DELIMITER \$\$" >> $cmd
echo "CREATE TRIGGER ${table}_jagtrgupd AFTER UPDATE ON ${table} FOR EACH ROW" >> $cmd
echo "  BEGIN" >> $cmd
echo "    INSERT INTO $changelog ( ts_, action_, status_, "  >> $cmd
((n=1))
while read col; do
	if ((n<numlines)); then
		echo "  $col," >> $cmd
	else
		echo "  $col" >> $cmd
	fi
	((n=n+1))
done < $desccolrc
echo "    ) values ( "  >> $cmd
echo "        now()," >> $cmd
echo "        'U'," >> $cmd
echo "        'N'," >> $cmd
((n=1))
while read line; do
	if ((n<numlines)); then
		echo "        new.$line," >> $cmd
	else
		echo "        new.$line" >> $cmd
	fi
	((n=n+1))
done < $desccolrc
echo  "        );" >> $cmd
echo  " END\$\$" >> $cmd
echo  "DELIMITER ;" >> $cmd
mysql -s -u$uid -p$pass $mydb < $cmd > /dev/null 2>&1
echo "Created update trigger ${table}_jagtrgupd"


##################### create mysql delete trigger
echo "drop trigger if exists ${table}_jagtrgdel;" > $cmd
echo "DELIMITER \$\$" >> $cmd
echo "CREATE TRIGGER ${table}_jagtrgdel AFTER DELETE ON ${table}" >> $cmd
echo "  FOR EACH ROW" >> $cmd
echo "  BEGIN" >> $cmd
echo "    INSERT INTO $changelog ( ts_, action_, status_, "  >> $cmd
((n=1))
while read col; do
	if ((n<numlines)); then
		echo "  $col," >> $cmd
	else
		echo "  $col" >> $cmd
	fi
	((n=n+1))
done < $desccolrc
echo "    ) values ( "  >> $cmd
echo "        now()," >> $cmd
echo "        'D'," >> $cmd
echo "        'N'," >> $cmd
((n=1))
while read line; do
	if ((n<numlines)); then
		echo "        old.$line," >> $cmd
	else
		echo "        old.$line" >> $cmd
	fi
	((n=n+1))
done < $desccolrc
echo  "        );" >> $cmd
echo  " END\$\$" >> $cmd
echo  "DELIMITER ;" >> $cmd
mysql -s -u$uid -p$pass $mydb < $cmd > /dev/null 2>&1
echo "Created delete trigger ${table}_jagtrgdel"


/bin/rm -f $cmd
/bin/rm -f $descrc
/bin/rm -f $desccolrc
cd $pd
/bin/rm -rf $dirn
