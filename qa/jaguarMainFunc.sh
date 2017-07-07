#!/bin/bash

# Main test to control the batch sql
# Script Name: jaguarMainFunc.sh
# Created by: Andrew Zhang
# Date: June 27, 2015
#

# Test 1:
# Create database and grant ussers; 

port=`grep PORT $HOME/jaguar/conf/server.conf |grep -v '#' | awk -F= '{print $2}'`

/bin/mkdir -p jaguarMainFunc.log
logf="jaguarMainFunc.log/jaguarFunc_sql_`date +%Y%m%d_%H%M%S`.log"
export PATH=$PATH:$HOME/jaguar/bin


echo "##### Main Function Test #####" 2>&1 | tee -a $logf
echo "##############################" 2>&1 | tee -a $logf

echo "Part 1: Now Running: Use jadmin to create user, db, etc" 2>&1 | tee -a $logf
echo "#########################################################" 2>&1 | tee -a $logf
echo " "  >> $logf


# Test delete and create database
echo $HOME/jaguar/bin/jql -u admin -p jaguar -e "dropdb myJaguar1" -h :$port -v yes
$HOME/jaguar/bin/jql -u admin -p jaguar -e "dropdb myJaguar1" -h :$port -v yes
echo "dropdb myJaguar1"

$HOME/jaguar/bin/jql -u admin -p jaguar -e "createdb myJaguar1" -h :$port -v yes
echo "createdb myJaguar1"

$HOME/jaguar/bin/jql -u admin -p jaguar -e "createdb myJaguar2" -h :$port -v yes
echo "createdb myJaguar2"

$HOME/jaguar/bin/jql -u admin -p jaguar -e "createuser user_myJaguar1:password" -h :$port -v yes
echo "createuser user_myJaguar1:password"

$HOME/jaguar/bin/jql -u admin -p jaguar -e "createuser user_myJaguar2:password" -h :$port -v yes
echo "jadmin createuser user_myJaguar2:password"

$HOME/jaguar/bin/jql -u admin -p jaguar -e "dropdb myJaguar2" -h :$port -v yes
echo "dropdb myJaguar2"

$HOME/jaguar/bin/jql -u admin -p jaguar -e "dropuser user_myJaguar2" -h :$port -v yes
echo "dropuser user_myJaguar2"

$HOME/jaguar/bin/jql -u admin -p jaguar -e "createuser test:test" -h :$port -v yes
echo "createuser test:test"

echo "Now run jaguarFunc.sql:" 2>&1 | tee -a $logf
echo "#####################" 2>&1 | tee -a $logf

ps -ef |grep jaguar 2>&1 | tee -a $logf

sleep 10
echo "$HOME/jaguar/bin/jql -u user_myJaguar1 -p password -d myJaguar1  -h 127.0.0.1:$port  -v yes"
$HOME/jaguar/bin/jql -u user_myJaguar1 -p password -d myJaguar1  -h 127.0.0.1:$port  -v yes < jaguarFunc.sql 2>&1 | tee -a $logf

