package com.simit.fragment;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.NotificationCompat;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.simit.activity.BaseActivity;
import com.simit.common.Constants;
import com.simit.database.DbController;
import com.simit.fragment.adapter.ArticleAdapter;
import com.simit.fragment.adapter.ArticleAdapter.OnItemClickListener;
import com.simit.database.Article;
import com.simit.network.HttpManager;
import com.simit.network.NetworkMonitor;
import com.simit.activity.DetailActivity;
import com.simit.activity.R;
import com.handmark.pulltorefresh.library.ILoadingLayout;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshRecyclerView;
import com.simit.service.UpdateService;
import com.simit.storage.SharedPreferenceUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;


/**
 * Created by liuchun on 2015/7/16.
 */
public class ArticleFragment extends Fragment {
    private static final String TAG = "ArticleFragment";

    private static final int[] THRESHOLD = {5, 15, 10, 10};
    /**
     * Fragment的根View
     */
    private View rootView;  //缓存根View,防止重复渲染
    /**
     * Fragment的UI组件
     */
    private PullToRefreshRecyclerView mRefreshView;  // RecyclerView wrapper
    private ILoadingLayout header, footer;
    private RecyclerView mRecyclerView;  // 列表
    private LinearLayoutManager mLayoutManager;
    private ArticleAdapter mAdapter;   //适配器
    private FloatingActionButton mGoTopBtn;
    private ProgressBar mLoadingBar;  //加载进度条
    /**
     * 当前页面标识
     */
    private int curPos;    //当前页面标识
    private boolean isShow = true;  //标示该页面是否在前端显示
    private Activity activity;
    // RecycleView数据集
    private List<Article> mArticles = new ArrayList<>();
    private int limit = 30;
    private String lastUpdated = "";
    // 数据库
    private DbController dbHelper;

    /**
     * 静态工厂方法
     * @param bundle
     * @return
     */
    public static Fragment newInstance(Bundle bundle){

        Fragment fragment = new ArticleFragment();
        fragment.setArguments(bundle);

        return fragment;
    }


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

        curPos = getArguments().getInt("pos");
        activity = getActivity();

        lastUpdated = SharedPreferenceUtils.getLocal(activity, "lastUpdated" + curPos, "");

