package com.example.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.example.datamodel.Article;

import java.util.List;

/**
 * Created by liuchun on 2015/12/12.
 */
public class MyViewPagerAdapter extends FragmentPagerAdapter {
    private List<Article> articles;

    public MyViewPagerAdapter(FragmentManager fm, List<Article> articles){
        super(fm);
        this.articles = articles;
    }

    @Override
    public Fragment getItem(int position) {
        DetailFragment fragment = new DetailFragment();
        Article article = articles.get(position);

        Bundle bundle = new Bundle();
        bundle.putParcelable("article", article);
        fragment.setArguments(bundle);

        return fragment;
    }

    @Override
    public int getCount() {
        return articles.size();
    }
}
