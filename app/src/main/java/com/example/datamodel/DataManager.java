package com.example.datamodel;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by liuchun on 2015/7/17.
 */
public class DataManager {
    private List<Datainfo> mTugua;       //图卦的数据集
    private List<Datainfo> mLehuo;       //乐活的数据集
    private List<Datainfo> mYitu;        //意图的数据集
    private List<Datainfo> mDuanzi;      //段子的数据集

    private static final Object LOCK = new Object();
    private static DataManager instance = null;

    private DataManager(){
        mTugua = new ArrayList<Datainfo>();
        mLehuo = new ArrayList<Datainfo>();
        mYitu = new ArrayList<Datainfo>();
        mDuanzi = new ArrayList<Datainfo>();
    }

    /**
     * 单例模式,获取数据管理器实例
     * @return
     */
    public static DataManager getInstance(){
        if(instance == null){
            synchronized (LOCK){
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
    public void addData(Datainfo datainfo){
        switch (datainfo.getType()){
            case Datainfo.TUGUA:
                mTugua.add(datainfo);
                break;
            case Datainfo.LEHUO:
                mLehuo.add(datainfo);
                break;
            case Datainfo.YITU:
                mYitu.add(datainfo);
                break;
            case Datainfo.DUANZI:
                mDuanzi.add(datainfo);
                break;
            default:break;
        }
    }

    /**
     * 添加集合到数据管理器
     * @param datoinfos
     */
    public void addDataset(List<Datainfo> datoinfos){
        //添加到数据集
        for(Datainfo datainfo : datoinfos){
            addData(datainfo);
        }
    }

    /**
     * 根据数据的类型,获取数据集
     * @param type
     * @return
     */
    public List<Datainfo> getData(int type){
        switch (type){
            case Datainfo.TUGUA:
                return mTugua;
            case Datainfo.LEHUO:
                return mLehuo;
            case Datainfo.YITU:
                return mYitu;
            case Datainfo.DUANZI:
                return mDuanzi;
            default: return null;
        }
    }


}