        dbHelper = DbController.getInstance(activity);
        //初始化界面View
        initRecyclerView();
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);

        isShow = !hidden;
    }

    /**
     * 初始化RecyclerView控件
     */
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
        // ProgressBar
        mLoadingBar = (ProgressBar) rootView.findViewById(R.id.loadingbar);

        // RecyclerView
        mRefreshView = (PullToRefreshRecyclerView) rootView.findViewById(R.id.tugua_list);
        mRecyclerView = mRefreshView.getRefreshableView();
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(activity);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        //定义Adapter
        mAdapter = new ArticleAdapter(mArticles, curPos);  //绑定数据集
        mRecyclerView.setAdapter(mAdapter);   //设置适配器

        //设置item点击事件
        mAdapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                // 图卦和乐活页面响应点击事件,段子页面无效
                if (curPos > 1) {
                    return;
                }

                Intent intent = new Intent(activity, DetailActivity.class);

                Article article = mArticles.get(position);
                intent.putExtra("position", position);
                intent.putExtra("article", article);

                startActivity(intent);
            }

            @Override
            public void onItemLongClick(View view, int position) {
                // 只有段子页面才响应长按分享功能
                if(curPos != Article.DUANZI){
                    return;
                }

                Article article = mArticles.get(position);
                /**TODO share article operation **/
                if(activity instanceof BaseActivity){
                    ((BaseActivity)activity).shareArticle(article);
                }
            }
        });

        // 为RecyclerView添加滑动监听
        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                int firstVisibleItem = mLayoutManager.findFirstVisibleItemPosition();
                if(firstVisibleItem > THRESHOLD[curPos]){
                    mGoTopBtn.setVisibility(View.VISIBLE);
                }else {
                    mGoTopBtn.setVisibility(View.GONE);
                }
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                int firstVisibleItem = mLayoutManager.findFirstVisibleItemPosition();
                if(firstVisibleItem > THRESHOLD[curPos]){
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
                    if(NetworkMonitor.isNetworkConnected(activity)){
                        // 执行网络请求
                        fetchArticleFromNetwork();

                        SimpleDateFormat sdf = new SimpleDateFormat(Constants.TIME_FORMAT_REFRESH, Locale.getDefault());
                        String last_refresh_time = sdf.format(new Date());
                        header.setLastUpdatedLabel(last_refresh_time);

                        SharedPreferenceUtils.putLocal(activity, "lastUpdated" + curPos, lastUpdated);

                    }else {
                        showToast("网络连接不可用,请稍后重试");
                        mRefreshView.onRefreshComplete();
                    }
                } else if (mRefreshView.isFooterShown()) {
                    // 从本地数据库加载数据
                    fetchArticleFromLocal();
                    //SimpleDateFormat sdf = new SimpleDateFormat(TIME_FORMAT_LOAD);
                    //String last_load_time = sdf.format(new Date());
                    //footer.setLastUpdatedLabel(last_load_time);
                } else {
                    mRefreshView.onRefreshComplete();
                }
            }
        });
        // 开始请求网络同时加载本地数据
        fetchArticleFromLocal();
        fetchArticleFromNetwork();
        mRefreshView.setRefreshing(true);
    }

    // 处理网络请求回应发过来的消息
    private Handler handler = new Handler(Looper.getMainLooper()){

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case Constants.MSG_NETWORK_SUCCESS:{
                    mAdapter.notifyDataSetChanged();
                    mLoadingBar.setVisibility(View.GONE);
                    invalidateOptionsMenu();
                    break;
                }
                case Constants.MSG_NETWORK_ERROR:{
                    //fetchArticleFromLocal();
                    break;
                }
                case Constants.MSG_LOCAL_LOAD_SUCCESS:{
                    int size = msg.arg1;
                    if(size > 0) {
                        limit = size + 10;
                        mAdapter.notifyDataSetChanged();
                        mLoadingBar.setVisibility(View.GONE);
                    }
                    invalidateOptionsMenu();
                    break;
                }
                case Constants.MSG_LOCAL_LOAD_FAIL:{
                    break;
                }
                case Constants.MSG_NEW_ARTICLE_ARRIVAL:{
                    int size = msg.arg1;
                    String text = "已刷新" + size + "篇文章";
                    showToast(text);
                    /**TODO 图卦需要展示通知 */
                    Article article = (Article) msg.obj;
                    boolean notify = SharedPreferenceUtils.get(activity, "notify", true);
                    if(article.getType() == Article.TUGUA && notify){
                        //
                        notifyNewArticle(article);
                    }
                    break;
                }
                case Constants.MSG_NO_NEW_ARTICLE:{
                    showToast("没有新的数据了");
                    break;
                }
                case Constants.MSG_NO_MORE_ARTICLE:{

                    showToast("本地数据已全部加载,没有更多数据了");
                    break;
                }
                case Constants.MSG_GET_PAGE_SOURCE:{
                    Article article = (Article) msg.obj;
                    Intent intent = new Intent(activity, UpdateService.class);
                    intent.putExtra("link", article.getDescription());
                    activity.startService(intent);
                    break;
                }
                default:break;
            }
            mRefreshView.onRefreshComplete();
        }
    };


    /**
     * 刷新菜单
     */
    private void invalidateOptionsMenu(){

        if(getActivity() != null){
            getActivity().supportInvalidateOptionsMenu();
        }
    }

    /**
     * 弹出Toast
     * @param text
     */
    private void  showToast(CharSequence text){
        Context context = getActivity();
        if(context != null && isShow){
            Toast.makeText(context, text, Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 从网络加载数据
     */
    private void fetchArticleFromNetwork(){

        HttpManager.getInstance(activity)
                .getArticle(curPos, new HttpManager.INetworkCallback<List<Article>>() {
                    @Override
                    public void onError(Exception e) {
                        Log.e(TAG, "fetch data from network error in YituFragment");
                        handler.sendEmptyMessage(Constants.MSG_NETWORK_ERROR);
                    }

                    @Override
                    public void onSuccess(final List<Article> data) {
                        Log.d(TAG, "fetch data from network success; data size: " + data.size());
                        //检查是否有新的文章
                        boolean hasUpdated = false;
                        List<Article> newArticles = new ArrayList<Article>();

                        String username = SharedPreferenceUtils.get(activity, "username", "any");
                        for(int i = 0; i < data.size(); i++){
                            Article article = data.get(i);

                            String description = article.getDescription();
                            String imgUrl = article.getImgUrl();
                            String localUrl = "";

                            Article tmp = dbHelper.getArticleByLink(description, username);
                            if(tmp != null){
                                //重复的文章,更新当前的article
                                data.set(i, tmp);
                                localUrl = article.getLocalLink();
                                if(curPos != Article.DUANZI && TextUtils.isEmpty(localUrl)){
                                    //源码未获取,通知service处理
                                    Message msg = handler.obtainMessage();
                                    msg.what = Constants.MSG_GET_PAGE_SOURCE;
                                    msg.obj = article;
                                    msg.sendToTarget();
                                }

                                continue;
                            }
                            //
                            if(curPos == Article.YITU) {
                                tmp = dbHelper.getArticleByImgUrl(imgUrl, username);
                                if(tmp != null) {
                                    // 重复的意图,更新当前article
                                    data.set(i, tmp);
                                    continue;
                                }
                            }

                            hasUpdated = true;
                            newArticles.add(article);
                        }

                        // 新的数据插入数据库
                        if(hasUpdated){
                            //插入数据库
                            dbHelper.insertMultiRecords(newArticles);

                            Message msg = handler.obtainMessage();
                            msg.what = Constants.MSG_NEW_ARTICLE_ARRIVAL;
                            msg.arg1 = newArticles.size();
                            msg.obj = data.get(0);
                            msg.sendToTarget();
                        }else {
                            handler.sendEmptyMessage(Constants.MSG_NO_NEW_ARTICLE);
                            
                        }
                        //从数据库加载对应的数据,需要更新id和favorite两个属性
                        ArrayList<Article> tmpList = new ArrayList<Article>();

                        for(Article article : data){
                            String description = article.getDescription();
                            Article tmp = dbHelper.getArticleByLink(description, username);
                            tmpList.add(tmp);
                        }
                        // 更新内存中的数据集
                        mArticles.clear();
                        mArticles.addAll(tmpList);
                        handler.sendEmptyMessage(Constants.MSG_NETWORK_SUCCESS);
                    }
                });
    }

    /**
     * 从本地数据库加载数据, 开启异步线程执行
     */
    private void fetchArticleFromLocal(){

        Runnable loadTask = new Runnable() {
            @Override
            public void run() {
                //从数据库查询最新的数据
                String username = SharedPreferenceUtils.get(activity, "username", "any");

                List<Article> articles = dbHelper.getArticles(curPos, limit, username);

                if(articles.size() > mArticles.size()){
                    //查询到更多的数据
                    mArticles.clear();
                    mArticles.addAll(articles);

                    Message msg = handler.obtainMessage();
                    msg.what = Constants.MSG_LOCAL_LOAD_SUCCESS;
                    msg.arg1 = articles.size();
                    msg.sendToTarget();
                }else{
                    //
                    Message msg = handler.obtainMessage();
                    msg.what = Constants.MSG_LOCAL_LOAD_SUCCESS;
                    msg.arg1 = articles.size();
                    msg.sendToTarget();

                    if(articles.size() > 0){
                        //没有更多数据了
                        handler.sendEmptyMessage(Constants.MSG_NO_MORE_ARTICLE);
                    }
                }

                if(curPos == Article.DUANZI){
                    //段子页面不需要缓存页面源码
                    return;
                }

                //检查是否有源码未获取的数据
                for(Article article : articles){
                    String description = article.getDescription();
                    String localUrl = dbHelper.getLocalUrl(description);
                    if(TextUtils.isEmpty(localUrl)){
                        //源码未获取,通知service处理
                        Message msg = handler.obtainMessage();
                        msg.what = Constants.MSG_GET_PAGE_SOURCE;
                        msg.obj = article;
                        msg.sendToTarget();
                    }
                }

            }
        };

        Constants.localFetcherPool.submit(loadTask);
    }

    /**
     * 收到新的文章,弹出通知栏,仅针对图卦页面
     * @param article
     */
    private void notifyNewArticle(Article article){

        NotificationManager nm = (NotificationManager) activity.getSystemService(Context.NOTIFICATION_SERVICE);
        String tickerText = article.getTitle();
        long when = System.currentTimeMillis();

        String contentTitle = article.getTitle();
        String contentText = article.getPubDate();

        Intent intent = new Intent(activity, DetailActivity.class);
        intent.putExtra("position", 0);
        intent.putExtra("article", article);
        PendingIntent pendingIntent = PendingIntent.getActivity(activity, 0, intent, 0);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(activity);
        builder.setSmallIcon(R.mipmap.logo)
                .setContentTitle(contentTitle)
                .setContentText(contentText)
                .setContentIntent(pendingIntent)
                .setTicker(tickerText)
                .setWhen(when)
                .setAutoCancel(true);

        Notification notification = builder.build();
        notification.defaults = Notification.DEFAULT_SOUND | Notification.DEFAULT_LIGHTS;
        nm.notify(0, notification);
    }
}
