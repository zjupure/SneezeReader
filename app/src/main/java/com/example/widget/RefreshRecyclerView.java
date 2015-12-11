package com.example.widget;

import android.content.Context;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.sneezereader.R;


/**
 * Created by liuchun on 2015/12/11.
 */
public class RefreshRecyclerView extends LinearLayout {

    private static final int REFRESH_FINISHED = 0;
    private static final int PULL_TO_REFRESH = 1;
    private static final int RELEASE_TO_REFRESH = 2;
    private static final int REFRESHING = 3;

    private static final int SCROLL_SPEED = -20;

    // components
    private RelativeLayout mRefreshView;
    private ProgressBar mRefreshProgress;
    private ImageView mRefreshImage;
    private TextView mRefreshText;
    private TextView mLastUpdateText;

    private RecyclerView mRecyclerView;
    private LinearLayoutManager mLayoutManager;
    private RecyclerView.Adapter mAdapter;

    private MarginLayoutParams headerLayoutParams;

    private int mCurrentScrollState;
    private int mRefreshState = REFRESH_FINISHED;

    private RotateAnimation mFilpAnimation;
    private RotateAnimation mReverseFlipAnimation;

    private int mRefreshViewHeight;
    private float mLastMotionY;

    public RefreshRecyclerView(Context context){
        this(context, null);
    }

    public RefreshRecyclerView(Context context, AttributeSet attrs){
        super(context, attrs);

        LayoutInflater mInflater = LayoutInflater.from(context);
        mRefreshView = (RelativeLayout) mInflater.inflate(R.layout.pull_to_refresh_header, this, false);
        mRefreshProgress = (ProgressBar) mRefreshView.findViewById(R.id.pull_to_refresh_progress);
        mRefreshImage = (ImageView) mRefreshView.findViewById(R.id.pull_to_refresh_image);
        mRefreshText = (TextView) mRefreshView.findViewById(R.id.pull_to_refresh_text);
        mLastUpdateText = (TextView) mRefreshView.findViewById(R.id.pull_to_refresh_updated_at);

        mRefreshImage.setMinimumHeight(50);

        mRecyclerView = new RecyclerView(context);
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(context);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());

        addView(mRefreshView, 0);
        addView(mRecyclerView, 1);

        measureView(mRefreshView);
        mRefreshViewHeight = mRefreshView.getMeasuredHeight();
        headerLayoutParams = (MarginLayoutParams) mRefreshView.getLayoutParams();
        headerLayoutParams.topMargin = -mRefreshViewHeight;

        Log.d("RefreshRecyclerView", "Measured Height: " + mRefreshViewHeight);
    }

    public void setAdapter(RecyclerView.Adapter adapter){
        mAdapter = adapter;
        mRecyclerView.setAdapter(adapter);
    }

    private void measureView(View child){
        ViewGroup.LayoutParams lp = child.getLayoutParams();
        if(lp == null){
            lp = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
        }

        int childWidthSpec = ViewGroup.getChildMeasureSpec(0, 0, lp.width);
        int childHeightSpec;
        int lpHeigth = lp.height;
        if(lpHeigth > 0){
            childHeightSpec = MeasureSpec.makeMeasureSpec(lpHeigth, MeasureSpec.EXACTLY);
        }else {
            childHeightSpec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
        }

        child.measure(childWidthSpec, childHeightSpec);
    }
}

