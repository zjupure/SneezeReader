package com.simit.json;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.simit.model.Article;

/**
 * Created by liuchun on 2015/12/6.
 */
public class ParserUtils {

    public static String parseSplashImgUrl(String json){
        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.create();

        SplashImgUrl jsonLoadUrl = gson.fromJson(json, SplashImgUrl.class);
        String loadUrl = jsonLoadUrl.getData();

        return loadUrl;
    }

    public static UpdateApkUrl parseUpdateApkUrl(String json){
        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.create();

        return gson.fromJson(json, UpdateApkUrl.class);
    }

    public static Article[] parseArticles(String json){
        GsonBuilder builder = new GsonBuilder();
        builder.registerTypeAdapter(Article.class, new ArticleDeserializer());
        Gson gson = builder.create();

        ArticleWrapper jsonArticle = gson.fromJson(json, ArticleWrapper.class);
        Article[] articles = jsonArticle.getArticles();

        return articles;
    }
}
