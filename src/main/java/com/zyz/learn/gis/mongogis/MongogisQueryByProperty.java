package com.zyz.learn.gis.mongogis;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;
import org.bson.Document;
import org.geotools.geojson.geom.GeometryJSON;
import org.geotools.geometry.jts.JTSFactoryFinder;
import org.geotools.referencing.factory.ReferencingObjectFactory;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Created by ZhangYuanzhuo.
 */
public class MongogisQueryByProperty {
    private final MongoCollection<Document> fCollection;
    private final MongoCollection<Document> crsCollection;

    public MongogisQueryByProperty(String ip, String dbName, String features, String crs) {
        MongoClient client = new MongoClient(ip);
        MongoDatabase db = client.getDatabase(dbName);
        fCollection = db.getCollection(features);
        crsCollection = db.getCollection(crs);
    }

    /**
     * 获取坐标参考系。
     *
     * @return 坐标参考系
     */
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
    public List<Document> findAll() {
        List<Document> list = new ArrayList<>();
        try (MongoCursor<Document> cursor = fCollection.find().iterator()) {
            while (cursor.hasNext()) {
                Document doc = cursor.next();
                list.add(doc);
            }
        }
        return list;
    }

    /**
     * 返回所有的属性名。
     *
     * @return 所有属性名
     */
    public Set<String> getPropertyName() {
        Document first = fCollection.find().first();
        Set<String> keySet = first.keySet();
        return keySet;
    }

    /**
     * 根据某个属性进行查询
     * 例如查询城市名为"北京"的。
     * <p>
     * 查询该property的值为value的Feature。
     *
     * @param property 属性
     * @param value    想要查询的Feature的属性值
     * @return Feature集合
     */
    public List<Document> queryByProperty(String property, Object value) {
        List<Document> query = new ArrayList<>();
        MongoCursor<Document> cursor = fCollection.find(new Document(property, value)).iterator();
        try {
            while (cursor.hasNext()) {
                query.add(cursor.next());
            }
        } finally {
            cursor.close();
        }
        return query;
    }

    /**
     * 按照某个属性进行排序
     * 例如按照省的面积
     *
     * @param property 进行排序所依据的属性
     * @return Feature集合
     */
    public List<Document> sortByProperty(String property) {
        List<Document> sort = new ArrayList<>();
        MongoCursor<Document> cursor = fCollection.find().sort(new Document(property, 1)).iterator();
        try {
            while (cursor.hasNext()) {
                sort.add(cursor.next());
            }
        } finally {
            cursor.close();
        }
        return sort;
    }

    // 计算面积并排序
//    List<Document> calculateArea() {
//        List<Document> sort = new ArrayList<>();
//        try (MongoCursor<Document> cursor = fCollection.find().iterator()) {
//            while (cursor.hasNext()) {
//                Document doc = cursor.next();
//
//                GeometryJSON json = new GeometryJSON();
//                Geometry geometry = null;
//                try {
//                    geometry = json.read(doc.get("geometry"));
//                    if (geometry.getGeometryType().equals("MultiLineString")
//                            || geometry.getGeometryType().equals("Polygon")
//                            || geometry.getGeometryType().equals("MultiPolygon")) {
//                        // 用一个Map存省名和面积
//                        geometry.getArea();
//                    }
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }
//        }
//        return sort;
//    }

    /**
     * 领域查询，查询点[x, y]附近的点，返回附近的limit个点，并按距离排序。
     *
     * @param x     查询点的横坐标
     * @param y     查询点的纵坐标
     * @param limit 返回点的个数
     * @return Feature集合
     */
    public List<Document> neighborQuery(double x, double y, int limit) {
        List<Document> query = new ArrayList<>();

        // 170724遇到问题，直接用mongodb可以做查询，所以说存储结构还是没有摸清楚
        // http://blog.csdn.net/ghlfllz/article/details/6774315
        // http://www.paolocorti.net/2009/12/06/using-mongodb-to-store-geographic-data/
        // https://docs.mongodb.com/manual/core/geospatial-indexes/
        MongoCursor<Document> cursor = fCollection.find(Filters.near("geometry", x, y, null, null)).limit(limit).iterator();
//        MongoCursor<Document> cursor = fCollection.find(new Document("$near", "[" + x + ", " + y + "]")).limit(limit).iterator();
        try {
            while (cursor.hasNext()) {
                query.add(cursor.next());
            }
        } finally {
            cursor.close();
        }
        return query;
    }

    /**
     * 根据鼠标获取到的选框查询属性。
     *
     * @param x1 左上角横坐标
     * @param y1 左上角纵坐标
     * @param x2 右下角横坐标
     * @param y2 右下角纵坐标
     * @return Feature集合
     */
    public List<Document> rangeQuery(double x1, double y1,
                                     double x2, double y2) {
        // 长方形查询
        String wktRectangle = "POLYGON((" +
                x1 + " " + y1 + ", " +
                x2 + " " + y1 + ", " +
                x2 + " " + y2 + ", " +
                x1 + " " + y2 + ", " +
                x1 + " " + y1 + "))";
        GeometryFactory gf = JTSFactoryFinder.getGeometryFactory(null);
        WKTReader reader = new WKTReader(gf);
        Geometry rectangle = null;
        try {
            rectangle = reader.read(wktRectangle);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        List<Document> query = new ArrayList<>();
        try (MongoCursor<Document> cursor = fCollection.find().iterator()) {
            while (cursor.hasNext()) {
                Document doc = cursor.next();

                GeometryJSON json = new GeometryJSON();
                Geometry geometry = null;
                try {
                    geometry = json.read(doc.get("geometry"));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                assert rectangle != null;
                assert geometry != null;
                if (rectangle.contains(geometry)) {
                    query.add(doc);
                }
            }
        }
        return query;
    }


    /**
     * 暂时放一放，先做属性空间查询。
     *
     * @return 是否导出成功
     */
    boolean writeToShapefile() {
        return false;
    }
}
