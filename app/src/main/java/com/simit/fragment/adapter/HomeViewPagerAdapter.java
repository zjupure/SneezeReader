package com.simit.fragment.adapter;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.simit.fragment.DetailFragment;
import com.simit.model.Article;

import java.util.HashMap;
import java.util.List;

/**
 * Created by liuchun on 2015/12/12.
 */
public class HomeViewPagerAdapter extends FragmentPagerAdapter {
    private List<Article> articles;
    private HashMap<Article, DetailFragment> fragMaps;

    public HomeViewPagerAdapter(FragmentManager fm, List<Article> articles){
        super(fm);
        this.articles = articles;
        fragMaps = new HashMap<>();
    }

    @Override
    public Fragment getItem(int position) {
        Article article = articles.get(position);
        DetailFragment fragment;

        if(fragMaps.containsKey(article)){
            // cache in the map
            fragment = fragMaps.get(article);
        }else{
            fragment = new DetailFragment();
            Bundle bundle = new Bundle();
            bundle.putParcelable("article", article);
            fragment.setArguments(bundle);
            // add to the cache
            fragMaps.put(article, fragment);
        }

        return fragment;
    }

    @Override
    public int getCount() {
        return articles.size();
    }
}
