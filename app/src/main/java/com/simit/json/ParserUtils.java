package com.simit.json;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.simit.database.Article;

import java.lang.reflect.Type;

/**
 * Created by liuchun on 2015/12/6.
 */
public class ParserUtils {
    private static final String TAG = "ParserUtils";

    /**
     * 解析Splash的图片
     * @param json
     * @return
     */
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


    /**
     *
     * @param json
     * @return
     */
    public static Article[] parseArticles(String json){
        GsonBuilder builder = new GsonBuilder();
        builder.registerTypeAdapter(Article.class, new ArticleDeserializer());
        builder.registerTypeAdapter(ArticleWrapper.class, new ArticleWrapperDeserializer());
        Gson gson = builder.create();


        ArticleWrapper jsonArticle = gson.fromJson(json, ArticleWrapper.class);
        Article[] articles = jsonArticle.getData();

        return articles;
    }


    /**
     * Article反序列化规则
     */
    public static class ArticleDeserializer implements JsonDeserializer<Article> {


        @Override
        public Article deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            final JsonObject jsonObject = json.getAsJsonObject();

            final Article article = new Article();

            String title = jsonObject.get("title").getAsString();
            String link = jsonObject.get("link").getAsString();
            String author = "";
            String pubDate = "";
            if(!title.equals("AD")){
                // not advertisement
                author = jsonObject.get("author").getAsString();
                pubDate = jsonObject.get("pubDate").getAsString();
            }
            String description = jsonObject.get("description").getAsString();
            description = description.trim();
            String imgurl = "";
            if(jsonObject.get("imgurl") != null){
                imgurl = jsonObject.get("imgurl").getAsString();
            }

            // update article information
            article.setTitle(title);
            article.setLink(link);
            article.setAuthor(author);
            article.setPubDate(pubDate);
            article.setDescription(description);
            article.setImgUrl(imgurl);

            return article;
        }
    }


    /**
     * ArtilceWrapper反序列化规则
     */
    public static class ArticleWrapperDeserializer implements JsonDeserializer<ArticleWrapper> {


        @Override
        public ArticleWrapper deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            final JsonObject jsonObject = json.getAsJsonObject();

            final ArticleWrapper wrapper = new ArticleWrapper();

            String msg = jsonObject.get("msg").getAsString();
            int error = jsonObject.get("error").getAsInt();

            wrapper.setMsg(msg);
            wrapper.setError(error);
            if(msg.equals("no data") || error != 0){
                //返回了错误的article信息
                wrapper.setData(new Article[0]);
            }else {
                //正常的信息
                JsonElement jsonElement = jsonObject.get("data");
                Article[] data = context.deserialize(jsonElement, Article[].class);
                wrapper.setData(data);
            }

            return wrapper;
        }
    }

}
