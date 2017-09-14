package com.jaguar.dbsync;

import java.io.FileReader;
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
    private static final boolean DEBUG = System.getProperty("debug") != null;
    
    public static void main(String[] args) throws Exception {
        
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
            String url = appProp.getProperty(SOURCE_JDBC_URL);
            url = url + appProp.getProperty(SOURCE_DB);

            if (DEBUG) {
                System.out.println("source " + url);
            }
            
            String user = appProp.getProperty(SOURCE_USER);
            String password = appProp.getProperty(SOURCE_PASSWORD);
            String table = appProp.getProperty(SOURCE_TABLE); 
            String[] keys = appProp.getProperty(KEYS).split(",");
            
            Connection conn = DriverManager.getConnection(url, user, password);
            Statement st = conn.createStatement();
            String sql = "select * from " + table;
            ResultSet metars = st.executeQuery(sql);
            ResultSetMetaData meta = metars.getMetaData();
            String[] columnNames = new String[meta.getColumnCount()];
            for(int i = 1; i <= meta.getColumnCount(); i++) {
                columnNames[i - 1] = meta.getColumnName(i).toLowerCase();
            }
            st.close();
            metars.close();
            
            // target database
            url = appProp.getProperty(TARGET_JDBC_URL);
            url = url + appProp.getProperty(TARGET_DB);

            if (DEBUG) {
                System.out.println("target" + url);
            }
            user = appProp.getProperty(TARGET_USER);
            password = appProp.getProperty(TARGET_PASSWORD);
            
            DBAccess targetdb = new DBAccess(url, user, password, table, keys, columnNames);
            targetdb.init();
            
            String changeLog = appProp.getProperty(CHANGE_LOG);
            PreparedStatement updateLogPS = conn.prepareStatement("update " + changeLog + " set status_ = 'D' where id_ = ?");
             
            st = conn.createStatement();
            sql = "select * from " + changeLog + " where status_ = 'I' order by ts_";
            ResultSet rs = st.executeQuery(sql);
            while (rs.next()) {
				// rs is changelog result
                String action = rs.getString("action_");
                Object id = rs.getObject("id_");
                System.out.println("id=" + id + "action=" + action);
                if (I.equals(action)) {
                    try {
                        targetdb.doInsert(rs);
                    }
                    catch (Exception e) {
                        if (DEBUG) {
                            e.printStackTrace();
                        }
                        targetdb.doUpdate(rs);
                    }
                } else if (U.equals(action)) {
                    if (targetdb.doUpdate(rs) == 0) {
                        targetdb.doInsert(rs);
                    }
                } else if (D.equals(action)) {
                    targetdb.doDelete(rs);
                }
                
                //update log status
                updateLogPS.clearParameters();
                updateLogPS.setObject(1, id);
                updateLogPS.executeUpdate();
                
                total++;
     
            }
            
            st.close();
            rs.close();
            updateLogPS.close();
            conn.close();
            targetdb.close();
            if (DEBUG) {
                System.out.println("sleep ...");
            }
            Thread.sleep(Long.parseLong(appProp.getProperty(SLEEP_IN_MILIS)));
        }
            
        System.out.println("Total rows updated: " + total);

    }

}
