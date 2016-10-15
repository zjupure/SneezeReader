package com.simit.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.simit.activity.BaseActivity;
import com.simit.common.Constants;
import com.simit.database.DbController;
import com.simit.fragment.adapter.HomeViewPagerAdapter;
import com.simit.database.Article;
import com.simit.network.HttpManager;
import com.simit.activity.R;
import com.handmark.pulltorefresh.library.ILoadingLayout;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.extras.viewpager.PullToRefreshViewPager;
import com.simit.network.NetworkMonitor;
import com.simit.service.UpdateService;
import com.simit.storage.SharedPreferenceUtils;

import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;



/**
 * Created by liuchun on 2015/7/16.
 */
public class YituFragment extends Fragment {
    private static final String TAG = "YituFragment";

    private static final String TIME_FORMAT_REFRESH = "上次更新: yyyy年MM月dd日 HH:mm";
    private static final String TIME_FORMAT_LOAD = "上次加载: yyyy年MM月dd日 HH:mm";
    /**
     * Fragment的根View
     */
    private View rootView;
    /**
     * ViewPager组件和刷新控件
     */
    private PullToRefreshViewPager mRefreshView;
    private ILoadingLayout header, footer;
    private ViewPager mViewPager;
    private HomeViewPagerAdapter mAdapter;
    private ProgressBar mLoadingBar;
    /**
     * 其他数据
     */
    private int curPos;    //当前页面标识
    private boolean isShow = true;    //页面是否在前端显示
    private Activity activity;
    // 数据集
    private List<Article> mArticles = new ArrayList<>();
    private int limit = 30;
    private String lastUpdated = "";
    private int position = 0;
    // 数据库操作
    private DbController dbHelper;


    /**
     * 静态工厂方法
     * @param bundle
     * @return
     */
    public static Fragment newInstance(Bundle bundle){

        Fragment fragment = new YituFragment();
        fragment.setArguments(bundle);

        return fragment;
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }


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

        curPos = getArguments().getInt("pos");

        activity = getActivity();

        lastUpdated = SharedPreferenceUtils.getLocal(activity, "lastUpdated" + curPos, "");

        dbHelper = DbController.getInstance(activity);

