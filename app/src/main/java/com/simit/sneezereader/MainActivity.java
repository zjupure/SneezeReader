package com.simit.sneezereader;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.simit.datamodel.Article;
import com.simit.fragment.ItemFragment;
import com.simit.fragment.YituFragment;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity{
    //Fragment　Tag
    public static final String[] FRAG_TAG = {"tugua", "lehuo", "yitu", "duanzi"};
    public static final int[] APP_TITLE = {R.string.title_tugua, R.string.title_lehuo,
                                R.string.title_yitu, R.string.title_duanzi};
    //界面组件
    private DrawerLayout mDrawerLayout;
    private NavigationView mNavView;
    private ActionBarDrawerToggle mToggle;
    private Toolbar mToolBar;
    private RadioGroup mTabMenu;
    // Menu
    private Menu topMenu;
    // Fragment UI
    public List<Fragment> mFragments;
    private FragmentManager fm;
    //基本信息
    private int curpos = 0;
    // Back按键时间
    private long lastBackPress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //lastBackPress = System.currentTimeMillis();
        // 初始化界面
        initView();
        // start service
        Intent intent = new Intent(this, UpdateService.class);
        startService(intent);
    }

    /**
     * 初始化界面
     */
    private void initView(){
        // ToolBar
        mToolBar = (Toolbar)findViewById(R.id.toolbar);
        //mToolBar.setTitle(R.string.app_title);
        mToolBar.setTitle(APP_TITLE[curpos]);
        setSupportActionBar(mToolBar);
        // set up toolbar
        mToolBar.setNavigationIcon(R.drawable.user_logo);
        // DrawerLayout, Navigation View
        mDrawerLayout = (DrawerLayout)findViewById(R.id.drawer_layout);
        mNavView = (NavigationView)findViewById(R.id.nav_view);
        //设置mToggle
        mToggle = new ActionBarDrawerToggle(this, mDrawerLayout, mToolBar, R.string.app_name, R.string.app_name);
        mToggle.syncState();
        mDrawerLayout.setDrawerListener(mToggle);
        //
        if(mNavView != null){

            mNavView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
                   @Override
                   public boolean onNavigationItemSelected(MenuItem menuItem) {
                       //切换对应的Fragment操作
                       menuItem.setCheckable(true);
                       mDrawerLayout.closeDrawers();

                       //根据菜单项跳转
                       Intent intent;
                       switch (menuItem.getItemId()) {
                           case R.id.nav_theme:
                               break;
                           case R.id.nav_setting:
                               intent = new Intent(MainActivity.this, SettingActivity.class);
                               startActivity(intent);
                               break;
                           case R.id.nav_about:
                               intent = new Intent(MainActivity.this, AboutActivity.class);
                               startActivity(intent);
                               break;
                           default:
                               break;
                       }

                       return false;
                   }
               }
            );

        }
        //
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
                        curpos = Article.TUGUA;
                        break;
                    case R.id.tab_lehuo:
                        curpos = Article.LEHUO;
                        break;
                    case R.id.tab_yitu:
                        curpos = Article.YITU;
                        break;
                    case R.id.tab_duanzi:
                        curpos = Article.DUANZI;
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

                mToolBar.setTitle(APP_TITLE[curpos]);
                setUpMenu();
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

    public static void saveLastUpdated(Activity activity, int curpos,  String lastUpdated){
        SharedPreferences preferences = activity.getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();

        editor.putString("lastUpdated" + curpos, lastUpdated);
        editor.commit();
    }

    public static String restoreLastUpdated(Activity activity, int curpos){
        SharedPreferences preferences = activity.getPreferences(Context.MODE_PRIVATE);

        String lastUpdated = preferences.getString("lastUpdated" + curpos,
                activity.getString(R.string.pull_to_refresh_last_update));

        return lastUpdated;
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState){
        super.onPostCreate(savedInstanceState);
        mToggle.syncState();  //状态同步
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // 渲染菜单
        getMenuInflater().inflate(R.menu.menu_main, menu);
        topMenu = menu;
        setUpMenu();

        return super.onCreateOptionsMenu(menu);
    }

    private void setUpMenu(){

        // 根据当前状态显示或隐藏菜单
        MenuItem refresh = topMenu.findItem(R.id.action_refresh);
        MenuItem favorite = topMenu.findItem(R.id.action_favorite);
        MenuItem share = topMenu.findItem(R.id.action_share);

        if (curpos == Article.YITU) {
            refresh.setVisible(true);
            favorite.setVisible(true);
            share.setVisible(true);
        } else {
            refresh.setVisible(false);
            favorite.setVisible(false);
            share.setVisible(false);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // 刷新事件,回调Fragment的函数
        if(item.getItemId() == R.id.action_refresh){
            FragmentManager fm = getSupportFragmentManager();
            Fragment fragment = fm.findFragmentByTag(FRAG_TAG[curpos]);
            if(fragment instanceof YituFragment){
                YituFragment yituFragment = (YituFragment)fragment;
                yituFragment.refreshCurrentWebView();
            }
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // 预设定比恢复的时间少2s
        lastBackPress = System.currentTimeMillis() - 2000;
    }

    @Override
    public void onBackPressed() {
        long curBackPress = System.currentTimeMillis();
        // 2s
        if(curBackPress - lastBackPress >= 2000){
            Toast.makeText(this, R.string.toast_back, Toast.LENGTH_SHORT).show();
        }else{
            super.onBackPressed();
        }
        lastBackPress = curBackPress;
    }
}
