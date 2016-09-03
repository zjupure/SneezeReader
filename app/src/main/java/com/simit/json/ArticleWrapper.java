package com.simit.json;

import com.simit.model.Article;

/**
 * Created by liuchun on 2015/12/6.
 */
public class ArticleWrapper {
    private String msg;
    private int error;
    private Article[] articles;

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public int getError() {
        return error;
    }

    public void setError(int error) {
        this.error = error;
    }

    public Article[] getArticles() {
        return articles;
    }

    public void setArticles(Article[] articles) {
        this.articles = articles;
    }

    @Override
    public String toString() {
        return "{\"msg\":" + msg + ",\"error\":" + error + ",\"data\":" + articles.toString() + "}";
    }
}
