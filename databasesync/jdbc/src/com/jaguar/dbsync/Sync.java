package com.jaguar.dbsync;

import java.io.FileReader;
import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.Date;

public class Sync {
    private static final String COM_JAGUAR_JDBC_JAGUAR_DRIVER = "com.jaguar.jdbc.JaguarDriver";
    private static final String APP_CONF = "app.conf";
    private static final String SLEEP_IN_MILLIS = "sleep_in_millis";
    private static final String CHANGE_LOG = "change_log";
    private static final String TARGET_PASSWORD = "dest_password";
    private static final String TARGET_USER = "dest_user";
    private static final String TARGET_DB = "dest_db";
    private static final String TARGET_JDBC_URL = "dest_jdbc_url";
    private static final String KEYS = "keys";
    private static final String SOURCE_TABLE = "source_table";
    private static final String SOURCE_PASSWORD = "source_password";
    private static final String SOURCE_USER = "source_user";
    private static final String SOURCE_DB = "source_db";
    private static final String SOURCE_JDBC_URL = "source_jdbc_url";
    private static final String STOP = "stop";
    private static final String D = "D";
    private static final String U = "U";
    private static final String I = "I";
    // private static final boolean DEBUG = System.getProperty("debug") != null;
    private static boolean DEBUG = false;
    
    public static void main(String[] args) throws Exception 
	{
        String appConf = System.getProperty(APP_CONF);
        if (appConf == null) {
            logit("Usage: java -cp jar1:jar2:... -Dapp.conf=<config_file> " + Sync.class.getName());
            return;
        }

		logit("Jaguar SyncServer started ...");
        
        // load Jaguar driver
        try {
            // Class.forName(COM_JAGUAR_JDBC_JAGUAR_DRIVER).newInstance();
            Class.forName(COM_JAGUAR_JDBC_JAGUAR_DRIVER);
        } catch (Exception e) {
            e.printStackTrace();
			logit("Jaguar SyncServer stopped");
			System.exit(1);
        }

        Properties appProp = new Properties();
        appProp.load(new FileReader(appConf));
        DEBUG = Boolean.parseBoolean(appProp.getProperty("debug"));

       // source database
        String srcurl = appProp.getProperty(SOURCE_JDBC_URL);
        srcurl = srcurl + appProp.getProperty(SOURCE_DB);
        if (DEBUG) {
            logit("sourceurl " + srcurl);
        }

        String sleepms = appProp.getProperty(SLEEP_IN_MILLIS, "1000" );
		logit("Sleep interval is " + sleepms + " milliseconds" );
        
        String user = appProp.getProperty(SOURCE_USER);
        String password = appProp.getProperty(SOURCE_PASSWORD);
        String table = appProp.getProperty(SOURCE_TABLE); 
        String[] keys = appProp.getProperty(KEYS).split(",");
		String keystr = String.join(",", keys);

		logit("Source database user is " + user );
		logit("Source and dest table is " + table );
		logit("Table keys: " + keystr );
        
        Connection srcconn = DriverManager.getConnection( srcurl, user, password);
        Statement srcst = srcconn.createStatement();
        String srcsql = "select * from " + table;
        ResultSet metars = srcst.executeQuery( srcsql);
        ResultSetMetaData srcmeta = metars.getMetaData();
        String[] columnNames = new String[ srcmeta.getColumnCount()];
		String colstr = String.join(" ", columnNames );
        for(int i = 1; i <= srcmeta.getColumnCount(); i++) {
            columnNames[i - 1] = srcmeta.getColumnName(i).toLowerCase();
        }
        srcst.close();
        metars.close();
		logit("columnNames: " + colstr );
        
        // dest database
        String desturl = appProp.getProperty(TARGET_JDBC_URL);
        desturl = desturl + appProp.getProperty(TARGET_DB);
        if (DEBUG) {
            logit("desturl " + desturl);
        }
        String changeLog = appProp.getProperty(CHANGE_LOG);
        PreparedStatement updateLogPS = srcconn.prepareStatement("update " + changeLog + " set status_='D' where id_=?");

        String destuser = appProp.getProperty(TARGET_USER);
        String destpassword = appProp.getProperty(TARGET_PASSWORD);
        DBAccess destdb = new DBAccess( desturl, destuser, destpassword, table, keys, columnNames, DEBUG );
        destdb.init();
        
        long total = 0;
		String  lastID="0", lastTS="0";
		String action, status, ts;
		long changenum = 0;
        while ( true ) {
            Properties appPropNew = new Properties();
            appPropNew.load(new FileReader(appConf));
            if (Boolean.parseBoolean(appPropNew.getProperty(STOP))) {
                break;
            }
        	DEBUG = Boolean.parseBoolean(appPropNew.getProperty("debug"));
     
            // srcst = srcconn.createStatement( ResultSet.TYPE_SCROLL_SENSITIVE );
            srcst = srcconn.createStatement( );
            srcsql = "select * from " + changeLog + " where status_='N'";
           	if (DEBUG) { logit("srcsql  " + srcsql ); }
            ResultSet changers = srcst.executeQuery( srcsql);
           	if (DEBUG) { logit("after srcst.executeQuery  " + srcsql ); }
            while ( changers.next()) {
				++ changenum;
           		if (DEBUG) { logit("inside changers.next() " + changers.toString() ); }
                action = changers.getString("action_");
                status = changers.getString("status_");
                ts = changers.getString("ts_");
                Object id = changers.getObject("id_");
				if ( DEBUG ) {
                	logit(" id=" + id + " action=" + action + " status=" + status + " ts=" + ts );
				}
				lastID = id.toString();
				lastTS = ts;

                if (I.equals(action)) {
            		if (DEBUG) { logit("Insert " ); }
                    try {
            			if (DEBUG) { logit("destdb.doInsert " ); }
                        destdb.doInsert( changers);
                    } catch (Exception e) {
                        if (DEBUG) {
                            e.printStackTrace();
                        }
            			if (DEBUG) { logit("destdb.doUpdate " ); }
                        destdb.doUpdate( changers);
                    }
                } else if (U.equals(action)) {
            		if (DEBUG) { logit("Update " ); }
                    if (destdb.doUpdate( changers) == 0) {
            			if (DEBUG) { logit("destdb.doInsert " ); }
                        destdb.doInsert( changers);
                    }
                } else if (D.equals(action)) {
            		if (DEBUG) { logit("destdb.doDelete " ); }
                    destdb.doDelete( changers);
                } else {
            		if (DEBUG) { logit("Unknown action " + action ); }
				}
                
                //update log status
                updateLogPS.clearParameters();
                updateLogPS.setObject(1, id);

                updateLogPS.executeUpdate();
           		if (DEBUG) { logit("updateLogPS.executeUpdate " + updateLogPS.toString() ); }
                
                total++;
            }
            
            srcst.close();
            changers.close();
            if (DEBUG) {
                logit("changenum=" + changenum + " sleep " + sleepms + " millisecs ...");
            }
            Thread.sleep(Long.parseLong( sleepms ) );
        }

        updateLogPS.close();
        srcconn.close();
        destdb.close();
            
        logit( "Total rows synched: " + total);
        logit( "Changelog lastID " + lastID + " lastTS " + lastTS );
		File file = new File("java.lock");
		file.delete();

    }

	public static String nowTime()
	{
		Date now = new Date();
		return now.toString();
	}
	public static void logit ( String msg )
	{
        System.out.println( nowTime() + " " + msg );
	}
}
