#include <iostream>
#include <stdio.h>
#include <string.h>
#include <string>
#include <stdlib.h>
#include <mysql/mysql.h>
#include <sstream>
#include <vector>
#include "JaguarAPI.h"
#include "postgreimport.h"

using namespace std;


/*
*	Function to print out the help message
*	@param NULL
*	@Author Robert
*/
void print_help( const char *prog ) 
{
	cout << prog << " <PARAMETERS> " << endl;
	cout << "Required PARAMETERS:" << endl;
	cout << "\t-srcdbtype  database_type (mysql or postgres)" << endl;
	cout << "\t-srcdb      source_database_name" << endl;
	cout << "\t-destdb     dest_database_name" << endl;
	cout << "\t-srchost    source_ip_address" << endl;
	cout << "\t-srcport    source_port (mysql port is 3306)" << endl;
	cout << "\t-srcuser    source_user_name" << endl;
	cout << "\t-srcpass    source_password" << endl;
	cout << "\t-desthost   dest_ip_adress" << endl;
	cout << "\t-destport   dest_port (jaguar port is 8888)" << endl;
	cout << "\t-destuser   jaguar username" << endl;
	cout << "\t-destpass   jaguar user password" << endl;
	cout << "Optional:" << endl;
	cout << "\t-table      table_name (if not provided, all tables are imported)" << endl;
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
			print_help( argv[0] );
			return 1;
		}

	}
	
	//Check if theres enough parameters
	if(argc < 23) {
		cout<<"Missing parameters"<<endl;
		print_help( argv[0] );
		return 1;
	} 

	
	for(int i = 1; i<argc; i++){//Parsing the arguments
		if(i+1<argc) {
			string curr_arg = string(argv[i]);
			string curr_argv = argv[++i];
			if(curr_arg == "-srcdb")source_database = curr_argv;
			if(curr_arg == "-table") {
				source_tbl = curr_argv;
				tbl_defined = 1;
			}

			if(curr_arg == "-destdb") dest_database = curr_argv;
			if(curr_arg == "-srcdbtype") source_dbtype = curr_argv;
			if(curr_arg == "-srchost") source_host = curr_argv;
			if(curr_arg == "-srcport") source_port = curr_argv;
			if(curr_arg == "-srcuser") source_username = curr_argv;
			if(curr_arg == "-srcpass") source_password = curr_argv;
			if(curr_arg == "-desthost") dest_host = curr_argv;
			if(curr_arg == "-destport") dest_port = curr_argv;
			if(curr_arg == "-destuser") dest_username = curr_argv;
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
	

}
