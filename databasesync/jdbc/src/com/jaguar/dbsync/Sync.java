package com.jaguar.dbsync;

import java.io.FileReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.util.ArrayList;
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
        
        // load Jaguar driver
        try {
            Class.forName("com.jaguar.jdbc.JaguarDriver");
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        
        Long startId = null;
        boolean isFirst = true;
        boolean notDone = false;
        
        while (!notDone) {
            Properties appProp = new Properties();
            appProp.load(new FileReader(appConf));
          
            if (isFirst) {
                startId = Long.parseLong(appProp.getProperty("start_id"));
                isFirst = false;
            }
            
            if (Boolean.parseBoolean(appProp.getProperty("stop"))) {
                System.out.println("next start id = " + startId);
                notDone = true;
                break;
            }
     
            // source database
            String url = appProp.getProperty("source_jdbc_url");
            if (!url.contains("oracle")) {
                url = url + appProp.getProperty("source_db");
            }
            System.out.println("source " + url);
            
            String user = appProp.getProperty("source_user");
            String password = appProp.getProperty("source_password");
            String table = appProp.getProperty("source_table");
            String[] keys = appProp.getProperty("keys").split(",");
            
            Connection conn = DriverManager.getConnection(url, user, password);
            Statement st = conn.createStatement();
            String sql = "select * from " + table;
            ResultSet rs = st.executeQuery(sql);
            ResultSetMetaData meta = rs.getMetaData();
            List<String> columnNames = new ArrayList<String>();
            for(int i = 1; i <= meta.getColumnCount(); i++) {
                columnNames.add(meta.getColumnName(i).toLowerCase());
            }
            st.close();
            rs.close();
            
            // target database
            url = appProp.getProperty("target_jdbc_url");
            if (!url.contains("oracle")) {
                url = url + appProp.getProperty("target_db");
            }
            System.out.println("target" + url);
            user = appProp.getProperty("target_user");
            password = appProp.getProperty("target_password");
            
            DBAccess db = new DBAccess(url, user, password, table, keys, columnNames.toArray(new String[columnNames.size()]));
            db.init();
            
            String changeLog = appProp.getProperty("change_log");
             
            st = conn.createStatement();
            sql = "select * from " + changeLog + " where id_ >= " + startId;
    
            rs = st.executeQuery(sql);
            while (rs.next()) {
                String action = rs.getString("action_");
                System.out.println("id=" + rs.getObject("id_") + "action=" + action);
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
                
                startId = rs.getLong("id_") + 1;
     
            }
            
            st.close();
            rs.close();
            db.close();
            System.out.println("sleep ...");
            Thread.sleep(Long.parseLong(appProp.getProperty("sleep_in_milis")));
        }
            
        System.out.println("done");

    }



}
