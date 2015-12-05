package com.example.fragment;

import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.datamodel.DataManager;
import com.example.datamodel.Datainfo;
import com.example.liuchun.sneezereader.R;

import java.util.List;

/**
 * Created by liuchun on 2015/7/16.
 */
public class ItemFragment extends Fragment {
    private final static int SWIPE_LONG = 2000;
    private final static String[] ENTRY_TAG = {"tugua_entry", "lehuo_entry"};   //图卦二级目录
    private View rootView;  //缓存根View,防止重复渲染
    private SwipeRefreshLayout mRefreshView;  //下拉刷新控件
    private RecyclerView mRecyclerView;   //列表控件
    private MyRecylcerAdapter mAdapter;   //适配器
    private int curpos;    //当前页面标识

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if(rootView == null){
            rootView = inflater.inflate(R.layout.main_entry_page, container, false);
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
        //初始化界面View
        initView();
    }


    public void initView(){
        mRefreshView = (SwipeRefreshLayout)rootView.findViewById(R.id.swipe_refresh);
        mRecyclerView = (RecyclerView)rootView.findViewById(R.id.tugua_list);

        //设置刷新时的颜色
        mRefreshView.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                //刷新操作
                //Toast.makeText(getActivity(), "正在刷新", Toast.LENGTH_SHORT).show();
                //发送网络请求更新数据

                //异步通知
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        //Toast.makeText(getActivity(), "刷新完成", Toast.LENGTH_SHORT).show();
                        //刷新完成

                        mRefreshView.setRefreshing(false);

                    }
                }, SWIPE_LONG);
            }
        });

        mRecyclerView.setHasFixedSize(true);
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());

        //根据当前页面的位置,从数据管理器中获取数据
        List<Datainfo> mDataset = DataManager.getInstance().getData(curpos);
        //定义Adapter
        mAdapter = new MyRecylcerAdapter(mDataset);  //绑定数据集
        mRecyclerView.setAdapter(mAdapter);   //设置适配器

        //设置item点击事件
        mAdapter.setOnClickListener(new ItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                //只有图卦和乐活页面的Item才响应点击事件
                if(curpos != 0 && curpos != 1)
                    return;

                Toast.makeText(getActivity(), "点击item", Toast.LENGTH_SHORT).show();

                //点击Item,替换Fragment
                Fragment frag = new YituFragment();    //新建一个Fragment
                Bundle bundle = new Bundle();
                bundle.putInt("pos", curpos);
                bundle.putInt("subitem", position);
                frag.setArguments(bundle);
                FragmentManager fm = getActivity().getSupportFragmentManager();  //获取父Activity的管理器
                FragmentTransaction ft = fm.beginTransaction();
                ft.replace(R.id.frag_container, frag, ENTRY_TAG[curpos]);
                ft.addToBackStack(null);
                ft.commit();

            }
        });
    }

    /**
     * Item单击接口
     */
    public  interface ItemClickListener {
        public void onItemClick(View view, int position);
    }
}
