package com.simit.fragment.adapter;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.simit.fragment.DetailFragment;
import com.simit.database.Article;

import java.util.HashMap;
import java.util.List;

/**
 * Created by liuchun on 2015/12/12.
 */
public class HomeViewPagerAdapter extends FragmentPagerAdapter {
    private List<Article> mArticles;
    private HashMap<Article, Fragment> fragMaps;

    public HomeViewPagerAdapter(FragmentManager fm, List<Article> articles){
        super(fm);
        mArticles = articles;
        fragMaps = new HashMap<>();
    }

    @Override
    public Fragment getItem(int position) {
        Article article = mArticles.get(position);
        Fragment fragment;

        if(fragMaps.containsKey(article)){
            // cache in the map
            fragment = fragMaps.get(article);
        }else{

            Bundle bundle = new Bundle();
            bundle.putParcelable("article", article);
            fragment = DetailFragment.newInstance(bundle);
            // add to the cache
            fragMaps.put(article, fragment);
        }

        return fragment;
    }


    @Override
    public int getCount() {
        return mArticles.size();
    }
}
