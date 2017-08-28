package com.zyz.learn.gis.dao;

import com.vividsolutions.jts.geom.Point;
import com.zyz.learn.gis.query.GISQuery;
import com.zyz.learn.gis.query.impl.MongogisQuery;
import com.zyz.learn.gis.query.impl.PostgisQuery;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.junit.Test;
import org.opengis.feature.Feature;

/**
 * Created by ZhangYuanzhuo.
 */
public class GISQueryTest {

    public static GISQuery[] getGqs(String dbName, String tableName) {
        GISQuery[] gqs = new GISQuery[2];
        gqs[0] = new MongogisQuery("localhost", dbName, tableName);
        gqs[1] = new PostgisQuery(dbName, "postgres", "123", tableName);
        return gqs;
    }

    @Test
    public void getCRS() throws Exception {
        // PostGIS的SRID是null
        for (GISQuery gq : getGqs("china", "省会城市")) {
            System.out.println("\n" + gq.getClass().getSimpleName());
            System.out.println(gq.getCRS());
        }
    }

    @Test
    public void findAll() throws Exception {
        for (GISQuery gq : getGqs("china", "省会城市")) {
            System.out.println("\n" + gq.getClass().getSimpleName());
            FeatureCollection fc = gq.findAll();

            FeatureIterator features = fc.features();
            while (features.hasNext()) {
                System.out.println(features.next());
            }
        }
    }

    @Test
    public void getPropertyNames() throws Exception {
        for (GISQuery gq : getGqs("china", "国界线")) {
            System.out.println("\n" + gq.getClass().getSimpleName());
            System.out.println(gq.getPropertyNames());
        }
    }

    @Test
    public void exactQuery() throws Exception {
        for (GISQuery gq : getGqs("china", "省会城市")) {
            System.out.println("\n" + gq.getClass().getSimpleName());
            FeatureCollection fc;
            FeatureIterator features;

            String value = "北京";
            System.out.println("查询名字为" + value + "的省会城市");
            fc = gq.exactQuery("name", value);

            features = fc.features();
            while (features.hasNext()) {
                System.out.println(features.next().getProperty("name").getValue());
            }
        }
    }

    @Test
    public void fuzzyQuery() throws Exception {
        for (GISQuery gq : getGqs("china", "省会城市")) {
            System.out.println("\n" + gq.getClass().getSimpleName());
            FeatureCollection fc;
            FeatureIterator features;

            String value = "北";
            System.out.println("查询名字带" + value + "的省会城市");
            fc = gq.fuzzyQuery("name", value);

            features = fc.features();
            while (features.hasNext()) {
                System.out.println(features.next().getProperty("name").getValue());
            }
        }
    }

    @Test
    public void queryByBox() throws Exception {
        for (GISQuery gq : getGqs("china", "省会城市")) {
            System.out.println("\n" + gq.getClass().getSimpleName());
            FeatureCollection fc = gq.queryByBox(
                    445293.84, 3428588.28,
                    1213169.30, 4232855.93);

            FeatureIterator features = fc.features();
            while (features.hasNext()) {
                System.out.println(features.next().getProperty("name").getValue());
            }
        }
    }

    @Test
    public void queryByNeighbor() throws Exception {
        for (GISQuery gq : getGqs("china", "省会城市")) {
            System.out.println("\n" + gq.getClass().getSimpleName());
            // 获取北京的坐标
            Feature beijing = gq.exactQuery("name", "北京").features().next();
            Point beijingPoint = (Point) beijing.getDefaultGeometryProperty().getValue();


            FeatureCollection fc = gq.queryByNeighbor("coordinates",
                    beijingPoint.getX(), beijingPoint.getY(), 500000, 0);
            // POINT (913088.021 4849293.6233)
            FeatureIterator features = fc.features();
            while (features.hasNext()) {
                System.out.println(features.next().getProperty("name").getValue());
            }
        }
    }

    @Test
    public void sortByProperty() throws Exception {
        for (GISQuery gq : getGqs("china", "全国县级统计数据")) {
//        MongogisQuery gq = new MongogisQuery("localhost", "china", "全国县级统计数据");
        System.out.println("\n" + gq.getClass().getSimpleName());
            FeatureCollection fc = gq.sortByProperty("AREA", 1);

            FeatureIterator features = fc.features();
            while (features.hasNext()) {
                System.out.println(features.next().getProperty("NAME").getValue());
            }
        }
    }

    @Test
    public void offset() throws Exception {
        for (GISQuery gq : getGqs("china", "全国县级统计数据")) {
            SimpleFeatureCollection fc = gq.offset(1.0, 1.1);
            FeatureIterator features = fc.features();
            while (features.hasNext()) {
                System.out.println(features.next().getProperty("NAME").getValue());
            }
        }
    }
}