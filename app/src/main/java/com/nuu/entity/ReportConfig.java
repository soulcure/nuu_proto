package com.nuu.entity;

import com.nuu.util.GsonUtil;

public class ReportConfig {

    /**
     * reportStorePath : /storage/sdcard0/nuu0
     * obtainReportRate : 120
     * sendReportRate : 600
     * sendToIp : 119.23.74.49
     * port : 18990
     * reportStoreKeepDays : 6
     */

    private String reportStorePath;   //日志存储的sdcard路径
    private int obtainReportRate;   //设备信息生成周期 2*60默认2分钟,
    private int sendReportRate;   //设备信息发送周期 10*60 默认10分钟
    private String sendToIp;     //设备信息发送服务器IP
    private int port;            //设备信息发送服务器PORT
    private int reportStoreKeepDays;   //默认文件保存在设备sdcard 30天
    private String routerHost;   //路由host
    private String routerPath;  //路由path

    public String getReportStorePath() {
        return reportStorePath;
    }

    public void setReportStorePath(String reportStorePath) {
        this.reportStorePath = reportStorePath;
    }

    public int getObtainReportRate() {
        return obtainReportRate;
    }

    public void setObtainReportRate(int obtainReportRate) {
        this.obtainReportRate = obtainReportRate;
    }

    public int getSendReportRate() {
        return sendReportRate;
    }

    public void setSendReportRate(int sendReportRate) {
        this.sendReportRate = sendReportRate;
    }

    public String getSendToIp() {
        return sendToIp;
    }

    public void setSendToIp(String sendToIp) {
        this.sendToIp = sendToIp;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public int getReportStoreKeepDays() {
        return reportStoreKeepDays;
    }

    public void setReportStoreKeepDays(int reportStoreKeepDays) {
        this.reportStoreKeepDays = reportStoreKeepDays;
    }

    public String getRouterHost() {
        return routerHost;
    }

    public void setRouterHost(String routerHost) {
        this.routerHost = routerHost;
    }

    public String getRouterPath() {
        return routerPath;
    }

    public void setRouterPath(String routerPath) {
        this.routerPath = routerPath;
    }

    @Override
    public String toString() {
        return GsonUtil.format(this);
    }
}
