package com.example.sneezereader;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
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

import com.example.datamodel.Article;
import com.example.fragment.ItemFragment;
import com.example.fragment.YituFragment;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity{
    //Fragment　Tag
    public static final String[] FRAG_TAG = {"tugua", "lehuo", "yitu", "duanzi"};
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
    //网络状态监听
    private BroadcastReceiver connReceiver;
    //
    private boolean netWorkAvaible;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initView();

        connReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                ConnectivityManager cm = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
                NetworkInfo networkInfo = cm.getActiveNetworkInfo();

                if(networkInfo == null){
                    // 无网络链接
                    netWorkAvaible = false;
                    Toast.makeText(MainActivity.this, "网络连接已断开", Toast.LENGTH_SHORT).show();
                }else{
                    netWorkAvaible = true;
                }
            }
        };
        IntentFilter filter = new IntentFilter();
        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(connReceiver, filter);
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

                MenuItem item = topMenu.findItem(R.id.action_refresh);
                if (curpos == Article.YITU) {
                    item.setVisible(true);
                } else {
                    item.setVisible(false);
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
        topMenu = menu;
        getMenuInflater().inflate(R.menu.menu_main, menu);

        MenuItem item = menu.findItem(R.id.action_refresh);
        if(curpos == Article.YITU){
            item.setVisible(true);
        }else {
            item.setVisible(false);
        }

        return super.onCreateOptionsMenu(menu);
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
    protected void onDestroy() {
        super.onDestroy();
        if(connReceiver != null){
            unregisterReceiver(connReceiver);
        }
    }
}
