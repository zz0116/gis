package com.zyz.learn.gis.query.impl;

import com.mongodb.MongoClient;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.IndexOptions;
import com.vividsolutions.jts.geom.Geometry;
import com.zyz.learn.gis.query.GISQuery;
import org.bson.Document;
import org.geotools.data.collection.ListFeatureCollection;
import org.geotools.data.shapefile.ShapefileDumper;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.geojson.feature.FeatureJSON;
import org.geotools.referencing.factory.ReferencingObjectFactory;
import org.opengis.feature.Property;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.Name;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Shp文件数据按一条一条Feature的形式存入MongoDB
 * <p>
 * Created by ZhangYuanzhuo.
 */
public class MongogisQuery implements GISQuery {
    private final MongoCollection<Document> fCollection;
    private final MongoCollection<Document> crsCollection;

    public MongogisQuery(String ip, String dbName, String tableName) {
        MongoClient client = new MongoClient(ip);
        MongoDatabase db = client.getDatabase(dbName);
        fCollection = db.getCollection(tableName);
        crsCollection = db.getCollection("crs");
    }

    private ListFeatureCollection docsToFeatures(FindIterable<Document> docs) {
        ListFeatureCollection fc = new ListFeatureCollection((SimpleFeatureType) null);
        for (Document doc : docs) {
            String json = doc.toJson();
            FeatureJSON fJson = new FeatureJSON();
            SimpleFeature feature = null;
            try {
                feature = fJson.readFeature(json);
            } catch (IOException e) {
                e.printStackTrace();
            }
            fc.add(feature);
        }
        return fc;
    }

    /**
     * 获取坐标参考系。
     *
     * @return 坐标参考系
     */
    @Override
    public CoordinateReferenceSystem getCRS() {
        String crsWords = crsCollection.distinct("crs", String.class).first();

        ReferencingObjectFactory referencingObjectFactory = new ReferencingObjectFactory();
        CoordinateReferenceSystem crs = null;
        try {
            crs = referencingObjectFactory.createFromWKT(crsWords); // 将取出的crs字符串转化为CRS对象
        } catch (FactoryException e) {
            e.printStackTrace();
        }
        return crs;
    }

    /**
     * 查询所有属性。
     *
     * @return 所有属性
     */
    @Override
    public SimpleFeatureCollection findAll() {
        FindIterable<Document> documents = fCollection.find();
        return docsToFeatures(documents);
    }

    /**
     * 返回所有的属性名。
     *
     * @return 所有属性名
     */
    @Override
    public Collection<Name> getPropertyNames() {
        Document first = fCollection.find().first();
        String json = first.toJson();
        FeatureJSON fJson = new FeatureJSON();
        Collection<Name> names = new ArrayList<>();
        try {
            SimpleFeature feature = fJson.readFeature(json);
            Collection<Property> properties = feature.getProperties();
            for (Property property : properties) {
                names.add(property.getName());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return names;
    }

    /**
     * 根据某个属性进行查询
     * 例如查询城市名为"北京"的，查询城市名带“北”的。
     *
     * @param value 查询过滤规则
     * @return Feature集合
     */
    @Override
    public SimpleFeatureCollection exactQuery(String fieldName, String value) {
        FindIterable<Document> documents = fCollection.find(
                new Document("properties." + fieldName, value));
        return docsToFeatures(documents);
    }

    @Override
    public SimpleFeatureCollection fuzzyQuery(String fieldName, String value) {
        FindIterable<Document> documents = fCollection.find(
                new Document("properties." + fieldName, new Document("$regex", ".*" + value +".*")));
        return docsToFeatures(documents);
    }

    /**
     * 根据鼠标获取到的选框查询属性。
     *
     * @param x1 左下角横坐标
     * @param y1 左下角纵坐标
     * @param x2 右上角横坐标
     * @param y2 右上角纵坐标
     * @return Feature集合
     */
    @Override
    public SimpleFeatureCollection queryByBox(double x1, double y1,
                                              double x2, double y2) {
        FindIterable<Document> documents = fCollection.find(
                Filters.geoWithinBox("geometry.coordinates", x1, y1, x2, y2));
        return docsToFeatures(documents);
    }

    /**
     * 邻域查询
     *
     * @param fieldName   空间属性名
     * @param x           所查点横坐标
     * @param y           所查点纵坐标
     * @param maxDistance 离点最大距离
     * @param minDistance 离点最小距离
     * @return Feature集合
     */
    @Override
    public SimpleFeatureCollection queryByNeighbor(String fieldName, double x, double y,
                                                   double maxDistance, double minDistance) {
        IndexOptions indexOptions = new IndexOptions();
        indexOptions.max(1.0e+07);
        indexOptions.min(-1.0e+07);
        fCollection.createIndex(new Document(fieldName, "2d"), indexOptions);

        FindIterable<Document> documents = fCollection.find(
                Filters.near("geometry." + fieldName, x, y, maxDistance, minDistance));
        return docsToFeatures(documents);
    }

    /**
     * 按照某个属性进行排序
     * 例如按照省的面积
     *
     * @param order 进行排序所依据的属性
     * @return Feature集合
     */
    @Override
    public SimpleFeatureCollection sortByProperty(String fieldName, int order) {
        FindIterable<Document> documents = fCollection.find().sort(new Document("properties." + fieldName, order));
        return docsToFeatures(documents);
    }

    /**
     * 对地理属性添加偏移量
     *
     * @param xOffset 横坐标偏移
     * @param yOffset 纵坐标偏移
     * @return 偏移处理后的所有属性
     */
    @Override
    public SimpleFeatureCollection offset(double xOffset, double yOffset) {
        FindIterable<Document> documents = fCollection.find();
        SimpleFeatureIterator features = docsToFeatures(documents).features();
        while (features.hasNext()) {
            SimpleFeature feature = features.next();
            Geometry geometry = (Geometry) feature.getProperty("geometry");
            // 未完成

        }
        return null;
    }

    /**
     * 将处理后的空间数据导出到shapefile
     *
     * @return 是否导出成功
     */
    boolean writeToShapeFile(SimpleFeatureCollection fc, String outputFolder, String shapefile) {

        // 数据导出路径
        File output = new File(outputFolder);
        if (output.mkdir()) {
            System.out.println("output文件夹创建成功！");
        }
        ShapefileDumper dumper = new ShapefileDumper(output);
        try {
            dumper.dump(fc);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }
}
