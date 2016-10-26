package com.simit.activity;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.simit.database.DbController;
import com.simit.database.Article;
import com.simit.fragment.DetailFragment;
import com.simit.fragment.adapter.HomeViewPagerAdapter;
import com.simit.storage.SharedPreferenceUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


/**
 * Created by liuchun on 2015/12/6.
 */
public class DetailActivity extends BaseActivity{
    private static final String TAG = "DetailActivity";
    // Component
    private ViewPager mViewPager;
    private HomeViewPagerAdapter mAdapter;
    // article info
    private Article curArticle;
    private List<Article> mArticles = new ArrayList<>();
    private int curPage;
    private int type;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    @Override
    protected int getLayoutId() {
        return R.layout.article_detail_layout;
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        curPage = intent.getIntExtra("position", 0);
        mViewPager.setCurrentItem(curPage);

        Log.i(TAG, "OnNewIntent>>curPage: " + curPage);
    }

    @Override
    protected void handleIntent(Intent intent) {
        super.handleIntent(intent);

        curPage = intent.getIntExtra("position", 0);
        curArticle = intent.getParcelableExtra("article");
        type = curArticle.getType();

        Log.i(TAG, "curPage: " + curPage);
    }

    @Override
    protected void initView() {
        super.initView();
        if(type == Article.TUGUA){
            setToolBarTitle(R.string.title_tugua);
        }else if(type == Article.LEHUO){
            setToolBarTitle(R.string.title_lehuo);
        }

        // ViewPager
        mViewPager = (ViewPager) findViewById(R.id.viewpager);
        mAdapter = new HomeViewPagerAdapter(getSupportFragmentManager(), mArticles);
        mViewPager.setAdapter(mAdapter);
        //
        //mViewPager.setOffscreenPageLimit(4);
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {

                curPage = position;
                curArticle = mArticles.get(curPage);
                supportInvalidateOptionsMenu();
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        //加载本地数据
        fetchArticleFromLocal();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_yitu, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {

        MenuItem favorite = menu.findItem(R.id.action_favorite);
        setFavoriteIcon(favorite, curArticle.isFavorite());

        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // 菜单点击操作
        switch (item.getItemId()){
            case R.id.action_favorite:
                refreshFavoriteState(curArticle);
                break;
            case R.id.action_refresh:
                refreshArticle();
                break;
            case R.id.action_share:
                shareArticle(curArticle);
                break;
            default:break;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * 从本地数据库加载数据
     */
    private void fetchArticleFromLocal(){

        Log.i(TAG, "fetchArticleFromLocal>>>start");
        AsyncTask<Void, Void, List<Article>> asyncTask = new AsyncTask<Void, Void, List<Article>>() {
            @Override
            protected List<Article> doInBackground(Void... params) {
                Log.i(TAG, "doInbackground>>load lcoal data");

                DbController dbHelper = DbController.getInstance(DetailActivity.this);
                //从数据库查询最新的数据
                String username = SharedPreferenceUtils.get(DetailActivity.this, "username", "any");
                List<Article> articles = dbHelper.getArticles(type, 0, username);

                return articles;
            }

            @Override
            protected void onPostExecute(List<Article> articles) {
                Log.i(TAG, "load local data finished");

                if(articles.size() < 0){
                    return;
                }

                int index = -1;
                for(int i = 0; i < articles.size(); i++){
                    Article tmp = articles.get(i);

                    if(tmp.getDescription().equals(curArticle.getDescription())){
                        index = i;
                        break;
                    }
                }

                Log.i(TAG, "find correct index: " + index);
                if(index < 0){
                    //把当前文章追加到最后
                    articles.add(curArticle);
                    index = articles.size() - 1;
                }

                mArticles.clear();
                mArticles.addAll(articles);

                mAdapter.notifyDataSetChanged();

                curPage = index;
                curArticle = mArticles.get(curPage);

                Log.i(TAG, "curPage: " + curPage);
                Log.i(TAG, "title: " + curArticle.getTitle());
                mViewPager.setCurrentItem(curPage);
            }
        };
        asyncTask.execute();

    }

    public void refreshArticle(){

        Fragment fragment = getSupportFragmentManager().findFragmentByTag("android:switcher:" + R.id.viewpager + ":" + curPage);
        if(fragment != null && fragment instanceof  DetailFragment){
            ((DetailFragment)fragment).displayArticle();
        }
    }

    @Override
    public void onBackPressed() {

        Fragment fragment = getSupportFragmentManager().
                findFragmentByTag("android:switcher:" + R.id.viewpager + ":" + curPage);

        if(fragment != null && fragment instanceof DetailFragment)
        {
            DetailFragment frag = (DetailFragment)fragment;
            if(frag.goBack()){
                return;
            }
        }

        super.onBackPressed();
    }
}
