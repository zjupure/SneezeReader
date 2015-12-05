package com.example.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;

import com.example.liuchun.sneezereader.R;


/**
 * Created by liuchun on 2015/7/16.
 */
public class YituFragment extends Fragment {
    private View rootView;
    private ViewPager mPager;
    private WebView mwebView;
    //
    private int curpos;  //当前位置
    private int subitem;  //item的位置
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if(rootView == null){
            rootView = inflater.inflate(R.layout.web_page, container, false);
        }
        //缓存的rootView已经被加载过parent,需要移除
        ViewGroup parent = (ViewGroup)rootView.getParent();
        if(parent != null){
            parent.removeView(rootView);
        }
        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        curpos = getArguments().getInt("pos");

        //说明是图卦和乐活的二级页面
        if(curpos == 0 || curpos == 1){
            subitem = getArguments().getInt("subitem");
        } else{    // curpos = 3
            subitem = -1;
        }

        mPager = (ViewPager)rootView.findViewById(R.id.viewpager);
        mwebView = (WebView)rootView.findViewById(R.id.webview);
    }
}
