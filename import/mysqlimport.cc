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

using namespace std;

//class mysqlimport {
/**************
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
	const char * mysqlimport::info_schema = "information_schema";
	const char * mysqlimport::my_db = "mydb";


	char const * ip;
	int port;
	char const * username;
	char const * password;
	char const * database;
	char * schema;
	char const * jaguar_host;
	int jaguar_port;
	char const * jaguar_uid;
	char const * jaguar_pass;
	char const * jaguar_db;
	int tbl_defined;
	string source_tbl;
	*******/

	


	
	void mysqlimport::initialize(char const * ip, int port, char const * username, char const * password, 
									char const * database, const char * schema, char const * jaguar_host, int jaguar_port,
									char const * jaguar_uid, char const * jaguar_pass, char const * jaguar_db,
									int tbl_defined, const char * source_tbl) 
	{
		info_schema = "information_schema";
		my_db = "mydb";

		this->ip = ip;
		this->port = port;
		this->username = username;
		this->password = password;
		this->database = database;
		this->schema = schema;
		this->jaguar_host = jaguar_host;
		this->jaguar_port = jaguar_port;
		this->jaguar_uid = jaguar_uid;
		this->jaguar_pass = jaguar_pass;
		this->jaguar_db = jaguar_db;
		this->tbl_defined = tbl_defined;
		this->source_tbl = source_tbl;
	}

	int mysqlimport::run() 
	{
		JaguarAPI jdb;
		if (!jdb.connect(jaguar_host.c_str(), jaguar_port, jaguar_uid.c_str(), jaguar_pass.c_str(), jaguar_db.c_str() )) {
			printf("Error connecting Jaguar database jaguar_host=[%s] jaguar_port=[%s] jaguar_uid=[%s] jaguar_pass=[%s] jaguar_db=[%s]\n",
				jaguar_host.c_str() , jaguar_port , jaguar_uid.c_str() , jaguar_pass.c_str() , jaguar_db.c_str() );
			jdb.close();
			exit(1);
		}

		//Create connection
		conn = mysql_init(NULL);

		conn = mysql_real_connect(conn, ip.c_str(), username.c_str(), password.c_str(), NULL, port, NULL, 0);
		// if (!mysql_real_connect(&mysql,"host","user","passwd","database",0,NULL,0))
		mysql_query(conn, "show databases");
		schema_res = mysql_store_result(conn);


		//TO-DO: Looping through schemas is not available at the moment
		while (curr_schema = mysql_fetch_row(schema_res)) {
			char const * db_name = curr_schema[0];
			if (!strcmp(db_name, info_schema.c_str() ) || !strcmp(db_name, my_db.c_str() )) continue;
			cout << "---------------------------FETCHING FROM DATABASE:" << curr_schema[0] << endl << endl;
			temp = "use ";
			temp += db_name;
			char const * schema_query = temp.c_str();
			mysql_query(conn, schema_query);
			//Connect to database
			tbl_res = mysql_list_tables(conn, NULL);
			int num_tbls = mysql_num_rows(tbl_res);

			//Loop through tables
			while (curr_tbl = mysql_fetch_row(tbl_res)) {
				//Get the field_types
				temp = "show columns from ";
				string temp_tbl_name = "";
				if (tbl_defined) {
					temp_tbl_name = source_tbl;
				}
				else {
					temp_tbl_name = curr_tbl[0];
				}
				char const * tbl_name = temp_tbl_name.c_str();
				temp += tbl_name;
				char const * field_query = temp.c_str();
				cout << "Start transfering data from table: " << db_name << "." << tbl_name << endl;
				cout << "---------------Running query: " << field_query << " on: " << tbl_name << endl;

				if (mysql_query(conn, field_query)) {
					fprintf(stderr, "%s\n", mysql_error(conn));
					exit(1);
				}

				if (!(field_type = mysql_store_result(conn))) {
					printf("Error Storing Result \n");
					exit(1);
				}

				//Start of table creation
				string create_table_query = "create table ";
				string field_names = "";//Used for insertion
				create_table_query += tbl_name;
				create_table_query += "( key: ";

				//Loop through columns and save to vector:
				vector<string> key_type_vector;
				vector<string> key_vector;
				vector<string> column_type_vector;
				vector<string> column_vector;
				int auto_key = 1;
				int max_char = 0;//Max length of char
				string max_char_string = "0";
				while ((curr_field = mysql_fetch_row(field_type)) != NULL) {

					string column = curr_field[0];

					//convert mysql type to jaguar type
					string type = curr_field[1];

					//primary key field
					string pk = curr_field[3];

					//TOUPPER the field
					for (int i = 0; i<type.length();i++) {
						type[i] = toupper(type[i]);
					}

					//Change datatype to the correct one		
					size_t f = type.find("VARCHAR");
					if (f != std::string::npos) {
						type.replace(f, string("VARCHAR").length(), "CHAR");
						string type_size_string = type.substr(type.find("(") + 1);
						type_size_string = type_size_string.substr(0, type_size_string.size() - 1);

						int char_length;
						istringstream(type_size_string) >> char_length;
						if (char_length > max_char) {
							max_char = char_length;
							max_char_string = type_size_string;
						}
					}

					f = type.find("DATE");
					if (f != std::string::npos && type.find("DATETIME") == std::string::npos) {
						type.replace(f, string("DATE").length(), "DATETIME");
					}

					f = type.find("TIME");
					if (f != std::string::npos && type.find("DATETIME") == std::string::npos) {
						type.replace(f, string("TIME").length(), "DATETIME");
					}

					if (pk.length()>0) {
						key_vector.push_back(column);
						key_type_vector.push_back(type);
						auto_key = 0;
					}
					else {
						column_vector.push_back(column);
						column_type_vector.push_back(type);
					}
					//printf("%s    %s       %s    %s\n", curr_field[0],curr_field[1],curr_field[2],curr_field[3]);
				}

				if (key_vector.size()>0) {
					for (int i = 0; i< key_vector.size();i++) {
						if (key_type_vector[i].find("CHAR") != std::string::npos) {
							key_type_vector[i].replace(5, key_type_vector[i].size() - 6, max_char_string);
						}

						if (key_type_vector[i].find("INT") != std::string::npos) {
							key_type_vector[i] = "INT";
						}

						create_table_query += key_vector[i];
						create_table_query += " ";
						create_table_query += key_type_vector[i];
						create_table_query += ",";
					}
				}
				else {
					create_table_query += "uuid uuid,";
					key_vector.push_back("uuid");
					key_type_vector.push_back("uuid");
				}

				create_table_query += "value: ";
				for (int i = 0; i< column_vector.size();i++) {
					string temp = column_type_vector[i];
					size_t char_pos = temp.find("CHAR");
					if (char_pos != std::string::npos) {
						column_type_vector[i].replace(5, column_type_vector[i].size() - 6, max_char_string);
					}

					if (temp.find("INT") != std::string::npos) {
						column_type_vector[i] = "INT";
					}
					create_table_query += column_vector[i];
					create_table_query += " ";
					create_table_query += column_type_vector[i];
					create_table_query += ",";
				}

				create_table_query = create_table_query.substr(0, create_table_query.size() - 1);
				create_table_query += ");";

				cout << "===============Executing Jaguar Query:" << create_table_query << endl;
				char const * final_create_table_query = create_table_query.c_str();
				rc = jdb.execute(final_create_table_query);
				if (!rc) {
					printf("Table creation error\n");
					cout << "Err msg:" << jdb.error() << endl;
					jdb.close();
					exit(1);
				}
				mysql_free_result(field_type);

				//End of table creation	

				//Start of data transfering
				//Fetch data from MySQL	
				temp = "select * from ";
				temp += tbl_name;

				char const * query = temp.c_str();

				cout << "---------------Executing MySQL Query: " << query << endl;


				if (mysql_query(conn, query)) {
					fprintf(stderr, "%s\n", mysql_error(conn));
					exit(1);
				}

				if (!(res = mysql_store_result(conn))) {
					printf("Error Storing Result \n");
					exit(1);
				}


				//Loop through columns and create the Jaguar query
				int uid = 0;
				while ((row = mysql_fetch_row(res)) != NULL) {
					//cout<<"entering table loop"<<endl;

					string insertion_query = "insert into ";
					insertion_query += tbl_name;
					insertion_query += " ( ";
					if (!auto_key) {
						for (int i = 0; i< key_vector.size();i++) {
							insertion_query += key_vector[i];
							insertion_query += ",";
						}
					}

					for (int i = 0; i< column_vector.size();i++) {
						insertion_query += column_vector[i];
						insertion_query += ",";
					}
					insertion_query = insertion_query.substr(0, insertion_query.size() - 1);
					insertion_query += " ) values (";

					//Generated ID
					if (auto_key) {
						//ostringstream ss;
						//ss<<uid;
						//string uid_str = ss.str();
						//insertion_query += uid_str;
						//insertion_query += ",";
					}
					else {
						for (int i = 0; i< key_vector.size();i++) {
							size_t int_pos = key_vector[i].find("INT");
							string single_quote_place_holder = "'";//This is for differenting char from other types
							if (int_pos != std::string::npos) {
								single_quote_place_holder = "";
							}
							insertion_query = insertion_query + single_quote_place_holder + row[i] + single_quote_place_holder + ",";
						}
					}

					//Loop through columns
					for (int i = key_vector.size() - 1; i<column_vector.size() + key_vector.size() - 1; i++) {
						size_t int_pos = column_vector[i].find("INT");
						string single_quote_place_holder = "'";//This is for differenting char from other types
						if (int_pos != std::string::npos) {
							single_quote_place_holder = "";
						}
						insertion_query = insertion_query + single_quote_place_holder + row[i] + single_quote_place_holder + ",";
					}
					insertion_query = insertion_query.substr(0, insertion_query.size() - 1);
					insertion_query += ");";
					char const * final_insertion_query = insertion_query.c_str();
					cout << "===============Executing Jaguar Query:" << final_insertion_query << endl;

					rc = jdb.execute(final_insertion_query);
					if (!rc) {
						printf("Error jagQuery insert rc=%d  errmsg=[%s]\n", rc, jdb.error());
						cout << "Error query:" << final_insertion_query << endl;
						jdb.close();
						exit(1);
					}
					uid++;
					//End of a line
				}
				//cout<<"insertion complete"<<endl;
				mysql_free_result(res);
				//cout<<"free complete"<<endl;
				cout << "(OK) Table: <" << tbl_name << "> transmitted" << endl << endl << endl;
				if (tbl_defined) break;
				//End of a table
			}

			break;//Remove this when the database iteration is online. This is the end of one database
		}

		cout << "Complete" << endl;
		mysql_free_result(schema_res);
		mysql_free_result(tbl_res);
		jdb.close();
		mysql_close(conn);

		return 0;

	}
//};
