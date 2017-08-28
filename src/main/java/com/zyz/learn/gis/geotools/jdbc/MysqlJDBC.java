package com.zyz.learn.gis.geotools.jdbc;

import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFinder;
import org.geotools.data.mysql.MySQLDataStoreFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by ZhangYuanzhuo.
 */
public class MysqlJDBC {
    public static DataStore dataStore() {
        Map<String, Object> params = new HashMap<>();
        params.put(MySQLDataStoreFactory.DBTYPE.key, "mysql");
        params.put(MySQLDataStoreFactory.HOST.key, "localhost");
        params.put(MySQLDataStoreFactory.PORT.key, 3309);
        params.put(MySQLDataStoreFactory.DATABASE.key, "china");
        params.put(MySQLDataStoreFactory.USER.key, "root");
        params.put(MySQLDataStoreFactory.PASSWD.key, "123");

        DataStore dataStore = null;
        try {
            dataStore = DataStoreFinder.getDataStore(params);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return dataStore;
    }

    public static void main(String[] args) {
        DataStore dataStore = PostgresJDBC.dataStore();
        if (dataStore != null) {
            System.out.println("MySQL连接成功！");
        }
    }
}
