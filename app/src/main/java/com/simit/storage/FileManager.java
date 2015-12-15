package com.simit.storage;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by liuchun on 2015/7/17.
 */
public class FileManager {
    public static final String DEFAULT_PAGE_DIR = "Articles";
    private static FileManager instance = null;
    private static Context context;

    private FileManager(Context ctx){
        boolean sdExist = CheckSDState();
        File file;

        context = ctx;
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

    public String writeHTML(String filename, String content){
        // 文件路径
        String storePath = DEFAULT_PAGE_DIR + File.separator + filename;
        String absolutePath = "";
        boolean sdExist = CheckSDState();
        File file;

        // 时刻都要检测SD卡是否存在
        if(sdExist){
            file = new File(context.getExternalFilesDir(null), storePath);
        }else {
            file = new File(context.getFilesDir(), storePath);
        }

        try {
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(content.getBytes());
            fos.flush();
            fos.close();
            // 写入成功
            absolutePath = file.getAbsolutePath();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return absolutePath;
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
