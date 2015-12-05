package com.example.network;

import android.os.Environment;
import android.util.Log;

import com.example.storage.FileManager;
import com.example.datamodel.Datainfo;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.util.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by liuchun on 2015/7/17.
 */
public class SneezeRules {
    private static final String TAG = "SneezeRules";
    //网站入口地址信息
    public static final String BASE_URL = "http://www.dapenti.com/blog/";  //喷嚏网基地址
    public static final String TUGUA_ENTRY = BASE_URL + "blog.asp?subjectid=70&name=xilei";  //图挂入口地址
    public static final String LEHUO_ENTRY = BASE_URL + "blog.asp?name=agile";  //乐活入口地址
    public static final String YITU_ENTRY = BASE_URL + "blog.asp?name=tupian";  //意图入口地址
    public static final String DUANZI_ENTRY = BASE_URL + "blog.asp?subjectid=137&name=xilei"; //段子入口地址
    public static final String[] PAGE_ENTRY ={TUGUA_ENTRY, LEHUO_ENTRY, YITU_ENTRY, DUANZI_ENTRY};
    //HTML页头和页尾
    public static final String PAGE_HEAD = "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">\n" +
            "<html xmlns=\"http://www.w3.org/1999/xhtml\" ><meta http-equiv='content-type' content='text/html; charset=gb2312' />\n" +
            "<meta http-equiv='expires' content='0' />\n" +
            "<meta name='resource-type' content='document' />\n" +
            "<meta name='distribution' content='global' />\n" +
            "<meta name='author' content='dapenti' />\n" +
            "<link rel='icon' href='http://www.dapenti.com/blog/dapentitu.ico' />\n" +
            "<link rel='shortcut icon' href='http://www.dapenti.com/blog/dapentitu.ico' />\n" +
            "<meta name='copyright' content='Copyright (c) 2008 dapenti. All Rights Reserved.' />\n" +
            "<meta name='robots' content='index, follow' />\n" +
            "<meta name='revisit-after' content='1 days' />\n" +
            "<meta name='rating' content='general' />\n" +
            "<meta name='keywords' content='图卦;喷嚏图卦;段子;铂程斋;喷嚏网;'>\n" +
            "<meta name='description' content='dapenti.com-喷嚏网：阅读、发现和分享：8小时外的健康生活！您的网络博客文摘，原创社会人文书评，每天只需5分钟的精神享受。来这儿打喷嚏！' />\n" +
            "<link rel='alternate' href='http://www.dapenti.com/blog/rss2.asp?name=xilei' type='application/rss+xml' title='喷嚏网-铂程斋' >\n" +
            "<body>\n";
    public static final String PAGE_TAIL = "\n</body>\n</html>";
    //网站编码方式
    public static final String WEB_CHARSET = "GBK";
    //页面内容提取规则
    public static final String TUGUA_ENTRY_RULE = "td.oblog_t_2 ul a[href]";  // <td class="oblog_t_2"><ul> <a>标签
    public static final String TUGUA_TITLE_RULE = "span.style1";   //<td class="oblog_t_4"><span class="style1"
    public static final String TUGUA_AUTHOR_RULE = "div[align=right] > span.oblog_text";  //<div align="right"><span class="oblog_text">
    public static final String TUGUA_CONTENT_RULE = "span.oblog_text > div.oblog_text";   //<span class="oblog_text"><div class="oblog_text" align="left"></div>

    public static final String LEHUO_ENTRY_RULE = "tbody div[align=left] ul a[href]"; // <div align="left"><ul><a>
    public static final String LEHUO_TITLE_RULE = "span.style1";  //<span class="style1">
    public static final String LEHUO_AUTHOR_RULE = "div[align=right] > span";  //<div align="right"><span>
    public static final String LEHUO_CONTENT_RULE = "td > div[align=left]";  // <td><div align="left">

    public static final String YITU_ENTRY_RULE = "td.oblog_text div[align=left] > a[href]";  //<div align="left"><a>
    public static final String YITU_TITLE_RULE = "span.style1"; //<td class="oblog_t_4"><span class="style1">
    public static final String YITU_AUTHOR_RULE = "div[align=right] > span.oblog_text"; //<div align="right"><span class="oblog_text">
    public static final String YITU_CONTENT_RULE = "span.oblog_text > div[align=left]"; //<span class="oblog_text"><div align="left">

