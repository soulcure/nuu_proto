package com.nuu.config;

import android.os.Environment;

import java.io.File;

public class FileConfig {

    private static final String TAG = "TcpClient";

    public static final String NuuPath = "/nuu";
    public static final String ApkPaths = NuuPath + "/apk/";
    public static final String FilePaths = NuuPath + "/file/";

    private static final String filePrefix = "log";
    private static final String fileSubfix = ".txt";

    /**
     * 描述：SD卡是否能用.
     *
     * @return true 可用,false不可用
     */
    public static boolean isCanUseSD() {
        try {
            return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }


    public static String getNuuPath() {
        String path = Environment.getExternalStorageDirectory().getPath() + NuuPath;
        File fileDir = new File(path);
        if (!fileDir.exists()) {
            fileDir.mkdirs();
        }
        return fileDir.getAbsolutePath();
    }

    public static String getFileDownLoadPath() {
        String path = Environment.getExternalStorageDirectory().getPath() + FilePaths;
        File fileDir = new File(path);
        if (!fileDir.exists()) {
            fileDir.mkdirs();
        }
        return fileDir.getAbsolutePath();
    }

    public static String getApkDownLoadPath() {
        String path = Environment.getExternalStorageDirectory().getPath() + ApkPaths;
        File fileDir = new File(path);
        if (!fileDir.exists()) {
            fileDir.mkdirs();
        }
        return fileDir.getAbsolutePath();
    }

}
