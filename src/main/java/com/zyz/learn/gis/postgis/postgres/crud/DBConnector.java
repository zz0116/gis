package com.zyz.learn.gis.postgis.postgres.crud;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Created by ZhangYuanzhuo.
 */
public class DBConnector {
    private Connection conn = null;

    public DBConnector(String dbUrl, String user, String pw, boolean ssl) {
        Properties properties = new Properties();
        properties.setProperty("user", user);
        properties.setProperty("password", pw);
        if (ssl) properties.setProperty("ssl", "true");

        try {
            conn = DriverManager.getConnection(dbUrl, properties);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public Connection getConnection() {
        return conn;
    }

    public void close() {
        try {
            if (conn != null) {
                conn.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}