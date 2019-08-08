package com.nuu;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.text.TextUtils;
import android.util.Log;

import com.google.protobuf.GeneratedMessageV3;
import com.nuu.config.FileConfig;
import com.nuu.entity.ReportData;
import com.nuu.http.DownloadListener;
import com.nuu.http.OkHttpConnector;
import com.nuu.nuuinfo.BuildConfig;
import com.nuu.proto.DeviceStatus;
import com.nuu.proto.ServerResponse;
import com.nuu.proto.UpdateRequest;
import com.nuu.service.NuuService;
import com.nuu.socket.NotifyListener;
import com.nuu.socket.ProtoCommandId;
import com.nuu.socket.ReceiveListener;
import com.nuu.util.AppUtils;
import com.nuu.util.DeviceInfo;
import com.nuu.util.HexUtil;
import com.nuu.util.ShellUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import cn.gosomo.proxy.IProxyCall;
import cn.gosomo.proxy.IProxyCallback;

public class MiFiManager {
    private static final String TAG = "TcpClient";

    private static final int HANDLER_CLEAR_LOCAL_LOG_FILE = 1;
    private static final int HANDLER_REPORT_DEVICE_INFO = 2;
    private static final int HANDLER_OBTAIN_DEVICE_INFO = 3;
    private static final int HANDLER_NUU_CHECK_UPDATE = 4;
    private static final int HANDLER_NUU_FILE_WATCH = 5;
    private static final int HANDLER_NUU_FILE_RELOAD = 6;

    private static MiFiManager instance;
    private Context mContext;  //ApplicationContext


    private enum BIND_STATUS {
        IDLE, BINDING, BINDED
    }

    private NuuService.NuuServiceBinder nuuService = null;
    private BIND_STATUS binded = BIND_STATUS.IDLE;


    private List<InitListener> mInitListenerList;

    private ProcessHandler mProcessHandler;

    private int netBrock;  //网络断开次数

    private long checkUpdateTime; //检测升级时间

    private volatile ReportData mReportData;

    /**
     * SDK初始化结果监听器
     */
    public interface InitListener {
        void success();

        void fail();
    }


    /**
     * 私有构造函数
     */
    private MiFiManager() {
        mInitListenerList = new ArrayList<>();
        reportDataList = new CopyOnWriteArrayList<>();
    }


    /**
     * 获取呼信sdk单例索引
     *
     * @return
     */
    public static MiFiManager instance() {
        if (instance == null) {
            instance = new MiFiManager();
        }
        return instance;
    }


    /**
     * 呼信sdk初始化
     *
     * @param context
     */
    public void init(Context context) {
        this.init(context, null);
    }


    /**
     * 呼信sdk初始化
     *
     * @param context
     */
    public void init(final Context context, InitListener listener) {
        mContext = context.getApplicationContext();
        netBrock = 0;

        if (listener != null) {
            mInitListenerList.add(listener);
        }

        Log.d(TAG, "MiFiManager init and binded:" + binded.toString());

        if (binded == BIND_STATUS.IDLE) {
            binded = BIND_STATUS.BINDING;
            bindServer(mContext);

            initHandler();
        } else if (binded == BIND_STATUS.BINDING) {

            //do nothing

        } else if (binded == BIND_STATUS.BINDED) {
            for (InitListener item : mInitListenerList) {
                item.success();
            }
            mInitListenerList.clear();
        }
    }


    private void bindServer(Context context) {
        Intent intent = new Intent(context.getApplicationContext(), NuuService.class);
        context.getApplicationContext().bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);

        bindProxyService();

