package com.zyz.learn.gis.query;

import org.geotools.data.simple.SimpleFeatureCollection;
import org.opengis.feature.type.Name;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import java.util.Collection;

/**
 * Created by ZhangYuanzhuo.
 */
public interface GISQuery {

    // 获取坐标参考系
    CoordinateReferenceSystem getCRS();

    // 查询所有属性
    SimpleFeatureCollection findAll();

    // 查询所有的属性名
    Collection<Name> getPropertyNames();

    // 根据某个属性进行精确查询
    SimpleFeatureCollection exactQuery(String fieldName, String value);

    // 根据某个属性进行精确查询
    SimpleFeatureCollection  fuzzyQuery(String fieldName, String value);

    // 根据鼠标选框查询属性
    SimpleFeatureCollection queryByBox(double x1, double y1,
                                       double x2, double y2);

    // 邻域查询
    SimpleFeatureCollection queryByNeighbor(String fieldName, double x, double y,
                                            double maxDistance, double minDistance);

    // 按照某个属性进行排序
    SimpleFeatureCollection sortByProperty(String fieldName, int order);

    // 对地理属性添加偏移量
    SimpleFeatureCollection offset(double xOffset, double yOffset);
}
