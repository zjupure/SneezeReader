package com.simit.jsonparser;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;

/**
 * Created by liuchun on 2015/12/6.
 */
public class ArticleDataDeserializer implements JsonDeserializer<ArticleData> {
    @Override
    public ArticleData deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        final JsonObject jsonObject = json.getAsJsonObject();

        final ArticleData article = new ArticleData();

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
        article.setImgurl(imgurl);

        return article;
    }
}
