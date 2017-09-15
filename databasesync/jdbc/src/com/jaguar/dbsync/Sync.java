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
    private static final String SLEEP_IN_MILIS = "sleep_in_milis";
    private static final String CHANGE_LOG = "change_log";
    private static final String TARGET_PASSWORD = "target_password";
    private static final String TARGET_USER = "target_user";
    private static final String TARGET_DB = "target_db";
    private static final String TARGET_JDBC_URL = "target_jdbc_url";
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
    private static final boolean DEBUG = true;
    
    public static void main(String[] args) throws Exception 
	{
        String appConf = System.getProperty(APP_CONF);
        if (appConf == null) {
            System.err.println("Usage: java -cp jar1:jar2:... -Dapp.conf=<config_file> " + Sync.class.getName());
            return;
        }
        
        // load Jaguar driver
        try {
            Class.forName(COM_JAGUAR_JDBC_JAGUAR_DRIVER);
        } catch (Exception e) {
            e.printStackTrace();
			System.exit(1);
        }
        
        boolean done = false;
        int total = 0;
        
        while (!done) {
            Properties appProp = new Properties();
            appProp.load(new FileReader(appConf));
          
            if (Boolean.parseBoolean(appProp.getProperty(STOP))) {
                done = true;
                break;
            }
     
            // source database
            String srcurl = appProp.getProperty(SOURCE_JDBC_URL);
            srcurl = srcurl + appProp.getProperty(SOURCE_DB);

            if (DEBUG) {
                System.out.println("sourceurl " + srcurl);
            }
            
            String user = appProp.getProperty(SOURCE_USER);
            String password = appProp.getProperty(SOURCE_PASSWORD);
            String table = appProp.getProperty(SOURCE_TABLE); 
            String[] keys = appProp.getProperty(KEYS).split(",");
            
            Connection srcconn = DriverManager.getConnection( srcurl, user, password);
            Statement srcst = srcconn.createStatement();
            String srcsql = "select * from " + table;
            ResultSet metars = srcst.executeQuery( srcsql);
            ResultSetMetaData srcmeta = metars.getMetaData();
            String[] columnNames = new String[ srcmeta.getColumnCount()];
            for(int i = 1; i <= srcmeta.getColumnCount(); i++) {
                columnNames[i - 1] = srcmeta.getColumnName(i).toLowerCase();
            }
            // srcst.close();
            metars.close();
            
            // target database
            String targeturl = appProp.getProperty(TARGET_JDBC_URL);
            targeturl = targeturl + appProp.getProperty(TARGET_DB);

            if (DEBUG) {
                System.out.println("targeturl " + targeturl);
            }
            user = appProp.getProperty(TARGET_USER);
            password = appProp.getProperty(TARGET_PASSWORD);
            
            DBAccess targetdb = new DBAccess( targeturl, user, password, table, keys, columnNames);
            targetdb.init();
            
            String changeLog = appProp.getProperty(CHANGE_LOG);
            PreparedStatement updateLogPS = srcconn.prepareStatement("update " + changeLog + " set status_ = 'D' where id_ = ?");
             
            // srcst = srcconn.createStatement();
            srcsql = "select * from " + changeLog + " where status_ = 'I' ";
           	if (DEBUG) { System.out.println("srcsql  " + srcsql ); }
            ResultSet changers = srcst.executeQuery( srcsql);
			String action, status, ts;
			long changenum = 0;
            while ( changers.next()) {
				++ changenum;
           		if (DEBUG) { System.out.println("inside changers.next() " + changers.toString() ); }

				// rs is changelog result
                action = changers.getString("action_");
                status = changers.getString("status_");
                ts = changers.getString("ts_");
                Object id = changers.getObject("id_");

				Date now = new Date();
                System.out.println(now.toString() + " id=" + id + " action=" + action + " status=" + status + " ts=" + ts );

                if (I.equals(action)) {
            		if (DEBUG) { System.out.println("Insert " ); }
                    try {
            			if (DEBUG) { System.out.println("targetdb.doInsert " ); }
                        targetdb.doInsert( changers);
                    } catch (Exception e) {
                        if (DEBUG) {
                            e.printStackTrace();
                        }
            			if (DEBUG) { System.out.println("targetdb.doUpdate " ); }
                        targetdb.doUpdate( changers);
                    }
                } else if (U.equals(action)) {
            		if (DEBUG) { System.out.println("Update " ); }
                    if (targetdb.doUpdate( changers) == 0) {
            			if (DEBUG) { System.out.println("targetdb.doInsert " ); }
                        targetdb.doInsert( changers);
                    }
                } else if (D.equals(action)) {
            		if (DEBUG) { System.out.println("targetdb.doDelete " ); }
                    targetdb.doDelete( changers);
                } else {
            		if (DEBUG) { System.out.println("Unknown action " + action ); }
				}
                
                //update log status
                updateLogPS.clearParameters();
                updateLogPS.setObject(1, id);

                updateLogPS.executeUpdate();
           		if (DEBUG) { System.out.println("updateLogPS.executeUpdate " + updateLogPS.toString() ); }
                
                total++;
            }
            
            srcst.close();
            changers.close();
            updateLogPS.close();
            srcconn.close();
            targetdb.close();
            if (DEBUG) {
                System.out.println("chnagenum=" + changenum + " sleep ...");
            }
            Thread.sleep(Long.parseLong(appProp.getProperty(SLEEP_IN_MILIS)));
        }
            
		Date now = new Date();
        System.out.println( now.toString() + " Total rows updated: " + total);
		File file = new File("java.lock");
		file.delete();

    }

}