        //初始化界面View
        initViewPager();
    }


    /**
     * 初始化ViewPager
     */
    private void initViewPager(){
        // LoaidngBar
        mLoadingBar = (ProgressBar) rootView.findViewById(R.id.loadingbar);

        // ViewPager
        mRefreshView = (PullToRefreshViewPager) rootView.findViewById(R.id.refresh_viewpager);
        mViewPager = mRefreshView.getRefreshableView();
        //
        FragmentManager fm = getChildFragmentManager();
        mAdapter = new HomeViewPagerAdapter(fm, mArticles);
        //设置适配器
        mViewPager.setAdapter(mAdapter);
        mViewPager.setCurrentItem(position);
        mViewPager.setOffscreenPageLimit(5);  //缓存5屏
        // 设置监听
        mViewPager.setOffscreenPageLimit(4);
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int pos) {
                position = pos;

                if(getActivity() != null){
                    getActivity().supportInvalidateOptionsMenu();
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        // 基本配置
        header = mRefreshView.getLoadingLayoutProxy(true, false);
        header.setPullLabel(getString(R.string.pull_to_right_pull_label));
        header.setReleaseLabel(getString(R.string.pull_to_right_release_label));
        header.setRefreshingLabel(getString(R.string.pull_to_right_refreshing_label));
        header.setLastUpdatedLabel(lastUpdated);

        footer = mRefreshView.getLoadingLayoutProxy(false, true);
        footer.setPullLabel(getString(R.string.pull_to_left_pull_label));
        footer.setReleaseLabel(getString(R.string.pull_to_left_release_label));
        footer.setRefreshingLabel(getString(R.string.pull_to_left_refreshing_label));

        // 设置刷新
        mRefreshView.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener<ViewPager>() {
            @Override
            public void onRefresh(PullToRefreshBase<ViewPager> refreshView) {

                if (mRefreshView.isHeaderShown()) {

                    if(NetworkMonitor.isNetworkConnected(activity)) {
                        // 执行网络请求
                        fetchArticleFromNetwork();

                        SimpleDateFormat sdf = new SimpleDateFormat(TIME_FORMAT_REFRESH, Locale.getDefault());
                        String last_refresh_time = sdf.format(new Date());
                        header.setLastUpdatedLabel(last_refresh_time);
                        SharedPreferenceUtils.putLocal(activity, "lastUpdated" + curPos, last_refresh_time);
                    }else {
                        showToast("网络连接不可用,请稍后重试");
                        mRefreshView.onRefreshComplete();
                    }
                } else if (mRefreshView.isFooterShown()) {
                    // 从数据库加载数据
                    fetchArticleFromLocal();
                    //SimpleDateFormat sdf = new SimpleDateFormat(TIME_FORMAT_LOAD);
                    //String last_load_time = sdf.format(new Date());
                    //footer.setLastUpdatedLabel(last_load_time);
                } else {
                    mRefreshView.onRefreshComplete();
                }
            }
        });
        // 开始请求网络并同时加载本地数据
        fetchArticleFromLocal();
        fetchArticleFromNetwork();
        mRefreshView.setRefreshing(true);
    }

    // 处理网络请求回应发过来的消息
    private Handler handler = new Handler(){
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
                    limit = size + 10;
                    mAdapter.notifyDataSetChanged();
                    mLoadingBar.setVisibility(View.GONE);
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
     * 显示Toast
     * @param text
     */
    private void showToast(CharSequence text){
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
                    public void onSuccess(List<Article> data) {
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
                            tmp = dbHelper.getArticleByImgUrl(imgUrl, username);
                            if(tmp != null) {
                                // 重复的意图,更新当前article
                                data.set(i, tmp);
                                continue;
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
                        //从数据库加载对应的数据, 需要更新id和favorite属性
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
     * 从本地数据库加载数据
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
                    //没有更多数据了
                    handler.sendEmptyMessage(Constants.MSG_NO_MORE_ARTICLE);
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

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);

        isShow = !hidden;
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
        try {
            Field childFragmentManager = Fragment.class.getDeclaredField("mChildFragmentManager");
            childFragmentManager.setAccessible(true);
            childFragmentManager.set(this, null);


        } catch (NoSuchFieldException e) {
            Log.e(TAG, "YituFragment--->onDestoryView() NoSuchFieldException: " + e.getMessage());
        } catch (IllegalAccessException e) {
            Log.e(TAG, "YituFragment--->onDestoryView() IllegalAccessException: " + e.getMessage());
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_yitu, menu);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);

        if(position >= mArticles.size()){
            return;
        }

        Article article = mArticles.get(position);
        MenuItem favorite = menu.findItem(R.id.action_favorite);
        setFavoriteIcon(favorite, article.isFavorite());
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()){
            case R.id.action_refresh:
                refreshCurrentWebView();
                break;
            case R.id.action_favorite:
                refreshFavoriteState();
                break;
            case R.id.action_share:
                shareCurrentPage();
                break;
            default:break;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * 刷新当前页面,重新加载WebView
     */
    public void refreshCurrentWebView(){
        position = mViewPager.getCurrentItem();
        Fragment fragment = getChildFragmentManager().
                findFragmentByTag("android:switcher:" + R.id.viewpager + ":" + position);
        if(fragment != null && fragment instanceof  DetailFragment){
            ((DetailFragment)fragment).displayArticle();
        }

    }

    /**
     * 收藏当前页面
     */
    public void refreshFavoriteState(){
        position = mViewPager.getCurrentItem();
        Article article = mArticles.get(position);
        // 收藏操作
        boolean isFavorite = !article.isFavorite();
        article.setFavorite(isFavorite);

        String username = SharedPreferenceUtils.get(activity, "username", "any");
        if(isFavorite){
            //添加收藏
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            String addTime = sdf.format(new Date());
            dbHelper.insertFavorite(article, username, addTime);
            // Toast
            showToast("添加收藏成功");
        }else {
            //删除收藏
            dbHelper.deleteFavorite(article.getId(), username);
            // Toast
            showToast("删除收藏成功");
        }
        getActivity().supportInvalidateOptionsMenu();
    }

    /**
     * 分享当前页面
     */
    public void shareCurrentPage(){

        if(mArticles == null || mArticles.size() == 0){
            return;
        }

        position = mViewPager.getCurrentItem();
        Article article = mArticles.get(position);

        // 分享操作
        if(activity instanceof BaseActivity){
            ((BaseActivity)activity).shareArticle(article);
        }
    }

    /**
     * 设置菜单icon的颜色
     * @param item
     * @param isFavorite
     */
    private void setFavoriteIcon(MenuItem item, boolean isFavorite){
        if(item == null){
            return;
        }
        // item存在则进行设置
        Drawable iconDrawable = getResources().getDrawable(R.mipmap.ic_favorite);
        Drawable wrappedDrawable = DrawableCompat.wrap(iconDrawable);

        int color = 0xffffff;  // while
        if (isFavorite) {
            color = getResources().getColor(R.color.favorite_color);
        }else {
            color = getResources().getColor(R.color.favorite_color_nor);
        }
        DrawableCompat.setTint(wrappedDrawable, color);
        item.setIcon(wrappedDrawable);
    }
}
