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
    private static final boolean DEBUG = System.getProperty("debug") != null;
    
    public static void main(String[] args) throws Exception {
        
        String appConf = System.getProperty("app.conf");
        if (appConf == null) {
            System.err.println("Usage: java -cp jar1:jar2:... -Dapp.conf=<config_file> " + Sync.class.getName());
            return;
        }
        
        // load Jaguar driver
        try {
            Class.forName("com.jaguar.jdbc.JaguarDriver");
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        
        boolean done = false;
        int total = 0;
        
        while (!done) {
            Properties appProp = new Properties();
            appProp.load(new FileReader(appConf));
          
            if (Boolean.parseBoolean(appProp.getProperty("stop"))) {
                done = true;
                break;
            }
     
            // source database
            String url = appProp.getProperty("source_jdbc_url");
            url = url + appProp.getProperty("source_db");

            if (DEBUG) {
                System.out.println("source " + url);
            }
            
            String user = appProp.getProperty("source_user");
            String password = appProp.getProperty("source_password");
            String table = appProp.getProperty("source_table");
            String[] keys = appProp.getProperty("keys").split(",");
            
            Connection conn = DriverManager.getConnection(url, user, password);
            Statement st = conn.createStatement();
            String sql = "select * from " + table;
            ResultSet rs = st.executeQuery(sql);
            ResultSetMetaData meta = rs.getMetaData();
            String[] columnNames = new String[meta.getColumnCount()];
            for(int i = 1; i <= meta.getColumnCount(); i++) {
                columnNames[i - 1] = meta.getColumnName(i);
            }
            st.close();
            rs.close();
            
            // target database
            url = appProp.getProperty("target_jdbc_url");
            url = url + appProp.getProperty("target_db");

            if (DEBUG) {
                System.out.println("target" + url);
            }
            user = appProp.getProperty("target_user");
            password = appProp.getProperty("target_password");
            
            DBAccess db = new DBAccess(url, user, password, table, keys, columnNames);
            db.init();
            
            String changeLog = appProp.getProperty("change_log");
            
            PreparedStatement updateLogPS = conn.prepareStatement("update " + changeLog + " set status_ = 'D' where id_ = ?");
             
            st = conn.createStatement();
            sql = "select * from " + changeLog + " where status_ != 'D' order by ts_";
    
            rs = st.executeQuery(sql);
            while (rs.next()) {
                String action = rs.getString("action_");
                Object id = rs.getObject("id_");

                System.out.println("id=" + id + "action=" + action);

                if ("I".equals(action)) {
                    db.doDelete(rs);
                    db.doInsert(rs);
                }
                else if ("U".equals(action)) {
                    ResultSet rs2 = db.doQuery(rs);
                    if (rs2.next()) {
                        db.doUpdate(rs);
                    }
                    else {
                        db.doInsert(rs);
                    }
                    rs2.close();
                }
                else if ("D".equals(action)) {
                    db.doDelete(rs);
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
            db.close();
            if (DEBUG) {
                System.out.println("sleep ...");
            }
            Thread.sleep(Long.parseLong(appProp.getProperty("sleep_in_milis")));
        }
            
        System.out.println("Total rows updated: " + total);

    }



}
