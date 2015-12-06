package com.example.sneezereader;

import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;

import com.example.liuchun.sneezereader.R;

/**
 * Created by liuchun on 2015/12/6.
 */
public class DetailActivity extends AppCompatActivity {
    private ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.article_detail_layout);


    }
}
