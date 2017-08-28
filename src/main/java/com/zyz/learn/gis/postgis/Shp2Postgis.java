package com.zyz.learn.gis.postgis;

import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFinder;
import org.geotools.data.FeatureSource;
import org.geotools.data.FeatureStore;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.referencing.CRS;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.ReferenceIdentifier;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Created by ZhangYuanzhuo.
 */
public class Shp2Postgis {
    public static void main(String[] args) {
        String db = "china"; // 数据库名称
        String featureCollectionName = "省会城市"; // 存储Features的Collection名称
        String shapeFileLoc = "src/main/resources/" +
                db + "/" + featureCollectionName + "/" + featureCollectionName + ".shp"; // ShapeFile相对路径
//        String shapeEPSG = "EPSG:4326"; // the epsg to use if not found from shapefile

        try {
            // shapefile loader
            Map<Object, Serializable> shapeParams = new HashMap<>();
            shapeParams.put("url", new File(shapeFileLoc).toURI().toURL());
            shapeParams.put("charset", "GBK"); // for greek chars
//            shapeParams.put( "charset", "ISO-8859-7" ); // for greek chars
            DataStore shapeDataStore = DataStoreFinder.getDataStore(shapeParams);

            // feature type
            String typeName = shapeDataStore.getTypeNames()[0];
            FeatureSource<SimpleFeatureType, SimpleFeature> featSource = shapeDataStore.getFeatureSource(typeName);
            FeatureCollection<SimpleFeatureType, SimpleFeature> featSrcCollection = featSource.getFeatures();
            SimpleFeatureType featureType = shapeDataStore.getSchema(typeName);

            // feature type copy to set the new name
            SimpleFeatureTypeBuilder builder = new SimpleFeatureTypeBuilder();
            builder.setName(featureCollectionName);
            builder.setAttributes(featureType.getAttributeDescriptors());
            builder.setCRS(featureType.getCoordinateReferenceSystem());

            SimpleFeatureType newSchema = builder.buildFeatureType();

            // management of the projection system
            CoordinateReferenceSystem crs = featureType.getCoordinateReferenceSystem();

            // test of the CRS based on the .prj file
            Integer crsCode = CRS.lookupEpsgCode(crs, true);

            Set<ReferenceIdentifier> refIds = featureType.getCoordinateReferenceSystem().getIdentifiers();
            if (((refIds == null) || (refIds.isEmpty())) && (crsCode == null)) {
//                CoordinateReferenceSystem crsEpsg = CRS.decode(shapeEPSG);
                newSchema = SimpleFeatureTypeBuilder.retype(newSchema, crs);
//                newSchema = SimpleFeatureTypeBuilder.retype(newSchema, crsEpsg);
            }

            Map postGISParams = new HashMap<String, Object>();
            postGISParams.put("dbtype", "postgis"); //must be postgis
            postGISParams.put("host", "localhost"); //the name or ip address of the machine running PostGIS
            postGISParams.put("port", 5432); //the port that PostGIS is running on (generally 5432)
            postGISParams.put("database", "china"); //the name of the database to connect to.
            postGISParams.put("user", "postgres"); //the user to connect with
            postGISParams.put("passwd", "123"); //the password of the user.
//            postGISParams.put("schema", "myschema"); //the schema of the database
            postGISParams.put("create spatial index", Boolean.TRUE);
            DataStore dataStore = null;
            try {
                // storage in PostGIS
                dataStore = DataStoreFinder.getDataStore(postGISParams);
            } catch (Exception e) {
                System.out.println("problem with datastore: " + e);
            }

            if (dataStore == null) {
                System.out.println("ERROR: dataStore is null");
            }

            assert dataStore != null;
            dataStore.createSchema(featureType);
//            dataStore.createSchema(newSchema);
            FeatureStore<SimpleFeatureType, SimpleFeature> featureStore =
                    (FeatureStore<SimpleFeatureType, SimpleFeature>) dataStore.getFeatureSource(featureCollectionName);
            featureStore.addFeatures(featSrcCollection);
        } catch (IOException | FactoryException e) {
            e.printStackTrace();
        }
        System.out.println("数据导入完成！");
    }
}
