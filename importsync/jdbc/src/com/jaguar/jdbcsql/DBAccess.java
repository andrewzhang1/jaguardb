package com.jaguar.jdbcsql;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DBAccess 
{
    private String jdbcUrl;
    private String user;
    private String password;
    private String table;
    private String[] keys;
    private Connection conn;
    private PreparedStatement queryPS;
    private PreparedStatement insertPS;
    private PreparedStatement updatePS;
    private PreparedStatement deletePS;
    private String[] columnNames;
    private boolean DEBUG = false;
    
    public DBAccess( String jdbcUrl, String user, String password, String table, 
					 String[] keys, String[] columnNames, boolean debug ) 
	{
        this.jdbcUrl = jdbcUrl;
        this.user = user;
        this.password = password;
        this.table = table;
        this.keys = keys;
        this.columnNames = columnNames;
		this.DEBUG = debug;
    }
    
    public void init() throws Exception {
        conn = DriverManager.getConnection(jdbcUrl, user, password);
       
        // query statement
        StringBuilder sb = new StringBuilder("select * from " + table + " where ");
        boolean isFirst = true;
        for(String key : keys) {
            if (isFirst) {
                sb.append(key + "=?");
                isFirst = false;
            } else {
                sb.append(" AND " + key +  "=?");
            }
        }
        if (DEBUG) {
            System.out.println("query st: " + sb.toString());
        }
        queryPS = conn.prepareStatement(sb.toString());
        
        // insert statement
        sb = new StringBuilder("insert into " + table + " (");
        for(int i = 0; i < columnNames.length; i++) {
            if ( 0 == i ) {
                sb.append(columnNames[i]);
            } else {
                sb.append("," + columnNames[i]);
            }
        }
       
        sb.append(") values (");
        for(int i = 0; i < columnNames.length; i++) {
            if ( 0 == i) {
                sb.append("?");
            } else {
                sb.append(",?");
            }
        }
        
        sb.append(")");
        
        if (DEBUG) {
            System.out.println("insert st: " + sb.toString());
        }
        insertPS = conn.prepareStatement(sb.toString());
        
        // update statement
        sb = new StringBuilder("update " + table + " set ");
        isFirst = true;
        for(int i = 0; i < columnNames.length; i++) {
            if (!isKey(columnNames[i])) {
                if (isFirst) {
                    sb.append(columnNames[i]).append("=?");
                    isFirst = false;
                } else {
                    sb.append("," + columnNames[i]).append("=?");
                }
            }
        }
        
        sb.append(" where ");
        isFirst = true;
        for(String key : keys) {
            if (isFirst) {
                sb.append(key + "=?");
                isFirst = false;
            } else {
                sb.append(" AND " + key + "=?");
            }
        }

        if (DEBUG) {
            System.out.println("update st: " + sb.toString());
        }
        updatePS = conn.prepareStatement(sb.toString());
        
        // delete statement
        sb = new StringBuilder("delete from " + table + " where ");
        isFirst = true;
        for(String key : keys) {
            if (isFirst) {
                sb.append(key + "=?");
                isFirst = false;
            } else {
                sb.append(" AND " + key + "=?");
            }
        }
 
        if (DEBUG) {
            System.out.println("delete st: " + sb.toString());
        }
        deletePS = conn.prepareStatement(sb.toString());
    }
    
    public Connection getConnection() {
        return conn;
    }
    
    public ResultSet doQuery(ResultSet rs) throws SQLException {
        queryPS.clearParameters();
        int i = 1;
        for(String key : keys) {
            queryPS.setString(i, rs.getString(key).replaceAll("'", "\\\\'") );
            i++;
        }
        
        if (DEBUG) { System.out.println("queryPS " + queryPS.toString() ); }
        return queryPS.executeQuery();
    }
    
    public int doInsert(ResultSet rs) throws SQLException {
        insertPS.clearParameters();
        for(int i = 0; i < columnNames.length; i++) {
            insertPS.setString(i+1, rs.getString(columnNames[i]).replaceAll("'", "\\\\'") );
        }
        
        if (DEBUG) { System.out.println("insertPS " + insertPS.toString() ); }
        return insertPS.executeUpdate();
    }
    
    public int doUpdate(ResultSet rs) throws SQLException {
        updatePS.clearParameters();
        int j = 1;
        for(int i = 0; i < columnNames.length; i++) {
            if (!isKey(columnNames[i])) {
                updatePS.setString(j, rs.getString(columnNames[i]).replaceAll("'", "\\\\'") );
                j++;
            }
        }
        for(String key : keys) {
            updatePS.setString(j, rs.getString(key).replaceAll("'", "\\\\'") );
            j++;
        }

        if (DEBUG) { System.out.println("updatePS " + updatePS.toString() ); }
        return updatePS.executeUpdate();
    }
    
    public int doDelete(ResultSet rs) throws Exception {
        deletePS.clearParameters();
        int j = 1;
        for(String key : keys) {
            deletePS.setString(j, rs.getString(key).replaceAll("'", "\\\\'") );
            j++;
        }
        if (DEBUG) { System.out.println("deletePS " + deletePS.toString() ); }
        return deletePS.executeUpdate();
    }
    
    public void close() throws SQLException {
        queryPS.close();
        insertPS.close();
        updatePS.close();
        deletePS.close();
        conn.close();
    }
    
    private boolean isKey(String columnName) {
        for (int i = 0; i < keys.length; i++) {
            if (keys[i].equalsIgnoreCase(columnName)) {
                return true;
            }
        }
        return false;
    }
}
