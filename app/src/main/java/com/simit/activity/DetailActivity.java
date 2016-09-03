package com.simit.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuItem;

import com.simit.fragment.adapter.HomeViewPagerAdapter;
import com.simit.model.Article;
import com.simit.model.DataManager;
import com.simit.fragment.DetailFragment;

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
    private List<Article> datainfos;
    private int position;
    private int type;
    // Menu
    private Menu topMenu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.article_detail_layout);

        Intent intent = getIntent();
        Bundle bundle = intent.getBundleExtra("detail");
        article = bundle.getParcelable("article");
        position = intent.getIntExtra("position", 0);
        type = article.getType();

        // 初始化界面
        initView();
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
        //
        datainfos = DataManager.getInstance().getData(type);
        //
        FragmentManager fm = getSupportFragmentManager();
        mAdapter = new HomeViewPagerAdapter(fm, datainfos);
        mViewPager.setAdapter(mAdapter);
        // 显示第position项
        mViewPager.setCurrentItem(position);
        article = datainfos.get(position);
        //
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int pos) {
                article = datainfos.get(pos);
                position = pos;
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
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        topMenu = menu;
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

    public void refreshArticle(){
        position = mViewPager.getCurrentItem();
        Fragment fragment = getSupportFragmentManager().findFragmentByTag("android:switcher:" + R.id.viewpager + ":" + position);
        if(fragment != null && fragment instanceof  DetailFragment){
            ((DetailFragment)fragment).displayArticle();
        }
    }

    @Override
    public void onBackPressed() {
        position = mViewPager.getCurrentItem();
        Fragment fragement = getSupportFragmentManager().
                findFragmentByTag("android:switcher:" + R.id.viewpager + ":" + position);

        if(fragement != null && fragement instanceof DetailFragment)
        {
            DetailFragment frag = (DetailFragment)fragement;
            if(frag.goBack()){
                return;
            }
        }

        super.onBackPressed();
    }
}
