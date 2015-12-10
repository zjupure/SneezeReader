package com.example.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.datamodel.Article;
import com.example.datamodel.DataManager;
import com.example.sneezereader.R;
import com.example.sneezereader.DetailActivity;

import java.util.List;

/**
 * Created by liuchun on 2015/7/16.
 */
public class ItemFragment extends Fragment {

    private static final int IDLE = 1;
    private static final int PULL_TO_REFRESH = 2;
    private static final int RELEASE_TO_REFRESH = 3;
    private static final int REFRESHING = 4;

    private View rootView;  //缓存根View,防止重复渲染
    // Refresh Header
    private RelativeLayout mRefreshView;
    private ProgressBar mRefreshProgressBar;
    private ImageView mRefreshImageView;
    private TextView mRefreshTextView;
    private TextView mLastUpdatedTextView;
    private ViewGroup.MarginLayoutParams headerLayoutParams;

    private RecyclerView mRecyclerView;   //列表控件
    private LinearLayoutManager mLayoutManager;
    private MyRecylcerAdapter mAdapter;   //适配器
    private int curpos;    //当前页面标识
    // RecycleView数据集
    private List<Article> mDataSet;

    private int mCurrentScrollState;
    private int mRefreshState;

    private RotateAnimation mFlipAnimation;
    private RotateAnimation mReverseFlipAnimation;

    private int mRefreshViewHeight;
    private int mRefreshOriginalTopPadding;
    private int mLastMotionY;

    private boolean mBounceHack;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if(rootView == null){
            rootView = inflater.inflate(R.layout.main_entry_page, container, false);
        }
        //缓存的rootView已经被加载过parent,需要移除
        ViewGroup parent = (ViewGroup)rootView.getParent();
        if(parent != null){
            parent.removeView(rootView);
        }

        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        curpos = getArguments().getInt("pos");
        //初始化界面View
        initRecyclerView();
        initSwipeRefreshView();
    }

    public void initRecyclerView(){

        mRecyclerView = (RecyclerView)rootView.findViewById(R.id.tugua_list);
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());

        //根据当前页面的位置,从数据管理器中获取数据
        mDataSet = DataManager.getInstance().getData(curpos);
        //定义Adapter
        mAdapter = new MyRecylcerAdapter(mDataSet, curpos);  //绑定数据集
        mRecyclerView.setAdapter(mAdapter);   //设置适配器

        mRecyclerView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                final int y = (int)event.getY();

                switch (event.getAction()){
                    case MotionEvent.ACTION_UP:
                        break;
                    case MotionEvent.ACTION_DOWN:
                        mLastMotionY = y;
                        break;
                    case MotionEvent.ACTION_MOVE:
                        adjustRefreshHeight(event);
                        break;
                }

                return false;
            }
        });

        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {

            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                mCurrentScrollState = newState;

                if(mCurrentScrollState == RecyclerView.SCROLL_STATE_IDLE){
                    mBounceHack = false;
                }
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                int firstVisibleItem = mLayoutManager.findFirstVisibleItemPosition();
                // When the refresh view is completely visible, change the text to say
                // "Release to refresh..." and flip the arrow drawable
                /*
                if(mCurrentScrollState == RecyclerView.SCROLL_STATE_DRAGGING
                        && mRefreshState != REFRESHING){

                    if(firstVisibleItem == 0){
                        mRefreshImageView.setVisibility(View.VISIBLE);
                        if((mRefreshView.getBottom() >= mRefreshViewHeight + 20
                                || mRefreshView.getTop() >= 0)
                            && mRefreshState != RELEASE_TO_REFRESH){
                            mRefreshTextView.setText(R.string.pull_to_refresh_release_label);
                            mRefreshImageView.clearAnimation();
                            mRefreshImageView.startAnimation(mFlipAnimation);
                            mRefreshState = RELEASE_TO_REFRESH;
                        }else if(mRefreshView.getBottom() < mRefreshViewHeight + 20
                                && mRefreshState != PULL_TO_REFRESH){
                            mRefreshTextView.setText(R.string.pull_to_refresh_pull_label);
                            if(mRefreshState != IDLE){
                                mRefreshImageView.clearAnimation();
                                mRefreshImageView.startAnimation(mReverseFlipAnimation);
                            }
                            mRefreshState = PULL_TO_REFRESH;
                        }
                    }
                }*/
            }
        });

        //设置item点击事件
        mAdapter.setOnItemClickListener(new ItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                // 图卦和乐活页面响应点击事件,段子页面无效
                if (curpos > 1) {
                    return;
                }

                Intent intent = new Intent(getActivity(), DetailActivity.class);
                Bundle bundle = new Bundle();
                Article article = mDataSet.get(position);
                bundle.putParcelable("article", article);
                intent.putExtra("detail", bundle);
                intent.putExtra("position", position);
                startActivity(intent);
            }
        });
    }


    private void initSwipeRefreshView(){
        // Load all of the animations
        mFlipAnimation = new RotateAnimation(0,-180, RotateAnimation.RELATIVE_TO_SELF, 0.5f,
                RotateAnimation.RELATIVE_TO_SELF, 0.5f);
        mFlipAnimation.setInterpolator(new LinearInterpolator());
        mFlipAnimation.setDuration(250);
        mFlipAnimation.setFillAfter(true);

        mReverseFlipAnimation = new RotateAnimation(-180, 0, RotateAnimation.RELATIVE_TO_SELF, 0.5f,
                RotateAnimation.RELATIVE_TO_SELF, 0.5f);
        mReverseFlipAnimation.setInterpolator(new LinearInterpolator());
        mReverseFlipAnimation.setDuration(250);
        mReverseFlipAnimation.setFillAfter(true);

        mRefreshView = (RelativeLayout) rootView.findViewById(R.id.pull_to_refresh_header);
        mRefreshTextView = (TextView) rootView.findViewById(R.id.pull_to_refresh_text);
        mLastUpdatedTextView = (TextView) rootView.findViewById(R.id.pull_to_refresh_updated_at);
        mRefreshProgressBar = (ProgressBar) rootView.findViewById(R.id.pull_to_refresh_progress);
        mRefreshImageView = (ImageView) rootView.findViewById(R.id.pull_to_refresh_image);

        mRefreshState = IDLE;
        mRefreshOriginalTopPadding = mRefreshView.getPaddingTop();
        mRefreshViewHeight = mRefreshView.getHeight();
        // hide the header
        headerLayoutParams = (ViewGroup.MarginLayoutParams) mRefreshView.getLayoutParams();
        headerLayoutParams.topMargin = -mRefreshViewHeight;
    }


    private void adjustRefreshHeight(MotionEvent event){

    }
    /**
     * Item单击接口
     */
    public  interface ItemClickListener {
        public void onItemClick(View view, int position);
    }
}
