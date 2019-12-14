/*
 * Copyright (C) 2019 SciJava
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package vtea.jdbc;

import java.awt.Polygon;
import java.awt.geom.Path2D;
import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import org.h2.tools.Csv;
import vteaobjects.MicroObject;


/**
 *
 * @author sethwinfree
 */
public class H2DatabaseEngine {

//derived from https://www.javatips.net/blog/h2-database-example

    private static final String DB_DRIVER = "org.h2.Driver";
    private static final String DB_CONNECTION = "jdbc:h2:" + ij.Prefs.getImageJDir() + vtea._vtea.H2_DATABASE + ";AUTO_SERVER=TRUE";
    private static final String DB_USER = "";
    private static final String DB_PASSWORD = "";
    
    private static final String DB_NAME = vtea._vtea.H2_DATABASE;
    
    private static String DB_PATH =  ij.Prefs.getImageJDir();
    
    private static ResultSetMetaData meta;
    
    public void H2DatabaseEngine(String path){

        try{
        ResultSet rs = new Csv().read(path, null, null);
        meta = rs.getMetaData();

        } catch (SQLException e) {
            System.out.println("Exception Message " + e.getLocalizedMessage());
        }
    }
    

    //H2 method for importing CSV file
    public static void insertFromCSV(File csvFile, Connection connection, String table) throws SQLException {

      PreparedStatement createPreparedStatement = null;

        try {
            connection.setAutoCommit(false);
            String ImportQuery = "CREATE TABLE " + table + " AS SELECT * FROM CSVREAD('" + csvFile.getAbsolutePath() + "')";
            createPreparedStatement = connection.prepareStatement(ImportQuery);
            createPreparedStatement.executeUpdate();
            createPreparedStatement.close(); 
        } catch (SQLException e) {
            System.out.println("ERROR: Exception Message " + e.getLocalizedMessage());
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            connection.close();
        }
    }
    
    //H2 method for creating dataset table
    public static ArrayList<String> getListOfTables(Connection connection) throws SQLException {
                ResultSet rs = null;
        
                ArrayList<String> al = new ArrayList<String>();
                PreparedStatement selectPreparedStatement = null;

        try {
           
            String SelectQuery = "SELECT * FROM INFORMATION_SCHEMA.TABLES where table_type IS NOT 'SYSTEM TABLE'";
        
            
        selectPreparedStatement = connection.prepareStatement(SelectQuery);
        rs = selectPreparedStatement.executeQuery();
        
        while (rs.next()) {
                System.out.println("PROFILING: Adding: " + rs.getString(3));
                al.add(rs.getString(3));
        }
        
        } catch (SQLException e) {
            System.out.println("ERROR: Exception Message " + e.getLocalizedMessage());
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            connection.close();
        }
        System.out.println("PROFILING: The number of tables are: " + al.size());
        return al;
    }
    
    // SQL for converting column position to label
    
    public static String getColumnLabel(String path, int column) {
        String SQL = new String();

        try{
        ResultSet rs = new Csv().read(path, null, null);
        
        SQL = meta.getColumnName(column);
        } catch (SQLException e) {
            System.out.println("Exception Message " + e.getLocalizedMessage());
        }
        return SQL;
    }
    
    //Table exists method from https://stackoverflow.com/questions/2942788/check-if-table-exists
    
    public static boolean tableExist(Connection conn, String tableName) {
    boolean tExists = false;
    try (ResultSet rs = conn.getMetaData().getTables(null, null, tableName, null)) {
        while (rs.next()) { 
            String tName = rs.getString("TABLE_NAME");
            if (tName != null && tName.equals(tableName)) {
                return tExists;
            }
        }
    } catch(SQLException e){}
    return tExists;
}

    
    
    //H2 SQL to get all column names
    
