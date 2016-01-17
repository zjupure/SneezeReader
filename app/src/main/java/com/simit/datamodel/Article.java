package com.simit.datamodel;

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
    public static final int[] TYPE = {TUGUA, LEHUO, YITU, DUANZI};

    private int id = 0;
    private int type;
    private String title;
    private String remote_link;
    private String author;
    private String pubDate;   //"2015-12-05 19:57:00"
    private String description;
    private String imgurl = "";
    private String local_link = "";
    private boolean isFavorite;

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

    public boolean isFavorite() {
        return isFavorite;
    }

    public void setIsFavorite(boolean isFavorite) {
        this.isFavorite = isFavorite;
    }

    @Override
    public int hashCode() {
        // remote_link is unique
        return remote_link.hashCode();
    }

    @Override
    public int describeContents() {
        return type;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeInt(type);
        dest.writeString(title);
        dest.writeString(remote_link);
        dest.writeString(author);
        dest.writeString(pubDate);
        dest.writeString(description);
        dest.writeString(imgurl);
        dest.writeString(local_link);
        byte fav = (byte)(isFavorite ? 1 : 0);
        dest.writeInt(fav);
    }

    public Article(){}

    public Article(Parcel src){
        id = src.readInt();
        type = src.readInt();
        title = src.readString();
        remote_link=  src.readString();
        author = src.readString();
        pubDate = src.readString();
        description = src.readString();
        imgurl = src.readString();
        local_link = src.readString();
        isFavorite = (src.readByte() != 0);
    }

    public static final Parcelable.Creator<Article> CREATOR = new Creator<Article>() {
        @Override
        public Article createFromParcel(Parcel source) {
            return new Article(source);
        }

        @Override
        public Article[] newArray(int size) {
            return new Article[size];
        }
    };
}
