package com.simit.storage;

import android.content.Context;
import android.net.Uri;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;

/**
 * Created by liuchun on 16/9/8.
 */
public class FileUtils {
    private static final String TAG = "FileUtils";

    private static final String DEFAULT_DIR = "Articles";

    private static final Object mScanLock = new Object();
    private static List<StorageUtils.StorageInfo> storageInfos;

    private static File defaultPath;

    /**
     * 扫描SD卡并缓存起来
     * @param context
     */
    public static void scanSDCards(Context context){

        synchronized (mScanLock) {
            List<StorageUtils.StorageInfo> sdcards = StorageUtils.getStorageList(context);
            if(sdcards.size() > 0){
                Collections.sort(sdcards);

                storageInfos = sdcards;

                String path = sdcards.get(0).mPath;
                String suffix = "Android/data/" + context.getPackageName() + "/files";
                defaultPath = new File(path, suffix);
                ContextCompat.getExternalFilesDirs(context, null);  // try to make the dir
            }else {
                defaultPath = context.getFilesDir();
            }
        }
    }

    /**
     * 启动异步线程执行SD卡扫描逻辑
     * @param context
     */
    public static void startScanTask(final Context context){

        new Thread(new Runnable() {
            @Override
            public void run() {
                //
                scanSDCards(context);
            }
        }).start();
    }


    /**
     * 向filename中写入数据
     * @param filename
     * @param content
     * @return
     */
    public static boolean write(String filename, String content){

        File file = new File(defaultPath, filename);

        FileOutputStream fos = null;
        try{
            if(!file.exists()){
                file.createNewFile();
            }

            fos = new FileOutputStream(file);

            fos.write(content.getBytes());
            fos.flush();

        }catch (IOException e){
            Log.e(TAG, "write() file open exception");
            return false;
        }finally {
            if(fos != null){
                try{
                    fos.close();
                }catch (IOException e){
                    Log.e(TAG, "write file close exception");
                }
            }
        }

        return true;
    }


    /**
     * 从输入流中读取数据并写入文件
     * @param filename
     * @param is
     * @return
     */
    public static boolean write(String filename, InputStream is){

        File file = new File(defaultPath, filename);
        FileOutputStream fos = null;

        try{
            if(!file.exists()){
                file.createNewFile();
            }

            fos = new FileOutputStream(file);

            byte[] buffer = new byte[1024];
            int len = -1;

            while((len = is.read(buffer, 0, 1024)) != -1){
                // 读取一定长度的数据,写入输出流
                fos.write(buffer, 0, len);
            }
            fos.flush();

        }catch (IOException e){
            Log.e(TAG, "write() file open exception");
            return false;
        }finally {
            if(fos != null){
                try{
                    fos.close();
                }catch (IOException e){
                    Log.e(TAG, "write file close exception");
                }
            }
        }


        return true;
    }


    /**
     * 根据链接生成文件名,写入sd卡,返回uri地址
     * @param link
     * @param is
     * @return
     */
    public static String writeHTML(String link, InputStream is){

        String[] paths = link.split("[?]");
        String filename = paths[paths.length - 1];
        paths = filename.split("[&]");
        filename = paths[paths.length - 1];
        filename = filename.replace('=', '_');
        filename += ".html";

        String storePath = DEFAULT_DIR + File.separator + filename;
        if(write(storePath, is)){

            Uri uri = Uri.fromFile(new File(defaultPath, storePath));

            return uri.toString();
        }

        return "";
    }

    /**
     * 递归求解文件或文件夹的大小
     * @param file
     * @return
     */
    public static long getFileSize(File file){

        if(!file.exists() || !file.canRead()){
            return 0L;
        }

        if(!file.isDirectory()){
            //非文件夹
            return file.length();
        }

        //文件夹
        long totalSize = 0L;
        for(File tmp : file.listFiles()){
            totalSize += getFileSize(tmp);
        }

        return totalSize;
    }

    /**
     * 递归删除文件或文件夹
     * @param file
     */
    public static boolean deleteFile(File file){

        if(!file.exists() || !file.canWrite()){
            return false;
        }

        if(!file.isDirectory()){
            //非文件夹
            return file.delete();
        }

        //文件夹
        boolean isOk = true;
        for(File tmp : file.listFiles()){
             isOk &= file.delete();
        }
        //file.delete();  //删除文件夹自身
        return isOk;
    }

    /**
     * 获取Cache的大小
     * @return
     */
    public static long getCacheDirSize(Context context){
        long totalSize = 0L;

        //获取内部存储上特定文件夹的大小
        File file = context.getFilesDir();
        file = new File(file, DEFAULT_DIR);
        totalSize += getFileSize(file);

        //获取外部存储上特定文件夹的大小
        String suffix = "Android/data/" + context.getPackageName() + "/files";
        for(StorageUtils.StorageInfo info : storageInfos){

            String path = info.mPath;
            file = new File(path, suffix + File.separator + DEFAULT_DIR);
            totalSize += getFileSize(file);
        }

        return totalSize;
    }


    /**
     * 清除Cache目录下的缓存数据
     * @param context
     * @return
     */
    public static boolean clearCache(Context context){
        boolean isOk = true;
        //清除内部存储上的缓存
        File file = context.getFilesDir();
        file = new File(file, DEFAULT_DIR);
        isOk &= deleteFile(file);

        //清除外部存储上的缓存
        String suffix = "Android/data/" + context.getPackageName() + "/files";
        for(StorageUtils.StorageInfo info : storageInfos){

            String path = info.mPath;
            file = new File(path, suffix + File.separator + DEFAULT_DIR);
            isOk &= deleteFile(file);
        }

        return isOk;
    }

}