    public static ArrayList<String> getColumnNames(String table){
        
    PreparedStatement selectPreparedStatement = null;
        ResultSet rs = null;
        
        ArrayList<String> al = new ArrayList<String>();

        try {
        
        String SelectQuery = "select COLUMN_NAME from INFORMATION_SCHEMA.COLUMNS" 
                + " WHERE TABLE_NAME =" + table; 
        
        Connection cn = H2DatabaseEngine.getDBConnection();
            
        selectPreparedStatement = cn.prepareStatement(SelectQuery);
        rs = selectPreparedStatement.executeQuery();
        
        while (rs.next()) {
                al.add(rs.getString(1));
        }
        
        } catch (SQLException e) {
            System.out.println("GetColumNames Exception Message " + e.getLocalizedMessage());
        }
        return al;
    }

    //H2 SQL get a column
    
    public static ArrayList getColumn(String table, 
            String column1) {
         Connection cn = getDBConnection();

        PreparedStatement selectPreparedStatement = null;
        ResultSet rs = null;
        
        ArrayList measurements = new ArrayList();

        try {
        
        String SelectQuery = "select " + column1 
                + " from " + table;
            
        selectPreparedStatement = cn.prepareStatement(SelectQuery);
        rs = selectPreparedStatement.executeQuery();
        
        while (rs.next()) {
                ArrayList al = new ArrayList();
                //al.add(rs.getDouble(0));
                al.add(rs.getDouble(1));
                measurements.add(al);
        }
        
        } catch (SQLException e) {
            System.out.println("getColumn Exception Message " + e.getLocalizedMessage());
        }
        return measurements;
    }
    
    //H2 SQL for returning ArrayList of all records for the desired columns
    
    public static ArrayList getColumns3D(String table, 
            String column1, String column2, String column3) {
         Connection cn = getDBConnection();

        PreparedStatement selectPreparedStatement = null;
        ResultSet rs = null;
        
        ArrayList measurements = new ArrayList();

        try {
        
        String SelectQuery = "select " + column1 
                + ", " + column2
                + ", " + column3 + " from " + table;
            
        selectPreparedStatement = cn.prepareStatement(SelectQuery);
        rs = selectPreparedStatement.executeQuery();
        
        while (rs.next()) {
                ArrayList al = new ArrayList();
                al.add(rs.getDouble(1));
                al.add(rs.getDouble(2));
                al.add(rs.getDouble(3));
                measurements.add(al);
        }
        
        } catch (SQLException e) {
            System.out.println("getColumns3D Exception Message " + e.getLocalizedMessage());
        }
        return measurements;
    }
    
    //H2 SQL for get related MicroObject by ID
    
    public static ArrayList<MicroObject> getRelatedMicroObjects(String table1, String table2){

        PreparedStatement selectPreparedStatement = null;
        ResultSet rs = null;
        
        Connection cn = H2DatabaseEngine.getDBConnection();

        ArrayList<MicroObject> objects = new ArrayList<MicroObject>();

        try {
        
        String SelectQuery = "select * FROM " + table1 + " LEFT JOIN " + 
                table2 + " ON OBJECT = OBJECT ";
            
        selectPreparedStatement = cn.prepareStatement(SelectQuery);
        rs = selectPreparedStatement.executeQuery();
        
        while (rs.next()) {
                objects.add(rs.getObject(2, MicroObject.class));
        }
        
        } catch (SQLException e) {
            System.out.println("Exception Message " + e.getLocalizedMessage());
        }
        return objects;
    }
   
    //H2 SQL for get Range of values query returned as 
    
    public static ArrayList<ArrayList> getObjectsInRange2DSubSelect(String table, String select1, String select2, String select3, String column1, double low1, 
                        double high1, String column2, double low2, double high2) {
            
        Connection cn = getDBConnection();
        PreparedStatement selectPreparedStatement = null;
        ResultSet rs = null;

        ArrayList<ArrayList> result = new ArrayList();


        try {

            
        
        String SelectQuery = "select " + select1 + ", "
                + select2 + ", " + select3 
                + ", PosX, PosY from " + table + " WHERE (" + 
                column1 + " BETWEEN " + low1 + " AND " +
                high1 + ") AND (" +
                column2 + " BETWEEN " + low2 + " AND " +
                high2 + ")";
            
        selectPreparedStatement = cn.prepareStatement(SelectQuery);
        rs = selectPreparedStatement.executeQuery();
        
        while (rs.next()) {
            ArrayList al = new ArrayList();
                al.add(rs.getDouble(1));
                al.add(rs.getDouble(2));
                al.add(rs.getDouble(3));
                al.add(rs.getDouble(4));
                al.add(rs.getDouble(5));
                result.add(al);
        }
        
        } catch (SQLException e) {
            System.out.println("Exception Message " + e.getLocalizedMessage());
        }
        return result;
    }
    

//H2 SQL for get Range of values query returned as 
    
