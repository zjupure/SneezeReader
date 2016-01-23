package com.simit.sneezereader;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.drawee.view.SimpleDraweeView;
import com.simit.database.DBManager;
import com.simit.datamodel.Article;
import com.simit.datamodel.DataManager;
import com.simit.fragment.ItemFragment;
import com.simit.fragment.YituFragment;
import com.sina.weibo.sdk.AccessTokenKeeper;
import com.sina.weibo.sdk.auth.AuthInfo;
import com.sina.weibo.sdk.auth.Oauth2AccessToken;
import com.sina.weibo.sdk.auth.WeiboAuthListener;
import com.sina.weibo.sdk.auth.sso.SsoHandler;
import com.sina.weibo.sdk.exception.WeiboException;
import com.sina.weibo.sdk.net.RequestListener;
import com.sina.weibo.sdk.openapi.UsersAPI;
import com.sina.weibo.sdk.openapi.models.User;


import java.util.ArrayList;
import java.util.List;

public class MainActivity extends BaseActivity implements View.OnClickListener{
    //Fragment　Tag
    public static final String[] FRAG_TAG = {"tugua", "lehuo", "yitu", "duanzi"};
    public static final int[] APP_TITLE = {R.string.title_tugua, R.string.title_lehuo,
                                R.string.title_yitu, R.string.title_duanzi};
    private static final int[] RADIO_BUTTON_ID = {R.id.tab_tugua, R.id.tab_lehuo, R.id.tab_yitu, R.id.tab_duanzi};
    //界面组件
    private DrawerLayout mDrawerLayout;
    private NavigationView mNavView;
    private ActionBarDrawerToggle mToggle;
    private RadioGroup mTabMenu;
    // Drawer Components
    private SimpleDraweeView mUserPhoto;
    private TextView mUserName;
    private TextView mUserSignature;
    // Menu
    private Menu topMenu;
    // Fragment UI
    public List<Fragment> mFragments;
    private FragmentManager fm;
    //基本信息
    private int curpos;
    // Back按键时间
    private long lastBackPress;
    // application context
    private SneezeApplication app;
    private boolean nightMode;
    // weibo sdk
    private AuthInfo mAuthInfo;
    private SsoHandler mSsoHandler;
    private Oauth2AccessToken mAccessToken;
    private UsersAPI mUsersAPI;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Intent intent = getIntent();
        curpos = intent.getIntExtra("pos", 0);
        app = (SneezeApplication) getApplication();
        nightMode = app.getNightMode();
        // 初始化界面
        initView();
        // start service
        intent = new Intent(this, UpdateService.class);
        startService(intent);
    }

    @Override
    protected void initView(){
        // init the toolbar
        super.initView();
        setToolBarTitle(APP_TITLE[curpos]);
        // DrawerLayout, Navigation View
        mDrawerLayout = (DrawerLayout)findViewById(R.id.drawer_layout);
        mNavView = (NavigationView)findViewById(R.id.nav_view);
        //设置mToggle
        mToggle = new ActionBarDrawerToggle(this, mDrawerLayout, getToolBar(), R.string.app_name, R.string.app_name);
        mToggle.syncState();
        mDrawerLayout.setDrawerListener(mToggle);
        // set up toolbar
        //mToolBar.setNavigationIcon(R.drawable.user_logo);
        if(mNavView != null){
            //为Navigation设置点击事件
            mNavView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
                   @Override
                   public boolean onNavigationItemSelected(MenuItem menuItem) {
                        // 菜单单击操作
                       navigationMenuChecked(menuItem);
                       return false;
                   }
               }
            );
            //查找Drawer header中的控件
            View headerView = mNavView.getChildAt(0);
            mUserPhoto = (SimpleDraweeView) headerView.findViewById(R.id.user_photo);
            mUserName = (TextView) headerView.findViewById(R.id.user_name);
            mUserSignature = (TextView) headerView.findViewById(R.id.user_signature);
            //恢复mAccessToken
            mAccessToken = AccessTokenKeeper.readAccessToken(this);
            if(mAccessToken.isSessionValid()){
                //app.setLoginState(true);
                // Token有效,获取用户信息并显示
                mUsersAPI = new UsersAPI(this, Constant.WEIBO_APP_KEY, mAccessToken);
                long uid = Long.parseLong(mAccessToken.getUid());
                mUsersAPI.show(uid, mWeiboListener);
            }else {
                app.setLoginState(false);
                mUserPhoto.setOnClickListener(this);
                mUserName.setOnClickListener(this);
            }
        }
        //初始化Fragment
        initFragments();
        //RadioButton
        mTabMenu = (RadioGroup)findViewById(R.id.tab_menu);
        if(app.getNightMode()){
            mTabMenu.setBackgroundResource(R.drawable.tab_background_night);
        }else {
            mTabMenu.setBackgroundResource(R.drawable.tab_background);
        }
        //设置Tab监听
        mTabMenu.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                //底部菜单栏点击操作
                tabMenuChecked(checkedId);
            }
        });
        //选中当前项
        RadioButton button = (RadioButton) findViewById(RADIO_BUTTON_ID[curpos]);
        button.setChecked(true);
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
        for(int i = 0; i < 4; i++){
            ft.add(R.id.content_container, mFragments.get(i), FRAG_TAG[i]);  //打Tag
            ft.hide(mFragments.get(i));  // hide all fragments
        }
        ft.show(mFragments.get(curpos));  // show first fragments
        ft.commit();
    }

    /**
     * 导航栏菜单点击操作
     * @param menuItem
     */
    private void navigationMenuChecked(MenuItem menuItem){
        //切换对应的Fragment操作
        menuItem.setChecked(true);
        mDrawerLayout.closeDrawers();
        //根据菜单项跳转
        Intent intent;
        switch (menuItem.getItemId()) {
            case R.id.nav_theme:
                app.setNightMode(!nightMode);
                updateTheme();
                break;
            case R.id.nav_setting:
                intent = new Intent(MainActivity.this, SettingActivity.class);
                startActivity(intent);
                break;
            case R.id.nav_about:
                intent = new Intent(MainActivity.this, AboutActivity.class);
                startActivity(intent);
                break;
            case R.id.nav_tugua:
                goFavorite(Article.TUGUA);
                break;
            case R.id.nav_lehuo:
                goFavorite(Article.LEHUO);
                break;
            case R.id.nav_yitu:
                goFavorite(Article.YITU);
                break;
            default:
                break;
        }
    }

    /**
     * 底部Tab按钮点击操作
     * @param checkedId
     */
    private void tabMenuChecked(int checkedId){
        int prepos = curpos;
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
        if (curpos != prepos) {
            FragmentTransaction ft = fm.beginTransaction();
            // hide/show to save the fragment state
            ft.hide(mFragments.get(prepos));  // hide previous fragment
            ft.show(mFragments.get(curpos));  // show current fragment
            //ft.replace(R.id.content_container, mFragments.get(curpos), FRAG_TAG[curpos]);  //打Tag
            ft.commit();
        }

        setToolBarTitle(APP_TITLE[curpos]);
        setUpMenu();
    }

    /**
     * 进入收藏页面
     * @param type
     */
    private void goFavorite(int type){
        Intent intent = new Intent(this, FavoriteActivity.class);
        intent.putExtra("curpos", type);
        startActivity(intent);
    }


    @Override
    protected void updateTheme() {
        //
        Handler handler =  new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                // finish itself
                overridePendingTransition(0, 0);
                finish();
                // restart itself
                Intent intent = getIntent();
                intent.putExtra("pos", curpos);
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                overridePendingTransition(0, 0);
                startActivity(intent);
            }
        }, 200);
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
        // set up toolbar
        //mToolBar.setNavigationIcon(R.drawable.user_logo);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // 渲染菜单
        getMenuInflater().inflate(R.menu.menu_main, menu);
        topMenu = menu;
        setUpMenu();
        Log.d("MainActivity", "OptionMenu created");
        return super.onCreateOptionsMenu(menu);
    }

    private void setUpMenu(){
        if(topMenu == null){
            return;  // Menu has not be loaded
        }
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

    /**
     * 根据id查找item
     * @return
     */
    public MenuItem findMenuItem(int resId){
        MenuItem item = null;
        if(topMenu != null){
            item = topMenu.findItem(resId);
        }
        return item;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // 刷新事件,回调Fragment的函数
        int resId = item.getItemId();
        switch (item.getItemId()){
            case R.id.action_refresh:
            case R.id.action_favorite:
            case R.id.action_share:
                updateYitu(resId);
                break;
            default:break;
        }

        return super.onOptionsItemSelected(item);
    }

    private void updateYitu(int resId){
        FragmentManager fm = getSupportFragmentManager();
        Fragment fragment = fm.findFragmentByTag(FRAG_TAG[curpos]);
        if(fragment instanceof YituFragment){
            YituFragment yituFragment = (YituFragment)fragment;
            // 根据不同的id回调不同的响应函数
            if(resId == R.id.action_refresh){
                yituFragment.refreshCurrentWebView();
            }else if(resId == R.id.action_favorite){
                yituFragment.favoriteCurrentPage();
            }else if(resId == R.id.action_share){
                yituFragment.shareCurrentPage();
            }
        }
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


    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.user_photo:
            case R.id.user_name:
                if(!app.getUserLogin()){
                    weiboLogin();
                }
                break;
            default:break;
        }
    }

    /**
     * 微博登陆
     */
    private void weiboLogin(){
        mAuthInfo = new AuthInfo(this, Constant.WEIBO_APP_KEY, Constant.WEIBO_REDIRECT_URL, Constant.WEIBO_SCOPE);
        mSsoHandler = new SsoHandler(this, mAuthInfo);
        // Web授权
        //mSsoHandler.authorizeWeb(new MyWeiboAuthListener());
        // SSO授权
        //mSsoHandler.authorizeClientSso(new MyWeiboAuthListener());
        // All in one
        //此种授权方式会根据手机是否安装微博客户端来决定使用sso授权还是网页授权，
        // 如果安装有微博客户端 则调用微博客户端授权，否则调用Web页面方式授权
        mSsoHandler.authorize(new MyWeiboAuthListener());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (mSsoHandler != null) {
            mSsoHandler.authorizeCallBack(requestCode, resultCode, data);
        }
    }

    /**
     * 异步回调接口,处理用户信息
     */
    private RequestListener mWeiboListener = new RequestListener() {
        @Override
        public void onComplete(String response) {
            if(!TextUtils.isEmpty(response)){
                // 调用User#parse将JSON串解析成User对象
                User user = User.parse(response);
                // 设置信息
                mUserName.setText(user.screen_name);
                mUserSignature.setVisibility(View.VISIBLE);
                mUserSignature.setText(user.description);
                Uri uri = Uri.parse(user.avatar_large);
                mUserPhoto.setImageURI(uri);
                //
                app.setUsername(user.screen_name);
                app.setLoginState(true);
            }
        }

        @Override
        public void onWeiboException(WeiboException e) {

        }
    };

    /**
     * 实现微博授权接口
     */
    class MyWeiboAuthListener implements WeiboAuthListener {
        @Override
        public void onComplete(Bundle bundle) {
            // 从Bundle中解析Token
            mAccessToken = Oauth2AccessToken.parseAccessToken(bundle);
            if(mAccessToken.isSessionValid()){
                // 保存Token到SharePreferences
                AccessTokenKeeper.writeAccessToken(MainActivity.this, mAccessToken);
                // 获取用户信息
                mUsersAPI = new UsersAPI(MainActivity.this, Constant.WEIBO_APP_KEY, mAccessToken);
                long uid = Long.parseLong(mAccessToken.getUid());
                mUsersAPI.show(uid, mWeiboListener);
            }else{
                //
            }
        }

        @Override
        public void onWeiboException(WeiboException e) {

        }

        @Override
        public void onCancel() {

        }
    }
}
