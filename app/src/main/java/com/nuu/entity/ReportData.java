package com.nuu.entity;

import android.content.Context;
import android.text.TextUtils;

import com.nuu.MiFiManager;
import com.nuu.util.DeviceInfo;
import com.nuu.util.GsonUtil;

import java.util.List;

public class ReportData {
    private String deviceSN;  //persist.telephony.imei1
    private String deviceId;  //Build.SERIAL
    private int unixTime;  //unix时间戳 单位秒
    private String ip;    //真实IP地址
    private String mac;   //mac地址
    private Sim1Bean sim1;  //卡1信息 种子卡
    private Sim2Bean sim2; //卡2信息 数据卡
    private int pow;   //电量
    private int charge;  //1 isCharging; 0 not charge
    private int netStatus; //网络状态  1 isAvailable；0 noAvailable
    private int hotPoint;  //热点开关 状态 1开启; 0 关闭
    private int adb;  //ADB开关 状态 1开启; 0 关闭
    private int hotAmount;  //热点连接数量
    private String speedState;  //网络速度
    private int netBrock;  //网络断开次数


    public ReportData(Context context) {
        deviceId = DeviceInfo.getDeviceId();
        deviceSN = DeviceInfo.getDeviceSN();
        unixTime = DeviceInfo.getUnixTimeStamp();
        ip = DeviceInfo.getIPAddress();

        List<WifiClient> wifiApClientList = DeviceInfo.getWifiApClientList();
        hotAmount = wifiApClientList.size();

        StringBuilder sb = new StringBuilder();
        int size = wifiApClientList.size();
        for (int i = 0; i < size; i++) {
            String macItem = wifiApClientList.get(i).getMac();
            if (!TextUtils.isEmpty(macItem)) {
                sb.append(macItem.replace(":", ""));
            }

            if (!TextUtils.isEmpty(macItem) && i < size - 1) {
                sb.append("|");
            }
        }
        mac = sb.toString();

        pow = DeviceInfo.getBatteryInfo(context).getPow();
        charge = DeviceInfo.getBatteryInfo(context).getCharge();
        speedState = DeviceInfo.getSpeedStateStr();
        hotPoint = DeviceInfo.getHotPointState(context);
        adb = DeviceInfo.getAdbStatus(context);
        netStatus = DeviceInfo.getNetStatus(context);

        netBrock = MiFiManager.instance().getNetBrock();  //网络断开次数

        sim1 = MiFiManager.instance().getSeedSimBean();
        sim2 = DeviceInfo.getSim2(context);
    }

    public String getDeviceSN() {
        return deviceSN;
    }

