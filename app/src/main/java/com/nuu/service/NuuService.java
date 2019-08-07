package com.nuu.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.text.TextUtils;
import android.util.Log;

import com.google.protobuf.GeneratedMessageV3;
import com.nuu.MiFiManager;
import com.nuu.config.AppConfig;
import com.nuu.socket.NotifyListener;
import com.nuu.socket.ReceiveListener;
import com.nuu.socket.TcpClient;
import com.nuu.util.AppUtils;

import java.net.InetSocketAddress;


public class NuuService extends Service {

    private static final String TAG = "TcpClient";

    public static final String REPORT_DEVICE_AM = "com.nuu.service.REPORT_DEVICE_AM";
    public static final String OBTAIN_DEVICE_AM = "com.nuu.service.OBTAIN_DEVICE_AM";
    public static final String CLEAR_LOG_FILE = "com.nuu.service.CLEAR_LOG_FILE";

    public static final String NUU_START_REPORT = "com.nuu.service.START_REPORT";
    public static final String NUU_STOP_REPORT = "com.nuu.service.STOP_REPORT";
    public static final String NUU_LOAD_CONFIG = "com.nuu.service.LOAD_CONFIG";

    private Context mContext;

    /**
     * socket client
     */
    private TcpClient mClient;

    /**
     * Activity绑定后回调
     */
    @Override
    public IBinder onBind(Intent intent) {
        Log.v(TAG, "service onBind...");
        return new NuuServiceBinder();
    }


    /**
     * activity和service通信接口
     */
    public class NuuServiceBinder extends Binder {
        /**
         * 发送socket协议
         *
         * @param msg      消息体
         * @param msgType  发送命令码
         * @param callback 回调
         */
        public void sendProto(GeneratedMessageV3 msg, short msgType,
                              ReceiveListener callback) {
            mClient.sendProto(msg, msgType, callback);
        }

        public void setNotifyListener(NotifyListener listener) {
            mClient.setNotifyListener(listener);
        }

        public void clearNotifyListener(NotifyListener listener) {
            mClient.clearNotifyListener(listener);
        }

        public boolean isLogin() {
            return mClient.isLogin();
        }

        public boolean isConnect() {
            return mClient.isConnect();
        }

        public void reConnect() {
            mClient.reConnect();
        }

        public void close() {
            mClient.close();
            mClient.setCallBack(null);
        }

        /**
         * 数据卡可用
         */
        public void dataSimCardEnable() {
            Log.d(TAG, "dataSimCardEnable");
            if (mClient != null && mClient.isIdle()) {
                Log.d(TAG, "dataSimCardEnable and tcp client connect...");
                mClient.connect(null);
            }
        }
    }


    @Override
    public void onCreate() {
        super.onCreate();
        mContext = getApplicationContext();//this
        mClient = new TcpClient(this);
        createTcp();
        Log.v(TAG, "NuuService is onCreate");
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        String action = null;

        if (intent != null) {
            action = intent.getAction();
        }

        if (!TextUtils.isEmpty(action)) {
            switch (action) {
                case REPORT_DEVICE_AM:
                    break;
                case OBTAIN_DEVICE_AM:
                    break;
                case CLEAR_LOG_FILE:
                    break;
                case NUU_START_REPORT:
                    break;
                case NUU_STOP_REPORT:
                    break;
                case NUU_LOAD_CONFIG:
                    break;
            }
        }

        // 系统就会重新创建这个服务并且调用onStartCommand()方法
        return START_STICKY;
    }


    @Override
    public void onTrimMemory(int level) {

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mClient.close();
        mClient = null;

        MiFiManager.instance().destroy();

        Log.v(TAG, "NuuService is onDestroy");
    }


    private void createTcp() {
        String ip = AppUtils.getStringSharedPreferences(mContext, "IP", AppConfig.getSocketHost());
        int port = AppUtils.getIntSharedPreferences(mContext, "PORT", AppConfig.getSocketPort());

        Log.d(TAG, "createTcp ip:" + ip + "   port:" + port);

        if (!mClient.isConnect()) {
            InetSocketAddress isa = new InetSocketAddress(ip, port);
            mClient.setRemoteAddress(isa);
            TcpClient.IClientListener callback = new TcpClient.IClientListener() {
                @Override
                public void connectSuccess() {
                    tcpLogin();
                }
            };
            mClient.connect(callback);
        }

    }

    /**
     * 发送登录IM服务器请求
     */
    private void tcpLogin() {
        Log.d(TAG, "socket connect success");
    }
}
