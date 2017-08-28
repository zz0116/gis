package com.zyz.learn.gis.mongogis.byFeature;

import com.mongodb.MongoClient;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.geotools.data.shapefile.ShapefileDumper;
import org.geotools.data.shapefile.files.ShpFiles;
import org.geotools.data.shapefile.files.StorageFile;
import org.geotools.feature.DefaultFeatureCollection;
import org.geotools.geojson.feature.FeatureJSON;
import org.opengis.feature.simple.SimpleFeature;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import static org.geotools.data.shapefile.files.ShpFileType.PRJ;

/**
 * Created by ZhangYuanzhuo.
 */
public class Mongo2ShpByFeature {
    public static void main(String[] args) throws IOException {

        final String IP_ADDRESS = "localhost"; // 本机地址
        final String DB_NAME = "china"; // 数据库名称

        final String FEATURE_COLLECTION_NAME = "省会城市"; // 存储Features的Collection名称
        final String CRS_COLLECTION_NAME = "crs"; // 存储CRS的Collection名称

        final String OUTPUT_FOLDER = "src/main/resources/" +
                DB_NAME + "/" + FEATURE_COLLECTION_NAME + "/" + "output/"; // 数据导出相对路径
        final String SHAPEFILE = "src/main/resources/" +
                DB_NAME + "/" + FEATURE_COLLECTION_NAME + "/" + "output/feature.shp"; // 生成.shp文件的路径

        // 初始化mongodb
        MongoClient client = new MongoClient(IP_ADDRESS);
        MongoDatabase db = client.getDatabase(DB_NAME);

        // 获取存储Features的集合和存储CRS的集合
        MongoCollection<Document> fCollection = db.getCollection(FEATURE_COLLECTION_NAME);
        MongoCollection<Document> crsCollection = db.getCollection(CRS_COLLECTION_NAME);

        // 从mongodb的Collection中遍历每一个Document，然后将Document转为GeoJSON字符串，最后将字符串转为Feature导出
        FindIterable<Document> documents = fCollection.find();
        DefaultFeatureCollection fc = new DefaultFeatureCollection();
        for (Document document : documents) {
            // Document转Feature
            String json = document.toJson();
            FeatureJSON fJson = new FeatureJSON();
            SimpleFeature feature = fJson.readFeature(json);
            fc.add(feature);
        }

        // 从mongodb的Collection获取到CRS
        Document crsDoc = crsCollection.find().first();
        String crsWords = ((String) crsDoc.get("crs")).replace("\"m\"", "\"meter\"");
        if (crsWords == null)
            throw new NullPointerException("CRS required for .prj file");

        // 条件查询或者偏移

        // 数据导出路径
        File output = new File(OUTPUT_FOLDER);
        if (output.mkdir()) {
            System.out.println("output文件夹创建成功！");
        }
        ShapefileDumper dumper = new ShapefileDumper(output);
        dumper.dump(fc);

        // 导出CRS到.prj替换掉原文件
        ShpFiles shpFiles = new ShpFiles(SHAPEFILE);
        StorageFile storageFile = shpFiles.getStorageFile(PRJ);
        try (FileWriter out = new FileWriter(storageFile.getFile())) {
            out.write(crsWords);
        }
        storageFile.replaceOriginal();

        client.close(); // 关闭数据库连接
        System.out.println("数据导出完毕！");

        // 对导出数据进行绘制
        // ShpDraw.draw(new File(OUTPUT_FOLDER + "feature.shp"));
    }
}
