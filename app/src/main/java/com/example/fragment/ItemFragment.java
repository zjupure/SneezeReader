package com.example.fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.database.DBManager;
import com.example.fragment.MyRecylcerAdapter.OnItemClickListener;
import com.example.datamodel.Article;
import com.example.datamodel.DataManager;
import com.example.network.SneezeClient;
import com.example.network.SneezeJsonResponseHandler;
import com.example.sneezereader.DetailActivity;
import com.example.sneezereader.R;
import com.handmark.pulltorefresh.library.ILoadingLayout;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshRecyclerView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Created by liuchun on 2015/7/16.
 */
public class ItemFragment extends Fragment {
    public static final int NEW_ARTICLE_ARRIVAL = 1;
    public static final int NO_NEW_ARTICLE = 2;
    public static final int LOAD_MORE_ARTICLE = 3;
    public static final int NO_MORE_ARTICLE = 4;
    public static final int NETWORK_ERROR = 5;
    public static final String DATASET_UPDATED_ACTION = "com.example.fragment";
    private static final String TIME_FORMAT_REFRESH = "上次更新: yyyy年MM月dd日 HH:mm";
    private static final String TIME_FORMAT_LOAD = "上次加载: yyyy年MM月dd日 HH:mm";
    // rootView
    private View rootView;  //缓存根View,防止重复渲染
    // compents
    private PullToRefreshRecyclerView mRefreshView;  // RecyclerView wrapper
    private ILoadingLayout header, footer;
    private RecyclerView mRecyclerView;  // 列表
    private LinearLayoutManager mLayoutManager;
    private MyRecylcerAdapter mAdapter;   //适配器
    private int curpos;    //当前页面标识
    // RecycleView数据集
    private List<Article> mDataSet;
    private int limit = 30;
    private String lastUpdated = "";
    //
    private SneezeClient client;
    private LocalBroadcastManager broadcastManager;
    private BroadcastReceiver receiver;

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
        client = SneezeClient.getInstance(getActivity());
        client.setUpdated(true);
        lastUpdated = restoreLastUpdated();
        //初始化界面View
        initRecyclerView();
        // 注册广播接收器
        broadcastManager = LocalBroadcastManager.getInstance(getActivity());
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(DATASET_UPDATED_ACTION);
        //
       receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                // update the ui
                mAdapter.notifyDataSetChanged();
            }
        };
        broadcastManager.registerReceiver(receiver, intentFilter);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        broadcastManager.unregisterReceiver(receiver);
    }

    public void initRecyclerView(){

        mRefreshView = (PullToRefreshRecyclerView) rootView.findViewById(R.id.tugua_list);
        mRecyclerView = mRefreshView.getRefreshableView();
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        //根据当前页面的位置,从数据管理器中获取数据
        mDataSet = DataManager.getInstance().getData(curpos);
        //定义Adapter
        mAdapter = new MyRecylcerAdapter(mDataSet, curpos);  //绑定数据集
        mRecyclerView.setAdapter(mAdapter);   //设置适配器

        //设置item点击事件
        mAdapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                // 图卦和乐活页面响应点击事件,段子页面无效
                if (curpos > 1) {
                    return;
                }

                Intent intent = new Intent(getActivity(), DetailActivity.class);
                Bundle bundle = new Bundle();
                Article article = mDataSet.get(position);
                bundle.putParcelable("article", article);
                intent.putExtra("detail", bundle);
                intent.putExtra("position", position);
                startActivity(intent);
            }
        });

        // 基本配置
        header = mRefreshView.getLoadingLayoutProxy(true, false);
        header.setPullLabel(getString(R.string.pull_to_refresh_pull_label));
        header.setReleaseLabel(getString(R.string.pull_to_refresh_release_label));
        header.setRefreshingLabel(getString(R.string.pull_to_refresh_refreshing_label));
        header.setLastUpdatedLabel(lastUpdated);
        //header.setLastUpdatedLabel(getString(R.string.pull_to_refresh_last_update));
        footer = mRefreshView.getLoadingLayoutProxy(false, true);
        footer.setPullLabel(getString(R.string.pull_to_load_pull_label));
        footer.setReleaseLabel(getString(R.string.pull_to_load_release_label));
        footer.setRefreshingLabel(getString(R.string.pull_to_load_refreshing_label));
        //footer.setLastUpdatedLabel(getString(R.string.pull_to_refresh_last_update));
        // 设置刷新
        mRefreshView.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener<RecyclerView>() {
            @Override
            public void onRefresh(PullToRefreshBase<RecyclerView> refreshView) {

                if (mRefreshView.isHeaderShown()) {
                    // 执行网络请求
                    client.getArticle(curpos, new SneezeJsonResponseHandler(getActivity(),
                            curpos, handler));

                    SimpleDateFormat sdf = new SimpleDateFormat(TIME_FORMAT_REFRESH);
                    String last_refresh_time = sdf.format(new Date());
                    header.setLastUpdatedLabel(last_refresh_time);
                    saveLastUpdated(last_refresh_time);
                } else if (mRefreshView.isFooterShown()) {
                    // 从数据库加载数据
                    loadFromDatabase(limit + 10);

                    //SimpleDateFormat sdf = new SimpleDateFormat(TIME_FORMAT_LOAD);
                    //String last_load_time = sdf.format(new Date());
                    //footer.setLastUpdatedLabel(last_load_time);
                } else {
                    mRefreshView.onRefreshComplete();
                }
            }
        });
    }

    // 处理网络请求回应发过来的消息
    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case NEW_ARTICLE_ARRIVAL:
                    int nums = msg.arg1;
                    loadFromDatabase(limit + nums);
                    break;
                case NO_NEW_ARTICLE:
                    break;
                case NETWORK_ERROR:
                    break;
                case LOAD_MORE_ARTICLE:
                    limit = msg.arg1;
                    mAdapter.notifyDataSetChanged();
                    break;
                case NO_MORE_ARTICLE:
                    mAdapter.notifyDataSetChanged();
                    break;
                default:break;
            }
            mRefreshView.onRefreshComplete();
        }
    };

    private void loadFromDatabase(int limit){
        Thread loadTask = new LoadMoreTask(limit);
        loadTask.start();
    }

    private void saveLastUpdated(String lastUpdated){
        SharedPreferences preferences = getActivity().getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();

        editor.putString("lastUpdated", lastUpdated);
        editor.commit();
    }

    private String restoreLastUpdated(){
        SharedPreferences preferences = getActivity().getPreferences(Context.MODE_PRIVATE);

        String lastUpdated = preferences.getString("lastUpdated",
                getString(R.string.pull_to_refresh_last_update));

        return lastUpdated;
    }
    /**
     * 数据库加载任务
     */
    private class LoadMoreTask extends Thread{
        private int num = 0;

        public LoadMoreTask(int limit){
            num = limit;
        }
        @Override
        public void run() {
            // 从数据库查询最新的数据
            List<Article> articles = DBManager.getInstance(getActivity()).getData(curpos, num);

            if(articles.size() > mDataSet.size()){
                // 查询到更多的数据,更新数据
                DataManager.getInstance().updateDataset(curpos, articles);
                Message message = handler.obtainMessage();
                message.what = LOAD_MORE_ARTICLE;
                message.arg1 = articles.size();

                handler.sendMessage(message);
            }else{
                // 没有更多数据了
                DataManager.getInstance().updateDataset(curpos, articles);
                handler.sendEmptyMessage(NO_MORE_ARTICLE);
            }
        }
    }
}
