package com.example.fragment;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
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

import com.example.datamodel.Article;
import com.example.datamodel.DataManager;
import com.example.liuchun.sneezereader.R;
import com.example.sneezereader.DetailActivity;

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
    private LinearLayoutManager mLayoutManager;
    private MyRecylcerAdapter mAdapter;   //适配器
    private int curpos;    //当前页面标识
    // RecycleView数据集
    private List<Article> mDataSet;

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
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        curpos = getArguments().getInt("pos");
        //初始化界面View
        initView();
    }


    public void initView(){
        mRefreshView = (SwipeRefreshLayout)rootView.findViewById(R.id.swipe_refresh);
        mRecyclerView = (RecyclerView)rootView.findViewById(R.id.tugua_list);

        //设置刷新时的颜色
        mRefreshView.setColorSchemeColors(Color.RED, Color.GREEN, Color.BLUE, Color.YELLOW);
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
        mLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());

        //根据当前页面的位置,从数据管理器中获取数据
        mDataSet = DataManager.getInstance().getData(curpos);
        //定义Adapter
        mAdapter = new MyRecylcerAdapter(mDataSet, curpos);  //绑定数据集
        mRecyclerView.setAdapter(mAdapter);   //设置适配器

        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            private int lastVisibleItem = 0;

            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);

                if (newState == RecyclerView.SCROLL_STATE_IDLE
                        && lastVisibleItem + 1 == mAdapter.getItemCount()) {
                    mRefreshView.setRefreshing(true);
                    //发起请求
                    //handler.sendEmptyMessageDelayed();
                }
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                lastVisibleItem = mLayoutManager.findLastVisibleItemPosition();
            }
        });

        //设置item点击事件
        mAdapter.setOnItemClickListener(new ItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                // 图卦和乐活页面响应点击事件,段子页面无效
                if (curpos > 1) {
                    return;
                }

                Intent intent = new Intent(getActivity(), DetailActivity.class);
                Bundle bundle = new Bundle();
                Article article = mDataSet.get(position);
                bundle.putString("title", article.getTitle());
                bundle.putString("remote_link", article.getRemote_link());
                bundle.putString("description", article.getDescription());
                bundle.putString("local_link", article.getLocal_link());
                intent.putExtra("detail", bundle);
                startActivity(intent);
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
