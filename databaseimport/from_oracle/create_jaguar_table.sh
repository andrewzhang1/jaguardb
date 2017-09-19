#!/bin/bash

##########################################################################################
##
## Usage:   ./create_jaguar_table.sh  <ORACLE_TABLE>
##
##########################################################################################

### col  type(precision,scale)   (*,scale)==(38,scale)
## precision is total length in number, scale is length after the dot
g_col=""
g_type=""
g_precision=""
g_scale=""
g_typestr=""
function getColType()
{
	line=$1
	#echo "s9390 line=$line"
	g_col=`echo $line|awk '{print $1}'`
	typ=`echo $line|awk '{print $2}'`
	#echo "s2920 g_col=$g_col   typ=$typ"
	### typ "number or number(32) or number(21, 4) or char(21) or timestamp 
	if echo $typ |grep -q '('; then
		otype=`echo $typ|awk -F'(' '{print $1}'`
		t1=`echo $typ|awk -F'(' '{print $2}'`
		t2=`echo $t1|tr -d ')'`
		#echo "s8281 otype=$otype  t2=$t2"
		## t2 is  32 or 12,4 
		if echo $t2|grep -q ',' 
		then
			g_precision=`echo $t2| awk -F',' '{print $1}'| tr -d ' '`
			if [[ "x*" = "x$g_precision" ]]; then
				g_precision=38
			fi
			g_scale=`echo $t2| awk -F',' '{print $2}' | tr -d ' '`
		else
			g_precision=`echo $t2|tr -d  ' '`
			g_scale=0
		fi
	else
		otype=$typ
		g_precision=0
		g_scale=0
		#echo "s8283 otype=$otype  g_precision=$g_precision g_scale=$g_scale"
	fi

	if [[ "x$otype" = "xvarchar" ]]; then
		g_type="char"
		g_typestr="char($g_precision)"
	elif [[ "x$otype" = "xchar" ]]; then
		g_type="char"
		g_typestr="char($g_precision)"
	elif [[ "x$otype" = "xvarchar2" ]]; then
		g_type="char"
		g_typestr="char($g_precision)"
	elif [[ "x$otype" = "xnumber" ]]; then
		if ((g_precision>0)) && ((g_scale<1)); then
			g_type="bigint"
			g_typestr="bigint"
		elif ((g_precision>0)) && ((g_scale>0)); then
			g_type="double"
			g_typestr="double($g_precision,$g_scale)"
		else
			g_type="bigint"
			g_typestr="bigint"
		fi
	elif [[ "x$typ" = "xdate" ]]; then
		g_type="date"
		g_typestr="date"
	elif [[ "x$typ" = "xtime" ]]; then
		g_type="time"
		g_typestr="time"
	elif [[ "x$typ" = "xnchar" ]]; then
		((g_precision=6*g_precision))
		g_type="char"
		g_typestr="char($g_precision)"
	elif [[ "x$typ" = "xnvarchar2" ]]; then
		g_type="char"
		((g_precision=6*g_precision))
		g_typestr="char($g_precision)"
	elif [[ "x$typ" = "xblob" ]]; then
		g_type="char"
		((g_precision=10000))
		echo "$g_col BLOB, please take care of it"
		g_typestr="char($g_precision)"
	elif [[ "x$typ" = "xclob" ]]; then
		g_type="char"
		((g_precision=10000))
		echo "$g_col CLOB, please take care of it"
		g_typestr="char($g_precision)"
	elif [[ "x$typ" = "xtimestamp" ]]; then
		if ((g_precision==9)); then
			g_type="datetimenano"
			g_typestr="datetimenano"
		else
			g_type="datetime"
			g_typestr="datetime"
		fi
	elif [[ "x$typ" = "xbinary_float" ]]; then
			g_type="float"
			g_typestr="float(20,6)"
	elif [[ "x$typ" = "xbinary_double" ]]; then
			g_type="float"
			g_typestr="float(38,12)"
	elif [[ "x$typ" = "xraw" ]]; then
			g_type="char"
			g_typestr="char($g_precision)"
	elif [[ "x$typ" = "xrowid" ]]; then
			g_type="uuid"
			g_typestr="uuid"
	elif [[ "x$typ" = "xint" ]]; then
			g_type="int"
			g_typestr="int"
	else
		g_type=$otype
		g_typestr=$otype
	fi

	#echo "s3372 g_typestr=$g_typestr"

}

######################## main ###########################

table=$1

if [[ "x$table" = "x" ]]; then
	echo "Usage:     $0  <TABLE_NAME>"
	echo
	echo "Example:   $0  table123 "
	exit 1
