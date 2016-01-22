package com.simit.sneezereader;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.handmark.pulltorefresh.library.ILoadingLayout;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshRecyclerView;
import com.simit.database.DBManager;
import com.simit.datamodel.Article;
import com.simit.fragment.MyRecylcerAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by liuchun on 2016/1/17.
 */
public class FavoriteActivity extends BaseActivity {
    private static final int[] TOOLBAR_TITLE = {R.string.favorite_tugua, R.string.favorite_lehuo, R.string.favorite_yitu};
    private static final int THRESHOLD = 15;
    private static final int INITIAL_CMD = 0;
    private static final int REFRESH_CMD = 1;
    private static final int LOADMORE_CMD = 2;
    private static final int NO_MORE_DATA = 3;
    // component
    private PullToRefreshRecyclerView mRefreshView;  // RecyclerView wrapper
    private ILoadingLayout footer;
    private RecyclerView mRecyclerView;  // 列表
    private LinearLayoutManager mLayoutManager;
    private MyRecylcerAdapter mAdapter;   //适配器
    private FloatingActionButton mGoTopBtn;
    private int curpos = 0;  //收藏类型
    //
    private TextView mBgText;
    // RecycleView数据集
    private List<Article> mDataSet;
    private int limit = 30;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorite);
        //
        Intent intent = getIntent();
        int defValue = restoreFavoriteType();
        curpos = intent.getIntExtra("curpos", defValue);
        if(curpos != defValue){
            saveFavoriteType(curpos);
        }
        // 初始化界面
        initView();
    }

    @Override
    protected void initView() {
        super.initView();
        setToolBarTitle(TOOLBAR_TITLE[curpos]);
        // 初始化界面组件
        mBgText = (TextView) findViewById(R.id.favorite_text);
        // Floating action bar
        mGoTopBtn = (FloatingActionButton) findViewById(R.id.go_top_btn);
        // 为按钮设置监听事件
        mGoTopBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //自动滑动到顶部
                mRecyclerView.smoothScrollToPosition(0);
                //mGoTopBtn.setVisibility(View.GONE);
            }
        });

        // RecyclerView
        mRefreshView = (PullToRefreshRecyclerView) findViewById(R.id.favorite_list);
        mRecyclerView = mRefreshView.getRefreshableView();
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        //根据当前页面的位置,Resume方法中获取数据
        mDataSet = new ArrayList<>();
        //定义Adapter
        mAdapter = new MyRecylcerAdapter(mDataSet, Article.LEHUO);  //绑定数据集
        mRecyclerView.setAdapter(mAdapter);   //设置适配器
        //从数据库加载数据
        //loadFromDatabase(INITIAL_CMD, limit);
        //设置item点击事件
        mAdapter.setOnItemClickListener(new MyRecylcerAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                // 点击跳转
                Intent intent = new Intent(FavoriteActivity.this, FavoriteDetailActivity.class);
                Bundle bundle = new Bundle();
                Article article = mDataSet.get(position);
                bundle.putParcelable("article", article);
                intent.putExtra("detail", bundle);
                intent.putExtra("position", position);
                startActivity(intent);
            }

            @Override
            public void onItemLongClick(View view, int position) {
                //不处理,段子没有收藏功能
            }
        });

        // 为RecyclerView添加滑动监听
        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                int firstVisibleItem = mLayoutManager.findFirstVisibleItemPosition();
                if (firstVisibleItem > THRESHOLD) {
                    mGoTopBtn.setVisibility(View.VISIBLE);
                } else {
                    mGoTopBtn.setVisibility(View.GONE);
                }
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                int firstVisibleItem = mLayoutManager.findFirstVisibleItemPosition();
                if (firstVisibleItem > THRESHOLD) {
                    mGoTopBtn.setVisibility(View.VISIBLE);
                } else {
                    mGoTopBtn.setVisibility(View.GONE);
                }
            }
        });

        // 刷新监听
        footer = mRefreshView.getLoadingLayoutProxy(false, true);
        footer.setPullLabel(getString(R.string.pull_to_load_pull_label));
        footer.setReleaseLabel(getString(R.string.pull_to_load_release_label));
        footer.setRefreshingLabel(getString(R.string.pull_to_load_refreshing_label));
        mRefreshView.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener<RecyclerView>() {
            @Override
            public void onRefresh(PullToRefreshBase<RecyclerView> refreshView) {
                //加载更多
                if(mRefreshView.isFooterShown()){
                    // 从数据库加载数据
                    loadFromDatabase(LOADMORE_CMD, limit + 10);
                }else {
                    mRefreshView.onRefreshComplete();
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        //根据当前页面的位置,从数据库获取数据
        loadFromDatabase(INITIAL_CMD, limit);
    }

    /**
     * 从数据库异步加载数据
     * @param cmd
     * @param num
     */
    private void loadFromDatabase(int cmd, int num){
        Thread thread = new LoadTask(cmd, num);
        thread.start();
    }

    private void saveFavoriteType(int type){
        SharedPreferences preferences = getSharedPreferences("config", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();

        editor.putInt("curpos", type);
        editor.commit();
    }

    private int restoreFavoriteType(){
        SharedPreferences preferences = getSharedPreferences("config", Context.MODE_PRIVATE);

        int type = preferences.getInt("curpos", 0);
        return type;
    }

    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case INITIAL_CMD:
                    mAdapter.notifyDataSetChanged();
                    break;
                case LOADMORE_CMD:
                    mAdapter.notifyDataSetChanged();
                    mRefreshView.onRefreshComplete();
                    break;
                case NO_MORE_DATA:
                    mRefreshView.onRefreshComplete();
                    Toast.makeText(FavoriteActivity.this, "没有更多数据了", Toast.LENGTH_SHORT).show();
                    break;
                default:break;
            }
            // 如果页面没有数据则显示空白提示页面
            if(mDataSet.size() > 0){
                mBgText.setVisibility(View.GONE);
            }else{
                mBgText.setVisibility(View.VISIBLE);
            }
        }
    };
    // 后台加载数据
    public class LoadTask extends Thread{
        private int msgId;
        private int num;
        public LoadTask(int msgId, int num){
            this.msgId = msgId;
            this.num = num;
        }
        @Override
        public void run() {
            DBManager dbManager = DBManager.getInstance(FavoriteActivity.this);
            SneezeApplication app = (SneezeApplication) getApplication();
            List<Article> articles = dbManager.getFavorites(curpos, num, app.getUsername());

            if(msgId == INITIAL_CMD || msgId == REFRESH_CMD){
                mDataSet.clear();
                for(Article article : articles){
                    mDataSet.add(article);
                }
                handler.sendEmptyMessage(msgId);
                return;
            }

            // 查询到更多的数据
            if(articles.size() > mDataSet.size()){
                mDataSet.clear();
                for(Article article : articles){
                    mDataSet.add(article);
                }
                limit = mDataSet.size();
                handler.sendEmptyMessage(msgId);
            }else {
                handler.sendEmptyMessage(NO_MORE_DATA);
            }
        }
    }
}
