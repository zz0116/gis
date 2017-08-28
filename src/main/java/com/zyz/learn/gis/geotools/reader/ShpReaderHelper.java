package com.zyz.learn.gis.geotools.reader;

/**
 * Created by ZhangYuanzhuo.
 */

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryCollection;
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

import java.io.File;
import java.util.*;

public class ShpReaderHelper {
    public static void main(String[] args) throws Exception {
        String path = "src/main/resources/world/World Time Zones/World Time Zones.shp";

        File file = new File(path);
        if (!file.exists() || !path.endsWith(".shp")) {
            throw new Exception("Invalid shapefile filepath: " + path);
        }
        ShapefileDataStore dataStore = new ShapefileDataStore(file.toURI().toURL());
        ContentFeatureSource featureSource = dataStore.getFeatureSource();

//        featureCollection.getSchema() // featureCollection里面有featureType，再下一级有properties，但是取不出来
//        String typeName = featureSource.getSchema().getTypeName(); // World%20Time%20Zones
        SimpleFeatureType schema = featureSource.getSchema(); // schema里面有properties
//        Class<Collection<Property>> binding = schema.getBinding(); // 取的类名，没用
        List<AttributeType> types = schema.getTypes(); // 这个是的，属性，不过还有类名在里面

        Iterator<AttributeType> typeIterator = types.iterator();
        List<Name> attributes = new ArrayList<>(types.size());
        while (typeIterator.hasNext()) {
            AttributeType next = typeIterator.next();
            Name name = next.getName();
            attributes.add(name);
        }

        ContentFeatureCollection featureCollection = featureSource.getFeatures();


        SimpleFeatureIterator iterator = featureCollection.features();

//        System.out.println(dataStore.getCharset());
//        System.out.println(dataStore.getTimeZone());
//        System.out.println(dataStore.getSchema().getCoordinateReferenceSystem());


//        FeatureReader<SimpleFeatureType, SimpleFeature> featureReader = dataStore.getFeatureReader();
//        while (featureReader.hasNext()) {
//            SimpleFeature feature = featureReader.next();
//            System.out.println(feature);
//        } // 得到的feature是一样的


//        List<AttributeType> types = null;
        List<Map<Name, Object>> table = new ArrayList<>();

        while (iterator.hasNext()) {
            SimpleFeature feature = iterator.next();

            // 拿到属性作为字段，第一个属性是不是总是Geometry数据（点、线或多边形）？
            // 不是，但是好像一般叫the_geom
//            if (types == null) {
//                types = feature.getFeatureType().getTypes();
//            }

            Map<Name, Object> row = new HashMap<>();
            Collection<Property> properties = feature.getProperties();
//            List values = new ArrayList();
            for (Property p : properties) {
                row.put(p.getName(), p.getValue());
//                values.add(p.getValue());
            }

            table.add(row);
//            List attributes = feature.getAttributes();


//            String id = feature.getID(); // 没用，自动生成就行了
//            SimpleFeatureType featureType = feature.getFeatureType();
//            Collection<? extends Property> value = feature.getValue(); // 跟想的不一样，不是拿出纯粹的数据（坐标以及地理数据）

//            geom.put(id, value);
        }

//        table.contains()

        // 所有信息被放到table里面，要想把地理信息属性取出来，必须得到Map里面地理信息对应的key
        Set<Name> names = table.get(0).keySet();
        Object o = names.toArray()[2]; // 通过debug获知地理信息下标为2，但是取出来还是什么类
        Object oValue = table.get(0).get(o);

        Class<?> oClass = o.getClass(); // 利用反射得到实例化o的类，得知类为NameImpl，有公开的构造方法
        NameImpl the_geom_key = new NameImpl("the_geom");
        Object o1 = table.get(0).get(the_geom_key); // 可以根据这个key获得地理信息，但是不知道地理信息的类
        Class<?> the_geomClass = o1.getClass(); // 继续反射获得类，类为com.vividsolutions.jts.geom.MultiPolygon，但是这个是多边形的类，父类为GeometryCollection

        GeometryCollection the_geom = (GeometryCollection) table.get(0).get(the_geom_key);

        // 得到了地理信息的集合，接下来就是取地理信息的类型（是点、线还是多边形），取坐标
//        int numGeometries = the_geom.getNumGeometries(); // 1个地理信息
        String geometryType = the_geom.getGeometryType(); // 类型
        Coordinate[] coordinates = the_geom.getCoordinates(); // 这个就是了，所有的坐标，能不能一个个的取出来？
        Coordinate coordinate = coordinates[0]; // 拿出一个坐标，xy能不能拿出来？
        double x = coordinate.x;
        double y = coordinate.y;
    }
}
