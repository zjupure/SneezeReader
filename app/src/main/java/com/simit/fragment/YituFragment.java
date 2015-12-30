package com.simit.fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.simit.database.DBManager;
import com.simit.datamodel.Article;
import com.simit.datamodel.DataManager;
import com.simit.network.SneezeClient;
import com.simit.network.SneezeJsonResponseHandler;
import com.simit.sneezereader.Config;
import com.simit.sneezereader.MainActivity;
import com.simit.sneezereader.R;
import com.handmark.pulltorefresh.library.ILoadingLayout;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.extras.viewpager.PullToRefreshViewPager;

import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;


/**
 * Created by liuchun on 2015/7/16.
 */
public class YituFragment extends Fragment {
    public static final int NEW_ARTICLE_ARRIVAL = 1;
    public static final int NO_NEW_ARTICLE = 2;
    public static final int LOAD_MORE_ARTICLE = 3;
    public static final int NO_MORE_ARTICLE = 4;
    public static final int NETWORK_ERROR = 5;
    public static final String DATASET_UPDATED_ACTION = "com.simit.fragment";
    private static final String TIME_FORMAT_REFRESH = "上次更新: yyyy年MM月dd日 HH:mm";
    private static final String TIME_FORMAT_LOAD = "上次加载: yyyy年MM月dd日 HH:mm";
    // rootView
    private View rootView;
    // ViewPager
    private PullToRefreshViewPager mRefreshView;
    private ILoadingLayout header, footer;
    private ViewPager mViewPager;
    private MyViewPagerAdapter mAdapter;

    private int curpos;    //当前页面标识
    //数据集
    private List<Article> mDataSet;
    private int limit = 30;
    private String lastUpdated = "";
    private int position = 0;
    private SneezeClient client;
    private LocalBroadcastManager broadcastManager;
    private BroadcastReceiver receiver;

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
        client = SneezeClient.getInstance(getActivity());
        client.setUpdated(true);
        lastUpdated = MainActivity.restoreLastUpdated(getActivity(), curpos);
        //初始化界面View
        initViewPager();
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

    private void initViewPager(){
        // ViewPager
        mRefreshView = (PullToRefreshViewPager) rootView.findViewById(R.id.refresh_viewpager);
        mViewPager = mRefreshView.getRefreshableView();

        //根据当前页面的位置,从数据管理器中获取数据
        mDataSet = DataManager.getInstance().getData(curpos);
        //
        FragmentManager fm = getChildFragmentManager();
        mAdapter = new MyViewPagerAdapter(fm, mDataSet);
        //设置适配器
        mViewPager.setAdapter(mAdapter);
        mViewPager.setCurrentItem(position);

        // 基本配置
        header = mRefreshView.getLoadingLayoutProxy(true, false);
        header.setPullLabel(getString(R.string.pull_to_right_pull_label));
        header.setReleaseLabel(getString(R.string.pull_to_right_release_label));
        header.setRefreshingLabel(getString(R.string.pull_to_right_refreshing_label));
        header.setLastUpdatedLabel(lastUpdated);
        //header.setLastUpdatedLabel(getString(R.string.pull_to_refresh_last_update));
        footer = mRefreshView.getLoadingLayoutProxy(false, true);
        footer.setPullLabel(getString(R.string.pull_to_left_pull_label));
        footer.setReleaseLabel(getString(R.string.pull_to_left_release_label));
        footer.setRefreshingLabel(getString(R.string.pull_to_left_refreshing_label));
        //footer.setLastUpdatedLabel(getString(R.string.pull_to_refresh_last_update));
        // 设置刷新
        mRefreshView.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener<ViewPager>() {
            @Override
            public void onRefresh(PullToRefreshBase<ViewPager> refreshView) {

                if (mRefreshView.isHeaderShown()) {
                    // 执行网络请求
                    client.getArticle(curpos, new SneezeJsonResponseHandler(getActivity(),
                            curpos, handler));

                    SimpleDateFormat sdf = new SimpleDateFormat(TIME_FORMAT_REFRESH);
                    String last_refresh_time = sdf.format(new Date());
                    header.setLastUpdatedLabel(last_refresh_time);
                    MainActivity.saveLastUpdated(getActivity(), curpos, last_refresh_time);
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
                    showToast("没有新的数据了");
                    break;
                case NETWORK_ERROR:
                    showToast("网络连接出错，请稍后重试");
                    break;
                case LOAD_MORE_ARTICLE:
                    limit = msg.arg1;
                    mAdapter.notifyDataSetChanged();
                    break;
                case NO_MORE_ARTICLE:
                    showToast("没有更多的数据了");
                    mAdapter.notifyDataSetChanged();
                    break;
                default:break;
            }
            mRefreshView.onRefreshComplete();
        }
    };

    private void showToast(CharSequence text){
        Context context = getActivity();
        if(context != null){
            Toast.makeText(context, text, Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 刷新当前页面,重新加载WebView
     */
    public void refreshCurrentWebView(){
        position = mViewPager.getCurrentItem();
        Fragment fragement = getChildFragmentManager().
                findFragmentByTag("android:switcher:" + R.id.viewpager + ":" + position);
        if(fragement != null && fragement instanceof  DetailFragment){
            ((DetailFragment)fragement).displayArticle();
        }

    }

    private void loadFromDatabase(int limit){
        Thread loadTask = new LoadMoreTask(limit);
        loadTask.start();
    }

    @Override
    public void onPause() {
        super.onPause();
        position = mViewPager.getCurrentItem();
    }

    @Override
    public void onDestroyView() {
        // TODO Auto-generated method stub
        super.onDestroyView();
        broadcastManager.unregisterReceiver(receiver);
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
