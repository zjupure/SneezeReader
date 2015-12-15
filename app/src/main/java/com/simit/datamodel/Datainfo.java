package com.simit.datamodel;


import java.util.Date;

/**
 * Created by liuchun on 2015/7/16.
 */
public class Datainfo {
    public final static int TUGUA = 0;
    public final static int LEHUO = 1;
    public final static int YITU = 2;
    public final static int DUANZI = 3;

    private int id = 0;      //唯一编号,默认是0,需要从数据库读取
    private String title = "";         //文章标题
    private int type;            //文章所属分类
    private String author = "";        //文章发表作者
    private Date  publishdate;   //文章发表时间
    private String content = "";  //文章具体内容,默认为空
    private String remote_url = "";  //远程URL地址
    private String local_url = ""; //本地新建Html文件地址

    //默认空构造函数
    public Datainfo(){

    }

    public Datainfo(int type, String remote_url){
        this.type = type;
        this.remote_url = remote_url;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Date getPublishdate() {

        return publishdate;
    }

    public void setPublishdate(Date publishdate) {
        this.publishdate = publishdate;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getRemote_url() {
        return remote_url;
    }

    public void setRemote_url(String remote_url) {
        this.remote_url = remote_url;
    }

    public String getLocal_url() {
        return local_url;
    }

    public void setLocal_url(String local_url) {
        this.local_url = local_url;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }
}
