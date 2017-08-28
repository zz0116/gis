package com.zyz.learn.gis.mongogis.mongodb.model;

import com.mongodb.BasicDBObject;

import java.util.Date;

/**
 * Created by ZhangYuanzhuo.
 */
public class User extends BasicDBObject {
    private long idCard;

    private String name;

    private Date createTime;

    public User(long idCard, String name) {
        this.idCard = idCard;
        this.name = name;
        this.createTime = new Date();

        append("idCard", idCard);
        append("name", name);
        append("createTime", createTime);
    }

    public long getIdCard() {
        return idCard;
    }

    public String getName() {
        return name;
    }

    public Date getCreateTime() {
        return createTime;
    }

}
