package com.jaguar.dbimport;

import java.io.FileReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.util.Properties;
import java.util.Date;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;

public class Watcher {
    public static void main(String[] args) throws Exception {

    	String appConf = System.getProperty("app.conf");
    	if (appConf == null) {
    		System.err.println("Usage: java -cp jar1:jar2:... -Dapp.conf=<config_file> " + Watcher.class.getName());
    		return;
    	}
    	
    	Properties appProp = new Properties();
    	appProp.load(new FileReader(appConf));
    	
    	// load Jaguar driver
    	try {
    		Class.forName("com.jaguar.jdbc.JaguarDriver");
    	} catch (Exception e) {
    		e.printStackTrace();
			System.exit(1);
    	}
 
        // source database
        String url = appProp.getProperty("source_jdbc_url") + appProp.getProperty("source_db");
        // System.out.println("source " + url);
        String user = appProp.getProperty("source_user");
        String password = appProp.getProperty("source_password");
        String srctable = appProp.getProperty("source_table");
		// source_table is the trigger-generated table:  timestamp, col1, col2, ..., action
		// col=2 .... N-1 are from original table
        Connection sconn = DriverManager.getConnection(url, user, password);
        Statement st = sconn.createStatement();
		String selqs;

		/***
        System.out.println("column count=" + meta.getColumnCount());
        for (int i = 1; i <= meta.getColumnCount(); i++) {
            System.out.println(meta.getColumnName(i) + " has type " + meta.getColumnType(i));
        }
		***/
        
        // target database
        String targeturl = appProp.getProperty("target_jdbc_url") + appProp.getProperty("target_db");
        System.out.println("target " + targeturl);
        String targettable = appProp.getProperty("target_table");
        user = appProp.getProperty("target_user");
        password = appProp.getProperty("target_password");
        Connection tconn = DriverManager.getConnection( targeturl, user, password);
		Statement  tst = tconn.createStatement();
        
        
        // System.out.println(insertStatement.toString());
        int rows = 0;
		String action, actionCol, key, delqs, ts;
		String lastTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());

		while ( true ) 
		{
			selqs = "select * from " + srctable + " where ts_ >= " + lastTime;
            ResultSet rs = st.executeQuery( selqs );
        	ResultSetMetaData meta = rs.getMetaData();
        	StringBuilder insertStatement = new StringBuilder("insert into " + targettable + " values (");
			int colCount = meta.getColumnCount();
        	for( int i = 2; i <= colCount-1; i++) {
        		if (i == colCount-1) {
        			insertStatement.append("?)");
        		} else {
        			insertStatement.append("?,");
        		}
        	}
        	PreparedStatement ips = tconn.prepareStatement(insertStatement.toString());

            while (rs.next()) {
    			// check if action_ column is 'I' 'U' 'D'
    			// if 'I'  insert; if 'U': delete record and insert again; if 'D' just delete it
    			actionCol = meta.getColumnName( colCount );
    			if ( actionCol.equals("action_")) {
    				action = rs.getString( colCount );
    			} else {
            		System.out.println("action is not in last column of trigger table");
    				// System.exit(1);
					break;
    			}
    
    			key = rs.getString( "uid" ); // key
    			ts = rs.getString( "ts_" ); // timestamp
				lastTime = ts;
            	delqs = "delete from " + targettable + " where uid='" + key + "';";
    			if ( action.equals("I") || action.equals("U") ) {
    				if ( action.equals("U") ) {
    					tst.executeUpdate( delqs );
    				}
    
            		ips.clearParameters();
                    for (int i = 2; i <= colCount-1; i++) {
                        Object o = rs.getObject(i);
                        ips.setObject(i, o);
    				}
                	ips.executeUpdate();
                } else if ( action.equals("D") ) {
    				tst.executeUpdate( delqs );
    			} 
    
                rows++;
                //System.out.println("update=" + c);
            }

			// delete from trigger table
			delqs = "delete from " + srctable + " where ts_ <= " + lastTime;
			if ( 0 == st.executeUpdate( delqs ) ) {
				break;
			}

			Thread.sleep(1000, 0);
		}
        
        sconn.close();
        tconn.close();
        
        System.out.println("total " + rows + " changed.");
    }



}
