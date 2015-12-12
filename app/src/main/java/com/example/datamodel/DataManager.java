package com.example.datamodel;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by liuchun on 2015/7/17.
 */
public class DataManager {
    private List<Article> mTugua;       //图卦的数据集
    private List<Article> mLehuo;       //乐活的数据集
    private List<Article> mYitu;        //意图的数据集
    private List<Article> mDuanzi;      //段子的数据集

    private static DataManager instance = null;

    private DataManager(){
        mTugua = new ArrayList<>();
        mLehuo = new ArrayList<>();
        mYitu = new ArrayList<>();
        mDuanzi = new ArrayList<>();
    }

    /**
     * 单例模式,获取数据管理器实例
     * @return
     */
    public static DataManager getInstance(){
        if(instance == null){
            synchronized (DataManager.class){
                if(instance == null){
                    instance = new DataManager();
                }
            }
        }

        return instance;
    }

    /**
     * 添加一个Datainfo到数据管理器
     * @param datainfo
     */
    public void addData(Article datainfo){
        switch (datainfo.getType()){
            case Article.TUGUA:
                mTugua.add(datainfo);
                break;
            case Article.LEHUO:
                mLehuo.add(datainfo);
                break;
            case Article.YITU:
                mYitu.add(datainfo);
                break;
            case Article.DUANZI:
                mDuanzi.add(datainfo);
                break;
            default:break;
        }
    }

    /**
     * 添加集合到数据管理器
     * @param datoinfos
     */
    public void addDataset(List<Article> datoinfos){
        //添加到数据集
        for(Article datainfo : datoinfos){
            addData(datainfo);
        }
    }


    public void setDataset(int type, List<Article> datainfos){
        switch (type){
            case Article.TUGUA:
                mTugua = datainfos;
                break;
            case Article.LEHUO:
                mLehuo = datainfos;
                break;
            case Article.YITU:
                mYitu = datainfos;
                break;
            case Article.DUANZI:
                mDuanzi = datainfos;
                break;
            default: break;
        }
    }

    /**
     * 根据数据的类型,获取数据集
     * @param type
     * @return
     */
    public List<Article> getData(int type){
        switch (type){
            case Article.TUGUA:
                return mTugua;
            case Article.LEHUO:
                return mLehuo;
            case Article.YITU:
                return mYitu;
            case Article.DUANZI:
                return mDuanzi;
            default: return null;
        }
    }


}
