package com.simit.jsonparser;

/**
 * Created by liuchun on 2016/1/23.
 */
public class JsonUpdateLink {
    private String msg;
    private int error;
    private String version;
    private String link;

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

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }


    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    @Override
    public String toString() {
        return "{\"msg\":\"" + msg + "\",\"error\":" + error + ",\"version\":\"" + version.toString() +
                "\",\"link\":\"" + link.toString() + "\"}";
    }
}
