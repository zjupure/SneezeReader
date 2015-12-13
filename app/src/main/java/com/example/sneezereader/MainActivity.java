package com.example.sneezereader;


import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.RadioGroup;

import com.example.datamodel.Article;
import com.example.fragment.ItemFragment;
import com.example.fragment.YituFragment;
import com.example.network.SneezeClient;
import com.example.network.SneezeJsonResponseHandler;
import com.loopj.android.http.AsyncHttpResponseHandler;

import java.util.ArrayList;
import java.util.List;

import cz.msebera.android.httpclient.Header;


public class MainActivity extends AppCompatActivity{
    //Fragment　Tag
    public static final String[] FRAG_TAG = {"tugua", "lehuo", "yitu", "duanzi"};
    //界面组件
    private DrawerLayout mDrawerLayout;
    private NavigationView mNavView;
    private ActionBarDrawerToggle mToggle;
    private Toolbar mToolBar;
    private RadioGroup mTabMenu;
    // Fragment UI
    public List<Fragment> mFragments;
    private FragmentManager fm;

    private SneezeClient client;
    //基本信息
    private int curpos = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.d("MainActivity", "this is main screen");

        client = SneezeClient.getInstance(this);
        initView();
    }

    /**
     * 初始化界面
     */
    private void initView(){
        // ToolBar
        mToolBar = (Toolbar)findViewById(R.id.toolbar);

        mToolBar.setTitle(R.string.app_title);
        setSupportActionBar(mToolBar);
        // set up toolbar
        mToolBar.setNavigationIcon(R.drawable.user_logo);

        mDrawerLayout = (DrawerLayout)findViewById(R.id.drawer_layout);
        mNavView = (NavigationView)findViewById(R.id.nav_view);

        if(mNavView != null){
            mNavView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener(){
                   @Override
                   public  boolean onNavigationItemSelected(MenuItem menuItem){
                       //切换对应的Fragment操作
                       menuItem.setCheckable(true);
                       mDrawerLayout.closeDrawers();
                       return false;
                   }
               }
            );

        }

        //设置mToggle
        mToggle = new ActionBarDrawerToggle(this, mDrawerLayout, mToolBar, R.string.app_name, R.string.app_name);
        mToggle.syncState();
        mDrawerLayout.setDrawerListener(mToggle);

        initFragments();

        //RadioButton
        mTabMenu = (RadioGroup)findViewById(R.id.tab_menu);
        //设置Tab监听
        mTabMenu.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                int precos = curpos;
                switch (checkedId) {
                    case R.id.tab_tugua:
                        curpos = 0;
                        break;
                    case R.id.tab_lehuo:
                        curpos = 1;
                        break;
                    case R.id.tab_yitu:
                        curpos = 2;
                        break;
                    case R.id.tab_duanzi:
                        curpos = 3;
                        break;
                    default:
                        break;
                }
                //开始事务替换
                if (curpos != precos) {
                    FragmentTransaction ft = fm.beginTransaction();
                    ft.replace(R.id.content_container, mFragments.get(curpos), FRAG_TAG[curpos]);  //打Tag
                    ft.commit();
                }
            }
        });
    }

    /**
     * 初始化ViewPager中的Fragments
     */
    private void initFragments(){
        //内容呈现页,新建Fragment添加到List
        mFragments = new ArrayList<>();
        for(int i = 0; i < 4; i++){
            Fragment frag;
            if(i == 2) {
                frag = new YituFragment();   //第3个页面是一个带WebView的ViewPager,承载图片和文字
            }else{
                frag = new ItemFragment();   //其他的都是RecylerView,承载标题
            }
            Bundle bundle = new Bundle();
            bundle.putInt("pos", i);
            frag.setArguments(bundle);
            mFragments.add(frag);
        }

        //添加第0个页面到frag_container中
        fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        ft.add(R.id.content_container, mFragments.get(0), FRAG_TAG[0]);  //打Tag
        ft.commit();
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState){
        super.onPostCreate(savedInstanceState);
        mToggle.syncState();  //状态同步
    }

    public void getUpdateArticles(){
        int type = Article.TYPE[curpos];
        client.getArticle(type, new SneezeJsonResponseHandler(this, type));
    }
}
