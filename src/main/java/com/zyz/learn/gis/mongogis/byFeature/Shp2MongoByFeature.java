package com.zyz.learn.gis.mongogis.byFeature;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.geojson.feature.FeatureJSON;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.Charset;

/**
 * Created by ZhangYuanzhuo.
 */
public class Shp2MongoByFeature {
    public static void main(String[] args) throws IOException {

        final String IP_ADDRESS = "localhost"; // 本机地址
        final String DB_NAME = "china"; // 数据库名称

        // 能否自动搜索所有子文件夹
        final String FEATURE_COLLECTION_NAME = "省会城市"; // 存储Features的Collection名称

        // CRS的Collection在有多个Layer的时候应该只需要生成一次
        final String CRS_COLLECTION_NAME = "crs"; // 存储CRS的Collection名称

        final String SHP_FILE = "src/main/resources/" +
                DB_NAME + "/" + FEATURE_COLLECTION_NAME + "/" + FEATURE_COLLECTION_NAME + ".shp"; // ShapeFile相对路径

        // 对shapefile进行绘制
        // ShpDraw.draw(new File(SHAPE_FILE));

        // 初始化mongodb
        MongoClient client = new MongoClient(IP_ADDRESS);
        MongoDatabase db = client.getDatabase(DB_NAME);

        // 获取存储Features的集合和存储CRS的集合
        db.getCollection(FEATURE_COLLECTION_NAME).drop();
        MongoCollection<Document> fCollection = db.getCollection(FEATURE_COLLECTION_NAME);
        MongoCollection<Document> crsCollection = db.getCollection(CRS_COLLECTION_NAME);

        // 使用GeoTools读取ShapeFile文件
        File file = new File(SHP_FILE);
        ShapefileDataStore store = new ShapefileDataStore(file.toURI().toURL());
        store.setCharset(Charset.forName("GBK"));
        SimpleFeatureSource sfSource = store.getFeatureSource();
        SimpleFeatureIterator sfIter = sfSource.getFeatures().features();

        // 读取crs并导出到Collection
        if (crsCollection.count() == 0) {
            CoordinateReferenceSystem crs = store.getSchema().getCoordinateReferenceSystem();
            Document crsDoc = new Document("crs", crs.toString().replace("\r\n", ""));
            crsCollection.insertOne(crsDoc);
        }

        // 从ShapeFile文件中遍历每一个Feature，然后将Feature转为GeoJSON字符串，最后将字符串插入到mongodb的Collection中
        while (sfIter.hasNext()) {
            SimpleFeature feature = sfIter.next();

            // Feature转FeatureJSON
            FeatureJSON fJson = new FeatureJSON();
            StringWriter writer = new StringWriter();

            // 设置Feature不包含CRS数据
            fJson.setEncodeFeatureCRS(false);

            fJson.writeFeature(feature, writer);
            String json = writer.toString();

            // 插入到Collection中
            Document doc = Document.parse(json);
            fCollection.insertOne(doc);
        }

        client.close(); // 关闭数据库连接
        System.out.println("数据导入完毕！");

    }
}
