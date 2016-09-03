package com.simit.storage;

import android.content.Context;
import android.os.Build;
import android.os.Environment;
import android.os.storage.StorageManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.os.EnvironmentCompat;
import android.text.TextUtils;
import android.util.Log;


import java.io.BufferedReader;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by liuchun on 16/9/3.
 */
public class StorageUtils {
    private static final String TAG = "StorageUtils";
    /** 存储卡的类型 */
    public static final int INTERNAL_MEMORY = 0;   // 内置系统分区/data/data目录
    public static final int INTERNAL_STORAGE = 1;  // 内置存储卡
    public static final int EXTERNAL_STORAGE = 2;  // 外置SD卡
    public static final int USB_DISK = 3;          // 外接USB存储
    public static final int MEDIA_UNKNOWM = 4;     // 未知设备
    /** 存储卡的优先级 */
    public static final int HIGH_PRIORITY = 1000;   // 高优先级，厂商认定的主卡, Environment.getExternalDirectory()返回结果
    public static final int MIDDLE_PRIORITY = 100;  // 中等优先级, 内置存储卡
    public static final int NORMAL_PRIORITY = 0;    // 普通优先级, 外置SD卡
    public static final int LOW_PRIORITY = -100;    // 低优先级，USB存储


    /**
     * 获取可用的SDCard信息
     * @param context
     * @return
     */
    public static List<StorageInfo> getStorageList(Context context){
        // 首先通过反射StorageManager类获取SDCard列表
        List<StorageInfo> arrayList = getStorageListByReflection(context);

        if(arrayList == null || arrayList.size() <= 0){
            // 获取失败, 低于4.0的设备，尝试通过读取mount表获取
            arrayList = getStorageListByMountFile(context);
        }

        return arrayList;
    }

    /**
     * 通过反射{@link android.os.storage.StorageManager}的隐藏API
     * 来获取SDCard路径
     * @param context
     * @return
     */
    private static List<StorageInfo> getStorageListByReflection(Context context){
        ArrayList<StorageInfo> arrayList = new ArrayList<>();

        StorageManager sm = (StorageManager)context.getSystemService(Context.STORAGE_SERVICE);
        try{
            Method a = sm.getClass().getMethod("getVolumeList");
            a.setAccessible(true);
            // 反射StorageManager类的方法getVolumeList()
            Object[] storageVolumes = (Object[]) a.invoke(sm);

            for(Object storageVolume : storageVolumes){
                StorageInfo info = new StorageInfo(storageVolume, context);
                //判断挂载状态和读写性
                if(Environment.MEDIA_MOUNTED.equals(info.mState) && info.canWrite(context) && info.totalSize > 0){
                    arrayList.add(info);
                }
            }
        }catch (Exception e){
            //
            Log.e(TAG, "StorageManager-->getVolumeList not found, reflection failed");
        }

        // 反射StorageVolume失败，则通过反射StorageManager的方法获取
        if(arrayList.size() <= 0){
            try{
                Method b = sm.getClass().getMethod("getVolumePaths");
                b.setAccessible(true);
                //Method c = sm.getClass().getMethod("getVolumeState", String.class);
                //c.setAccessible(true);

                String[] paths = (String[]) b.invoke(sm);  // 调用getVolumePaths
                for(String path : paths){
                    if(!checkPathValidate(path)){
                        continue;  // 校验路径的有效性
                    }

                    StorageInfo info = new StorageInfo(path, context);
                    if(Environment.MEDIA_MOUNTED.equals(info.mState) && info.canWrite(context) && info.totalSize > 0){
                        arrayList.add(info);
                    }
                }
            }catch (Exception e){
                //
                Log.e(TAG, "StorageManager-->getVolumePaths not found, reflection failed");
            }
        }
        // 查找Primary分区, 通常是内置卡
        StorageInfo primary = null;
        for(StorageInfo info : arrayList){
            if(info.mPrimary){
                primary = info;   // 找到了主分区
            }
        }
        // 4.0-4.2之间, StorageVolume没有isPrimary方法, 认定不可移除的分区为内置卡
        if(primary == null){
            for(StorageInfo info : arrayList){
                if(!info.mRemovable){
                    info.mPrimary = true;
                    primary = info;
                    break;
                }
            }
        }
        // 若都是可移除的设备, 认为第一个设备为主设备
        if(primary == null && arrayList.size() > 0){
            primary = arrayList.get(0);
            primary.mPrimary = true;
        }
        // 按内置卡和外置卡的顺序放置
        ArrayList<StorageInfo> results = new ArrayList<>();
        for(StorageInfo info : arrayList){
            if(info.storageType == INTERNAL_STORAGE){
                results.add(0, info);
            }else {
                results.add(info);
            }
        }

        return results;
    }