    public void setDeviceSN(String deviceSN) {
        this.deviceSN = deviceSN;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public int getUnixTime() {
        return unixTime;
    }

    public void setUnixTime(int unixTime) {
        this.unixTime = unixTime;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getMac() {
        return mac;
    }

    public void setMac(String mac) {
        this.mac = mac;
    }

    public Sim1Bean getSim1() {
        return sim1;
    }

    public void setSim1(Sim1Bean sim1) {
        this.sim1 = sim1;
    }

    public Sim2Bean getSim2() {
        return sim2;
    }

    public void setSim2(Sim2Bean sim2) {
        this.sim2 = sim2;
    }

    public int getPow() {
        return pow;
    }

    public void setPow(int pow) {
        this.pow = pow;
    }

    public int getCharge() {
        return charge;
    }

    public void setCharge(int charge) {
        this.charge = charge;
    }

    public int getNetStatus() {
        return netStatus;
    }

    public void setNetStatus(int netStatus) {
        this.netStatus = netStatus;
    }

    public int getHotPoint() {
        return hotPoint;
    }

    public void setHotPoint(int hotPoint) {
        this.hotPoint = hotPoint;
    }

    public int getAdb() {
        return adb;
    }

    public void setAdb(int adb) {
        this.adb = adb;
    }

    public int getHotAmount() {
        return hotAmount;
    }

    public void setHotAmount(int hotAmount) {
        this.hotAmount = hotAmount;
    }

    public String getSpeedState() {
        return speedState;
    }

    public void setSpeedState(String speedState) {
        this.speedState = speedState;
    }

    public int getNetBrock() {
        return netBrock;
    }

    public void setNetBrock(int netBrock) {
        this.netBrock = netBrock;
    }

    public static class Sim1Bean {
        private String imsi = ""; //sim卡ID
        private String plmn = "";//国家营运商编码
        private int signal;  //the signal strength as dBm
        private int lac;  //16-bit Location Area Code
        private int ci; //Either 16-bit GSM Cell Identity described 基站编号
        private int psc; ////16位跟踪区域代码 16-bit Tracking Area Code, Integer.MAX_VALUE if unknown
        private int netMode;  //网络类型

        public String getImsi() {
            return imsi;
        }

        public void setImsi(String imsi) {
            this.imsi = imsi;
        }

        public String getPlmn() {
            return plmn;
        }

        public void setPlmn(String plmn) {
            this.plmn = plmn;
        }

        public int getSignal() {
            return signal;
        }

        public void setSignal(int signal) {
            this.signal = signal;
        }

        public int getLac() {
            return lac;
        }

        public void setLac(int lac) {
            this.lac = lac;
        }

        public int getCi() {
            return ci;
        }

        public void setCi(int ci) {
            this.ci = ci;
        }

        public int getPsc() {
            return psc;
        }

        public void setPsc(int psc) {
            this.psc = psc;
        }

        public int getNetMode() {
            return netMode;
        }

        public void setNetMode(int netMode) {
            this.netMode = netMode;
        }


        @Override
        public String toString() {
            return " {" +
                    "\"ci\":" + ci +
                    ",\"imsi\":\"" + imsi + '\"' +
                    ",\"lac\":" + lac +
                    ",\"netMode\":" + netMode +
                    ",\"plmn\":\"" + plmn + '\"' +
                    ",\"psc\":" + psc +
                    ",\"signal\":" + signal +
                    '}';
        }
    }

    public static class Sim2Bean {
        private String imsi = ""; //sim卡ID
        private String plmn = "";//国家营运商编码
        private int signal;  //the signal strength as dBm
        private int lac;  //16-bit Location Area Code
        private int ci; //Either 16-bit GSM Cell Identity described 基站编号
        private int psc; ////16位跟踪区域代码 16-bit Tracking Area Code, Integer.MAX_VALUE if unknown
        private int netMode;  //网络类型

        public String getImsi() {
            return imsi;
        }

        public void setImsi(String imsi) {
            this.imsi = imsi;
        }

        public String getPlmn() {
            return plmn;
        }

        public void setPlmn(String plmn) {
            this.plmn = plmn;
        }

        public int getSignal() {
            return signal;
        }

        public void setSignal(int signal) {
            this.signal = signal;
        }

        public int getLac() {
            return lac;
        }

        public void setLac(int lac) {
            this.lac = lac;
        }

        public int getCi() {
            return ci;
        }

        public void setCi(int ci) {
            this.ci = ci;
        }

        public int getPsc() {
            return psc;
        }

        public void setPsc(int psc) {
            this.psc = psc;
        }

        public int getNetMode() {
            return netMode;
        }

        public void setNetMode(int netMode) {
            this.netMode = netMode;
        }

        @Override
        public String toString() {
            return " {" +
                    "\"ci\":" + ci +
                    ",\"imsi\":\"" + imsi + '\"' +
                    ",\"lac\":" + lac +
                    ",\"netMode\":" + netMode +
                    ",\"plmn\":\"" + plmn + '\"' +
                    ",\"psc\":" + psc +
                    ",\"signal\":" + signal +
                    '}';
        }
    }

    @Override
    public int hashCode() {
        return unixTime;
    }


    @Override
    public boolean equals(Object o) {
        if (o instanceof ReportData) {
            int time = unixTime - ((ReportData) o).unixTime;

            return Math.abs(time) < 10;  //10秒钟之内 视为相同

        } else {
            return false;
        }
    }

    @Override
    public String toString() {
        return " {" +
                "\"adb\":" + adb +
                ",\"charge\":" + charge +
                ",\"deviceId\":\"" + deviceId + '\"' +
                ",\"deviceSN\":\"" + deviceSN + '\"' +
                ",\"hotAmount\":" + hotAmount +
                ",\"hotPoint\":" + hotPoint +
                ",\"ip\":\"" + ip + '\"' +
                ",\"mac\":\"" + mac + '\"' +
                ",\"netBrock\":" + netBrock +
                ",\"netStatus\":" + netStatus +
                ",\"pow\":" + pow +
                ",\"sim1\":" + sim1.toString() +
                ",\"sim2\":" + sim2.toString() +
                ",\"speedState\":\"" + speedState + '\"' +
                ",\"unixTime\":" + unixTime +
                '}';
    }

    public String toJson() {
        return GsonUtil.format(this);
    }


    public int getDeviceStatus() {
        int netBrock_byte = (netBrock & 0xFF) << 19;// 网络断开数 使用int32  [19,27) 位存储
        int hotAmount_byte = (hotAmount & 0xFF) << 11; // 热点连接数      使用int32  [11,19) 位存储
        int adb_byte = (adb & 0xFF) << 10;// adb开启状态 使用int32  [10,11) 位存储
        int hotPoint_byte = (hotPoint & 0xFF) << 9;// 热点开启状态 使用int32  [9,10) 位存储
        int netStatus_byte = (netStatus & 0xFF) << 8; // 网络连接状态 使用int32  [8,9) 位存储
        int charge_byte = (charge & 0xFF) << 7; // 充电状态 使用int32  [7,8) 位存储
        int pow_byte = pow & 0x7F; // 电量 使用int32  [0,7) 位存储
        return netBrock_byte | hotAmount_byte | adb_byte | hotPoint_byte | netStatus_byte | charge_byte | pow_byte;
    }


    public void refresh(Context context) {
        unixTime = DeviceInfo.getUnixTimeStamp();

        List<WifiClient> wifiApClientList = DeviceInfo.getWifiApClientList();
        hotAmount = wifiApClientList.size();

        StringBuilder sb = new StringBuilder();
        int size = wifiApClientList.size();
        for (int i = 0; i < size; i++) {
            String macItem = wifiApClientList.get(i).getMac();
            if (!TextUtils.isEmpty(macItem)) {
                sb.append(macItem.replace(":", ""));
            }

            if (!TextUtils.isEmpty(macItem) && i < size - 1) {
                sb.append("|");
            }
        }
        mac = sb.toString();

        charge = DeviceInfo.getBatteryInfo(context).getCharge();
        netStatus = DeviceInfo.getNetStatus(context);
        sim1 = MiFiManager.instance().getSeedSimBean();
        sim2 = DeviceInfo.getSim2(context);
    }
}
