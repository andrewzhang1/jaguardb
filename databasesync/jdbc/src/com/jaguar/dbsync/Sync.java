package com.jaguar.dbsync;

import java.io.FileReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

public class Sync {
    public static void main(String[] args) throws Exception {

    	String appConf = System.getProperty("app.conf");
    	if (appConf == null) {
    		System.err.println("Usage: java -cp jar1:jar2:... -Dapp.conf=<config_file> " + Sync.class.getName());
    		return;
    	}
    	
    	Properties appProp = new Properties();
    	appProp.load(new FileReader(appConf));
    	
    	// load Jaguar driver
    	try {
    		Class.forName("com.jaguar.jdbc.JaguarDriver");
    	}
    	catch (Exception e) {
    		e.printStackTrace();
    	}
 
        // source database
        String url = appProp.getProperty("source_jdbc_url") + appProp.getProperty("source_db");
        System.out.println("source " + url);
        
        String user = appProp.getProperty("source_user");
        String password = appProp.getProperty("source_password");
        String table = appProp.getProperty("source_table");
        List<String> keys = Arrays.asList(appProp.getProperty("key_columns").split(","));
        
        DBAccess sourceDB = new DBAccess(url, user, password, table, keys);
        sourceDB.init();
        
        // target database
        url = appProp.getProperty("target_jdbc_url") + appProp.getProperty("target_db");
        System.out.println("target" + url);
        user = appProp.getProperty("target_user");
        password = appProp.getProperty("target_password");
        
        DBAccess targetDB = new DBAccess(url, user, password, table, keys);
        targetDB.init();
        
        String changeLog = appProp.getProperty("change_log");
        String startId = appProp.getProperty("start_id");
        
        Statement st = sourceDB.getConnection().createStatement();
        String sql = "select * from " + changeLog + " where id >= " + startId;

        ResultSet rs = st.executeQuery(sql);
        while (rs.next()) {
        	System.out.println("id=" + rs.getObject("id"));
        	ResultSet rs2 = sourceDB.doQuery(rs);
        	if (rs2.next()) {
        		ResultSet rs3 = targetDB.doQuery(rs);
        		if (rs3.next()) {
        			targetDB.doUpdate(rs2);
        		}
        		else {
        			targetDB.doInsert(rs2);
        		}
        		rs3.close();
        	}
        	else {
        		targetDB.doDelete(rs);
        	}
		rs2.close();
        }
        
        st.close();
        rs.close();
        sourceDB.close();
        targetDB.close();
        	
        System.out.println("done");

    }



}
