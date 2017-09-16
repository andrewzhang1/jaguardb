package com.jaguar.dbimport;

import java.io.FileReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.util.Properties;

public class Importer 
{
    private static final String DEST_PASSWORD = "dest_password";
    private static final String DEST_USER = "dest_user";
    private static final String DEST_DB = "dest_db";
    private static final String DEST_JDBC_URL = "dest_jdbc_url";
    private static final String SOURCE_TABLE = "source_table";
    private static final String SOURCE_PASSWORD = "source_password";
    private static final String SOURCE_USER = "source_user";
    private static final String SOURCE_DB = "source_db";
    private static final String SOURCE_JDBC_URL = "source_jdbc_url";
    private static final String COM_JAGUAR_JDBC_JAGUAR_DRIVER = "com.jaguar.jdbc.JaguarDriver";
    private static final String APP_CONF = "app.conf";
    private static final String IMPORT_ROWS = "import_rows";

    public static void main(String[] args) throws Exception 
	{
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
    	} catch (Exception e) {
    		e.printStackTrace();
			System.exit(1);
    	}
 
        // source database
        String import_rows = appProp.getProperty(IMPORT_ROWS);
		if ( null == import_rows ) import_rows = "0";
		long importrows = Long.parseLong(import_rows, 10);
        String srcurl = appProp.getProperty(SOURCE_JDBC_URL) + appProp.getProperty(SOURCE_DB);
        System.out.println("sourceurl " + srcurl);
        String user = appProp.getProperty(SOURCE_USER);
        String password = appProp.getProperty(SOURCE_PASSWORD);
        String table = appProp.getProperty(SOURCE_TABLE).toLowerCase();
        Connection sconn = DriverManager.getConnection(srcurl, user, password);
        Statement srcst = sconn.createStatement();
        ResultSet srcrs = srcst.executeQuery("select * from " + table);
        ResultSetMetaData srcmeta = srcrs.getMetaData();
        System.out.println("column count=" + srcmeta.getColumnCount());
		String colname, coltypename;
        for (int i = 1; i <= srcmeta.getColumnCount(); i++) {
			colname = srcmeta.getColumnName(i);
			coltypename = srcmeta.getColumnTypeName(i);
            System.out.println( colname + " has type=" + srcmeta.getColumnType(i) + " typename=" + coltypename );
        }
        
        // dest database
        String desturl = appProp.getProperty(DEST_JDBC_URL) + appProp.getProperty(DEST_DB).toLowerCase();
        System.out.println("desturl " + desturl);
        user = appProp.getProperty(DEST_USER);
        password = appProp.getProperty(DEST_PASSWORD);
        Connection tconn = DriverManager.getConnection( desturl, user, password);
        
        // insert statement
        Statement tst = tconn.createStatement();
        long goodrows = 0, badrows = 0;
		String val0, val;
        while ( srcrs.next()) {
        	StringBuilder sb = new StringBuilder("insert into " + table + " values (");
            for (int i = 1; i <= srcmeta.getColumnCount(); i++) {
				val0 = srcrs.getObject(i).toString();
				val = val0.replaceAll("'", "\\\\'");
				if ( i > 1 ) { sb.append( "," ); }
				sb.append( "'"+ val + "'" );
            }

			// add extra columns if needed
			// sb.append( ",'extra1', 'extra2'" );

			sb.append( ")");

            if ( tst.executeUpdate( sb.toString() ) > 0 ) {
        		System.out.println( "OK " + sb.toString() );
				++ goodrows;
			} else {
        		System.out.println( "ER " + sb.toString() );
				++ badrows;
			}

			if ( importrows > 0 ) {
				if ( ( goodrows+badrows ) >= importrows ) {
					break;
				}
			}
        }
        
        sconn.close();
        tconn.close();
        
        System.out.println("Total goodrows=" + goodrows + " imported. badrows=" + badrows );
    }
}
