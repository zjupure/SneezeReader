package com.simit.activity;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
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
    // Component
    private ViewPager mViewPager;
    private HomeViewPagerAdapter mAdapter;
    // article info
    private Article article;
    private List<Article> mArticles;
    private int curPage;
    private int type;
    private int limit = 30;
    // Menu
    private Menu topMenu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    @Override
    protected int getLayoutId() {
        return R.layout.article_detail_layout;
    }

    @Override
    protected void handleIntent(Intent intent) {
        super.handleIntent(intent);

        Bundle bundle = intent.getBundleExtra("detail");
        article = bundle.getParcelable("article");
        curPage = intent.getIntExtra("position", 0);
        type = article.getType();
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
        mArticles = new ArrayList<>();
        if(article != null){
            mArticles.add(article);
        }
        mAdapter = new HomeViewPagerAdapter(getSupportFragmentManager(), mArticles);
        mViewPager.setAdapter(mAdapter);
        //
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                article = mArticles.get(position);
                curPage = position;
                // 更新图标
                if (topMenu != null) {
                    MenuItem item = topMenu.findItem(R.id.action_favorite);
                    setFavoriteIcon(item, article.isFavorite());
                }
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
        setFavoriteIcon(favorite, article.isFavorite());

        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // 菜单点击操作
        switch (item.getItemId()){
            case R.id.action_favorite:
                refreshFavoriteState(article);
                break;
            case R.id.action_refresh:
                refreshArticle();
                break;
            case R.id.action_share:
                shareArticle(article);
                break;
            default:break;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * 从本地数据库加载数据
     */
    private void fetchArticleFromLocal(){

        AsyncTask<Void, Void, List<Article>> asyncTask = new AsyncTask<Void, Void, List<Article>>() {
            @Override
            protected List<Article> doInBackground(Void... params) {

                DbController dbHelper = DbController.getInstance(DetailActivity.this);
                //从数据库查询最新的数据
                String username = SharedPreferenceUtils.get(DetailActivity.this, "username", "any");
                List<Article> articles = dbHelper.getData(type, limit, username);

                return articles;
            }


            @Override
            protected void onPostExecute(List<Article> articles) {
                if(articles.size() < 0){
                    return;
                }

                int index = -1;
                for(int i = 0; i < articles.size(); i++){
                    Article tmp = articles.get(i);

                    if(TextUtils.equals(article.getDescription(), tmp.getDescription())){
                        index = i;
                        break;
                    }
                }

                if(index < 0){
                    return;
                }

                curPage = index;
                article = articles.get(index);
                mArticles.clear();
                mArticles.addAll(articles);
                mAdapter.notifyDataSetChanged();
                mViewPager.setCurrentItem(curPage);
            }
        };
        asyncTask.execute();

    }

    public void refreshArticle(){
        curPage = mViewPager.getCurrentItem();
        Fragment fragment = getSupportFragmentManager().findFragmentByTag("android:switcher:" + R.id.viewpager + ":" + curPage);
        if(fragment != null && fragment instanceof  DetailFragment){
            ((DetailFragment)fragment).displayArticle();
        }
    }

    @Override
    public void onBackPressed() {
        curPage = mViewPager.getCurrentItem();
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
