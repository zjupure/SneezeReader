package com.simit.jsonparser;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * Created by liuchun on 2015/12/6.
 */
public class JsonParserUtil {

    public static String JsonLoadUrlParser(String json){
        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.create();

        JsonLoadUrl jsonLoadUrl = gson.fromJson(json, JsonLoadUrl.class);
        String loadUrl = jsonLoadUrl.getData();

        return loadUrl;
    }

    public static JsonUpdateLink JsonUpdateLinkParser(String json){
        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.create();

        return gson.fromJson(json, JsonUpdateLink.class);
    }

    public static ArticleData[] JsonArticleParser(String json){
        GsonBuilder builder = new GsonBuilder();
        builder.registerTypeAdapter(ArticleData.class, new ArticleDataDeserializer());
        Gson gson = builder.create();

        JsonArticle jsonArticle = gson.fromJson(json, JsonArticle.class);
        ArticleData[] datas = jsonArticle.getData();

        return datas;
    }
}
