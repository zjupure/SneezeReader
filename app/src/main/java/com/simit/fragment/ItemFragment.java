package com.simit.fragment;

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

import com.simit.database.DBManager;
import com.simit.fragment.MyRecylcerAdapter.OnItemClickListener;
import com.simit.datamodel.Article;
import com.simit.datamodel.DataManager;
import com.simit.network.NetworkMonitor;
import com.simit.network.SneezeClient;
import com.simit.network.SneezeJsonResponseHandler;
import com.simit.sneezereader.Config;
import com.simit.sneezereader.DetailActivity;
import com.simit.sneezereader.MainActivity;
import com.simit.sneezereader.R;
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
    private static final int[] THRESHOLD = {5, 15, 10, 10};
    // rootView
    private View rootView;  //缓存根View,防止重复渲染
    // compents
    private PullToRefreshRecyclerView mRefreshView;  // RecyclerView wrapper
    private ILoadingLayout header, footer;
    private RecyclerView mRecyclerView;  // 列表
    private LinearLayoutManager mLayoutManager;
    private MyRecylcerAdapter mAdapter;   //适配器
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
        lastUpdated = MainActivity.restoreLastUpdated(getActivity(), curpos);
        //初始化界面View
        initRecyclerView();
        // 注册广播接收器
        broadcastManager = LocalBroadcastManager.getInstance(getActivity());
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Config.DATASET_UPDATED_ACTION);
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
                    int networkState = NetworkMonitor.getNetWorkState(getActivity());
                    if(networkState >= NetworkMonitor.WIFI){
                        // 执行网络请求
                        client.getArticle(curpos, new SneezeJsonResponseHandler(getActivity(),
                                curpos, handler));

                        SimpleDateFormat sdf = new SimpleDateFormat(Config.TIME_FORMAT_REFRESH);
                        String last_refresh_time = sdf.format(new Date());
                        header.setLastUpdatedLabel(last_refresh_time);
                        MainActivity.saveLastUpdated(getActivity(), curpos, last_refresh_time);
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
                case Config.NEW_ARTICLE_ARRIVAL:
                    int nums = msg.arg1;
                    loadFromDatabase(limit + nums);
                    break;
                case Config.NO_NEW_ARTICLE:
                    showToast("没有新的数据了");
                    break;
                case Config.NETWORK_ERROR:
                    showToast("网络连接出错,请稍后重试");
                    break;
                case Config.LOAD_MORE_ARTICLE:
                    limit = msg.arg1;
                    mAdapter.notifyDataSetChanged();
                    break;
                case Config.NO_MORE_ARTICLE:
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
            List<Article> articles = DBManager.getInstance(getActivity()).getData(curpos, num);

            if(articles.size() > mDataSet.size()){
                // 查询到更多的数据,更新数据
                DataManager.getInstance().updateDataset(curpos, articles);
                Message message = handler.obtainMessage();
                message.what = Config.LOAD_MORE_ARTICLE;
                message.arg1 = articles.size();

                handler.sendMessage(message);
            }else{
                // 没有更多数据了
                DataManager.getInstance().updateDataset(curpos, articles);
                handler.sendEmptyMessage(Config.NO_MORE_ARTICLE);
            }
        }
    }

}
