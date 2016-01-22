package com.simit.sneezereader;


import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.simit.datamodel.Article;
import com.simit.fragment.DetailFragment;

/**
 * Created by liuchun on 2016/1/17.
 */
public class FavoriteDetailActivity extends BaseActivity {
    private static final int[] TOOLBAR_TITLE = {R.string.title_tugua, R.string.title_lehuo, R.string.title_yitu};
    // article info
    private Article article;
    private int position;
    private int type;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorite_detail);
        // 获取数据
        Intent intent = getIntent();
        Bundle bundle = intent.getBundleExtra("detail");
        article = bundle.getParcelable("article");
        position = intent.getIntExtra("position", 0);
        type = article.getType();
        // 初始化界面
        initView();
        // 初始化分享组件
        //initShareConponents(savedInstanceState);
    }

    @Override
    protected void initView() {
        super.initView();
        setToolBarTitle(TOOLBAR_TITLE[type]);

        //
        FragmentManager fm = getSupportFragmentManager();
        DetailFragment fragment = new DetailFragment();
        Bundle bundle = new Bundle();
        bundle.putParcelable("article", article);
        fragment.setArguments(bundle);
        FragmentTransaction ft = fm.beginTransaction();
        ft.replace(R.id.content_container, fragment, "FavoriteFrag");
        ft.commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_detail, menu);
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
                changeFavoriteState(item, article);
                break;
            case R.id.action_share:
                shareArticle(article);
                break;
            default:break;
        }

        return super.onOptionsItemSelected(item);
    }
}
