package com.zyz.learn.gis.geotools.reader;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryCollection;
import org.geotools.data.Query;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.data.store.ContentFeatureCollection;
import org.geotools.data.store.ContentFeatureSource;
import org.geotools.feature.NameImpl;
import org.opengis.feature.Property;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeType;
import org.opengis.feature.type.Name;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Created by ZhangYuanzhuo.
 */
public class ShpResolve {
    private final NameImpl THE_GEOM_KEY = new NameImpl("the_geom");
    private ContentFeatureSource featureSource; // 从shapefile获取feature数据源
    private int size;

    public ShpResolve(String path) {
        File file = new File(path);
        try {
            if (!file.exists() || !path.endsWith(".shp")) {
                throw new IOException("Invalid shapefile filepath: " + path);
            }
            ShapefileDataStore dataStore = new ShapefileDataStore(file.toURI().toURL());
            featureSource = dataStore.getFeatureSource(); // 所有的信息，不管是.dbf还是.prj的信息都在里面
            size = featureSource.getCount(new Query());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 取出shapefile所有Feature存入一个List
     *
     * @return 存有所有Feature的List
     */
    public List<Map<Name, Object>> getTable() {
        ContentFeatureCollection featureCollection = null;
        try {
            featureCollection = featureSource.getFeatures();
        } catch (IOException e) {
            e.printStackTrace();
        }
        SimpleFeatureIterator features = featureCollection.features();

        List<Map<Name, Object>> table = new ArrayList<>(size);
        while (features.hasNext()) {
            SimpleFeature feature = features.next();
            Collection<Property> properties = feature.getProperties();

            // 将每一行属性和属性所对应的值存入Map
            Map<Name, Object> row = new HashMap<>();
            for (Property p : properties) {
                row.put(p.getName(), p.getValue());
            }

            // 将所有的行存入List
            table.add(row);
        }
        return table;
    }

    /**
     * 取出所有属性（对应数据库中一张表的所有字段）
     *
     * @return 所有属性
     */
    public List<String> getAttributeNames() {
        SimpleFeatureType schema = featureSource.getSchema();
        List<AttributeType> types = schema.getTypes();
        Iterator<AttributeType> typeIterator = types.iterator();

        // 获取每一个属性并装入List
        List<String> attributeNames = new ArrayList<>(size);
        while (typeIterator.hasNext()) {
            attributeNames.add(typeIterator.next().getName().toString());
        }
        return attributeNames;
    }

    /**
     * 获取所有feature的地理属性
     * 可以由此获取到具体的类型（点、线、多边形），获取x、y
     *
     * @return 所有地理属性
     */
    public List<GeometryCollection> getAllGeometry() {
        List<GeometryCollection> gcs = new ArrayList<>(size);
        for (Map<Name, Object> row : getTable()) {
            GeometryCollection row_geom = (GeometryCollection) row.get(THE_GEOM_KEY);
            gcs.add(row_geom);
        }
        return gcs;
    }

    /**
     * 获取该shapefile所使用的坐标参考系统
     *
     * @return 坐标参考系统
     */
    public CoordinateReferenceSystem getCRS() {
        return featureSource.getSchema().getCoordinateReferenceSystem();
    }

    /**
     * @param xOffset 横坐标偏移
     * @param yOffset 纵坐标偏移
     * @return 偏移处理后的所有属性
     */
    public List<Map<Name, Object>> setOffset(double xOffset, double yOffset) {
        List<Map<Name, Object>> tableAfterOffset = new ArrayList<>(getTable());
        for (Map<Name, Object> row : tableAfterOffset) {
            GeometryCollection row_geom = (GeometryCollection) row.get(THE_GEOM_KEY);
            Coordinate[] coordinates = row_geom.getCoordinates();
            for (Coordinate coordinate : coordinates) {
                coordinate.x += xOffset;
                coordinate.y += yOffset;
            }
        }
        return tableAfterOffset;
    }
}
