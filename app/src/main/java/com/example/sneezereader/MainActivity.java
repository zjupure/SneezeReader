package com.example.sneezereader;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.example.fragment.ItemFragment;
import com.example.fragment.YituFragment;
import com.example.liuchun.sneezereader.R;
import com.example.network.SneezeClient;

import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity{
    //Fragment　Tag
    public static final String[] FRAG_TAG = {"tugua", "lehuo", "yitu", "duanzi"};
    //界面组件
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mToggle;
    private Toolbar mToolBar;
    private TextView mTitle;
    public List<Fragment> mFragments;
    private FragmentManager fm;
    private RadioGroup mTabMenu;
    //基本信息
    private int curpos = 0;
    private int[] pagetitle = {R.string.title_tugua, R.string.title_wenzhang, R.string.title_yitu, R.string.title_duanzi};
    //
    private  SneezeClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initView();
    }

    /**
     * 初始化界面
     */
    public void initView(){
        //渲染主页面
        FrameLayout mContainer = (FrameLayout)findViewById(R.id.content_container);
        View view = View.inflate(this, R.layout.mainpage, null);
        mContainer.addView(view);

        //查找相关控件
        mToolBar = (Toolbar)findViewById(R.id.toolbar);
        if(mToolBar != null){
            setSupportActionBar(mToolBar);
            mToolBar.setNavigationIcon(R.drawable.user_logo);
        }

        //页面title
        mTitle = (TextView)findViewById(R.id.pagetitle);
        //内容呈现页,新建Fragment添加到List
        mFragments = new ArrayList<Fragment>();
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
        ft.add(R.id.frag_container, mFragments.get(0), FRAG_TAG[0]);  //打Tag
        ft.commit();

        //RadioButton
        mTabMenu = (RadioGroup)findViewById(R.id.tab_menu);
        //设置Tab监听
        mTabMenu.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                int precos = curpos;
                switch (checkedId){
                    case R.id.tab_tugua:
                        curpos = 0;
                        break;
                    case R.id.tab_wenzhang:
                        curpos = 1;
                        break;
                    case R.id.tab_yitu:
                        curpos = 2;
                        break;
                    case R.id.tab_duanzi:
                        curpos = 3;
                        break;
                    default:break;
                }
                //开始事务替换
                if(curpos != precos){
                    mTitle.setText(getString(pagetitle[curpos]));
                    FragmentTransaction ft = fm.beginTransaction();
                    ft.replace(R.id.frag_container, mFragments.get(curpos), FRAG_TAG[curpos]);  //打Tag
                    ft.commit();
                }
            }
        });

        mDrawerLayout = (DrawerLayout)findViewById(R.id.drawer_layout);
        NavigationView mNavView = (NavigationView)findViewById(R.id.nav_view);

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

    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState){
        super.onPostCreate(savedInstanceState);
        mToggle.syncState();  //状态同步
    }

}