fi

if type sqlplus; then
	echo "sqlplus is found, continue ..."
else
	echo "sqlplus is not found, quit"
	exit 1
fi

pd=`pwd`
dirn="tmpdir$$"
/bin/mkdir -p $dirn
cd $dirn

echo -n "Enter Oracle user name: "
read uid
echo -n "Enter $uid password: "
read -s pass
echo

cmd="tmpcmd.sql"
log="describe.log"
echo "spool $log;" > $cmd
echo "describe $table;" >> $cmd
echo "spool off;" >> $cmd

sqlplus -S $uid/$pass < $cmd  >/dev/null
/bin/rm -f $cmd
descrc="describe_${table}.txt"
desccolrc="describe_${table}_colname.txt"
desctyperc="describe_${table}_coltype.txt"
cat $log|tr '[:upper:]' '[:lower:]'|grep -v 'SQL>' |grep -vi 'null?'|grep -v '\-\-\-\-\-\-\-'|sed -e 's/not.*null//gI' -e 's/ \+/ /g' -e '/^$/d' > $descrc
/bin/rm -f $log
awk '{print $1}' $descrc > $desccolrc
awk '{print $2}' $descrc > $desctyperc


##################### get key columns of oracle table
TABLE=`echo $table | tr '[:lower:]' '[:upper:]'`
lowtable=`echo $table | tr '[:upper:]' '[:lower:]'`
echo "SELECT cols.column_name
FROM all_constraints cons, all_cons_columns cols
WHERE cols.table_name = '$TABLE'
AND cons.constraint_type = 'P'
AND cons.constraint_name = cols.constraint_name
AND cons.owner = cols.owner;" > $cmd
sqlplus -S $uid/$pass < $cmd | grep -v 'COLUMN_NAME' |grep -v '\-\-\-\-\-' > keycols.txt.upper
cat keycols.txt.upper|tr '[:upper:]' '[:lower:]' > keycols.txt


##################### create jaguar table
/bin/rm -f $cmd
numlines=`wc -l $descrc|cut -d' ' -f1`
#echo "drop table if exists $lowtable;" >> $cmd
echo "create table $lowtable (" >> $cmd
### key cols first
((n=1))
while read line
do
	col=`echo $line|awk '{print $1}'`
	typ=`echo $line|awk '{print $2}'`
	#echo "s7320 col=[$col] typ=[$typ]"
	if ! grep -q $col keycols.txt; then
		#echo "s4372 keycols.txt has no $col skipp..."
		continue
	fi 
    #echo "s1282 getColType $line"
    getColType "$line"

	if ((n==1)); then
		echo "  key:" >> $cmd
	fi
	echo "    $col $g_typestr," >> $cmd
	((n=n+1))
done < $descrc



### value columns
((n=1))
while read line
do
	col=`echo $line|awk '{print $1}'`
	typ=`echo $line|awk '{print $2}'`
	if grep -q $col keycols.txt; then
		continue
	fi 
    getColType "$line"

	if ((n==1)); then
		echo "  value: $col $g_typestr, " >> $cmd
	elif ((n<numlines)); then
		echo "    $col $g_typestr," >> $cmd
	else
		echo "    $col $g_typestr" >> $cmd
	fi
	((n=n+1))
done < $descrc

echo  "); " >> $cmd

cmd2="${cmd}.tmprc"
tr '[:upper:]' '[:lower:]' < $cmd > $cmd2
/bin/mv -f $cmd2 $cmd

if [[ -f $HOME/.jaguarhome ]]; then
	export JAGUAR_HOME=`cat $HOME/.jaguarhome`
else
	export JAGUAR_HOME=$HOME
fi

#cat $cmd
echo "Creating $table in Jaguar database"
echo -n "Which database do you want the table $table to be created in ? "
read db
echo -n "Enter Jaguar user name: "
read uid
echo -n "Enter $uid password: "
read -s pass
echo
port=`cat $JAGUAR_HOME/jaguar/conf/server.conf |grep PORT|grep -v '#'|cut -d= -f2`
uo=`uname -o`
echo "Creating table, please wait a few seconds ..."
if [[ "x$uo" = "xMsys" ]] || [[ "x$uo" = "xCygwin" ]]; then
	$JAGUAR_HOME/jaguar/bin/jql.exe -u $uid -p $pass -h :$port -d $db -f $cmd -q
else
	$JAGUAR_HOME/jaguar/bin/jql.bin -u $uid -p $pass -h :$port -d $db -f $cmd -q
fi

cd $pd
/bin/rm -rf $dirn
