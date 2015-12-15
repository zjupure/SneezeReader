package com.simit.sneezereader;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.simit.datamodel.Article;
import com.simit.datamodel.DataManager;
import com.simit.fragment.DetailFragment;
import com.simit.fragment.MyViewPagerAdapter;

import java.util.List;


/**
 * Created by liuchun on 2015/12/6.
 */
public class DetailActivity extends AppCompatActivity{
    // Component
    private Toolbar mToolBar;
    private ViewPager mViewPager;
    private MyViewPagerAdapter mAdapter;
    // article info
    private Article article;
    private int position;
    private int type;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.article_detail_layout);

        Intent intent = getIntent();
        Bundle bundle = intent.getBundleExtra("detail");
        article = bundle.getParcelable("article");
        position = intent.getIntExtra("position", 0);
        type = article.getType();

        initView();
    }


    private void initView(){
        // ToolBar
        mToolBar = (Toolbar)findViewById(R.id.toolbar);
        if(type == Article.TUGUA){
            mToolBar.setTitle(R.string.title_tugua);
        }else if(type == Article.LEHUO){
            mToolBar.setTitle(R.string.title_lehuo);
        }
        setSupportActionBar(mToolBar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // ViewPager
        mViewPager = (ViewPager) findViewById(R.id.viewpager);
        //
        List<Article> mDataSet = DataManager.getInstance().getData(type);
        //
        FragmentManager fm = getSupportFragmentManager();
        mAdapter = new MyViewPagerAdapter(fm, mDataSet);
        mViewPager.setAdapter(mAdapter);
        // 显示第position项
        mViewPager.setCurrentItem(position);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_detail, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()){
            case R.id.action_favorite:
                break;
            case R.id.action_share:
                break;
            default:break;
        }

        return super.onOptionsItemSelected(item);
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
                Log.d("DetailActivity", "Back Pressed");
                return;
            }
            Log.d("DetailActivity", "WebView can not go back");
        }

        super.onBackPressed();
    }
}
