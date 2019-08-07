package com.nuu.config;

public class AppConfig {

    /**
     * pref文件名定义
     */
    public static final String SHARED_PREFERENCES = "sdk_app";

    /**
     * 充电桩是否启用加密
     */
    public static final boolean isRSA = true;

    /**
     * HuXin 服务器连接配置
     */
    private final static int LAUNCH_MODE = 0; //0 充电桩本地服务器        1充电桩50服务器

    private final static String SOCKET_HOST[] = new String[]{"192.168.0.218", "120.24.37.50"};

    private final static int SOCKET_PORT[] = new int[]{7654, 7654};

    public static String getSocketHost() {
        return SOCKET_HOST[LAUNCH_MODE];
    }

    public static int getSocketPort() {
        return SOCKET_PORT[LAUNCH_MODE];
    }

}
