package com.jaguar.dbimport;

import java.io.FileReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.util.Properties;

public class Importer {
    public static void main(String[] args) throws Exception {

    	String appConf = System.getProperty("app.conf");
    	if (appConf == null) {
    		System.err.println("Usage: java -cp jar1:jar2:... -Dapp.conf=<config_file> " + Importer.class.getName());
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
        Connection sconn = DriverManager.getConnection(url, user, password);
        Statement st = sconn.createStatement();
        ResultSet rs = st.executeQuery("select * from " + table);
        ResultSetMetaData meta = rs.getMetaData();
        System.out.println("column count=" + meta.getColumnCount());
        for (int i = 1; i <= meta.getColumnCount(); i++) {
            System.out.println(meta.getColumnName(i) + " has type " + meta.getColumnType(i));
        }
        
        // target database
        url = appProp.getProperty("target_jdbc_url") + appProp.getProperty("target_db");
        System.out.println("target" + url);
        user = appProp.getProperty("target_user");
        password = appProp.getProperty("target_password");
        //String table = appProp.getProperty("source_table");
        Connection tconn = DriverManager.getConnection(url, user, password);
        
        StringBuilder insertStatement = new StringBuilder("insert into " + table + " values(");
        for(int i = 1; i <= meta.getColumnCount(); i++) {
        	if (i == meta.getColumnCount()) {
        		insertStatement.append("?)");
        	}
        	else {
        		insertStatement.append("?,");
        	}
        }
        
        System.out.println(insertStatement.toString());
        
        PreparedStatement ps = tconn.prepareStatement(insertStatement.toString());
        int rows = 0;
        while (rs.next()) {
        	ps.clearParameters();
            for (int i = 1; i <= meta.getColumnCount(); i++) {
                Object o = rs.getObject(i);
                //System.out.println("class:" + o.getClass().getName() + ",value=" + o.toString());
                ps.setObject(i, o);
            }
            ps.executeUpdate();
            rows++;
            //System.out.println("update=" + c);
        }
        
        sconn.close();
        tconn.close();
        
        System.out.println("total " + rows + " imported.");
    }



}
