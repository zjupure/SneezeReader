package com.simit.fragment;


import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.simit.datamodel.Article;
import com.simit.sneezereader.R;
import com.simit.sneezereader.ShareActivity;

/**
 * Created by liuchun on 2016/1/22.
 */
public class ShareDialogFragment extends DialogFragment implements View.OnClickListener{
    // share component
    private ImageView mWeiboPhoto;
    private ImageView mWeixinPhoto;
    private ImageView mWeixinFriendPhoto;
    private TextView mShareCancel;
    //
    private Article article;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        //
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View rootView = inflater.inflate(R.layout.share_dialog, null);
        // 初始化View
        mWeiboPhoto = (ImageView) rootView.findViewById(R.id.share_weibo);
        mWeixinPhoto = (ImageView) rootView.findViewById(R.id.share_weixin);
        mWeixinFriendPhoto = (ImageView)rootView.findViewById(R.id.share_weixin_friend);
        mShareCancel = (TextView) rootView.findViewById(R.id.share_cancel);
        //
        mWeiboPhoto.setOnClickListener(this);
        mWeixinPhoto.setOnClickListener(this);
        mWeixinFriendPhoto.setOnClickListener(this);
        mShareCancel.setOnClickListener(this);
        //
        builder.setView(rootView);
        AlertDialog dialog = builder.create();
        //
        //dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        Window window = dialog.getWindow();
        WindowManager.LayoutParams lp = window.getAttributes();
        lp.gravity = Gravity.BOTTOM;
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        window.setAttributes(lp);
        window.setWindowAnimations(R.style.popwindow_anim_style);

        return dialog;
    }

    /**
     * 创建实例
     * @param article
     * @return
     */
    public static ShareDialogFragment newInstance(Article article){
        ShareDialogFragment fragment = new ShareDialogFragment();
        Bundle bundle = new Bundle();
        bundle.putParcelable("article", article);
        fragment.setArguments(bundle);

        return fragment;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        //获取参数
        Bundle bundle = getArguments();
        article = bundle.getParcelable("article");
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.share_weibo:
                startShareActivity(article, "weibo");
                break;
            case R.id.share_weixin:
                startShareActivity(article, "weixin");
                break;
            case R.id.share_weixin_friend:
                startShareActivity(article, "weixinfriend");
                break;
            case R.id.share_cancel:
            case R.id.share_mask:
            default:break;
        }
        getDialog().dismiss();
    }

    /**
     * 启动分享Activity
     * @param article
     * @param from
     */
    public void startShareActivity(Article article, String from){
        Intent intent = new Intent(getActivity(), ShareActivity.class);
        Bundle bundle = new Bundle();
        bundle.putParcelable("article", article);
        bundle.putString("from", from);
        intent.putExtra("share", bundle);
        // 启动分享Activity
        startActivity(intent);
    }
}
