package com.simit.activity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.Toast;

import com.facebook.common.references.CloseableReference;
import com.facebook.datasource.DataSource;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.imagepipeline.core.ImagePipeline;
import com.facebook.imagepipeline.image.CloseableBitmap;
import com.facebook.imagepipeline.image.CloseableImage;
import com.facebook.imagepipeline.request.ImageRequest;
import com.facebook.imagepipeline.request.ImageRequestBuilder;
import com.simit.common.Constants;
import com.simit.database.Article;
import com.sina.weibo.sdk.api.ImageObject;
import com.sina.weibo.sdk.api.TextObject;
import com.sina.weibo.sdk.api.WebpageObject;
import com.sina.weibo.sdk.api.WeiboMessage;
import com.sina.weibo.sdk.api.WeiboMultiMessage;
import com.sina.weibo.sdk.api.share.BaseResponse;
import com.sina.weibo.sdk.api.share.IWeiboHandler;
import com.sina.weibo.sdk.api.share.IWeiboShareAPI;
import com.sina.weibo.sdk.api.share.SendMessageToWeiboRequest;
import com.sina.weibo.sdk.api.share.SendMultiMessageToWeiboRequest;
import com.sina.weibo.sdk.api.share.WeiboShareSDK;
import com.sina.weibo.sdk.constant.WBConstants;
import com.sina.weibo.sdk.utils.Utility;
import com.tencent.mm.sdk.modelmsg.SendMessageToWX.Req;
import com.tencent.mm.sdk.modelmsg.WXMediaMessage;
import com.tencent.mm.sdk.modelmsg.WXTextObject;
import com.tencent.mm.sdk.modelmsg.WXWebpageObject;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.WXAPIFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Created by liuchun on 2016/1/21.
 */
public class ShareActivity extends Activity implements IWeiboHandler.Response{
    private static final String TAG = "ShareActivity";

    private static final String WEIBO_SHARE_ACTION = "com.sina.weibo.sdk.action.ACTION_SDK_REQ_ACTIVITY";
    private static final int TIMELINE_SUPPORTED_VERSION = 0x21020001;
    // weibo share component
    private IWeiboShareAPI mWeiboShareAPI = null;
    // weixin share component
    private IWXAPI mWeixinShareAPI = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 空白1像素View
        View view = new View(this);
        LayoutParams layoutParams = new LayoutParams(1, 1);
        view.setLayoutParams(layoutParams);
        view.setBackgroundColor(0x00ffffff);  //透明activity
        setContentView(view);

        // 注册微博分享组件
        mWeiboShareAPI = WeiboShareSDK.createWeiboAPI(this, Constants.WEIBO_APP_KEY);
        mWeiboShareAPI.registerApp();

        // 注册微信分享组件
        mWeixinShareAPI = WXAPIFactory.createWXAPI(this, Constants.WEIXIN_APP_KEY, true);
        mWeixinShareAPI.registerApp(Constants.WEIXIN_APP_KEY);

        //优先处理微博的回调
        Intent intent = getIntent();
        String action = intent.getAction();
        if(action != null && action.equals(WEIBO_SHARE_ACTION)){
            Log.i(TAG, "WeiboShare handle response");
            mWeiboShareAPI.handleWeiboResponse(getIntent(), this);
            return;
        }

        // 被分享操作触发
        Bundle bundle = intent.getBundleExtra("share");
        Article article = bundle.getParcelable("article");
        String from = bundle.getString("from");
        if(from == null){
            finish();
            return;
        }