    /**
     * 通过读取/proc/mounts文件或执行mount命令获取
     * 同时结合/system/etc/vold.fstab文件内容
     * 并对结果进行过滤，筛选出有效的存储路径
     * @param context
     * @return
     */
    private static List<StorageInfo> getStorageListByMountFile(Context context){
        // 读取/proc/mounts文件或执行mount命令
        ArrayList<String> mMounts = new ArrayList<>();
        ArrayList<String> mVold = new ArrayList<>();  // 读取/system/etc/vold.fstab无法区分挂载状态
        // 挂载点与文件系统表
        HashMap<String, String> mFileSystems = new HashMap<>();
        // 挂载点与设备名映射表
        HashMap<String, String> mMountDevs = new HashMap<>();

        //添加默认的路径/mnt/sdcard
        mMounts.add("/mnt/sdcard");
        mVold.add("/mnt/sdcard");
        mFileSystems.put("/mnt/sdcard", "vfat");  // default vfat

        /** 尝试读取/proc/mounts文件  */
        BufferedReader bufferedReader = null;
        try{
            File mountFile = new File("/proc/mounts");
            if(mountFile.exists() && mountFile.canRead()){
                bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(mountFile)));  // 读取/proc/mounts文件
            }else{
                bufferedReader = new BufferedReader(new InputStreamReader(Runtime.getRuntime().exec("mount").getInputStream())); // 执行linux mount命令
            }
            //bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(new File("/proc/mounts")), "UTF8"));  // 读取/proc/mounts文件
            //bufferedReader = new BufferedReader(new InputStreamReader(Runtime.getRuntime().exec("mount").getInputStream()));    // 执行linux mount命令
            while (true){
                String line = bufferedReader.readLine();

                if(line == null){
                    break;  //读到文件的末尾
                }

                if(line.contains("secure") || line.contains("asec") || line.contains("firmware") || line.contains("obb") || line.contains("tmpfs")){
                    // 需要排除的关键词，不是sdcard挂载点
                    continue;
                }

                String[] split = line.split("\\s+");  //空格分割
                if(split == null || split.length <= 4){
                    continue;  // /proc/mounts文件一行一般为6个
                }

                // <dev> <mount_point> <filesystem> <rw,useid,groudid> <0> <0>
                String dev = split[0];          //设备名
                String mount_point = split[1];  // 挂载点
                String filesystem = split[2];   // 文件系统

                // sdcard可能的文件类型是vfat或fuse
                /* usb存储可能是其他文件类型,先注释掉
                if(!filesystem.equals("vfat") || !filesystem.equals("fuse")){
                    continue;
                }*/

                String tmp = mount_point.toLowerCase();
                if(dev.contains("/dev/block/vold/") || dev.contains("/dev/block/sd") || dev.contains("/dev/sd") || dev.contains("/dev/fuse") || dev.contains("/dev/lefuse")){
                    // 常见设备名
                    if(!tmp.equals("/mnt/sdcard")){
                        mMounts.add(mount_point);  //添加可能的挂载点
                    }
                    //mMounts.add(mount_point);
                    mFileSystems.put(mount_point, filesystem);
                    mMountDevs.put(mount_point, dev);
                }else if(tmp.contains("emmc") || tmp.contains("storage") || tmp.contains("sdcard") || tmp.contains("external") || tmp.contains("ext_sd")
                        || tmp.contains("ext_card") || tmp.contains("extsdcard") || tmp.contains("external_sd") || tmp.contains("emulated")
                        || tmp.contains("/mnt/media_rw/") || tmp.contains("flash")){
                    // 常见挂载点
                    if(!tmp.equals("/mnt/sdcard")){
                        mMounts.add(mount_point);  //添加可能的挂载点
                    }
                    //mMounts.add(mount_point);
                    mFileSystems.put(mount_point, filesystem);
                    mMountDevs.put(mount_point, dev);
                }
            }
        }catch (Exception e){
            //
            Log.e(TAG, "Read /proc/mounts failed");
        }finally {
            closeSilently(bufferedReader);
        }

        /** 尝试读取/system/etc/vold.fstab */
        try{
            File voldFile = new File("/system/etc/vold.fstab");
            if(!voldFile.exists()){
                voldFile = new File("/etc/vold.fstab");  //有些手机存放位置不一致
            }

            if(voldFile.exists()){
                bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(voldFile)));

                while (true){
                    String line = bufferedReader.readLine();

                    if(line == null){
                        break;   // 读到文件的末尾
                    }

                    // vold.fstab文件都是以dev_mount开头
                    // Format: dev_mount <label> <mount_point> <part> <sysfs_path1...>
                    if(line.startsWith("dev_mount")){
                        String[] splits = line.split("\\s+");  //空格分割
                        String dev = splits[1];
                        String mount_point = splits[2];

                        if(mount_point.contains(":")){
                            mount_point = mount_point.substring(0, mount_point.indexOf(":"));
                        }

                        if(!mount_point.equals("/mnt/sdcard")){
                            mVold.add(mount_point);
                        }
                    }
                }
            }
        }catch (Exception e){
            Log.e(TAG, "read /system/etc/vold.fstab failed");
        }finally {
            closeSilently(bufferedReader);
        }

        //合并两个文件的内容
        for(String vold : mVold){
            if(!mMounts.contains(vold)){
                mMounts.add(vold);
            }
        }
        mVold.clear();

        /**
         * 对读取的结果进行过滤，分为三个阶段
         * 1. 文件路径的标准化，很多路径可能是软连接，如/mnt/sdcard--->/storage/emulated/0
         * 2. 同一设备挂载到多个目录，需要过滤，如/dev/fuse-->/mnt/shell/emulated,/storage/emulated/0
         * 3. 根据/Android/data/{package_name}/files目录读写性过滤
         */
        // step 1, 标准化路径
        ArrayList<String> tmp1 = new ArrayList<>();
        for(String mount : mMounts){
            if(!checkPathValidate(mount)){
                continue;  //无效路径, pass
            }

            String realPath = mount;
            try{
                File file = new File(mount);
                realPath = file.getCanonicalPath();
            }catch (IOException e){
                Log.d(TAG, "File-->getCanonicalPath failed with path = " + mount);
            }

            // 不是重复路径就添加到结果集合
            if(!tmp1.contains(realPath)){
                tmp1.add(realPath);
            }
        }

        // step2, 解决同一设备挂载到多个目录现象
        ArrayList<String> tmp2 = new ArrayList<>();
        for(String mount : tmp1){
            if(mount.contains("legacy")){
                continue;   // 向下兼容的挂载目录,需要过滤掉
            }

            String dev = mMountDevs.get(mount);  // 获取该挂载点的真实设备名
            if(dev == null){
                tmp2.add(mount);
                continue;
            }

            boolean exist = false;
            for(int i = 0; i < tmp2.size(); i++){
                String tmpDev = mMountDevs.get(tmp2.get(i));
                if(tmpDev != null && tmpDev.equals(dev)){
                    exist = true;
                    // 是否需要替换之前的路径
                    if(mount.contains("storage")){
                        tmp2.set(i, mount);  // replace
                    }
                    break;
                }
            }
            //
            if(!exist){
                tmp2.add(mount);
            }
        }

        // step 3, 根据读写性进一步过滤,得出最终的结果
        ArrayList<StorageInfo> results = new ArrayList<>();
        for(String mount : tmp2){
            StorageInfo info = new StorageInfo(mount, context);
            String fileSystem = mFileSystems.get(mount);
            if(fileSystem != null){
                info.mFileSystem = fileSystem;
            }

            if(Environment.MEDIA_MOUNTED.equals(info.mState) && info.canWrite(context) && info.totalSize > 0){
                //
                results.add(info);
            }
        }

        return results;
    }

    /**
     * 关闭文件流
     * @param closeable
     */
    private static void closeSilently(Closeable closeable){
        if(closeable != null){
            try{
                closeable.close();
            }catch (IOException e){
                e.printStackTrace();
            }finally {
                closeable = null;
            }
        }
    }

    /**
     * 校验根路径的有效性
     * @param path
     * @return
     */
    private static boolean checkPathValidate(String path){
        File file = new File(path);

        return file.exists() && file.isDirectory();
    }



    /**
     * 存储卡基本信息
     */
    public static class StorageInfo implements Comparable<StorageInfo>{
        public int storageType = MEDIA_UNKNOWM;    // 存储卡类型，内置SD卡，外置SD卡，外接USB存储
        public String mPath;        // 根路径
        public String mFileSystem;  // 文件系统
        //public File mFile;        // 文件句柄
        public long totalSize;      // 总容量
        public long usedSize;       // 已用容量
        public long availSize;      // 可用容量
        // 其他一些信息
        public boolean mPrimary = false;    // 是否主卡
        public boolean mRemovable = true;   // 是否可移除
        public boolean mEmulated = false;   // 是否模拟的
        public String mState;               // 当前挂载状态
        //
        public int mPriority = NORMAL_PRIORITY;   // 卡的优先级

        /**
         * 通过mount表实现的构造方法
         * @param path
         */
        StorageInfo(String path, Context context){
            mPath = path;
            mRemovable = isRemovable();
            mEmulated = isEmulated();
            if(!mRemovable){
                mPrimary = true;  // 通常不可移除的卡视为主卡
            }
            // 初始化其他一些信息
            init(context);
        }

        /**
         * 通过反射的构造函数
         * @param storageVolume  StorageVolume类的实例,反射其方法初始化StorageInfo
         * @param context
         * @throws IllegalAccessException
         * @throws InvocationTargetException
         */
        StorageInfo(Object storageVolume, Context context) throws IllegalAccessException, InvocationTargetException {
            // 遍历StorageVolume所有方法
            for(Method m : storageVolume.getClass().getMethods()){

                if (m.getName().equals("getPath") && m.getParameterTypes().length == 0 && m.getReturnType() == String.class)
                    mPath = (String) m.invoke(storageVolume); // above Android 4.0

                if (m.getName().equals("getState") && m.getParameterTypes().length == 0 && m.getReturnType() == String.class)
                    mState = (String) m.invoke(storageVolume); // above Android 4.4.1

                if (m.getName().equals("isRemovable") && m.getParameterTypes().length == 0 && m.getReturnType() == boolean.class)
                    mRemovable = (Boolean) m.invoke(storageVolume); // above Android 4.0

                if (m.getName().equals("isPrimary") && m.getParameterTypes().length == 0 && m.getReturnType() == boolean.class)
                    mPrimary = (Boolean) m.invoke(storageVolume); // above Android 4.2

                if (m.getName().equals("isEmulated") && m.getParameterTypes().length == 0 && m.getReturnType() == boolean.class)
                    mEmulated = (Boolean) m.invoke(storageVolume); // above Android 4.0

            }

            // sdk >= 4.0
            if(mPath != null){
                // 初始化其他一些信息
                init(context);
            }
        }

        /**
         * 初始化文件系统容量信息，存储卡类型，优先级等
         * @param context
         */
        private void init(Context context){
            // 初始化容量信息
            File file = new File(mPath);
            totalSize = file.getTotalSpace();
            availSize = file.getUsableSpace();
            usedSize = totalSize - availSize;
            // 初始化状态信息
            if(TextUtils.isEmpty(mState)){
                // 4.0-4.4之间的系统无法通过反射StorageVolume获取状态信息
                mState = getState(context);
            }
            // 获取存储卡类型
            storageType = getStorageType();
            // 设置默认文件系统
            if(storageType == INTERNAL_STORAGE){
                mFileSystem = "fuse";
            }else{
                mFileSystem = "vfat";
            }
            // 获取优先级
            mPriority = getPriority(context);
        }

        /**
         * 按照优先级降序排列
         * @param other
         * @return
         */
        @Override
        public int compareTo(StorageInfo other) {
            // 首先按优先级降序排列，然后按总容量降序排列，最后按可用容量降序排序
            int ret = 0;
            if(other.mPriority != this.mPriority){
                ret = other.mPriority - this.mPriority;
            }else if(other.totalSize != this.totalSize){
                ret = other.totalSize - this.totalSize > 0 ? 1 : -1;
            }else if(other.availSize != this.availSize){
                ret = other.availSize - this.availSize > 0 ? 1 : -1;
            }

            return ret;
        }

        /**
         * 检查给定SD卡根路径下的/Android/data/{package_name}/files目录是否可写
         * @param context
         * @return
         */
        protected boolean canWrite(Context context){

            String appFilesPath = mPath + "/Android/data/" + context.getPackageName() + "/files";
            File appFile = new File(appFilesPath);
            if(!appFile.exists()){
                // try to make dirs first
                appFile.mkdirs();
                // This is important for Android 4.4+, secondary storage write permission is limited
                //context.getExternalFilesDir(null);
                ContextCompat.getExternalFilesDirs(context, null);

                if (!appFile.exists()) {
                    Log.d("StorageInfo", "mInnerPath does not exist!");
                } else {
                    Log.d("StorageInfo", "mInnerPath is exist!");
                }
            }

            return  appFile.canWrite();
        }

        /**
         * 根据存储卡的类型和{@link Context#getExternalFilesDir(String)}返回结果
         * 决定该卡的优先级
         * @param context
         * @return
         */
        private int getPriority(Context context){

            int priority = NORMAL_PRIORITY;
            // 首先根据存储卡的类型决定基本优先级
            if(storageType == INTERNAL_STORAGE){
                priority = MIDDLE_PRIORITY;  // 优先写内置卡
            }else if(storageType == USB_DISK){
                priority = LOW_PRIORITY;     //  USB存储卡优先级最低
            }else {
                priority = NORMAL_PRIORITY;  // 外置sd卡是正常的优先级
            }

            File appFiles = context.getExternalFilesDir(null);
            if(appFiles != null && appFiles.getAbsolutePath().startsWith(mPath)){
                // 与Open API调用返回的路径一致, 提升优先级
                priority = HIGH_PRIORITY;   // 优先写厂商认定的主卡
            }

            return priority;
        }

        /**
         * 获取当前存储分区的状态
         * @param context
         * @return
         */
        private String getState(Context context){

            File file = new File(mPath);
            String state = EnvironmentCompat.MEDIA_UNKNOWN;
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
                // 5.0+
                state = Environment.getExternalStorageState(file);
            }else if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT){
                // 4.4+
                state = Environment.getStorageState(file);
            }else if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH){
                // 4.0-4.4.
                StorageManager sm = (StorageManager) context.getSystemService(Context.STORAGE_SERVICE);
                try{
                    Method m = sm.getClass().getMethod("getVolumeState", String.class);
                    m.setAccessible(true);

                    state = (String)m.invoke(sm, mPath);
                }catch (Exception e){
                    // reflection failed
                    if(file.canRead() && file.getTotalSpace() > 0){
                        state = Environment.MEDIA_MOUNTED;
                    }else {
                        state = EnvironmentCompat.MEDIA_UNKNOWN;
                    }
                }
            }

            return state;
        }

        /**
         * 获取当前存储分区的类型
         * @see StorageUtils#INTERNAL_STORAGE
         * @see StorageUtils#EXTERNAL_STORAGE
         * @see StorageUtils#USB_DISK
         * @see StorageUtils#MEDIA_UNKNOWM
         * @return
         */
        private int getStorageType(){
            int type = MEDIA_UNKNOWM;

            String tmp = mPath.toLowerCase();
            if(!mRemovable){
                // 不可移除, 内置卡
                type = INTERNAL_STORAGE;
            }else if(tmp.contains("usb") || tmp.contains("udisk")){
                // 可以移除，含有usb和udisk字样, 是USB存储
                type = USB_DISK;
            }else {
                // 可以移除, 外置SD卡
                type = EXTERNAL_STORAGE;
            }

            return type;
        }

        /**
         * 判断当前存储分区是否可以移除
         * @return
         */
        private boolean isRemovable(){
            boolean removable = true;  // 默认是可移除的

            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
                try{
                    removable = Environment.isExternalStorageRemovable(new File(mPath));
                }catch (IllegalArgumentException e){
                    Log.d("StorageInfo", "isRemovable()-->The path is not a valid storage device");
                }
            }else {
                File extFile = Environment.getExternalStorageDirectory();
                if(extFile != null && extFile.getAbsolutePath().equals(mPath)) {  // 某些机型可能获取为空
                    // 和默认存储返回路径一致
                    removable = Environment.isExternalStorageRemovable();
                }
            }

            return removable;
        }

        /**
         * 判断当前存储分区是否是模拟的
         * @return
         */
        private boolean isEmulated(){
            boolean emulated = false;

            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
                try{
                    emulated = Environment.isExternalStorageEmulated(new File(mPath));
                }catch (IllegalArgumentException e){
                    Log.d("StorageInfo", "isEmulated()-->The path is not a valid storage device");
                }
            }else if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB){
                File extFile = Environment.getExternalStorageDirectory();
                if(extFile != null && extFile.getAbsolutePath().equals(mPath)){
                    // 和默认存储返回路径一致
                    emulated = Environment.isExternalStorageEmulated();
                }
            }

            return emulated;
        }

        @Override
        public String toString() {
            return "DiskSpaceInfo{type=" + storageType + ", path=" + mPath + ", filesystem=" + mFileSystem
                    + ", totalSize=" + totalSize + ", usedSize=" + usedSize + ", availSize=" + availSize
                    + ", primary=" + mPrimary + ", removable=" + mRemovable + ", emulated=" + mEmulated
                    + ", state=" + mState + "}";
        }
    }
}
