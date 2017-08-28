package com.zyz.learn.gis.mongogis.byProperty;

import com.mongodb.BasicDBObject;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.vividsolutions.jts.geom.Geometry;
import org.bson.Document;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.data.store.ContentFeatureCollection;
import org.geotools.data.store.ContentFeatureSource;
import org.geotools.geojson.geom.GeometryJSON;
import org.opengis.feature.Property;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Collection;

/**
 * Created by ZhangYuanzhuo.
 */
public class Shp2MongoByProperty {
    public static void main(String[] args) throws IOException {

        final String IP_ADDRESS = "localhost"; // 本机地址
        final String DB_NAME = "china"; // 数据库名称

        // 能否自动搜索所有子文件夹
        final String FEATURE_COLLECTION_NAME = "省会城市"; // 存储Features的Collection名称
        final String CRS_COLLECTION_NAME = "crs"; // 存储CRS的Collection名称

        final String SHP_FILE = "src/main/resources/" +
                DB_NAME + "/" + FEATURE_COLLECTION_NAME + "/" + FEATURE_COLLECTION_NAME + ".shp"; // ShapeFile相对路径

        // 初始化mongodb
        MongoClient client = new MongoClient(IP_ADDRESS);
        MongoDatabase db = client.getDatabase(DB_NAME);

        db.getCollection(FEATURE_COLLECTION_NAME).drop();
        MongoCollection<Document> fCollection = db.getCollection(FEATURE_COLLECTION_NAME);
        MongoCollection<Document> crsCollection = db.getCollection(CRS_COLLECTION_NAME);

        // 使用GeoTools读取ShapeFile文件
        File file = new File(SHP_FILE);
        ShapefileDataStore dataStore = new ShapefileDataStore(file.toURI().toURL());
        dataStore.setCharset(Charset.forName("GBK"));

        // 读取crs并导出到Collection
        // crs的Collection在有多个Layer的时候应该只需要生成一次
        if (crsCollection.count() == 0) {
            CoordinateReferenceSystem crs = dataStore.getSchema().getCoordinateReferenceSystem();
            Document crsDoc = new Document("crs", crs.toString().replace("\r\n", ""));
            crsCollection.insertOne(crsDoc);
        }

        // 读取features并导出到Collection
        ContentFeatureSource featureSource = dataStore.getFeatureSource();
        ContentFeatureCollection fc = featureSource.getFeatures();
        SimpleFeatureIterator sfIter = fc.features();

        // 按照属性集合存入mongodb
        BasicDBObject dbObject;
        while (sfIter.hasNext()) {
            SimpleFeature feature = sfIter.next();
            Collection<Property> properties = feature.getProperties();

            GeometryJSON geometryJSON = new GeometryJSON();
            dbObject = new BasicDBObject(properties.size());
            dbObject.append("type", "Feature");
            for (Property p : properties) {
                if ("the_geom".equals(p.getName().toString())) {
                    Geometry geometry = (Geometry) p.getValue();
                    dbObject.append("the_geom", geometryJSON.toString(geometry));
                } else {
                    dbObject.append(p.getName().toString(), p.getValue());
                }
            }
            String json = dbObject.toJson();

            // 插入到Collection中
            Document doc = Document.parse(json);
            fCollection.insertOne(doc);
        }

        client.close(); // 关闭数据库连接
        System.out.println("数据导入完毕！");

    }
}
