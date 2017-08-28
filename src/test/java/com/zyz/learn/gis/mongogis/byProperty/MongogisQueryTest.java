package com.zyz.learn.gis.mongogis.byProperty;

import com.zyz.learn.gis.mongogis.MongogisQueryByProperty;
import org.bson.Document;
import org.junit.Test;

import java.util.List;
import java.util.Set;

/**
 * Created by ZhangYuanzhuo.
 */
public class MongogisQueryTest {
    private MongogisQueryByProperty mq = new MongogisQueryByProperty(
            "localhost",
            "china",
            "省会城市",
            "crs");

    @Test
    public void getCRS() throws Exception {
        System.out.println(mq.getCRS());
    }

    @Test
    public void findAll() throws Exception {
        List<Document> documents = mq.findAll();

        for (Document doc : documents) {
            System.out.println(doc);
        }
    }

    @Test
    public void getPropertyName() throws Exception {
        Set<String> names = mq.getPropertyName();
        for (String name : names) {
            System.out.println(name);
        }
    }

    @Test
    public void queryByProperty() throws Exception {
        // 省会城市
        List<Document> documents = mq.queryByProperty("name",
                new Document("$regex", ".京"));
//        List<Document> documents = mq.queryByProperty("name", "北京");
        for (Document document : documents) {
            System.out.println(document);
        }
    }

    @Test
    public void sortByProperty() throws Exception {
        // 全国县级统计数据
        List<Document> documents = mq.sortByProperty("AREA");
        for (Document document : documents) {
            System.out.println(document.get("NAME"));
        }
    }

    @Test
    public void neighborQuery() throws Exception {
        List<Document> documents = mq.neighborQuery(372452.58, 4238371.63, 10);
        for (Document document : documents) {
            System.out.println(document);
        }
    }

    @Test
    public void rangeQuery() throws Exception {
        List<Document> documents = mq.rangeQuery(
                2026479.559, 2826604.794,
                1154975.422, 3731628.321);
        for (Document document : documents) {
            System.out.println(document);
        }
    }

    @Test
    public void writeToShapefile() throws Exception {
    }

}