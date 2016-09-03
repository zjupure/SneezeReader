package com.simit.fragment;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.simit.database.DbController;
import com.simit.fragment.adapter.ArticleAdapter;
import com.simit.fragment.adapter.ArticleAdapter.OnItemClickListener;
import com.simit.model.Article;
import com.simit.model.DataManager;
import com.simit.network.NetworkMonitor;
import com.simit.network.SneezeClient;
import com.simit.network.SneezeJsonResponseHandler;
import com.simit.activity.Constant;
import com.simit.activity.DetailActivity;
import com.simit.activity.MainActivity;
import com.simit.activity.R;
import com.handmark.pulltorefresh.library.ILoadingLayout;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshRecyclerView;
import com.simit.activity.SneezeApplication;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Created by liuchun on 2015/7/16.
 */
public class ItemFragment extends Fragment {
    private static final int[] THRESHOLD = {5, 15, 10, 10};
    // rootView
    private View rootView;  //缓存根View,防止重复渲染
    // compents
    private PullToRefreshRecyclerView mRefreshView;  // RecyclerView wrapper
    private ILoadingLayout header, footer;
    private RecyclerView mRecyclerView;  // 列表
    private LinearLayoutManager mLayoutManager;
    private ArticleAdapter mAdapter;   //适配器
    private FloatingActionButton mGoTopBtn;
    private int curpos;    //当前页面标识
    // RecycleView数据集
    private List<Article> mDataSet;
    private int limit = 30;
    private String lastUpdated = "";
    // 网络请求相关
    private SneezeClient client;
    private LocalBroadcastManager broadcastManager;
    private BroadcastReceiver receiver;
    // 数据库
    private Activity  context;
    private DbController dbManager;
    private DataManager dataManager;
    private SneezeApplication app;

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
        context = getActivity();
        client = SneezeClient.getInstance(context);
        client.setUpdated(true);
        lastUpdated = MainActivity.restoreLastUpdated(context, curpos);
        app = (SneezeApplication) getActivity().getApplication();
        dbManager = DbController.getInstance(context);
        dataManager = DataManager.getInstance();
        //初始化界面View
        initRecyclerView();
        // 注册广播接收器
        broadcastManager = LocalBroadcastManager.getInstance(context);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Constant.DATASET_UPDATED_ACTION);
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
        // Floating action bar
        mGoTopBtn = (FloatingActionButton) rootView.findViewById(R.id.go_top_btn);
        // 为按钮设置监听事件
        mGoTopBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //自动滑动到顶部
                mRecyclerView.smoothScrollToPosition(0);
                //mGoTopBtn.setVisibility(View.GONE);
            }
        });

        // RecyclerView
        mRefreshView = (PullToRefreshRecyclerView) rootView.findViewById(R.id.tugua_list);
        mRecyclerView = mRefreshView.getRefreshableView();
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(context);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        //根据当前页面的位置,从数据管理器中获取数据
        mDataSet = DataManager.getInstance().getData(curpos);
        //定义Adapter
        mAdapter = new ArticleAdapter(mDataSet, curpos);  //绑定数据集
        mRecyclerView.setAdapter(mAdapter);   //设置适配器

        //设置item点击事件
        mAdapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                // 图卦和乐活页面响应点击事件,段子页面无效
                if (curpos > 1) {
                    return;
                }

                Intent intent = new Intent(context, DetailActivity.class);
                Bundle bundle = new Bundle();
                Article article = mDataSet.get(position);
                bundle.putParcelable("article", article);
                intent.putExtra("detail", bundle);
                intent.putExtra("position", position);
                startActivity(intent);
            }

            @Override
            public void onItemLongClick(View view, int position) {
                // 只有段子页面才响应长按分享功能
                if(curpos != Article.DUANZI){
                    return;
                }

                Article article = mDataSet.get(position);
                if(context instanceof MainActivity){
                    MainActivity parent = (MainActivity)context;
                    parent.shareArticle(article);
                }
            }
        });

        // 为RecyclerView添加滑动监听
        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                int firstVisibleItem = mLayoutManager.findFirstVisibleItemPosition();
                if(firstVisibleItem > THRESHOLD[curpos]){
                    mGoTopBtn.setVisibility(View.VISIBLE);
                }else {
                    mGoTopBtn.setVisibility(View.GONE);
                }
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                int firstVisibleItem = mLayoutManager.findFirstVisibleItemPosition();
                if(firstVisibleItem > THRESHOLD[curpos]){
                    mGoTopBtn.setVisibility(View.VISIBLE);
                }else {
                    mGoTopBtn.setVisibility(View.GONE);
                }
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
                    //
                    //int networkState = NetworkMonitor.getNetWorkState(context);

                    if(NetworkMonitor.isNetworkConnected(context)){
                        // 执行网络请求
                        client.getArticle(curpos, new SneezeJsonResponseHandler(context,
                                curpos, handler));

                        SimpleDateFormat sdf = new SimpleDateFormat(Constant.TIME_FORMAT_REFRESH);
                        String last_refresh_time = sdf.format(new Date());
                        header.setLastUpdatedLabel(last_refresh_time);
                        MainActivity.saveLastUpdated(context, curpos, last_refresh_time);
                    }else {
                        showToast("网络连接不可用,请稍后重试");
                        mRefreshView.onRefreshComplete();
                    }
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
                case Constant.NEW_ARTICLE_ARRIVAL:
                    int nums = msg.arg1;
                    loadFromDatabase(limit + nums);
                    break;
                case Constant.NO_NEW_ARTICLE:
                    //loadFromDatabase(limit);
                    showToast("没有新的数据了");
                    break;
                case Constant.NETWORK_ERROR:
                    showToast("网络连接出错,请稍后重试");
                    break;
                case Constant.LOAD_MORE_ARTICLE:
                    limit = msg.arg1;
                    mAdapter.notifyDataSetChanged();
                    break;
                case Constant.NO_MORE_ARTICLE:
                    showToast("没有更多的数据了");
                    mAdapter.notifyDataSetChanged();
                    break;
                default:break;
            }
            mRefreshView.onRefreshComplete();
        }
    };

    private void  showToast(CharSequence text){
        Context context = getActivity();
        if(context != null){
            Toast.makeText(context, text, Toast.LENGTH_SHORT).show();
        }
    }

    private void loadFromDatabase(int limit){
        Thread loadTask = new LoadMoreTask(limit);
        loadTask.start();
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
            String username = app.getUsername();
            List<Article> articles = dbManager.getData(curpos, num, username);

            if(articles.size() > mDataSet.size()){
                // 查询到更多的数据,更新数据
                dataManager.updateDataset(curpos, articles);
                Message message = handler.obtainMessage();
                message.what = Constant.LOAD_MORE_ARTICLE;
                message.arg1 = articles.size();

                handler.sendMessage(message);
            }else{
                // 没有更多数据了
                dataManager.updateDataset(curpos, articles);
                handler.sendEmptyMessage(Constant.NO_MORE_ARTICLE);
            }
        }
    }

}
