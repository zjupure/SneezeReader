package com.example.jsonparser;

/**
 * Created by liuchun on 2015/12/6.
 */
public class JsonArticle {
    private String msg;
    private int error;
    private ArticleData[] data;

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

    public ArticleData[] getData() {
        return data;
    }

    public void setData(ArticleData[] data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "{\"msg\":" + msg + ",\"error\":" + error + ",\"data\":" + data.toString() + "}";
    }
}
