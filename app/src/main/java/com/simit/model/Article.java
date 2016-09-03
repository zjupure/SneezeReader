package com.simit.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by liuchun on 2015/12/6.
 */
public class Article implements Parcelable{
    public static final int TUGUA = 0;
    public static final int LEHUO = 1;
    public static final int YITU = 2;
    public static final int DUANZI = 3;
    /**
     * 文章id, 数据库唯一标识
     */
    private int id;
    /**
     * 文章所属分类
     */
    private int type;
    /**
     * 文章标题
     */
    private String title;
    /**
     * 文章链接
     */
    private String link;
    /**
     * 文章作者
     */
    private String author;
    /**
     * 文章发表日期
     * format: "2015-12-05 19:57:00"
     */
    private String pubDate;
    /**
     * 文章描述
     */
    private String description;
    /**
     * 文章图片地址
     */
    private String imgUrl = "";
    /**
     * 文章存储在本地的文件路径
     */
    private String localLink = "";
    /**
     * 用户是否收藏了该篇文章
     */
    private boolean isFavorite;

    public Article(){

    }

    protected Article(Parcel in) {
        id = in.readInt();
        type = in.readInt();
        title = in.readString();
        link = in.readString();
        author = in.readString();
        pubDate = in.readString();
        description = in.readString();
        imgUrl = in.readString();
        localLink = in.readString();
        isFavorite = in.readByte() != 0;
    }

    public static final Creator<Article> CREATOR = new Creator<Article>() {
        @Override
        public Article createFromParcel(Parcel in) {
            return new Article(in);
        }

        @Override
        public Article[] newArray(int size) {
            return new Article[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeInt(type);
        dest.writeString(title);
        dest.writeString(link);
        dest.writeString(author);
        dest.writeString(pubDate);
        dest.writeString(description);
        dest.writeString(imgUrl);
        dest.writeString(localLink);
        dest.writeByte(isFavorite ? (byte)1 : (byte)0);
    }

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

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
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

    public String getImgUrl() {
        return imgUrl;
    }

    public void setImgUrl(String imgUrl) {
        this.imgUrl = imgUrl;
    }

    public String getLocalLink() {
        return localLink;
    }

    public void setLocalLink(String localLink) {
        this.localLink = localLink;
    }

    public boolean isFavorite() {
        return isFavorite;
    }

    public void setFavorite(boolean favorite) {
        isFavorite = favorite;
    }

    @Override
    public int hashCode() {
        // link is unique
        return link.hashCode();
    }

    @Override
    public String toString() {

        return "{\"title\":" + title + ", \"link\":" + link + ",\"author\":" + author +
                ", \"pubDate\":" + pubDate + ",\"description\":" + description +
                ", \"imgurl\":" + imgUrl;
    }


}
