package com.example.storage;

import android.os.Environment;

import com.example.network.SneezeRules;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by liuchun on 2015/7/17.
 */
public class FileManager {
    private static final Object LOCK = new Object();
    private static FileManager instance = null;

    private FileManager(){
        File file = new File(Environment.getExternalStorageDirectory(), SneezeRules.PAGE_DIR);
        if(isSDExist()){
            file.mkdir();    //创建目录
        }
    }

    /**
     * 单例模式
     * @return
     */
    public static FileManager getInstance(){
        if(instance == null){
            synchronized (LOCK){
                if(instance == null){
                    instance = new FileManager();
                }
            }
        }

        return instance;
    }

    /**
     * 把网页内容写到外部SD卡,保存为html文件
     * @param filename
     */
    public boolean writeHTML(String filename, String content){
        File file = new File(Environment.getExternalStorageDirectory(), filename);
        boolean isOk = false;
        if(isSDExist()){
            try {
                FileOutputStream fos = new FileOutputStream(file);
                fos.write(content.getBytes());
                fos.flush();
                fos.close();

                isOk = true;   //写入成功
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return isOk;
    }
    /**
     * 判断SD卡是否存在
     * @return
     */
    public boolean isSDExist(){
        if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
            return true;
        }
        else{
            return false;
        }
    }
}