    public static final String DUANZI_ENTRY_RULE = "td.oblog_t_2 ul a[href]";  //<td class="oblog_t_2"><ul><a>
    public static final String DUANZI_TITLE_RULE = "span.style1"; //<td class"oblog_t_4"><span class="style1">
    public static final String DUANZI_AUTHOR_RULE = "div[align=right] > span.oblog_text"; //<div align="right"><span class="oblog_text">
    public static final String DUANZI_CONTENT_RULE = "span.oblog_text > div.oblog_text";  //<span class="oblog_text"><div class="oblog_text">
    //汇总
    public static final String[] ENTRY_RULE = {TUGUA_ENTRY_RULE, LEHUO_ENTRY_RULE, YITU_ENTRY_RULE, DUANZI_ENTRY_RULE};
    public static final String[] TITLE_RULE = {TUGUA_TITLE_RULE, LEHUO_TITLE_RULE, YITU_TITLE_RULE, DUANZI_TITLE_RULE};
    public static final String[] AUTHOR_RULE = {TUGUA_AUTHOR_RULE, LEHUO_AUTHOR_RULE, YITU_AUTHOR_RULE, DUANZI_AUTHOR_RULE};
    public static final String[] CONTENT_RULE = {TUGUA_CONTENT_RULE, LEHUO_CONTENT_RULE, YITU_CONTENT_RULE, DUANZI_CONTENT_RULE};
    //文件存储
    public static final String PAGE_DIR = "SneezeReader";
    public static final String[] FILE_NAME_PRE = {"tugua", "lehuo", "yitu", "duanzi"};
    //网页链接的正则表达式
    //example: http://www.dapenti.com/blog/more.asp?name=xilei&id=101667
    public static final String REFURL_MODE = "^more\\.asp\\?name=[a-z]{3,10}&id=\\d{6}$";

    /**
     * 解析入口页面的超链接
     * @param page  页面Html源码
     * @param type  页面分类
     * @return
     */
    public static List<String> getRemoteUrl(String page, int type){
        List<String> urlList = new ArrayList<String>();

        Document doc = Jsoup.parse(page);
        Elements links = doc.select(ENTRY_RULE[type]);
        for(Element link : links){
            String refurl = link.attr("href");   //获取相对链接
            //使用正则表达式进一步过滤其中某些非法地址
            Pattern pattern = Pattern.compile(REFURL_MODE);
            Matcher matcher = pattern.matcher(refurl);
            if(matcher.matches()){
                //匹配的链接加入集合
                urlList.add(BASE_URL + refurl);

                //Log.d(TAG, BASE_URL + refurl + " " + type);
            }
        }

        return urlList;
    }

    /**
     * 从内容页面提出标题,作者,时间,文章内容等信息
     * @param page   内容页面的Html源码
     * @param type   页面分类
     * @return
     */
    public static Datainfo getData(String page, int type){
        Datainfo datainfo = new Datainfo();
        Document doc = Jsoup.parse(page);

        Element titleLable = doc.select(TITLE_RULE[type]).first();
        Element authorLable = doc.select(AUTHOR_RULE[type]).first();
        Element contentLable = doc.select(CONTENT_RULE[type]).first();

        //Log.d(TAG, "page type: " + type);

        String title = titleLable.text();
        String authorinfo = authorLable.text();

        Log.d(TAG, "title: " + title + "\n");
        //从authorinfo中解析出作者和时间,  example: "xilei 发布于 2015-7-17 13:59:00"
        String[] info = authorinfo.split(" ");  //用空格拆分
        String author = info[0];   // 第一个是作者

        //Log.d(TAG, "author: " + author + "\n");
        //处理日期
        String strdate = info[info.length - 2];   //倒数第二个是日期,最后一个是时间
        //2015-7-17转换成标准格式2015-07-17
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");  //yyyy-MM-dd 年-月-日
        Date date = null;   //转换成Date
        try {
            date = sdf.parse(strdate);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        //Log.d(TAG, "date: " + strdate + "\n");
        //提取主要内容
        String content = contentLable.html();  //获取html内容
        content = "<div><font size=\"3\">" + titleLable.html() + "</font></div>" +
                  "<p><br></br><br></br><br></br></p>" +   //填加一些换行,让WebView效果突出
                  "<div>" + content + "</div>";
        content = PAGE_HEAD + content + PAGE_TAIL;
        //以日期和类型命名html文件
        String filename = PAGE_DIR + File.separator + FILE_NAME_PRE[type] + "_" + strdate + ".html";
        FileManager.getInstance().writeHTML(filename, content);
        String filepath = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + filename;

        //特殊处理最后一页的Content
        String description = "";
        if(type ==2 || type == 3){
            //段子栏目
            description = contentLable.text();   //获取文本内容

            //Log.d(TAG, "duanzi: " + description + "\n");
        }

        //开始写入datainfo
        datainfo.setTitle(title);
        datainfo.setType(type);
        datainfo.setAuthor(author);
        datainfo.setPublishdate(date);
        datainfo.setContent(description);
        datainfo.setLocal_url(filepath);

        return datainfo;
    }

}
