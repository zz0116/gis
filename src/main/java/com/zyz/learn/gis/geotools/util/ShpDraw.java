package com.zyz.learn.gis.geotools.util;

import org.geotools.data.FileDataStore;
import org.geotools.data.FileDataStoreFinder;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.feature.FeatureCollection;
import org.geotools.map.FeatureLayer;
import org.geotools.map.Layer;
import org.geotools.map.MapContent;
import org.geotools.styling.SLD;
import org.geotools.styling.Style;
import org.geotools.swing.JMapFrame;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by ZhangYuanzhuo.
 */
public class ShpDraw {
    public static void draw(FeatureCollection fc) {
        MapContent map = new MapContent();

        Style style = SLD.createSimpleStyle(fc.getSchema());
        Layer layer = new FeatureLayer(fc, style);
        map.addLayer(layer);

        JMapFrame.showMap(map);
    }

    public static void draw(List<File> files) throws IOException {
        MapContent map = new MapContent();

        for (File file : files) {
            FileDataStore store = FileDataStoreFinder.getDataStore(file);
            SimpleFeatureSource featureSource = store.getFeatureSource();

            Style style = SLD.createSimpleStyle(featureSource.getSchema());
            Layer layer = new FeatureLayer(featureSource, style);
            map.addLayer(layer);
        }

        JMapFrame.showMap(map);
    }

    public static void main(String[] args) throws IOException {
        List<File> files = new ArrayList<>();
        List<String> names = new ArrayList<>();

        names.add("主要公路");
        names.add("全国县级统计数据");
        names.add("国界线");
        names.add("省会城市");
        names.add("线状省界");

        for (String name : names) {
            files.add(
                    new File("src/main/resources/china/" + name + "/" + name + ".shp"));
        }
        ShpDraw.draw(files);
    }
}
