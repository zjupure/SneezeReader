package com.simit.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.Toast;

import com.simit.common.Constants;
import com.simit.database.Article;
import com.sina.weibo.sdk.api.TextObject;
import com.sina.weibo.sdk.api.WeiboMultiMessage;
import com.sina.weibo.sdk.api.share.BaseResponse;
import com.sina.weibo.sdk.api.share.IWeiboHandler;
import com.sina.weibo.sdk.api.share.IWeiboShareAPI;
import com.sina.weibo.sdk.api.share.SendMultiMessageToWeiboRequest;
import com.sina.weibo.sdk.api.share.WeiboShareSDK;
import com.sina.weibo.sdk.constant.WBConstants;
import com.tencent.mm.sdk.modelmsg.SendMessageToWX.Req;
import com.tencent.mm.sdk.modelmsg.WXMediaMessage;
import com.tencent.mm.sdk.modelmsg.WXTextObject;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.WXAPIFactory;

/**
 * Created by liuchun on 2016/1/21.
 */
public class ShareActivity extends Activity implements IWeiboHandler.Response{
    private static final String WEIBO_SHARE_ACTION = "com.sina.weibo.sdk.action.ACTION_SDK_REQ_ACTIVITY";
    private static final int TIMELINE_SUPPORTED_VERSION = 0x21020001;
    // weibo share component
    private IWeiboShareAPI mWeiboShareAPI = null;
    // weixin share component
    private IWXAPI mWeixinShareAPI = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 空白View
        View view = new View(this);
        LayoutParams layoutParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        view.setLayoutParams(layoutParams);
        view.setBackgroundColor(0xffffff);
        setContentView(view);
        // 注册微博分享组件
        mWeiboShareAPI = WeiboShareSDK.createWeiboAPI(this, Constants.WEIBO_APP_KEY);
        mWeiboShareAPI.registerApp();
        // 注册微信分享组件
        mWeixinShareAPI = WXAPIFactory.createWXAPI(this, Constants.WEIXIN_APP_KEY, true);
        mWeixinShareAPI.registerApp(Constants.WEIXIN_APP_KEY);

        Intent intent = getIntent();
        String action = intent.getAction();
        if(savedInstanceState != null){
            if(action != null && action.equals(WEIBO_SHARE_ACTION)){
                mWeiboShareAPI.handleWeiboResponse(getIntent(), this);
                return;
            }
        }
        // 被分享操作触发
        Bundle bundle = intent.getBundleExtra("share");
        Article article = bundle.getParcelable("article");
        String from = bundle.getString("from");
        if(from == null){
            finish();
            return;
        }
        // 根据from值判断分享类型
        if(from.equals("weibo")){
            shareToWeibo(article);
        }else if(from.equals("weixin")){
            shareToWeixin(article, Req.WXSceneSession);
        }else if(from.equals("weixinfriend")){
            shareToWeixin(article, Req.WXSceneTimeline);
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        // 被微博唤醒的回调
        String action = intent.getAction();
        if(mWeiboShareAPI != null && action != null && action.equals(WEIBO_SHARE_ACTION)){
            mWeiboShareAPI.handleWeiboResponse(intent, this);
        }
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
        // 分享文章的链接
        TextObject textObject = new TextObject();
        // 文本内容
        if(article.getType() == Article.DUANZI){
            textObject.text = "[段子]" + article.getDescription();
        }else{
            textObject.text = article.getTitle() + article.getDescription();
        }
        //
        WeiboMultiMessage message = new WeiboMultiMessage();
        message.textObject = textObject;
        // 初始化Request
        SendMultiMessageToWeiboRequest request = new SendMultiMessageToWeiboRequest();
        request.transaction = String.valueOf(System.currentTimeMillis());
        request.multiMessage = message;
        // 发起分享Request
        mWeiboShareAPI.sendRequest(this, request);
    }

    /**
     * 分享到微信好友及朋友圈
     * @param article
     * @param scene
     */
    public void shareToWeixin(Article article, int scene){
        // check weixin version, 版本过低不支持分享到朋友圈
        if(scene == Req.WXSceneTimeline && mWeixinShareAPI.getWXAppSupportAPI() < TIMELINE_SUPPORTED_VERSION){
            Toast.makeText(this, "当前微信版本过低,不支持分享到朋友圈", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        // 分享文章的链接
        WXTextObject textObject = new WXTextObject();
        if(article.getType() == Article.DUANZI){
            textObject.text = "[段子]" + article.getDescription();
        }else{
            textObject.text = article.getTitle() + article.getDescription();
        }
        // 分享消息
        WXMediaMessage message = new WXMediaMessage();
        message.mediaObject = textObject;
        message.description = article.getTitle();
        // 构造Requst请求对象
        Req req = new Req();
        req.transaction = String.valueOf(System.currentTimeMillis());
        req.message = message;
        req.scene = scene;
        // 调用接口发送数据到微信
        mWeixinShareAPI.sendReq(req);
        finish();  // 销毁自身
    }
}
