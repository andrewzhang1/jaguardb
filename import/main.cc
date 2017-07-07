#include <iostream>
#include <stdio.h>
#include <string.h>
#include <string>
#include <stdlib.h>
#include <mysql/mysql.h>
#include <sstream>
#include <vector>
#include "JaguarAPI.h"
#include "mysqlimport.h"
#include "postgreimport.h"

using namespace std;


/*
*	Function to print out the help message
*	@param NULL
*	@Author Robert
*/
void print_help() {
	cout << "Required:" << endl;
	cout << "\t-srcdb\t\tsource_database_name" << endl;
	cout << "\t-destdb\t\tdest_database_name" << endl;
	cout << "\t-srcdbtype\t\tdatabase_type" << endl;
	cout << "\t-srchost\t\tsource_ip_address" << endl;
	cout << "\t-srcport\t\tsource_ip_port" << endl;
	cout << "\t-srcusr\t\tsource_user_name" << endl;
	cout << "\t-srcpass\t\tsource_password" << endl;
	cout << "\t-desthost\t\tdest_ip_adress" << endl;
	cout << "\t-destport\t\tdest_port" << endl;
	cout << "\t-destusr\t\tdest_username" << endl;
	cout << "\t-destpass\t\tdest_password" << endl;
	cout << "Optional:" << endl;
	cout << "\t-tbl\t\ttable_name" << endl;
}


	
int main(int argc, char* argv[]){

	string source_host="";
	string source_port="";
	string source_username="";
	string source_password="";
	string source_database="";
	string source_tbl="";
	string source_dbtype="";

	string dest_database="";
	string dest_host="";
	string dest_port="";
	string dest_username="";
	string dest_password="";
	int tbl_defined = 0;

	//Seach for --help and return if exists
	for (int i = 1; i < argc; i++) {
		if (string(argv[i]) == "--help") {
			print_help();
			return 1;
		}
		//Test for postpq
		if(string(argv[i]) == "--test"){
			postgreimport * pg = new postgreimport();
			pg->run();
			delete pg;
		}
	}
	
	//Check if theres enough parameters
	if(argc < 23) {
	//cout<<"Usage:-srcdb srcdbname -destdb destdbname (-tbl tablename) -srcdbtype dbtype -srchost host1 -srcport port1 -srcusr username -srcpass password -desthost host2 -destport port2 -destusr username -destpass password"<<endl;	
		cout<<"Missing parameter, please use --help to see instruction"<<endl;
		return 1;
	} 

	
	for(int i = 1; i<argc; i++){//Parsing the arguments
		if(i+1<argc) {
			string curr_arg = string(argv[i]);
			string curr_argv = argv[++i];
			if(curr_arg == "-srcdb")source_database = curr_argv;
			if(curr_arg == "-tbl") {
				source_tbl = curr_argv;
				tbl_defined = 1;
			}

			if(curr_arg == "-destdb") dest_database = curr_argv;
			if(curr_arg == "-srcdbtype") source_dbtype = curr_argv;
			if(curr_arg == "-srchost") source_host = curr_argv;
			if(curr_arg == "-srcport") source_port = curr_argv;
			if(curr_arg == "-srcusr") source_username = curr_argv;
			if(curr_arg == "-srcpass") source_password = curr_argv;
			if(curr_arg == "-desthost") dest_host = curr_argv;
			if(curr_arg == "-destport") dest_port = curr_argv;
			if(curr_arg == "-destusr") dest_username = curr_argv;
			if(curr_arg == "-destpass") dest_password = curr_argv;
			
		}
	}
	
	char const * ip = source_host.c_str();
	int port = atoi(source_port.c_str());
	char const * username = source_username.c_str();
	char const * password = source_password.c_str();
	char const * database = source_database.c_str();
	char * schema;
	char const * jaguar_host = dest_host.c_str();
	int jaguar_port = atoi(dest_port.c_str());
	char const * jaguar_uid = dest_username.c_str();
	char const * jaguar_pass = dest_password.c_str();
	char const * jaguar_db = dest_database.c_str();
	

	if(source_dbtype != "mysql") {//TO-DO: Remove this when more db are supported
		cout<<"mysql support only, current db input:"<<source_dbtype<<endl;
		return 1;
	}
	
	if (source_dbtype=="mysql") {	
		mysqlimport * imp = new mysqlimport();

		imp->initialize( ip,port,username,password,database,schema,jaguar_host,jaguar_port,
						 jaguar_uid,jaguar_pass,jaguar_db,tbl_defined,source_tbl.c_str() );

		return imp->run();

	} else {
		return 1;
	}
}
