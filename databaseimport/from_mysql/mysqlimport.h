#ifndef MYSQLIMPORT_H
#define MYSQLIMPORT_H

#include <iostream>
#include <stdio.h>
#include <string.h>
#include <string>
#include <stdlib.h>
#include <mysql/mysql.h>
#include <sstream>
#include <vector>
#include "JaguarAPI.h"

using namespace std;

class mysqlimport {
        private:
        MYSQL * conn;
        MYSQL * schema_conn;
        MYSQL_RES * tbl_res;
        MYSQL_RES * schema_res;
        MYSQL_RES * res;
        MYSQL_ROW row;
        MYSQL_ROW curr_schema;
        MYSQL_ROW curr_tbl;

        MYSQL_RES * field_type;
        MYSQL_ROW curr_field;
        string temp;
        int rc;

        //The following two dbs are created automatically; Remove this when the code can detect system created dbs automatically
        string info_schema;
        string my_db;
        string ip;
        int port;
        string username;
        string password;
        string  database;
        string schema;
        string  jaguar_host;
        int jaguar_port;
        string jaguar_uid;
        string jaguar_pass;
        string jaguar_db;
        int tbl_defined;
        string source_tbl;
                                        
        public:
        void initialize( char const * ip, int port, char const * username, char const * password, char const * database,
        				 const char * schema, char const * jaguar_host, int jaguar_port, char const * jaguar_uid, 
						 char const * jaguar_pass, char const * jaguar_db, int tbl_defined, const char* source_tbl);
		int run();
	
        


};
#endif
