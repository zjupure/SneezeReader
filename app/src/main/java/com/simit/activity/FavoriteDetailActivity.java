package com.simit.activity;


import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.Menu;
import android.view.MenuItem;

import com.simit.fragment.DetailFragment;
import com.simit.database.Article;

/**
 * Created by liuchun on 2016/1/17.
 */
public class FavoriteDetailActivity extends BaseActivity {
    private static final int[] TOOLBAR_TITLE = {R.string.title_tugua, R.string.title_lehuo, R.string.title_yitu};
    // article info
    private Article article;
    private int position;
    private int type;
    private DetailFragment fragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    @Override
    protected int getLayoutId() {
        return R.layout.activity_favorite_detail;
    }

    @Override
    protected void handleIntent(Intent intent) {
        super.handleIntent(intent);

        Bundle bundle = intent.getBundleExtra("detail");
        article = bundle.getParcelable("article");
        position = intent.getIntExtra("position", 0);
        type = article.getType();
    }

    @Override
    protected void initView() {
        super.initView();
        setToolBarTitle(TOOLBAR_TITLE[type]);
        //
        FragmentManager fm = getSupportFragmentManager();
        fragment = new DetailFragment();
        Bundle bundle = new Bundle();
        bundle.putParcelable("article", article);
        fragment.setArguments(bundle);
        FragmentTransaction ft = fm.beginTransaction();
        ft.replace(R.id.content_container, fragment, "FavoriteFrag");
        ft.commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_yitu, menu);
        // 根据文章的收藏状态设置图标
        MenuItem item = menu.findItem(R.id.action_favorite);
        setFavoriteIcon(item, article.isFavorite());
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // 菜单点击操作
        switch (item.getItemId()){
            case R.id.action_favorite:
                refreshFavoriteState(article);
                break;
            case R.id.action_refresh:
                if(fragment != null){
                    fragment.displayArticle();
                }
                break;
            case R.id.action_share:
                shareArticle(article);
                break;
            default:break;
        }

        return super.onOptionsItemSelected(item);
    }
}
