package com.zyz.learn.gis.geotools.jdbc;

import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFinder;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by ZhangYuanzhuo.
 */
public class PostgresJDBC {
    public static DataStore dataStore() {
        Map<String, Object> params = new HashMap<>();
        params.put("dbtype", "postgis");
        params.put("host", "localhost");
        params.put("port", 5432);
        params.put("schema", "public");
        params.put("database", "china");
        params.put("user", "postgres");
        params.put("passwd", "123");

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
            System.out.println("PostgreSQL连接成功！");
        }
    }
}
