package com.zyz.learn.gis.postgis;

import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFinder;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.data.simple.SimpleFeatureStore;
import org.opengis.feature.simple.SimpleFeatureType;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by ZhangYuanzhuo.
 */
public class Shp2PostgisHelper {
    public static void main(String[] args) throws IOException {
        final String IP_ADDRESS = "localhost"; // 本机地址
        final String DB_NAME = "china"; // 数据库名称

        // 能否自动搜索所有子文件夹
        final String TABLE_NAME = "省会城市"; // 存储Features的Collection名称

        final String SHP_FILE = "learn/src/main/resources/" +
                DB_NAME + "/" + TABLE_NAME + "/" + TABLE_NAME + ".shp"; // ShapeFile相对路径

        // 初始化PostGIS参数
        Map params = new HashMap<String, Object>();
        params.put("dbtype", "postgis"); // 使用PostGIS类型PostgreSQL数据库
        params.put("host", IP_ADDRESS);
        params.put("port", 5432);
        params.put("schema", "public"); // 建表
        params.put("database", DB_NAME);
        params.put("user", "postgres");
        params.put("passwd", "123");
        DataStore dataStore = DataStoreFinder.getDataStore(params);

        // 使用GeoTools读取ShapeFile文件
        File file = new File(SHP_FILE);
        ShapefileDataStore store = new ShapefileDataStore(file.toURI().toURL());
        store.setCharset(Charset.forName("GBK"));
        SimpleFeatureSource shpSource = store.getFeatureSource();
        SimpleFeatureType featureSchema = shpSource.getSchema();
        SimpleFeatureCollection fc = shpSource.getFeatures();

        // 将feature数据导入PostgreSQL
        try {
            dataStore.removeSchema(featureSchema.getName());
        } catch (IllegalArgumentException | IOException e) {
            e.printStackTrace();
        }
        dataStore.createSchema(featureSchema);
        SimpleFeatureStore dbSource = (SimpleFeatureStore) dataStore.getFeatureSource(featureSchema.getName());
        dbSource.addFeatures(fc);

        // 将srid导入srid对应的表中


        System.out.println("数据导入完成！");
    }
}
