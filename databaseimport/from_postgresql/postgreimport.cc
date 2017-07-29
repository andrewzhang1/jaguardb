#include <iostream>
#include <stdio.h>
#include <string.h>
#include <string>
#include <stdlib.h>
#include <sstream>
#include <vector>
#include "JaguarAPI.h"
#include "postgreimport.h"

using namespace std;

	
void postgreimport::initialize(char const * ip, char const * port, char const * username, char const * password,
								   char const * database, const char * schema, char const * jaguar_host, 
								   char const * jaguar_port, char const * jaguar_uid, char const * jaguar_pass, 
								   char const * jaguar_db, int tbl_defined, const char *source_tbl) 
{
		this->pg_ip = ip;
		this->pg_port = port;
		this->pg_username = username;
		this->pg_password = password;
		this->pg_database = database;
		this->pg_schema = schema;
		this->pg_jaguar_host = jaguar_host;
		this->pg_jaguar_port = jaguar_port;
		this->pg_jaguar_uid = jaguar_uid;
		this->pg_jaguar_pass = jaguar_pass;
		this->pg_jaguar_db = jaguar_db;
		this->pg_tbl_defined = tbl_defined;
		this->pg_source_tbl = source_tbl;
}

int postgreimport::run() 
{
		JaguarAPI jdb;
		if (!jdb.connect( pg_jaguar_host.c_str(), atoi(pg_jaguar_port.c_str()), pg_jaguar_uid.c_str(),
						  pg_jaguar_pass.c_str(), pg_jaguar_db.c_str()  )) {
			printf("Error connecting Jaguar host=[%s] port=[%s] uid=[%s] pass=[%s] jaguar_db=[%s]\n",
				pg_jaguar_host.c_str(), pg_jaguar_port.c_str(), pg_jaguar_uid.c_str(), pg_jaguar_pass.c_str(), pg_jaguar_db.c_str());
			jdb.close();
			exit(1);
		}


		//Create string
		char connection_info[500];
		strcpy(connection_info,"host=");
		strcat(connection_info,pg_ip.c_str());
		strcat(connection_info,"port=");
		strcat(connection_info,pg_port.c_str());
		strcat(connection_info,"user=");
		strcat(connection_info,pg_username.c_str());
		strcat(connection_info,"password=");
		strcat(connection_info,pg_password.c_str());
		strcat(connection_info,"dbname=");
		strcat(connection_info, pg_database.c_str());
		//Create connection to postgre
		
		pg_conn = PQconnectdb(connection_info);

		string temp;
		int rc;

		//TO-DO: Looping through schemas is not available at the moment
		//while (curr_schema = mysql_fetch_row(schema_res)) {
			char const * db_name = pg_database.c_str();
			//Connect to database
			pg_tbl_res = PQexec(pg_conn, "show tables" );
			int num_tbls = PQntuples(pg_tbl_res);

			//Loop through tables
			for(int curr_tbl=0;curr_tbl<num_tbls;curr_tbl++) {
				//Get the field_types
				temp = "SELECT * FROM ";
				string temp_tbl_name = PQgetvalue(pg_tbl_res,curr_tbl,0);
				
				char const * tbl_name = temp_tbl_name.c_str();
				temp += tbl_name;
				temp += " WHERE Id = 0";
				char const * field_query = temp.c_str();
				cout << "Start transfering data from table: " << db_name << "." << tbl_name << endl;
				cout << "---------------Running column query: " << field_query << " on: " << tbl_name << endl;

				PGresult *column_fetch_res = PQexec(pg_conn,temp.c_str()); 

				//Loop through columns and save to vector:(key and type not available for postgre for now)
					//vector<string> key_type_vector;
					//vector<string> key_vector;
					//vector<string> column_type_vector;
				vector<string> column_vector;

				//push column names
				int ncols = PQnfields(column_fetch_res);
				printf("There are %d columns\n",ncols);
				for(int i=0;i<ncols;i++){
					char * name = PQfname(column_fetch_res,i);
					printf("%s\n",name);
					column_vector.push_back(name);
				}	

				//Start of table creation
				string create_table_query = "create table ";
				string field_names = "";//Used for insertion
				create_table_query += tbl_name;
				create_table_query += "( key: ";


				int auto_key = 1;
				int max_char = 0;//Max length of char
				string max_char_string = "0";
				//Type conversion deleted, not available for postgre for now

				create_table_query += "uuid uuid,";
//				key_vector.push_back("uuid");
	//			key_type_vector.push_back("uuid");


				create_table_query += "value: ";
				for (int i = 0; i< column_vector.size();i++) {
					//string temp = column_type_vector[i];
					string temp = "VARCHAR";//Type is not available for now, use default varchar for now


					create_table_query += column_vector[i];
					create_table_query += " ";
					create_table_query += "VARCHAR";
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

				//End of table creation	

				//Start of data transfering
				//Fetch data from MySQL	
				temp = "select * from ";
				temp += tbl_name;

				char const * query = temp.c_str();

				cout << "---------------Executing PostgreSQL Query: " << query << endl;


				PGresult *res = PQexec (pg_conn, query);
			    if (PQresultStatus(res) != PGRES_TUPLES_OK) {

			        printf("No data retrieved\n");
			        PQclear(res);
			        
			    }

			    int rows = PQntuples(res);

				//Loop through columns and create the Jaguar query
				int uid = 0;
				for(int row = 0 ; row< rows; row++){
					//cout<<"entering table loop"<<endl;

					string insertion_query = "insert into ";
					insertion_query += tbl_name;
					insertion_query += " ( ";

					for (int i = 0; i< column_vector.size();i++) {
						insertion_query += column_vector[i];
						insertion_query += ",";
					}
					insertion_query = insertion_query.substr(0, insertion_query.size() - 1);
					insertion_query += " ) values (";

					//Loop through columns
					for (int i = 0; i<column_vector.size(); i++) {
						size_t int_pos = column_vector[i].find("INT");
						string single_quote_place_holder = "'";//This is for differenting char from other types
						if (int_pos != std::string::npos) {
							single_quote_place_holder = "";
						}
						insertion_query = insertion_query + single_quote_place_holder + PQgetvalue(res,row,i) + single_quote_place_holder + ",";
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
				//cout<<"free complete"<<endl;
				cout << "(OK) Table: <" << tbl_name << "> transmitted" << endl << endl << endl;
				if ( pg_tbl_defined ) break;
				//End of a table
			}

		//	break;//Remove this when the database iteration is online. This is the end of one database
		
		//}
		cout << "Complete" << endl;
		jdb.close();

		return 0;

}
