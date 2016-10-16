package com.simit.activity.wxapi;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.Toast;

import com.simit.common.Constants;
import com.tencent.mm.sdk.modelbase.BaseReq;
import com.tencent.mm.sdk.modelbase.BaseResp;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.IWXAPIEventHandler;
import com.tencent.mm.sdk.openapi.WXAPIFactory;

public class WXEntryActivity extends Activity implements IWXAPIEventHandler {
	// weixin share component
	private IWXAPI mWeixinShareAPI = null;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// 空白View
        View view = new View(this);
        LayoutParams layoutParams = new LayoutParams(1, 1);
        view.setLayoutParams(layoutParams);
        view.setBackgroundColor(0x00ffffff);  //透明activity
		setContentView(view);
		// 注册微信分享组件
		mWeixinShareAPI = WXAPIFactory.createWXAPI(this, Constants.WEIXIN_APP_KEY, true);
		mWeixinShareAPI.registerApp(Constants.WEIXIN_APP_KEY);
		// 处理回调响应
		mWeixinShareAPI.handleIntent(getIntent(), this);
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		//
		setIntent(intent);
		mWeixinShareAPI.handleIntent(intent, this);
	}

	// 微信主动发送请求到第三方的回调
	@Override
	public void onReq(BaseReq baseReq) {

	}

	// 第三方应用发送到微信的请求处理后的响应结果,回调该方法
	@Override
	public void onResp(BaseResp baseResp) {
		switch (baseResp.errCode){
			case BaseResp.ErrCode.ERR_OK:
				Toast.makeText(this, "分享成功", Toast.LENGTH_SHORT).show();
				break;
			case BaseResp.ErrCode.ERR_USER_CANCEL:
				Toast.makeText(this, "取消分享", Toast.LENGTH_SHORT).show();
				break;
			case BaseResp.ErrCode.ERR_AUTH_DENIED:
				Toast.makeText(this, "授权被拒绝", Toast.LENGTH_SHORT).show();
				break;
			case BaseResp.ErrCode.ERR_SENT_FAILED:
				Toast.makeText(this, "分享失败", Toast.LENGTH_SHORT).show();
				break;
			case BaseResp.ErrCode.ERR_UNSUPPORT:
				Toast.makeText(this, "当前微信版本不支持", Toast.LENGTH_SHORT).show();
				break;
			case BaseResp.ErrCode.ERR_COMM:
				Toast.makeText(this, "分享失败", Toast.LENGTH_SHORT).show();
				break;
			default:break;
		}
        finish(); // 销毁自身
	}
}