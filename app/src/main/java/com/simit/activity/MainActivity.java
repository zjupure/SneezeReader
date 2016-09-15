package com.simit.activity;


import android.content.Intent;
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
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.drawee.view.SimpleDraweeView;
import com.simit.common.Constants;
import com.simit.database.DbController;
import com.simit.fragment.ArticleFragment;
import com.simit.fragment.YituFragment;
import com.simit.database.Article;
import com.simit.service.UpdateService;
import com.simit.storage.SharedPreferenceUtils;
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
    private static final String TAG = "MainActivity";
    //Fragment　Tag
    public static final String[] FRAG_TAG = {"tugua", "lehuo", "yitu", "duanzi"};
    public static final int[] APP_TITLE = {R.string.title_tugua, R.string.title_lehuo,
                                R.string.title_yitu, R.string.title_duanzi};
    private static final int[] RADIO_BUTTON_ID = {R.id.tab_tugua, R.id.tab_lehuo, R.id.tab_yitu, R.id.tab_duanzi};
    /**
     * 界面组件
     */
    private DrawerLayout mDrawerLayout;
    private NavigationView mNavView;
    private ActionBarDrawerToggle mToggle;
    private RadioGroup mTabMenu;
    /**
     * NavigationView的headerLayout控件
     */
    private SimpleDraweeView mUserPhoto;
    private TextView mUserName;
    private TextView mUserSignature;
    /**
     * ViewPager的Fragment UI
     */
    public List<Fragment> mFragments;
    /**
     * 记录当前Fragment的position
     */
    private int curPos;
    /**
     * 记录Back按键时间
     */
    private long lastBackPressTime;
    /**
     * 微博sdk, 用户登陆使用
     */
    private AuthInfo mAuthInfo;
    private SsoHandler mSsoHandler;
    private Oauth2AccessToken mAccessToken;
    private UsersAPI mUsersAPI;

    /**
     * 记录用户登陆信息和状态状态
     */
    private String userName = "any";
    private boolean isLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        DbController.getInstance(this);
        //初始化Fragments
        initFragments(savedInstanceState);
        // 启动后台轮询service
        Intent intent = new Intent(this, UpdateService.class);
        startService(intent);
    }

    @Override
    protected int getLayoutId() {

        return R.layout.activity_main;
    }

    @Override
    protected void handleIntent(Intent intent) {
        super.handleIntent(intent);

        curPos = intent.getIntExtra("pos", 0);
    }

    @Override
    protected void initView(){
        // init the toolbar
        super.initView();
        setToolBarTitle(APP_TITLE[curPos]);
        if(getSupportActionBar() != null){
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        // DrawerLayout, Navigation View
        mDrawerLayout = (DrawerLayout)findViewById(R.id.drawer_layout);
        mNavView = (NavigationView)findViewById(R.id.nav_view);
        //设置mToggle
        mToggle = new ActionBarDrawerToggle(this, mDrawerLayout, mToolBar, R.string.app_name, R.string.app_name);
        mToggle.syncState();
        mDrawerLayout.addDrawerListener(mToggle);
        //
        if(mNavView != null){
            //为Navigation设置点击事件
            mNavView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
                   @Override
                   public boolean onNavigationItemSelected(MenuItem menuItem) {
                        // 菜单单击操作
                       navigationMenuClicked(menuItem);
                       return false;
                   }
               }
            );
            //查找Drawer header中的控件
            View headerView = mNavView.getHeaderView(0);
            mUserPhoto = (SimpleDraweeView) headerView.findViewById(R.id.user_photo);
            mUserName = (TextView) headerView.findViewById(R.id.user_name);
            mUserSignature = (TextView) headerView.findViewById(R.id.user_signature);
            //恢复mAccessToken
            isLogin = false;
            mAccessToken = AccessTokenKeeper.readAccessToken(this);
            if(mAccessToken.isSessionValid()){
                // Token有效,获取用户信息并显示
                mUsersAPI = new UsersAPI(this, Constants.WEIBO_APP_KEY, mAccessToken);
                long uid = Long.parseLong(mAccessToken.getUid());
                mUsersAPI.show(uid, mWeiboListener);
            }else {
                mUserPhoto.setOnClickListener(this);
                mUserName.setOnClickListener(this);
            }
        }
        //RadioButton
        mTabMenu = (RadioGroup)findViewById(R.id.tab_menu);
        //设置Tab监听
        mTabMenu.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                //底部菜单栏点击操作
                tabMenuChecked(checkedId);
            }
        });
        //选中当前项
        RadioButton button = (RadioButton) findViewById(RADIO_BUTTON_ID[curPos]);
        if(button != null){
            button.setChecked(true);
        }
    }

    /**
     * 初始化ViewPager中的Fragments
     * 根据bundle决定是否创建新的Fragment
     * @param bundle
     */
    private void initFragments(Bundle bundle){
        //内容呈现页,新建Fragment添加到List
        mFragments = new ArrayList<>();
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = null;
        if(bundle != null){
            //恢复栈中原有的Fragment
            mFragments.clear();
            for(int i = 0; i < 4; i++){
                Fragment frag = fm.findFragmentByTag(FRAG_TAG[i]);
                mFragments.add(frag);
            }
            curPos = bundle.getInt("curpos");
            //隐藏其他所有的fragment
            ft = fm.beginTransaction();
            for(int i = 0; i < 4; i++){
                if(i != curPos) {
                    //不等于当前的页面都隐藏起来
                    ft.hide(mFragments.get(i));
                }
            }
            //ft.show(mFragments.get(curPos));  //显示当前Fragment
            ft.commit();
        }else {
            //新建Fragment
            for(int i = 0; i < 4; i++){
                Bundle args = new Bundle();
                args.putInt("pos", i);

                Fragment frag;
                if(i == 2) {
                    frag = YituFragment.newInstance(args);   //第3个页面是一个带WebView的ViewPager,承载图片和文字
                }else{
                    frag = ArticleFragment.newInstance(args);   //其他的都是RecyclerView,承载标题
                }
                mFragments.add(frag);
            }
            //第一次需要add进去
            ft = fm.beginTransaction();
            for(int i = 0; i < 4; i++){
                ft.add(R.id.content_container, mFragments.get(i), FRAG_TAG[i]);
                if(i != curPos) {
                    //不等于当前的页面都隐藏起来
                    ft.hide(mFragments.get(i));
                }
            }
            //ft.show(mFragments.get(curPos));  // 显示当前Fragment
            ft.commit();
        }
    }

    /**
     * 导航栏菜单点击操作
     * @param menuItem
     */
    private void navigationMenuClicked(MenuItem menuItem){
        //切换对应的Fragment操作
        menuItem.setChecked(true);
        mDrawerLayout.closeDrawers();
        //根据菜单项跳转
        Intent intent;
        switch (menuItem.getItemId()) {
            case R.id.nav_theme:
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {

                        setCurrentTheme(mNightMode, true);
                    }
                }, 500);
                mNightMode = !mNightMode;
                mDrawerLayout.closeDrawers();
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
        int prePos = curPos;
        switch (checkedId) {
            case R.id.tab_tugua:
                curPos = Article.TUGUA;
                break;
            case R.id.tab_lehuo:
                curPos = Article.LEHUO;
                break;
            case R.id.tab_yitu:
                curPos = Article.YITU;
                break;
            case R.id.tab_duanzi:
                curPos = Article.DUANZI;
                break;
            default:
                break;
        }
        //开始事务替换
        if (curPos != prePos) {
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            // hide/show to save the fragment state
            ft.hide(mFragments.get(prePos));  // hide previous fragment
            ft.show(mFragments.get(curPos));  // show current fragment
            //ft.replace(R.id.content_container, mFragments.get(curPos), FRAG_TAG[curPos]);  //打Tag
            ft.commit();
        }

        setToolBarTitle(APP_TITLE[curPos]);
    }


    @Override
    protected void onPostCreate(Bundle savedInstanceState){
        super.onPostCreate(savedInstanceState);
        mToggle.syncState();  //状态同步
    }


    /**
     * 进入收藏页面
     * @param type
     */
    private void goFavorite(int type){
        Intent intent = new Intent(this, FavoriteActivity.class);
        intent.putExtra("curPos", type);
        startActivity(intent);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {

        outState.putInt("curpos", curPos);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // 预设定比恢复的时间少2s
        lastBackPressTime = System.currentTimeMillis() - 2000;
    }

    @Override
    public void onBackPressed() {
        long curBackPress = System.currentTimeMillis();
        // 2s
        if(curBackPress - lastBackPressTime >= 2000){
            Toast.makeText(this, R.string.toast_back, Toast.LENGTH_SHORT).show();
        }else{
            super.onBackPressed();
        }
        lastBackPressTime = curBackPress;
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.user_photo:
            case R.id.user_name:
                if(!isLogin){
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
        mAuthInfo = new AuthInfo(this, Constants.WEIBO_APP_KEY, Constants.WEIBO_REDIRECT_URL, Constants.WEIBO_SCOPE);
        mSsoHandler = new SsoHandler(this, mAuthInfo);
        // Web授权
        //mSsoHandler.authorizeWeb(new LoginWeiboAuthListener());
        // SSO授权
        //mSsoHandler.authorizeClientSso(new LoginWeiboAuthListener());
        // All in one
        // 此种授权方式会根据手机是否安装微博客户端来决定使用sso授权还是网页授权，
        // 如果安装有微博客户端 则调用微博客户端授权，否则调用Web页面方式授权
        try {
            mSsoHandler.authorize(new LoginWeiboAuthListener());
        }catch (Exception e){
            Log.e(TAG, "weibo login exception: " + e.getMessage());
        }
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
                userName = user.screen_name;
                isLogin = true;
                // 写入sp
                SharedPreferenceUtils.put(MainActivity.this, "userName", userName);
                SharedPreferenceUtils.put(MainActivity.this, "isLogin", isLogin);

            }
        }

        @Override
        public void onWeiboException(WeiboException e) {
            Toast.makeText(MainActivity.this, "微博登录失败", Toast.LENGTH_SHORT).show();
        }
    };

    /**
     * 实现微博授权接口
     */
    class LoginWeiboAuthListener implements WeiboAuthListener {
        @Override
        public void onComplete(Bundle bundle) {
            // 从Bundle中解析Token
            mAccessToken = Oauth2AccessToken.parseAccessToken(bundle);
            if(mAccessToken.isSessionValid()){
                // 保存Token到SharePreferences
                AccessTokenKeeper.writeAccessToken(MainActivity.this, mAccessToken);
                // 获取用户信息
                mUsersAPI = new UsersAPI(MainActivity.this, Constants.WEIBO_APP_KEY, mAccessToken);
                long uid = Long.parseLong(mAccessToken.getUid());
                mUsersAPI.show(uid, mWeiboListener);
            }else{
                //
            }
        }

        @Override
        public void onWeiboException(WeiboException e) {
            Toast.makeText(MainActivity.this, "微博登录失败", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onCancel() {
            Toast.makeText(MainActivity.this, "取消微博登录", Toast.LENGTH_SHORT).show();
        }
    }
}
