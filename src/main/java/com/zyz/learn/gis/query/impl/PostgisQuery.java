package com.zyz.learn.gis.query.impl;

import com.zyz.learn.gis.postgis.postgres.crud.DBConnector;
import com.zyz.learn.gis.query.GISQuery;
import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFinder;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.filter.text.cql2.CQL;
import org.geotools.filter.text.cql2.CQLException;
import org.opengis.feature.Property;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.GeometryDescriptor;
import org.opengis.feature.type.Name;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by ZhangYuanzhuo.
 */
public class PostgisQuery implements GISQuery {
    private SimpleFeatureSource featureSource;
    private Connection connection;
    private String tableName;

    public PostgisQuery(String dbName, String username, String password, String tableName) {
        this.tableName = tableName;

        Map<String, Object> params = new HashMap<>();
        params.put("dbtype", "postgis");
        params.put("host", "localhost");
        params.put("port", 5432);
        params.put("schema", "public");
        params.put("database", dbName);
        params.put("user", username);
        params.put("passwd", password);

        try {
            DataStore dataStore = DataStoreFinder.getDataStore(params);
            featureSource = dataStore.getFeatureSource(tableName.replace(" ", "%20"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        connection = new DBConnector("jdbc:postgresql://localhost:5432/" + dbName, username, password, false).getConnection();
    }

    @Override
    public CoordinateReferenceSystem getCRS() {
        SimpleFeatureType schema = featureSource.getSchema(); // crs = null
        GeometryDescriptor geometryDescriptor = schema.getGeometryDescriptor();
        Map<Object, Object> userData = geometryDescriptor.getUserData();
        int srid = (int) userData.get("nativeSRID");
        System.out.println(srid);
//        SimpleFeatureType featureType = null;
//        try {
//            featureType = featureSource.getFeatures().features().next().getFeatureType();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        System.out.println(featureType);
//        return null;
        return featureSource.getSchema().getCoordinateReferenceSystem();
    }

    @Override
    public SimpleFeatureCollection findAll() {
        SimpleFeatureCollection fc = null;
        try {
            fc = featureSource.getFeatures();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return fc;
    }

    @Override
    public Collection<Name> getPropertyNames() {
        Collection<Property> properties = null;
        try {
            properties = featureSource.getFeatures().features().next().getProperties();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Collection<Name> names = new ArrayList<>();
        assert properties != null;
        for (Property property : properties) {
            names.add(property.getName());
        }
        return names;
    }

    @Override
    public SimpleFeatureCollection exactQuery(String fieldName, String value) {
        SimpleFeatureCollection fc = null;
        try {
            fc = featureSource.getFeatures(CQL.toFilter(fieldName + " = '" + value + "'"));
        } catch (IOException | CQLException e) {
            e.printStackTrace();
        }
        return fc;
    }

    @Override
    public SimpleFeatureCollection fuzzyQuery(String fieldName, String value) {
        SimpleFeatureCollection fc = null;
        try {
            fc = featureSource.getFeatures(CQL.toFilter(fieldName + " like " + "'%" + value + "%'"));
        } catch (IOException | CQLException e) {
            e.printStackTrace();
        }
        return fc;
    }

    @Override
    public SimpleFeatureCollection queryByBox(double x1, double y1,
                                              double x2, double y2) {
        ResultSet resultSet;
        Statement statement;
        Collection<String> names = new ArrayList<>();
        try {
            statement = connection.createStatement();
            resultSet = statement.executeQuery(
                    "SELECT name" +
                            " FROM " + tableName +
                            " WHERE ST_DWithin(the_geom, ST_MakeEnvelope(" + x1 + "," + y1 + "," + x2 + "," + y2 + "), 100)");

            while (resultSet.next()) {
                names.add(resultSet.getString(1));
            }

            resultSet.close();
            statement.close();
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // 无法包装成FeatureCollection返回，只能看具体情况返回。
//        return names;
        System.out.println(names);
        return null;
    }

    @Override
    public SimpleFeatureCollection queryByNeighbor(String fieldName, double x, double y,
                                                   double maxDistance, double minDistance) {
        ResultSet resultSet;
        Statement statement;
        Collection<String> names = new ArrayList<>();
        try {
            statement = connection.createStatement();
            String point = "point(" + x + " " + y + ")";
            resultSet = statement.executeQuery(
                    "SELECT name" +
                            " FROM " + tableName +
                            " WHERE st_distance(the_geom, st_geomfromtext('" + point + "', 0)) BETWEEN " + minDistance + " AND " + maxDistance);

            while (resultSet.next()) {
                names.add(resultSet.getString(1));
            }

            resultSet.close();
            statement.close();
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        System.out.println(names);
        return null;
    }

    @Override
    public SimpleFeatureCollection sortByProperty(String fieldName, int order) {
        ResultSet resultSet;
        Statement statement;
        Collection<String> names = new ArrayList<>();
        try {
            statement = connection.createStatement();
            String sortOrder;
            if (order == 1) {
                sortOrder = "ASC";
            } else {
                sortOrder = "DESC";
            }
            resultSet = statement.executeQuery(
                    "SELECT \"NAME\"" +
                            " FROM " + tableName +
                            " ORDER BY \"" + fieldName + "\" " + sortOrder);

            while (resultSet.next()) {
                names.add(resultSet.getString(1));
            }

            resultSet.close();
            statement.close();
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        System.out.println(names);
        return null;
    }

    @Override
    public SimpleFeatureCollection offset(double xOffset, double yOffset) {
        return null;
    }
}
