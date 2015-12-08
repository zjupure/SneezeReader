package com.example.sneezereader;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.webkit.WebView;

import com.example.datamodel.Article;
import com.example.liuchun.sneezereader.R;


/**
 * Created by liuchun on 2015/12/6.
 */
public class DetailActivity extends AppCompatActivity {
    private Toolbar mToolBar;
    private WebView mWebView;

    private Article article;
    private int position;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.article_detail_layout);

        Intent intent = getIntent();
        Bundle bundle = intent.getBundleExtra("detail");
        article = bundle.getParcelable("article");
        position = intent.getIntExtra("position", 0);

        initView();
    }


    private void initView(){
        // ToolBar
        mToolBar = (Toolbar)findViewById(R.id.toolbar);
        //mToolBar.setTitle(R.string.app_title);
        setSupportActionBar(mToolBar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // mWebView
        mWebView = (WebView) findViewById(R.id.article_detail_container);
    }
}
