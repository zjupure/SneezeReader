package com.example.jsonparser;

/**
 * Created by liuchun on 2015/12/6.
 */
public class ArticleData {
    private String title;
    private String link;
    private String author;
    private String pubDate;
    private String description;
    private String imgurl;

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

    public String getImgurl() {
        return imgurl;
    }

    public void setImgurl(String imgurl) {
        this.imgurl = imgurl;
    }


    @Override
    public String toString() {

        return "{\"title\":" + title + ", \"link\":" + link + ",\"author\":" + author +
                ", \"pubDate\":" + pubDate + ",\"description\":" + description +
                ", \"imgurl\":" + imgurl;
    }
}
