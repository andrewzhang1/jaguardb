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
    private static final String TARGET_PASSWORD = "target_password";
    private static final String TARGET_USER = "target_user";
    private static final String TARGET_DB = "target_db";
    private static final String TARGET_JDBC_URL = "target_jdbc_url";
    private static final String SOURCE_TABLE = "source_table";
    private static final String SOURCE_PASSWORD = "source_password";
    private static final String SOURCE_USER = "source_user";
    private static final String SOURCE_DB = "source_db";
    private static final String SOURCE_JDBC_URL = "source_jdbc_url";
    private static final String COM_JAGUAR_JDBC_JAGUAR_DRIVER = "com.jaguar.jdbc.JaguarDriver";
    private static final String APP_CONF = "app.conf";

    public static void main(String[] args) throws Exception {

    	String appConf = System.getProperty(APP_CONF);
    	if (appConf == null) {
    		System.err.println("Usage: java -cp jar1:jar2:... -Dapp.conf=<config_file> " + Importer.class.getName());
    		return;
    	}
    	
    	Properties appProp = new Properties();
    	appProp.load(new FileReader(appConf));
    	
    	// load Jaguar driver
    	try {
    		Class.forName(COM_JAGUAR_JDBC_JAGUAR_DRIVER);
    	}
    	catch (Exception e) {
    		e.printStackTrace();
    	}
 
        // source database
        String url = appProp.getProperty(SOURCE_JDBC_URL) + appProp.getProperty(SOURCE_DB);
        System.out.println("source " + url);
        String user = appProp.getProperty(SOURCE_USER);
        String password = appProp.getProperty(SOURCE_PASSWORD);
        String table = appProp.getProperty(SOURCE_TABLE);
        Connection sconn = DriverManager.getConnection(url, user, password);
        Statement st = sconn.createStatement();
        ResultSet rs = st.executeQuery("select * from " + table);
        ResultSetMetaData meta = rs.getMetaData();
        System.out.println("column count=" + meta.getColumnCount());
        for (int i = 1; i <= meta.getColumnCount(); i++) {
            //System.out.println(meta.getColumnName(i) + " has type " + meta.getColumnType(i));
        }
        
        // target database
        url = appProp.getProperty(TARGET_JDBC_URL) + appProp.getProperty(TARGET_DB);
        System.out.println("target" + url);
        user = appProp.getProperty(TARGET_USER);
        password = appProp.getProperty(TARGET_PASSWORD);
        //String table = appProp.getProperty("source_table");
        Connection tconn = DriverManager.getConnection(url, user, password);
        
        // insert statement
        StringBuilder sb = new StringBuilder("insert into " + table + " (");
        boolean isFirst = true;
        for(int i = 1; i <= meta.getColumnCount(); i++) {
            if (isFirst) {
                sb.append(meta.getColumnName(i).toLowerCase());
                isFirst = false;
            }
            else {
                sb.append("," + meta.getTableName(i).toLowerCase());
            }
        }

        sb.append(") values (");
        isFirst = true;
        for(int i = 0; i < meta.getColumnCount(); i++) {
            if (isFirst) {
                sb.append("?");
                isFirst = false;
            }
            else {
                 sb.append(",?");
            }
        }

        sb.append(")");

        System.out.println("insert st: " + sb.toString());

        PreparedStatement ps = tconn.prepareStatement(sb.toString());
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
