package com.example.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.datamodel.Article;
import com.example.datamodel.DataManager;
import com.example.sneezereader.R;

import java.lang.reflect.Field;
import java.util.List;


/**
 * Created by liuchun on 2015/7/16.
 */
public class YituFragment extends Fragment {
    // rootView
    private View rootView;
    // ViewPager
    private ViewPager mViewPager;
    private MyViewPagerAdapter mAdapter;

    private int curpos;    //当前页面标识
    //数据集
    private List<Article> mDataSet;
    private int position = 0;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // rootView
        if(rootView == null){
            rootView = inflater.inflate(R.layout.yitu_page_layout, container, false);
        }
        //缓存的rootView已经被加载过parent,需要移除
        ViewGroup parent = (ViewGroup)rootView.getParent();
        if(parent != null){
            parent.removeView(rootView);
        }

        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        curpos = getArguments().getInt("pos");
        //初始化界面View
        initView();
    }

    private void initView(){
        // ViewPager
        mViewPager = (ViewPager) rootView.findViewById(R.id.viewpager);

        //根据当前页面的位置,从数据管理器中获取数据
        mDataSet = DataManager.getInstance().getData(curpos);
        //
        FragmentManager fm = getChildFragmentManager();
        mAdapter = new MyViewPagerAdapter(fm, mDataSet);
        //设置适配器
        mViewPager.setAdapter(mAdapter);
        mViewPager.setCurrentItem(position);
    }


    @Override
    public void onPause() {
        //
        position = mViewPager.getCurrentItem();
        super.onPause();
    }

    @Override
    public void onDestroyView() {
        // TODO Auto-generated method stub
        super.onDestroyView();
        try {
            Field childFragmentManager = Fragment.class.getDeclaredField("mChildFragmentManager");
            childFragmentManager.setAccessible(true);
            childFragmentManager.set(this, null);


        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}
