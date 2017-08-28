package com.zyz.learn.gis.mongogis.mongodb.crud;

import com.mongodb.BasicDBObject;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;
import com.zyz.learn.gis.mongogis.mongodb.model.User;
import org.bson.Document;

import java.util.ArrayList;
import java.util.List;

import static com.mongodb.client.model.Filters.eq;

/**
 * Created by ZhangYuanzhuo.
 */
public class MongoDBCRUD {
    public static void main(String[] args) {
        MongoClient client = new MongoClient("localhost", 27017);
        MongoDatabase db = client.getDatabase("crud");
        db.getCollection("user").drop();
        MongoCollection<Document> collection = db.getCollection("user");

        List<Document> docList = new ArrayList<>();
        for (int i = 1; i <= 5; i++) {
            BasicDBObject o = new User(i, "zyz-" + i);
            Document doc = new Document(o);
            docList.add(doc);
        }

        // create user
        collection.insertMany(docList);

        // read user
        System.out.println("1. Find first matched document");
        Document first = collection.find().first();
        System.out.println(first.toJson());

        System.out.println("\n1. Find all matched documents");
        MongoCursor<Document> cursor = collection.find().iterator();
        try {
            while (cursor.hasNext()) {
                System.out.println(cursor.next().toJson());
            }
        } finally {
            cursor.close();
        }

        System.out.println("\n1. Get 'name' field only");
        MongoCursor<String> name = collection.distinct("name", String.class).iterator();
        try {
            while (name.hasNext()) {
                System.out.println(name.next());
            }
        } finally {
            name.close();
        }

        System.out.println("\n2. Find where idCard = 5");
        cursor = collection.find(eq("idCard", 5)).iterator();
        try {
            while (cursor.hasNext()) {
                System.out.println(cursor.next().toJson());
            }
        } finally {
            cursor.close();
        }

        System.out.println("\n2. Find where idCard in 2,4 and 5");
        Document filter = new Document();
        List<Integer> list = new ArrayList<>();
        list.add(2);
        list.add(4);
        list.add(5);
        filter.put("idCard", new Document("$in", list));
        cursor = collection.find(filter).iterator();
        try {
            while (cursor.hasNext()) {
                System.out.println(cursor.next().toJson());
            }
        } finally {
            cursor.close();
        }

        System.out.println("\n2. Find where 5 > idCard > 2");
        filter = new Document();
        filter.put("idCard", new Document("$gt", 2).append("$lt", 5));
        cursor = collection.find(filter).iterator();
        try {
            while (cursor.hasNext()) {
                System.out.println(cursor.next().toJson());
            }
        } finally {
            cursor.close();
        }

        System.out.println("\n2. Find where idCard != 4");
        filter = new Document();
        filter.put("idCard", new Document("$ne", 4));
        cursor = collection.find(filter).iterator();
        try {
            while (cursor.hasNext()) {
                System.out.println(cursor.next().toJson());
            }
        } finally {
            cursor.close();
        }

        System.out.println("\n3. Find when idCard = 2 and name = 'zyz-2' example");
        filter = new Document();
        docList = new ArrayList<>();
        docList.add(new Document("idCard", 2));
        docList.add(new Document("name", "zyz-2"));
        filter.put("$and", docList);
        System.out.println(filter.toJson());
        cursor = collection.find(filter).iterator();
        try {
            while (cursor.hasNext()) {
                System.out.println(cursor.next().toJson());
            }
        } finally {
            cursor.close();
        }

        System.out.println("\n4. Find where name = 'Zy.*-[1-3]', case sensitive example");
        filter = new Document();
        // . 匹配除了"\n"之外的任何单个字符。
        // * 匹配前面的子表达式零次或多次。例如，zo* 能匹配 "z" 以及 "zoo"。
        filter.put("name",
                new Document("$regex", "Zy.*-[1-3]").append("$options", "i"));
        System.out.println(filter.toJson());
        cursor = collection.find(filter).iterator();
        try {
            while (cursor.hasNext()) {
                System.out.println(cursor.next().toJson());
            }
        } finally {
            cursor.close();
        }

        // update user
        filter = new Document();
        filter.put("name", "zyz-4");
        UpdateResult updateResult = collection.updateOne(filter,
                new Document("$set", new Document("name", "李四")));

        // delete user
        filter = new Document();
        DeleteResult deleteResult = collection.deleteMany(filter);

        // close resources
        client.close();

    }

}
