package com.simit.json;

import com.simit.database.Article;

import java.util.Arrays;

/**
 * Created by liuchun on 2015/12/6.
 */
public class ArticleWrapper {
    private String msg;
    private int error;
    private Article[] data;

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

    public Article[] getData() {
        return data;
    }

    public void setData(Article[] data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "{\"msg\":" + msg + ",\"error\":" + error + ",\"data\":" + Arrays.toString(data) + "}";
    }
}
