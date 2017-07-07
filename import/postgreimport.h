#ifndef POSTGREIMPORT_H
#define POSTGREIMPORT_H

#include <iostream>
#include <stdio.h>
#include <string.h>
#include <string>
#include <stdlib.h>
#include <mysql/mysql.h>
#include <sstream>
#include <vector>
#include <libpq-fe.h>
#include "JaguarAPI.h"

using namespace std;

class postgreimport 
{
    private:
        PGconn * pg_conn;
        PGconn * pg_schema_conn;
        PGresult * pg_tbl_res;
        PGresult * pg_schema_res;
        PGresult * pg_res;

        PGresult * pg_field_type;
        string pg_temp;
        int pg_rc;
        
        string pg_ip;
        string pg_port;
        string pg_username;
        string pg_password;
        string pg_database;
        string pg_schema;
        string pg_jaguar_host;
        string pg_jaguar_port;
        string pg_jaguar_uid;
        string pg_jaguar_pass;
        string pg_jaguar_db;
        int    pg_tbl_defined;
        string pg_source_tbl;
                                        
    public:
        void initialize( char const * ip, char const * port, char const * username, char const * password,
						 char const * database, const char * schema, char const * jaguar_host, 
						 char const * jaguar_port, char const * jaguar_uid, char const * jaguar_pass, 
						 char const * jaguar_db, int tbl_defined, const char *source_tbl );
		int run();
	

};
#endif
