package com.zyz.learn.gis.mongogis.byFeature;

import com.vividsolutions.jts.geom.Point;
import com.zyz.learn.gis.mongogis.MongogisQueryByFeature;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.junit.Test;
import org.opengis.feature.Feature;

/**
 * Created by ZhangYuanzhuo.
 */
public class MongogisQueryByPropertyByFeatureTest {
    private MongogisQueryByFeature mq;

    @Test
    public void getCRS() throws Exception {
    }

    @Test
    public void findAll() throws Exception {
        mq = new MongogisQueryByFeature("localhost", "china", "省会城市");
        FeatureCollection fc = mq.findAll();

        FeatureIterator features = fc.features();
        while (features.hasNext()) {
            System.out.println(features.next());
        }
    }

    @Test
    public void getPropertyNames() throws Exception {
        mq = new MongogisQueryByFeature("localhost", "china", "国界线");
        System.out.println(mq.getPropertyNames());
    }

    @Test
    public void queryByProperty() throws Exception {
        mq = new MongogisQueryByFeature("localhost", "china", "省会城市");
        FeatureCollection fc;
        FeatureIterator features;

        System.out.println("1. 查询名字为“北京”的省会城市。");
        fc = mq.queryByProperty("properties.name", "北京");

        features = fc.features();
        while (features.hasNext()) {
            System.out.println(features.next().getProperty("name").getValue());
        }

        System.out.println("\n2. 查询名字带“北”的省会城市");
        fc = mq.queryByProperty("properties.name", ".*北.*");

        features = fc.features();
        while (features.hasNext()) {
            System.out.println(features.next().getProperty("name").getValue());
        }
    }

    @Test
    public void queryByBox() throws Exception {
        mq = new MongogisQueryByFeature("localhost", "china", "省会城市");
        FeatureCollection fc = mq.queryByBox(
                445293.84, 3428588.28,
                1213169.30, 4232855.93);

        FeatureIterator features = fc.features();
        while (features.hasNext()) {
            System.out.println(features.next().getProperty("name").getValue());
        }
    }

    @Test
    public void queryNear() throws Exception {
        mq = new MongogisQueryByFeature("localhost", "china", "省会城市");

        // 获取北京的坐标
        Feature beijing = mq.queryByProperty("properties.name", "北京").features().next();
        Point beijingPoint = (Point) beijing.getDefaultGeometryProperty().getValue();


        FeatureCollection fc = mq.queryByNeighbor("geometry.coordinates", beijingPoint.getX(), beijingPoint.getY(), 500000, 0);
        // POINT (913088.021 4849293.6233)
        FeatureIterator features = fc.features();
        while (features.hasNext()) {
            System.out.println(features.next().getProperty("name").getValue());
        }
    }

    @Test
    public void sortByProperty() throws Exception {
        mq = new MongogisQueryByFeature("localhost", "china", "全国县级统计数据");
        FeatureCollection fc = mq.sortByProperty("properties.AREA", 1);

        FeatureIterator features = fc.features();
        while (features.hasNext()) {
            System.out.println(features.next().getProperty("NAME").getValue());
        }
    }

    @Test
    public void main() throws Exception {
    }
}