        Log.i(TAG, "from=" + from);
        // 根据from值判断分享类型
        if(from.equals("weibo")){
            shareToWeibo(article);
        }else if(from.equals("weixin")){
            shareToWeixin(article, Req.WXSceneSession);
        }else if(from.equals("weixinfriend")){
            shareToWeixin(article, Req.WXSceneTimeline);
        }
        finish();  //销毁自身
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        // 被微博唤醒的回调
        setIntent(intent);
        mWeiboShareAPI.handleWeiboResponse(getIntent(), this);
    }

    @Override
    public void onResponse(BaseResponse baseResponse) {
        //接收微博分享后的返回数据
        switch (baseResponse.errCode){
            case WBConstants.ErrorCode.ERR_OK:
                Toast.makeText(this, "分享成功", Toast.LENGTH_SHORT).show();
                break;
            case WBConstants.ErrorCode.ERR_CANCEL:
                Toast.makeText(this, "取消分享", Toast.LENGTH_SHORT).show();
                break;
            case WBConstants.ErrorCode.ERR_FAIL:
                Toast.makeText(this, "分享失败", Toast.LENGTH_SHORT).show();
                break;
            default:break;
        }
        finish();
    }

    /**
     * 分享到微博
     * @param article
     */
    public void shareToWeibo(Article article) {

        Log.i(TAG, "ShareToWeibo>>" + article.getDescription());

        if(mWeiboShareAPI.isWeiboAppSupportAPI()){
            int supportApi = mWeiboShareAPI.getWeiboAppSupportAPI();
            if(supportApi >= 10351){
                sendMultiWeiboMessage(article);
            }else {
                sendSingleWeiboMessage(article);
            }
        }else {
            Toast.makeText(this, "当前微博版本太低，不支持分享到微博", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    /**
     * 分享多种消息到微博
     * @param article
     */
    private void sendMultiWeiboMessage(Article article){

        Log.i(TAG, "sendMultiWeiboMessage>>" + article.getDescription());
        // 分享文本
        TextObject textObject = new TextObject();
        // 分享图片
        ImageObject imageObject = new ImageObject();
        // 分享网页链接
        WebpageObject webObject = new WebpageObject();
        // 分享消息
        WeiboMultiMessage message = new WeiboMultiMessage();
        // 文本内容
        if(article.getType() == Article.DUANZI){

            String desp = extractTextFromHtml(article.getDescription());

            textObject.text = "[段子]" + desp +  article.getLink();

            message.textObject = textObject;
        }else{

            textObject.text = article.getTitle() + article.getDescription();

            webObject.identify = Utility.generateGUID();
            webObject.title = article.getTitle();
            webObject.description = article.getTitle();

            webObject.defaultText = article.getTitle();

            message.textObject = textObject;

            if(!TextUtils.isEmpty(article.getImgUrl())){
                //图片地址非空
                Bitmap bm = getBitmapFromCache(article.getImgUrl());
                if(bm != null){
                    imageObject.setImageObject(bm);
                    message.imageObject = imageObject;

                    webObject.setThumbImage(bm);
                    webObject.actionUrl = article.getDescription();

                    //message.textObject = null;
                    //message.mediaObject = webObject;
                }
            }
        }

        // 分享请求
        SendMultiMessageToWeiboRequest request = new SendMultiMessageToWeiboRequest();
        request.transaction = String.valueOf(System.currentTimeMillis());
        request.multiMessage = message;

        // 发起请求
        mWeiboShareAPI.sendRequest(this, request);
    }

    /**
     * 分享单种消息
     * @param article
     */
    private void sendSingleWeiboMessage(Article article){

        Log.i(TAG, "sendSingleWeiboMessage>>" + article.getDescription());
        //分享文本
        TextObject textObject = new TextObject();
        // 分享图片
        ImageObject imageObject = new ImageObject();
        //分享网页
        WebpageObject webObject = new WebpageObject();
        //分享消息
        WeiboMessage message = new WeiboMessage();

        if(article.getType() == Article.DUANZI){

            String desp = extractTextFromHtml(article.getDescription());

            textObject.text = "[段子]" + desp + article.getLink();

            message.mediaObject = textObject;
        }else {

            textObject.text = article.getTitle() + article.getDescription();

            webObject.identify = Utility.generateGUID();
            webObject.title = article.getTitle();
            webObject.description = article.getTitle();

            webObject.defaultText = article.getTitle();
            // default
            message.mediaObject = textObject;

            if(!TextUtils.isEmpty(article.getImgUrl())){
                //图片地址非空
                Bitmap bm = getBitmapFromCache(article.getImgUrl());

                if(bm != null) {

                    imageObject.setImageObject(bm);

                    webObject.setThumbImage(bm);
                    bm.recycle();
                    webObject.actionUrl = article.getDescription();

                    message.mediaObject = webObject;
                }
            }
        }

        //分享请求
        SendMessageToWeiboRequest request = new SendMessageToWeiboRequest();
        request.transaction = String.valueOf(System.currentTimeMillis());
        request.message = message;

        //发送请求
        mWeiboShareAPI.sendRequest(this, request);
    }

    /**
     * 分享到微信好友及朋友圈
     * @param article
     * @param scene
     */
    public void shareToWeixin(Article article, int scene){

        if(!mWeixinShareAPI.isWXAppInstalled()){
            Toast.makeText(this, "微信未安装", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        // check weixin version, 版本过低不支持分享到朋友圈
        if(scene == Req.WXSceneTimeline && mWeixinShareAPI.getWXAppSupportAPI() < TIMELINE_SUPPORTED_VERSION){
            Toast.makeText(this, "当前微信版本过低,不支持分享到朋友圈", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        Log.i(TAG, "shareToWeixin>>>" + article.getDescription());
        // 分享文本
        WXTextObject textObject = new WXTextObject();
        // 分享网页链接
        WXWebpageObject webObject = new WXWebpageObject();
        // 分享消息
        WXMediaMessage message = new WXMediaMessage();

        message.title = article.getTitle();
        if(article.getType() == Article.DUANZI){
            //段子，解析html
            String desp = extractTextFromHtml(article.getDescription());

            textObject.text = "[段子]" + desp;

            message.mediaObject = textObject;
            message.description = desp;
        }else{
            //
            webObject.webpageUrl = article.getDescription();

            message.mediaObject = webObject;
            message.description = article.getTitle();

            if(!TextUtils.isEmpty(article.getImgUrl())){

                Bitmap bm = getBitmapFromCache(article.getImgUrl());
                if(bm != null) {
                    Bitmap thumb = Bitmap.createScaledBitmap(bm, 100, 100, true);
                    bm.recycle();
                    message.thumbData = bmpToByteArray(thumb);
                    thumb.recycle();
                }
            }
        }

        // 构造Requst请求对象
        Req req = new Req();
        req.transaction = String.valueOf(System.currentTimeMillis());
        req.message = message;
        req.scene = scene;
        // 调用接口发送数据到微信
        mWeixinShareAPI.sendReq(req);
    }

    /**
     * 从Fresco的缓存中获取Bitmap
     * @return
     */
    private Bitmap getBitmapFromCache(String imgUrl){

        Bitmap bm = null;

        ImagePipeline imagePipeline = Fresco.getImagePipeline();
        ImageRequest request = ImageRequestBuilder
                .newBuilderWithSource(Uri.parse(imgUrl))
                .build();


        DataSource<CloseableReference<CloseableImage>> dataSource =
                imagePipeline.fetchImageFromBitmapCache(request, this);
        try{
            CloseableReference<CloseableImage> imageReference = dataSource.getResult();
            if(imageReference != null){
                try{
                    // do something
                    CloseableImage image = imageReference.get();
                    if(image instanceof CloseableBitmap){

                        Bitmap bitmap = ((CloseableBitmap)image).getUnderlyingBitmap();

                        Log.i(TAG, "getBitmapFromCache>>> get cached bitmap success");
                        bm = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight());

                        return bm;
                    }
                }finally {
                    CloseableReference.closeSafely(imageReference);
                }
            }else {
                // cache miss
                return null;
            }
        }finally {
            dataSource.close();
        }

        return null;
    }

    /**
     * Bitmap转byte[]
     * @param bitmap
     */
    private byte[] bmpToByteArray(Bitmap bitmap){

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, bos);

        byte[] result = bos.toByteArray();
        try{
            bos.close();
        }catch (IOException e){
            e.printStackTrace();
        }

        return result;
    }

    /**
     * 从html中提取纯文本
     * @param html
     * @return
     */
    private String extractTextFromHtml(String html){

        String text = html.replaceAll("<.+?>", "");
        text = text.replace("<", "");
        text = text.replace(">", "");
        text = text.replace("&nbsp;", " ");
        text = text.replace("\n", "");
        text = text.replace("\t", "");

        return  text;
    }
}
