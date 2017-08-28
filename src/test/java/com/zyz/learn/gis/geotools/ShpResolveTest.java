package com.zyz.learn.gis.geotools;

import com.vividsolutions.jts.geom.GeometryCollection;
import org.junit.Test;
import org.opengis.feature.type.Name;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.cs.CoordinateSystem;

import java.util.List;
import java.util.Map;

/**
 * Created by ZhangYuanzhuo.
 */
public class ShpResolveTest {
    private com.zyz.learn.gis.geotools.reader.ShpResolve shpResolve = new com.zyz.learn.gis.geotools.reader.ShpResolve("src/main/resources/china/国界线/国界线.shp");

    @Test
    public void getTable() throws Exception {
        List<Map<Name, Object>> table = shpResolve.getTable();
    }

    @Test
    public void getAttributeNames() throws Exception {
        System.out.println(shpResolve.getAttributeNames());
    }

    @Test
    public void getAllGeometry() throws Exception {
        System.out.println(shpResolve.getAllGeometry().get(0).getGeometryType());
        List<GeometryCollection> allGeometry = shpResolve.getAllGeometry();
    }

    @Test
    public void getCRS() throws Exception {
        CoordinateReferenceSystem crs = shpResolve.getCRS();
        System.out.println(crs);
        CoordinateSystem coordinateSystem = crs.getCoordinateSystem();
        System.out.println("------------------");
        System.out.println(coordinateSystem);
        System.out.println("------------------");
        System.out.println(crs.toWKT());
    }

    @Test
    public void setOffset() throws Exception {
        List<Map<Name, Object>> tableAfterOffset = shpResolve.setOffset(10.0, 0);
    }

}