    public static ArrayList<ArrayList> getObjectsInRange2D(Path2D.Double p, String table, String column1, double low1, 
                        double high1, String column2, double low2, double high2) {
            
        Connection cn = getDBConnection();
        PreparedStatement selectPreparedStatement = null;
        ResultSet rs = null;

        ArrayList<ArrayList> result = new ArrayList();


        try {

            
        
        String SelectQuery = "select Object," +  column1 +  "," +  column2 
                +  " from " + table + " WHERE (" + 
                column1 + " BETWEEN " + low1 + " AND " +
                high1 + ") AND (" +
                column2 + " BETWEEN " + low2 + " AND " +
                high2 + ")";
        
        //System.out.println("SQL statement: " + SelectQuery);
            
        selectPreparedStatement = cn.prepareStatement(SelectQuery);
        rs = selectPreparedStatement.executeQuery();
        
        while (rs.next()) {
            
            if(p.contains(rs.getDouble(column1), rs.getDouble(column2))){
                    
               
            ArrayList al = new ArrayList();
                al.add(rs.getDouble(1));
                al.add(rs.getDouble(2));
                al.add(rs.getDouble(3));
                result.add(al);
            }
        }
        
        } catch (SQLException e) {
            System.out.println("Exception Message " + e.getLocalizedMessage());
        }
        return result;
    }
    
    
//H2 SQL for get Range of values with third channel
    
     public static ArrayList<ArrayList> getObjectsInRange2D(Path2D.Double p, String table, String column1, double low1, 
                        double high1, String column2, double low2, double high2, String column3) {
            
        Connection cn = getDBConnection();
        PreparedStatement selectPreparedStatement = null;
        ResultSet rs = null;

        ArrayList<ArrayList> result = new ArrayList();


        try {

            
        
        String SelectQuery = "select Object," +  column1 +  
                "," +  column2 + "," +  column3 +
                " from " + table + " WHERE (" + 
                column1 + " BETWEEN " + low1 + " AND " +
                high1 + ") AND (" +
                column2 + " BETWEEN " + low2 + " AND " +
                high2 + ")";
        
        //System.out.println("SQL statement: " + SelectQuery);
            
        selectPreparedStatement = cn.prepareStatement(SelectQuery);
        rs = selectPreparedStatement.executeQuery();
        
        while (rs.next()) {
            
            if(p.contains(rs.getDouble(column1), rs.getDouble(column2))){
                    
               
            ArrayList al = new ArrayList();
                al.add(rs.getDouble(1));
                al.add(rs.getDouble(2));
                al.add(rs.getDouble(3));
                al.add(rs.getDouble(4));
                result.add(al);
            }
        }
        
        } catch (SQLException e) {
            System.out.println("Exception Message " + e.getLocalizedMessage());
        }
        return result;
    }
    
       



//H2 SQL for get cells in polygon 
    