        Log.v(TAG, "MiFiManager in bind server");

    }


    /**
     * 呼信sdk销毁
     *
     * @param
     */
    public void destroy() {
        if (mContext != null) {

            if (binded == BIND_STATUS.BINDED) {
                binded = BIND_STATUS.IDLE;
                mContext.unbindService(serviceConnection);
            }

            if (mProxyCallService != null) {
                try {
                    mProxyCallService.unregisterCallback(mContext.getPackageName(), mProxyCallback);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        }


    }


    /**
     * 判断SDK服务是否已经绑定成功
     *
     * @return
     */
    public boolean isBinded() {
        boolean res = false;
        if (mContext != null && binded == BIND_STATUS.BINDED) {
            res = true;
        }
        return res;
    }


    /**
     * 判断tcp是否连接成功
     *
     * @return
     */
    public boolean isConnect() {
        boolean res = false;
        if (mContext != null && binded == BIND_STATUS.BINDED) {
            res = nuuService.isConnect();
        }
        return res;
    }


    /**
     * tcp 重新连接
     */
    public void reConnect() {
        if (mContext != null && binded == BIND_STATUS.BINDED) {
            nuuService.reConnect();
        }
    }


    /**
     * 关闭tcp连接
     *
     * @return
     */
    public void close() {
        if (mContext != null && binded == BIND_STATUS.BINDED) {
            nuuService.close();
        }
    }


    /**
     * 获取版本
     *
     * @return
     */
    public int version() {
        return AppUtils.getVerCode(mContext);
    }


    private void waitBindingProto(final GeneratedMessageV3 msg, final short commandId,
                                  final ReceiveListener callback) {
        init(mContext, new InitListener() {
            @Override
            public void success() {
                nuuService.sendProto(msg, commandId, callback);
            }


            @Override
            public void fail() {
                String log = "bind server fail!";
            }
        });
    }


    private void waitBindingNotify(final NotifyListener listener) {
        init(mContext, new InitListener() {
            @Override
            public void success() {
                nuuService.setNotifyListener(listener);
            }

            @Override
            public void fail() {
                Log.e(TAG, "bind server fail!");
            }
        });
    }


    /**
     * 发送socket协议
     *
     * @param commandId  命令码
     * @param callback 数据
     */
    public void sendProto(GeneratedMessageV3 msg, short commandId,
                          ReceiveListener callback) {
        if (mContext != null) {
            if (binded == BIND_STATUS.BINDED) {
                nuuService.sendProto(msg, commandId, callback);
            } else {
                waitBindingProto(msg, commandId, callback);
            }
        } else {
            throw new IllegalStateException("nuu manager no init");
        }

    }


    public void setNotifyListener(NotifyListener listener) {
        if (mContext != null) {
            if (binded == BIND_STATUS.BINDED) {
                nuuService.setNotifyListener(listener);
            } else {
                waitBindingNotify(listener);
            }
        } else {
            throw new IllegalStateException("nuu manager no init");
        }

    }

    public void clearNotifyListener(NotifyListener listener) {
        if (mContext != null && binded == BIND_STATUS.BINDED) {
            nuuService.clearNotifyListener(listener);
        }
    }


    public int getNetBrock() {
        return netBrock;
    }

    public void netBrock() {
        netBrock++;
    }

    /**
     * @param callback
     */
    public void checkUpdate(String devId, String curVerCode, String brand,
                            String model, ReceiveListener callback) {
        short msgType = ProtoCommandId.requestDeviceUpgrade();

        UpdateRequest.DeviceUpgradeReq.Builder builder = UpdateRequest.DeviceUpgradeReq.newBuilder();
        builder.setDevId(devId);
        builder.setCurVerCode(curVerCode);
        builder.setBrand(brand);
        builder.setModel(model);
        UpdateRequest.DeviceUpgradeReq msg = builder.build();

        sendProto(msg, msgType, callback);
    }

    /**
     * @param callback
     */
    public void deviceStatus(List<ReportData> list, ReceiveListener callback) {
        if (list == null || list.size() == 0) {
            return;
        }

        short msgType = ProtoCommandId.reportDeviceStatusMsgType();
        DeviceStatus.ReportDeviceStatusInfoReq.Builder reqBuilder = DeviceStatus.ReportDeviceStatusInfoReq.newBuilder();

        for (ReportData item : list) {
            DeviceStatus.ReportDeviceStatusReq.Builder builder = DeviceStatus.ReportDeviceStatusReq.newBuilder();
            builder.setDeviceId(item.getDeviceId());//set device id
            builder.setMac(item.getMac());//设置mac
            builder.setIp(item.getIp());//设置ip
            builder.setDeviceStatus(item.getDeviceStatus());
            builder.setUtc(item.getUnixTime());

            ReportData.Sim1Bean sim1Bean = item.getSim1();
            if (sim1Bean != null) {
                DeviceStatus.SimCardSlot.Builder b = DeviceStatus.SimCardSlot.newBuilder();
                b.setCi(sim1Bean.getCi());
                b.setImsi(sim1Bean.getImsi());
                b.setLac(sim1Bean.getLac());
                try {
                    int plmn = Integer.parseInt(sim1Bean.getPlmn());
                    b.setPlmn(plmn);
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }
                b.setPsc(sim1Bean.getPsc());
                b.setSignal(sim1Bean.getSignal());

                int netMode = sim1Bean.getNetMode();
                b.setMode(DeviceStatus.NetworkMode.forNumber(netMode));
                builder.setSlot1(b);
            }

            ReportData.Sim2Bean sim2Bean = item.getSim2();
            if (sim2Bean != null) {
                DeviceStatus.SimCardSlot.Builder b = DeviceStatus.SimCardSlot.newBuilder();
                b.setCi(sim2Bean.getCi());
                b.setImsi(sim2Bean.getImsi());
                b.setLac(sim2Bean.getLac());
                try {
                    b.setPlmn(Integer.parseInt(sim2Bean.getPlmn()));
                } catch (NumberFormatException e) {
                    b.setPlmn(0);
                    e.printStackTrace();
                }
                b.setPsc(sim2Bean.getPsc());
                b.setSignal(sim2Bean.getSignal());

                int netMode = sim2Bean.getNetMode();
                b.setMode(DeviceStatus.NetworkMode.forNumber(netMode));
                builder.setSlot2(b);
            }
            reqBuilder.addDeviceStatus(builder);
        }

        if (BuildConfig.DEBUG) {
            Log.d(TAG, "Report Device Builder:" + reqBuilder.build().toString());
        }

        DeviceStatus.ReportDeviceStatusInfoReq msg = reqBuilder.build();
        sendProto(msg, msgType, callback);
    }

    public String getProp(String key) {
        String msg = "";
        ShellUtils.CommandResult result = ShellUtils.execCmd(key, false);
        if (result.result == 0) {
            msg = result.successMsg;
        }

        return msg;
    }

    /**
     * bind service callback
     */
    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            if (service instanceof NuuService.NuuServiceBinder) {
                nuuService = (NuuService.NuuServiceBinder) service;
                binded = BIND_STATUS.BINDED;
                for (InitListener item : mInitListenerList) {
                    item.success();
                }
                mInitListenerList.clear();
                Log.v(TAG, "Service Connected...");
            }
        }

        // 连接服务失败后，该方法被调用
        @Override
        public void onServiceDisconnected(ComponentName name) {
            nuuService = null;
            binded = BIND_STATUS.IDLE;
            for (InitListener item : mInitListenerList) {
                item.fail();
            }
            mInitListenerList.clear();
            Log.e(TAG, "Service Failed...");
        }
    };


    private final static String PROXY_SERVICE = "cn.gosomo.proxy.ProxyService";
    private final static String PROXY_SERVICE_PACKAGE = "cn.gosomo.proxy";

    private IProxyCall mProxyCallService;

    /**
     * 定义sim1卡为种子卡信息
     */
    private ReportData.Sim1Bean seedSimBean = new ReportData.Sim1Bean();


    public ReportData.Sim1Bean getSeedSimBean() {
        if (seedSimBean.getCi() <= 0) {
            String ciStr = getProp("getprop gsm.cell.cid");
            int ci = HexUtil.convertHexStringToInt(ciStr);
            seedSimBean.setCi(ci);
            Log.d(TAG, "set cid from linux prop:" + ci);
        }

        if (seedSimBean.getLac() <= 0) {
            String lacStr = getProp("getprop gsm.cell.lac");
            int lac = HexUtil.convertHexStringToInt(lacStr);
            seedSimBean.setLac(lac);
            Log.d(TAG, "set lac from linux prop:" + lac);
        }

        if (seedSimBean.getPsc() <= 0) {
            String pscStr = getProp("getprop gsm.cell.psc");
            int psc = HexUtil.convertHexStringToInt(pscStr);
            seedSimBean.setPsc(psc);
            Log.d(TAG, "set psc from linux prop:" + psc);
        }

        if (TextUtils.isEmpty(seedSimBean.getPlmn())) {
            String plmn = getProp("getprop gsm.cell.plmn");
            seedSimBean.setPlmn(plmn);
            Log.d(TAG, "set plmn from linux prop:" + plmn);
        }

        if (TextUtils.isEmpty(seedSimBean.getImsi())) {
            String imsi = getProp("getprop gsm.imsi.m2.slot0");
            seedSimBean.setImsi(imsi);
            Log.d(TAG, "set imsi from linux prop:" + imsi);
        }

        return seedSimBean;
    }


    private void bindProxyService() {
        Intent intent = new Intent(PROXY_SERVICE);
        intent.setClassName(PROXY_SERVICE_PACKAGE, PROXY_SERVICE);
        mContext.bindService(intent, mProxyCallConn, Context.BIND_AUTO_CREATE);
    }


    private ServiceConnection mProxyCallConn = new ServiceConnection() {
        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.d(TAG, "proxy server onServiceDisconnected");
            mProxyCallService = null;
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d(TAG, "proxy server onServiceConnected");
            mProxyCallService = IProxyCall.Stub.asInterface(service);
            try {
                String packageName = mContext.getPackageName();
                //注册了回调
                mProxyCallService.registerCallback(packageName, mProxyCallback);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    };


    /**
     * board:1 为数据卡板,board:2 为种子卡板
     * slot:0 卡板默认都使用卡槽0，slot:1卡槽，两个卡板都未使用
     */
    private IProxyCallback mProxyCallback = new IProxyCallback.Stub() {
        @Override
        public void onEventSys(int board, int event, int value) {
            Log.d(TAG, "onEventSys board:" + board + "@event:" + event + "@value:" + value);
        }

        @Override
        public void onSimcardStateChange(int board, int slot, String imsi) throws RemoteException {
            Log.d(TAG, "onSimcardStateChange board:" + board + "@slot:" + slot + "@imsi:" + imsi);
            if (slot == 0) {  //状态可用
                if (board == 1) {  //数据卡

                } else if (board == 2) { //种子卡
                    String tempImsi = imsi == null ? "" : imsi;
                    seedSimBean.setImsi(tempImsi);
                }
            }
        }

        @Override
        public void onLocationChange(int board, int slot, String plmn, int lac, int cid, int psc) throws RemoteException {
            Log.d(TAG, "onLocationChange board:" + board + "@slot:" + slot + "@plmn:" + plmn + "@lac:" + lac + "@cid:" + cid + "@psc:" + psc);
            if (slot == 0) {  //状态可用
                if (board == 1) {  //数据卡

                } else if (board == 2) { //种子卡
                    String tempPlmn = plmn == null ? "" : plmn;
                    seedSimBean.setPlmn(tempPlmn);
                    seedSimBean.setCi(cid);
                    seedSimBean.setLac(lac);
                    seedSimBean.setPsc(psc);
                }
            }

        }

        @Override
        public void onServiceStateChange(int board, int slot, int serviceState, int networkType, int rssi) throws RemoteException {
            Log.d(TAG, "onServiceStateChange board:" + board + "@slot:" + slot + "@serviceState:" + serviceState + "@networkType:" + networkType + "@rssi:" + rssi);
            //onServiceStateChange board:1@slot:0@serviceState:0@networkType:13@rssi:-87  //数据卡准备完毕

            if (slot == 0) {  //状态可用
                if (board == 1) {  //数据卡

                } else if (board == 2) { //种子卡
                    seedSimBean.setSignal(rssi);
                    seedSimBean.setNetMode(networkType);
                }
            }

        }

        @Override
        public void onDataStateChange(int board, int slot, int dataState, int networkType, int rssi) throws RemoteException {
            Log.d(TAG, "onDataStateChange board:" + board + "@slot:" + slot + "@dataState:" + dataState + "@networkType:" + networkType + "@rssi:" + rssi);
            //onDataStateChange board:1@slot:0@dataState:2@networkType:13@rssi:-85  //数据卡准备完毕

            if (slot == 0) {  //状态可用
                if (board == 1) {  //数据卡
                    if (dataState == 2) { //数据卡数据可用
                        if (nuuService != null) {
                            nuuService.dataSimCardEnable();
                        }
                    }

                } else if (board == 2) { //种子卡
                    seedSimBean.setNetMode(networkType);
                    seedSimBean.setSignal(rssi);
                }
            }
        }

        @Override
        public void onSignalStrengthChange(int board, int slot, int rssi) throws RemoteException {
            Log.d(TAG, "onSignalStrengthChange board:" + board + "@slot:" + slot + "@rssi:" + rssi);
            if (slot == 0) {  //状态可用
                if (board == 1) {  //数据卡

                } else if (board == 2) { //种子卡
                    seedSimBean.setSignal(rssi);
                }
            }
        }


    };


    /**
     * 线程初始化
     */
    private void initHandler() {
        if (mProcessHandler == null) {
            HandlerThread handlerThread = new HandlerThread(
                    "handler looper Thread");
            handlerThread.start();
            mProcessHandler = new ProcessHandler(handlerThread.getLooper());
        }
    }

    /**
     * 子线程handler,looper
     *
     * @author Administrator
     */
    private class ProcessHandler extends Handler {

        public ProcessHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case HANDLER_REPORT_DEVICE_INFO:
                    sendDeviceInfoList();
                    break;
                case HANDLER_OBTAIN_DEVICE_INFO:
                    initDeviceInfo();
                    break;
                case HANDLER_NUU_CHECK_UPDATE:
                    checkUpdate();
                    break;
                case HANDLER_NUU_FILE_RELOAD:
                    Intent in = new Intent(mContext, NuuService.class);
                    in.setAction(NuuService.NUU_LOAD_CONFIG);
                    mContext.startService(in);
                    break;
                default:
                    break;
            }

        }

    }



    /////////////////////////////////////////////

    public interface OnDeviceInfo {
        void onSuccess(String json);
    }

    private OnDeviceInfo mOnDeviceInfo;

    private List<ReportData> reportDataList;

    public String getDeviceInfo() {
        if (mReportData == null) {
            mReportData = new ReportData(mContext);
        } else {
            mReportData.refresh(mContext);
        }
        String json = mReportData.toJson();
        if (!AppUtils.isJson(json)) {
            Log.e(TAG, "ReportData to json error:" + json);
            json = mReportData.toString();
        }

        return json;
    }


    public void nuuCheckUpdate() {
        long curTime = System.currentTimeMillis();
        if (curTime - checkUpdateTime < 3600 * 1000) {
            Log.v(TAG, "check nuuinfo update Less than 1 hour");
            return;
        }

        Log.v(TAG, "check nuuinfo update");
        checkUpdateTime = curTime;
        mProcessHandler.sendEmptyMessageDelayed(HANDLER_NUU_CHECK_UPDATE, 30 * 1000); //delay 30秒钟 请求更新配置
    }


    public void clearLocalLogFile() {
        Log.v(TAG, "clear local log file");
        mProcessHandler.sendEmptyMessage(HANDLER_CLEAR_LOCAL_LOG_FILE);
    }


    public void obtainDeviceInfo(OnDeviceInfo callback) {
        this.mOnDeviceInfo = callback;

        mProcessHandler.sendEmptyMessage(HANDLER_OBTAIN_DEVICE_INFO);
    }


    public void reportDeviceInfo() {
        mProcessHandler.sendEmptyMessage(HANDLER_REPORT_DEVICE_INFO);

        nuuCheckUpdate();//检查更新每小时检查一次，使用上报设备信息定时器触发
    }


    private void initDeviceInfo() {
        ReportData data = new ReportData(mContext);
        mReportData = data;

        boolean isAdd = true;
        for (ReportData item : reportDataList) {
            if (item.equals(data)) {
                isAdd = false;

                Log.d(TAG, "ReportData  with acquaintance time=" + data.getUnixTime());
                break;
            }
        }
        if (isAdd) {
            if (BuildConfig.DEBUG) {
                Log.d(TAG, "reportDataList add ReportData:" + data.toJson());
            }
            reportDataList.add(data);
        }


        if (mOnDeviceInfo != null) {
            mOnDeviceInfo.onSuccess(data.toString());
        }
    }


    private void sendDeviceInfoList() {
        if (reportDataList == null || reportDataList.size() == 0) {
            return;
        }
        int times;
        if (reportDataList.size() / 5 == 0) {
            times = reportDataList.size() / 5;
        } else {
            times = reportDataList.size() / 5 + 1;
        }
        Log.d(TAG, "sendDeviceInfoList by times:" + times);

        List<List<ReportData>> temp = new ArrayList<>();

        for (int i = 0; i < times; i++) {
            List<ReportData> item = new ArrayList<>(5);
            int isFinish = 0;

            for (int index = i * 5; index < reportDataList.size(); index++) {
                if (index % 5 == 0) {
                    isFinish++;
                }
                if (isFinish > 1) {
                    break;
                }
                item.add(reportDataList.get(index));
            }
            temp.add(item);
        }


        for (List<ReportData> item : temp) {
            Log.d(TAG, "sendDeviceInfoList....");
            sendDeviceInfoList(item);
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Log.e(TAG, e.getMessage());
            }

        }
    }

    private void sendDeviceInfoList(final List<ReportData> sendList) {
        ReceiveListener callback = new ReceiveListener() {
            @Override
            public void OnRec(byte[] body) {
                try {
                    final ServerResponse.ReportDeviceStatusInfoResp ack = ServerResponse.
                            ReportDeviceStatusInfoResp.parseFrom(body);
                    StringBuilder sb = new StringBuilder();
                    sb.append("sendDeviceInfoList:").append(ack.toString());
                    sb.append("reportDataList clear size before=").append(reportDataList.size());

                    reportDataList.removeAll(sendList);

                    sb.append("reportDataList clear size after=").append(reportDataList.size());
                    sb.append("  and clear size=").append(sendList.size());

                    Log.d(TAG, sb.toString());
                } catch (ExceptionInInitializerError e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                } catch (NoClassDefFoundError e) {
                    e.printStackTrace();
                }
            }
        };
        deviceStatus(sendList, callback);
        Log.d(TAG, "sendDeviceInfoList size=" + sendList.size());
    }

    public void checkUpdate() {
        String devId = DeviceInfo.getDeviceId();
        String curVerCode = String.valueOf(AppUtils.getVerCode(mContext));
        String brand = DeviceInfo.getBrand();
        String model = DeviceInfo.getModel();

        Log.d(TAG, "nuu info check update curVerCode:" + curVerCode);

        ReceiveListener callback = new ReceiveListener() {
            @Override
            public void OnRec(byte[] body) {
                try {
                    final UpdateRequest.DeviceUpgradeResp ack = UpdateRequest.DeviceUpgradeResp.parseFrom(body);
                    String newVerCode = ack.getNewVerCode();
                    String url = ack.getUrl();
                    boolean result = ack.getResult();
                    Log.d(TAG, "checkUpdate:" + ack.toString());

                    if (result && !TextUtils.isEmpty(url)) {
                        String fileName = "nuuinfo_" + newVerCode + ".apk"; //nuuinfo_5.apk
                        final String filePath = FileConfig.getApkDownLoadPath();

                        String reqUrl;
                        if (url.contains("47.91.250.107")) { //107测试服下载，使用php配置下载
                            String deviceId = DeviceInfo.getDeviceId();
                            String token = AppUtils.md5("@com.nuu@" + deviceId);
                            reqUrl = url + "?hwid=" + deviceId + "&vercode=" + newVerCode + "&token=" + token;
                        } else {  //95下载服务器使用nginx配置静态文件
                            reqUrl = url + File.separator + fileName;
                        }

                        Log.d(TAG, "reqUrl:" + reqUrl);

                        OkHttpConnector.httpDownload(reqUrl, null,
                                filePath, fileName, new DownloadListener() {
                                    @Override
                                    public void onProgress(int cur, int total) {
                                        int rate = (cur * 100) / total;
                                        Log.d(TAG, "onProgress:" + rate + "%");
                                    }

                                    @Override
                                    public void onFail(String err) {
                                        Log.e(TAG, "download file onFail");
                                    }

                                    @Override
                                    public void onSuccess(String path) {
                                        Log.d(TAG, "download file onSuccess and mute install");
                                    }
                                });

                    }

                } catch (ExceptionInInitializerError e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                } catch (NoClassDefFoundError e) {
                    e.printStackTrace();
                }
            }
        };
        checkUpdate(devId, curVerCode, brand, model, callback);
    }
}
