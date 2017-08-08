package com.datajugar.dbimport;

import java.io.FileReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.util.Properties;

import javax.sql.DataSource;

import com.jaguar.jdbc.JaguarDataSource;

public class Test {
    public static void main(String[] args) throws Exception {

    	String appConf = System.getProperty("app.conf");
    	if (appConf == null) {
    		System.err.println("Usage: java -cp jar1:jar2:... -Dapp.conf=<config_file> com.datajugar.dataimport.Test");
    		return;
    	}
    	
    	Properties appProp = new Properties();
    	appProp.load(new FileReader(appConf));
    	
    	
        // Jaguar database
    	System.loadLibrary("JaguarClient");
    	String jhost = appProp.getProperty("jaguar_host");
    	String jport = appProp.getProperty("jaguar_port");
    	String juser = appProp.getProperty("jaguar_user");
    	String jpassword = appProp.getProperty("jaguar_password");
    	String jdb = appProp.getProperty("jaguar_db");
        DataSource ds = new JaguarDataSource(jhost, Integer.parseInt(jport), jdb);
        Connection jc = ds.getConnection(juser, jpassword);
        //PreparedStatement js = jc.prepareStatement("insert into pet values (?, ?, ?)");
       
        // mysql
        String url = appProp.getProperty("source_jdbc_url") + appProp.getProperty("source_db");
        System.out.println(url);
        String user = appProp.getProperty("source_user");
        String password = appProp.getProperty("source_password");
        String table = appProp.getProperty("source_table");
        Connection connection = DriverManager.getConnection(url, user, password);
        Statement st = connection.createStatement();
        ResultSet rs = st.executeQuery("select * from " + table);
        ResultSetMetaData meta = rs.getMetaData();
        System.out.println("column count=" + meta.getColumnCount());
        for (int i = 1; i <= meta.getColumnCount(); i++) {
            System.out.println(meta.getColumnName(i) + " has type " + meta.getColumnType(i));
        }
        
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
        
        PreparedStatement js = jc.prepareStatement(insertStatement.toString());
        int rows = 0;
        while (rs.next()) {
        	js.clearParameters();
            for (int i = 1; i <= meta.getColumnCount(); i++) {
                Object o = rs.getObject(i);
                //System.out.println("class:" + o.getClass().getName() + ",value=" + o.toString());
                js.setObject(i, o);
            }
            int c = js.executeUpdate();
            rows++;
            //System.out.println("update=" + c);
        }
        
        jc.close();
        
        System.out.println("total " + rows + " imported.");
    }



}