    public static ArrayList getObjectsInPolygon(String table, Polygon p,  String column1, String column2) {
        
         Connection cn = getDBConnection();
        
        PreparedStatement selectPreparedStatement = null;
        ResultSet rs = null;
        
        ArrayList al = new ArrayList();
        
        double xMax = p.getBounds2D().getMaxX();
        double xMin = p.getBounds2D().getMinX();
        double yMax = p.getBounds2D().getMaxY();
        double yMin = p.getBounds2D().getMinY();

        try {
            
        //get bounding box of objects with SQL

        String SelectQuery = "select * from " + table + " WHERE " + 
                column1 + " (BETWEEN " + xMin + " AND " +
                xMax + ") AND " +
                column2 + " (BETWEEN " + yMin + " AND " +
                yMax + ")";
            
        selectPreparedStatement = cn.prepareStatement(SelectQuery);
        rs = selectPreparedStatement.executeQuery();
        
        //check against polygon2D
        
        while (rs.next()) {
                if(p.contains(rs.getDouble(column2), rs.getDouble(column2))){
                    al.add(rs.getDouble(0));
                }
        }
        
        } catch (SQLException e) {
            System.out.println("Exception Message " + e.getLocalizedMessage());
        }
        return al;
    }
    
public static void dropTable(String table){
    
    Connection cn = getDBConnection();
        
        PreparedStatement selectPreparedStatement = null;
        

        try {
            
        //get bounding box of objects with SQL

        String SelectQuery = "DROP TABLE " + table;
            
        selectPreparedStatement = cn.prepareStatement(SelectQuery);
        selectPreparedStatement.executeUpdate();
        
        //check against polygon2D
        

        
        } catch (SQLException e) {
            System.out.println("Exception Message " + e.getLocalizedMessage());
        }
    
}
    
public static Connection getDBConnection() {

        Connection dbConnection = null;
        try {
            Class.forName(DB_DRIVER);
        } catch (ClassNotFoundException e) {
            System.out.println(e.getMessage());
        }
        try {
            dbConnection = DriverManager.getConnection(DB_CONNECTION, DB_USER,
                    DB_PASSWORD);
            return dbConnection;
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return dbConnection;
    }

public static void startupDBConnection() {
    
    
    
    
}
  
    // Examples of H2 functionality
    // H2 SQL Prepared Statement Example
    public static void insertWithPreparedStatement() throws SQLException {
        Connection connection = getDBConnection();
        PreparedStatement createPreparedStatement = null;
        PreparedStatement insertPreparedStatement = null;
        PreparedStatement selectPreparedStatement = null;

        String CreateQuery = "CREATE TABLE OBJECTS(id int primary key, name varchar(255))";
        String InsertQuery = "INSERT INTO OBJECTS" + "(id, name) values" + "(?,?)";
        String SelectQuery = "select * from OBJECTS";
        try {
            connection.setAutoCommit(false);
           
            createPreparedStatement = connection.prepareStatement(CreateQuery);
            createPreparedStatement.executeUpdate();
            createPreparedStatement.close();
           
            insertPreparedStatement = connection.prepareStatement(InsertQuery);
            insertPreparedStatement.setInt(1, 1);
            insertPreparedStatement.setString(2, "Cell1");
            insertPreparedStatement.executeUpdate();
            insertPreparedStatement.close();
           
            selectPreparedStatement = connection.prepareStatement(SelectQuery);
            ResultSet rs = selectPreparedStatement.executeQuery();
            System.out.println("H2 Database inserted through PreparedStatement");
            while (rs.next()) {
                System.out.println("Id "+rs.getInt("id")+" Object "+rs.getString("name"));
            }
            selectPreparedStatement.close();
           
            //connection.commit();
        } catch (SQLException e) {
            System.out.println("Exception Message " + e.getLocalizedMessage());
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            //connection.close();
        }
    }

    // H2 SQL Statement Example
    public static void insertWithStatement() throws SQLException {
        Connection connection = getDBConnection();
        Statement stmt = null;
        try {
            connection.setAutoCommit(false);
            stmt = connection.createStatement();
            stmt.execute("CREATE TABLE OBJECTS(id int primary key, name varchar(255))");
            stmt.execute("INSERT INTO OBJECTS(id, name) VALUES(1, 'Cell1')");
            stmt.execute("INSERT INTO OBJECTS(id, name) VALUES(2, 'Cell2')");
            stmt.execute("INSERT INTO OBJECTS(id, name) VALUES(3, 'Cell3')");

            ResultSet rs = stmt.executeQuery("select * from OBJECTS");
            System.out.println("H2 Database inserted through Statement");
            while (rs.next()) {
                System.out.println("Id "+rs.getInt("id")+" Object "+rs.getString("name"));
            }
            stmt.close();
            connection.commit();
        } catch (SQLException e) {
            System.out.println("Exception Message " + e.getLocalizedMessage());
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            connection.close();
        }
    }

    
}

