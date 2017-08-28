package com.zyz.learn.gis.postgis.postgres.crud;

import java.sql.ResultSet;
import java.sql.Statement;

/**
 * Created by ZhangYuanzhuo.
 */
public class PostgreSQLCRUD {
    private DBConnector db = null;

    public PostgreSQLCRUD(String url, String user, String pw) {
        // no SSL
        db = new DBConnector(url, user, pw, false);
        System.out.println("Opened database successfully!");
    }

    public void createTable() {
        Statement stmt;
        try {
            stmt = db.getConnection().createStatement();

            String sql = "CREATE TABLE student " +
                    "(ID INT PRIMARY KEY     NOT NULL, " +
                    " NAME           TEXT    NOT NULL, " +
                    " AGE            INT     NOT NULL) ";

            stmt.executeUpdate(sql);
            stmt.close();
        } catch (Exception e) {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
            System.exit(0);
        }

        System.out.println("Table created successfully!");
    }

    public void insertData() {
        Statement stmt;

        try {
            String sql;
            stmt = db.getConnection().createStatement();

            sql = "INSERT INTO student ( ID, NAME, AGE ) " +
                    "VALUES (1, 'Mike', 20 );";
            stmt.executeUpdate(sql);

            sql = "INSERT INTO student ( ID, NAME, AGE ) " +
                    "VALUES (2, 'Bill', 22 );";
            stmt.executeUpdate(sql);

            sql = "INSERT INTO student ( ID, NAME, AGE ) " +
                    "VALUES (3, 'James', 25 );";
            stmt.executeUpdate(sql);

            stmt.close();
        } catch (Exception e) {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
            System.exit(0);
        }

        System.out.println("Insert successfully!");
    }

    public void updateData() {
        Statement stmt;
        try {
            stmt = db.getConnection().createStatement();

            String sql = "UPDATE student set age = 33 where ID = 1;";
            stmt.executeUpdate(sql);
            stmt.close();
        } catch (Exception e) {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
            System.exit(0);
        }

        System.out.println("Update successfully!");
    }

    public void deleteData() {
        Statement stmt;
        try {
            stmt = db.getConnection().createStatement();

            String sql = "DELETE FROM student WHERE ID=2;";
            stmt.executeUpdate(sql);
            stmt.close();
        } catch (Exception e) {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
            System.exit(0);
        }

        System.out.println("Delete successfully!");
    }

    public void selectData() {
        Statement stmt;
        int rows = 0;

        try {
            stmt = db.getConnection().createStatement();

            ResultSet rs = stmt.executeQuery("SELECT * FROM student;");

            while (rs.next()) {
                int id = rs.getInt("id");
                String name = rs.getString("name");
                int age = rs.getInt("age");
                System.out.println("ID = " + id);
                System.out.println("NAME = " + name);
                System.out.println("AGE = " + age);
                System.out.println();
                rows++;
            }

            rs.close();
            stmt.close();
        } catch (Exception e) {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
            System.exit(0);
        }

        System.out.println("Selected " + rows + " rows successfully!");
    }

    public void close() {
        db.close();
        System.out.println("Closed DBConnector!");
    }
}
