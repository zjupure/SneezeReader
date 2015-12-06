package com.example.datamodel;

/**
 * Created by liuchun on 2015/12/6.
 */
public class Article {
    public static final int TUGUA = 0;
    public static final int LEHUO = 1;
    public static final int YITU = 2;
    public static final int DUANZI = 3;

    private int id = 0;
    private int type;
    private String title;
    private String remote_link;
    private String author;
    private String pubDate;   //"2015-12-05 19:57:00"
    private String description;
    private String imgurl = "";
    private String local_link = "";

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getRemote_link() {
        return remote_link;
    }

    public void setRemote_link(String remote_link) {
        this.remote_link = remote_link;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getPubDate() {
        return pubDate;
    }

    public void setPubDate(String pubDate) {
        this.pubDate = pubDate;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImgurl() {
        return imgurl;
    }

    public void setImgurl(String imgurl) {
        this.imgurl = imgurl;
    }

    public String getLocal_link() {
        return local_link;
    }

    public void setLocal_link(String local_link) {
        this.local_link = local_link;
    }
}
