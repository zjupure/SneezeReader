package com.simit.storage;

import android.content.Context;
import android.os.Environment;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;

/**
 * Created by liuchun on 2015/7/17.
 */
public class FileManager {
    public static final String DEFAULT_PAGE_DIR = "Articles";
    private static FileManager instance = null;
    private static Context context;

    private FileManager(Context ctx){
        File file;
        context = ctx;
        // save to internal storage
        file = new File(context.getFilesDir(), DEFAULT_PAGE_DIR);
        if(!file.exists()){
            // create a directory
            file.mkdir();
        }

        boolean sdExist = CheckSDState();
        if(sdExist){
            // sdcard is exsit
            file = new File(context.getExternalFilesDir(null), DEFAULT_PAGE_DIR);
            if(!file.exists()){
                // create a directory
                file.mkdir();
            }
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

        // 时刻都要检测SD卡是否存在,优先存储到外部存储
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

    public String writeUpdateApk(String filename, byte[] data){
        // 文件路径
        String storePath = filename;
        String absolutePath = "";
        boolean sdExist = CheckSDState();
        File file;

        // 时刻都要检测SD卡是否存在,优先存储到外部存储
        if(sdExist){
            file = new File(context.getExternalFilesDir(null), storePath);
        }else {
            file = new File(context.getFilesDir(), storePath);
        }

        try {
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(data);
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

    public String getStoreDirSize(){
        // 文件夹路径
        String storePath = DEFAULT_PAGE_DIR;
        File file;
        long size = 0L;

        file = new File(context.getFilesDir(), storePath);
        size += getFileSize(file);
        // 时刻都要检测SD卡是否存在
        boolean sdExist = CheckSDState();
        if(sdExist){
            file = new File(context.getExternalFilesDir(null), storePath);
            size += getFileSize(file);
        }

        return FormetFileSize(size);
    }

    // 递归求解文件夹大小
    public long getFileSize(File file){
        long size = 0;
        File flist[] = file.listFiles();

        for(int i = 0; i < flist.length; i++){
            if(flist[i].isDirectory()){
                size += getFileSize(flist[i]);
            }
            else{
                size += flist[i].length();
            }
        }

        return size;
    }

    private static String FormetFileSize(long fileSize) {
        DecimalFormat df = new DecimalFormat("#.00");
        String fileSizeString = "";
        String wrongSize = "0B";
        if (fileSize == 0) {
            return wrongSize;
        }
        if (fileSize < 1024) {
            fileSizeString = df.format((double) fileSize) + "B";
        } else if (fileSize < 1048576) {
            fileSizeString = df.format((double) fileSize / 1024) + "KB";
        } else if (fileSize < 1073741824) {
            fileSizeString = df.format((double) fileSize / 1048576) + "MB";
        } else {
            fileSizeString = df.format((double) fileSize / 1073741824) + "GB";
        }
        return fileSizeString;
    }

    public void clearStoreDir(){
        // 文件夹路径
        String storePath = DEFAULT_PAGE_DIR;
        File file;

        file = new File(context.getFilesDir(), storePath);
        deleteFile(file);
        // 时刻都要检测SD卡是否存在
        boolean sdExist = CheckSDState();
        if(sdExist){
            file = new File(context.getExternalFilesDir(null), storePath);
            deleteFile(file);
        }
    }

    public void deleteFile(File file){
        File flist[] = file.listFiles();

        for(int i = 0; i < flist.length; i++){
            if(flist[i].isDirectory()){
                deleteFile(flist[i]);
            }else {
                flist[i].delete();
            }
        }
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
