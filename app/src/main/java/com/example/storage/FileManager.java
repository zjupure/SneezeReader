package com.example.storage;

import android.content.Context;
import android.os.Environment;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by liuchun on 2015/7/17.
 */
public class FileManager {
    public static final String DEFAULT_PAGE_DIR = "SneezeReader";
    private static FileManager instance = null;

    private boolean sdExist = true;
    private String storage_dir = "";

    private FileManager(Context context){
        sdExist = CheckSDState();
        File file;

        if(sdExist){
            // sdcard is exsit
            file = new File(context.getExternalFilesDir(null), DEFAULT_PAGE_DIR);
        }else {
            // save to internal storage
            file = new File(context.getFilesDir(), DEFAULT_PAGE_DIR);
        }

        if(!file.exists()){
            // create a directory
            file.mkdir();
            storage_dir = file.getAbsolutePath();
        }
    }

    /**
     * 单例模式
     * @return
     */
    public static FileManager getInstance(Context context){
        if(instance == null){
            synchronized (FileManager.class){
                if(instance == null){
                    instance = new FileManager(context);
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
        File file = new File(storage_dir, filename);
        boolean isOk = false;

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

        return isOk;
    }

    /**
     * 获取文件绝对路径
     * @param filename
     * @return
     */
    public String getAbsolutPath(String filename){
        File file = new File(storage_dir, filename);

        return file.getAbsolutePath();
    }

    /**
     * 判断SD卡是否存在
     * @return
     */
    public boolean CheckSDState(){
        if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
            return true;
        }
        else{
            return false;
        }
    }
}
