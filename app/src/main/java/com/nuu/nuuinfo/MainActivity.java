package com.nuu.nuuinfo;

import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;

import com.blakequ.rsa.FileUtils;
import com.blakequ.rsa.RSAProvider;
import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.nuu.MiFiManager;
import com.nuu.proto.Common;
import com.nuu.proto.Nuu;
import com.nuu.socket.PduUtil;
import com.nuu.socket.ReceiveListener;
import com.nuu.utils.DESCrypt;

import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;


public class MainActivity extends Activity implements View.OnClickListener {
    private static final String TAG = "TcpClient";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "MainActivity onCreate");

        setContentView(R.layout.activity_main);
        initView();
    }


    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "MainActivity onResume");
    }


    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "MainActivity onPause");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "MainActivity onDestroy");
    }


    private void initView() {
        findViewById(R.id.btn_update).setOnClickListener(this);
        findViewById(R.id.btn_obtain).setOnClickListener(this);
        findViewById(R.id.btn_report).setOnClickListener(this);
        findViewById(R.id.btn_sim_card_req).setOnClickListener(this);
        findViewById(R.id.btn_sim_data_req).setOnClickListener(this);
        findViewById(R.id.btn_release_sim_card_req).setOnClickListener(this);
        findViewById(R.id.btn_sim_auth_req).setOnClickListener(this);
        findViewById(R.id.btn_report_req).setOnClickListener(this);
        findViewById(R.id.btn_force_release_sim_card_req).setOnClickListener(this);
        findViewById(R.id.btn_des).setOnClickListener(this);
    }


    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.btn_update:
                MiFiManager.instance().checkUpdate();
                break;
            case R.id.btn_obtain:
                MiFiManager.instance().obtainDeviceInfo(null);
                break;
            case R.id.btn_report:
                MiFiManager.instance().reportDeviceInfo();
                break;
            case R.id.btn_sim_card_req:
                reqSimCard();
                break;
            case R.id.btn_sim_data_req:
                reqSimData();
                break;
            case R.id.btn_release_sim_card_req:
                reqReleaseSimCard();
                break;
            case R.id.btn_sim_auth_req:
                reqSimAuth();
                break;
            case R.id.btn_report_req:
                reqReport();
                break;
            case R.id.btn_force_release_sim_card_req:
                reqForceReleaseSimCard();
                break;
            case R.id.btn_des:
                //desTest();
                testRsa();
                testRsa1();
                testRsa2();
                break;
        }
    }


    //commandId 0x01
    private void reqSimCard() {
        String imei = "8a9adcd4";
        int plmn = 46001;
        int lac = 166;
        int ci = 111888899;
        int flag = 0;
        int speed = 1;
        int allocData = 1024 * 1024;
        String exceptImsi = "362693080702174";
        String releaseReason = "信号弱";
        List<Integer> mccs = new ArrayList<>();
        mccs.add(1);
        mccs.add(2);
        mccs.add(3);
        ReceiveListener callback = new ReceiveListener() {
            @Override
            public void OnRec(byte[] body) {
                try {
                    Nuu.GetSimCardRsp resp = Nuu.GetSimCardRsp.parseFrom(body);
                    Nuu.BaseResponse result = resp.getBaseRsp();
                    long test1 = resp.getUniqueKey();
                    String test2 = resp.getImsi();
                    int test3 = resp.getCardType();
                    String test4 = resp.getKi();
                    String test5 = resp.getOpc();
                    int test6 = resp.getAllocData();
                    int test7 = resp.getRenewAllocData();
                    int test8 = resp.getSpeed();
                    List<String> test9 = resp.getOplmnList();
                    String test10 = resp.getRplmn();
                    List<String> test11 = resp.getFplmnList();
                    Common.RadioTechType test12 = resp.getRadioType();
                    List<Common.Apn> test13 = resp.getApnList();
                    int test14 = resp.getMncLen();
                    List<Integer> test15 = resp.getMccsList();
                    int test16 = resp.getEndTime();
                    int test17 = resp.getServerTime();
                    int test18 = resp.getReportInterval();
                    int test19 = resp.getReportDataThreshold();

                    Log.d(TAG, "GetSimCardRsp");
                    Log.d(TAG, "GetSimCardRsp:" + result.getRet() + "#" + result.getErrMsg() + "#" + test1 + "#" + test2 + "#"
                            + test3 + "#" + test4 + "#" + test5 + "#" + test6 + "#" + test7 + "#" + test8 + "#"
                            + test9 + "#" + test10 + "#" + test11 + "#" + test12 + "#" + test13 + "#" + test14 + "#"
                            + test15 + "#" + test16 + "#" + test17 + "#" + test18 + "#" + test19);


                } catch (InvalidProtocolBufferException e) {
                    Log.e(TAG, e.getMessage());
                }
            }
        };

        MiFiManager.instance().getSimCardReq(imei, plmn, lac, ci, flag,
                speed, allocData, exceptImsi, releaseReason, mccs, callback);
    }

    //commandId 0x03
    private void reqSimData() {
        long uniqueKey = 1;
        String imei = "352693080702174";
        String imsi = "204046869125886";
        int report_time = 1565249235;
        long up_stream = 1024000;
        long down_stream = 2048000;
        int alloc_data = 10000;
        int speed = 1;

        int plmn = 46001;
        int lac = 166;
        int ci = 111888899;

        ReceiveListener callback = new ReceiveListener() {
            @Override
            public void OnRec(byte[] body) {
                try {
                    Nuu.GetSimDataRsp resp = Nuu.GetSimDataRsp.parseFrom(body);

                    Nuu.BaseResponse result = resp.getBaseRsp();
                    int test1 = resp.getAllocData();
                    int test2 = resp.getRenewAllocData();
                    int test3 = resp.getSpeed();
                    int test4 = resp.getReportInterval();
                    int test5 = resp.getReportDataThreshold();
                    int test6 = resp.getEndTime();

                    Log.d(TAG, "GetSimDataRsp");
                    Log.d(TAG, "GetSimCardRsp:" + result.getRet() + "#" + result.getErrMsg() +
                            "#" + test1 + "#" + test2 + "#" +
                            test3 + "#" + test4 + "#" + test5 + "#" + test6);


                } catch (InvalidProtocolBufferException e) {
                    Log.e(TAG, e.getMessage());
                }
            }
        };

        MiFiManager.instance().getSimDataReq(uniqueKey, imei, imsi, report_time,
                up_stream, down_stream, alloc_data, speed, plmn, lac, ci, callback);
    }


    //commandId 0x05
    private void reqReleaseSimCard() {
        long uniqueKey = 1;
        String imei = "352693080702174";
        String imsi = "204046869125886";

        int plmn = 46001;
        int lac = 166;
        int ci = 111888899;

        Common.SimcardReleaseReason reason = Common.SimcardReleaseReason.BACKEND_NETWORK_ERROR;

        ReceiveListener callback = new ReceiveListener() {
            @Override
            public void OnRec(byte[] body) {
                try {
                    Nuu.ReleaseSimCardRsp resp = Nuu.ReleaseSimCardRsp.parseFrom(body);

                    Nuu.BaseResponse result = resp.getBaseRsp();

                    Log.d(TAG, "ReleaseSimCard");
                    Log.d(TAG, "GetSimCardRsp:" + result.getRet() + "#" + result.getErrMsg());

                } catch (InvalidProtocolBufferException e) {
                    Log.e(TAG, e.getMessage());
                }
            }
        };

        MiFiManager.instance().releaseSimCardReq(uniqueKey, imei, imsi, plmn, lac, ci,
                reason, callback);
    }


    //commandId 0x07
    private void reqSimAuth() {
        long uniqueKey = 1;
        String imei = "352693080702174";
        String imsi = "204046869125886";

        ByteString auth_data = ByteString.copyFrom("hello".getBytes());


        int plmn = 46001;
        int lac = 166;
        int ci = 111888899;

        ReceiveListener callback = new ReceiveListener() {
            @Override
            public void OnRec(byte[] body) {
                try {
                    Nuu.SimAuthRsp resp = Nuu.SimAuthRsp.parseFrom(body);

                    Nuu.BaseResponse result = resp.getBaseRsp();
                    ByteString test1 = resp.getAuthData();

                    Log.d(TAG, "SimAuthRsp");
                    Log.d(TAG, "GetSimCardRsp:" + result.getRet() + "#" + result.getErrMsg() + "#" + test1);

                } catch (InvalidProtocolBufferException e) {
                    Log.e(TAG, e.getMessage());
                }
            }
        };

        MiFiManager.instance().simAuthReq(uniqueKey, imei, imsi, auth_data, plmn, lac, ci, callback);
    }


    //commandId 0x09
    private void reqReport() {
        long uniqueKey = 1;
        String imei = "352693080702174";
        String imsi = "204046869125886";

        int report_time = 1565249235;
        long up_stream = 1024000;
        long down_stream = 2048000;
        int type = 1;
        int reg_time = 1565250473;

        int plmn = 46001;
        int lac = 166;
        int ci = 111888899;

        Common.NetworkType network_type = Common.NetworkType.NETWORK_TYPE_LTE;
        int rssi = 1;

        ReceiveListener callback = new ReceiveListener() {
            @Override
            public void OnRec(byte[] body) {
                try {
                    Nuu.ReportRsp resp = Nuu.ReportRsp.parseFrom(body);
                    Nuu.BaseResponse result = resp.getBaseRsp();
                    Log.d(TAG, "ReportRsp");
                    Log.d(TAG, "GetSimCardRsp:" + result.getRet() + "#" + result.getErrMsg());


                } catch (InvalidProtocolBufferException e) {
                    Log.e(TAG, e.getMessage());
                }
            }
        };

        MiFiManager.instance().reportReq(uniqueKey, imei, imsi, report_time, up_stream,
                down_stream, type, reg_time, plmn, lac, ci, network_type, rssi, callback);
    }

    //commandId 0xa0
    private void reqForceReleaseSimCard() {
        long uniqueKey = 1;
        String imei = "352693080702174";
        String imsi = "204046869125886";

        long msgid = 1000;

        ReceiveListener callback = new ReceiveListener() {
            @Override
            public void OnRec(byte[] body) {
                try {
                    Nuu.ForceReleaseSimCardRsp resp = Nuu.ForceReleaseSimCardRsp.parseFrom(body);
                    Nuu.BaseResponse result = resp.getBaseRsp();
                    String test1 = resp.getImei();
                    long test2 = resp.getMsgid();

                    Log.d(TAG, "ForceReleaseSimCardRsp");
                    Log.d(TAG, "GetSimCardRsp:" + result.getRet() + "#" + result.getErrMsg() + "#" + test1 + "#" + test2);


                } catch (InvalidProtocolBufferException e) {
                    Log.e(TAG, e.getMessage());
                }
            }
        };

        MiFiManager.instance().forceReleaseSimCardReq(imei, imsi, msgid, callback);
    }


    private void desTest() {
        String content = "ABCDEFG";

        Log.d(TAG, "DESCrypt decrypt before:" + content);

        byte[] data = content.getBytes();

        byte[] byteContent = DESCrypt.instance().encrypt(data);

        String CryptTest = PduUtil.bytes2HexString(byteContent);

        Log.d(TAG, "DESCrypt:" + CryptTest);


        byte[] des = DESCrypt.instance().decrypt(byteContent);

        String desStr = new String(des);

        Log.d(TAG, "DESCrypt decrypt after:" + desStr);


    }


    private void testRsa() {
        Log.d(TAG, "测试RAS 私钥加密 公钥解密:");

        String content = "ABCDEFG";
        byte[] data = content.getBytes();

        Log.d(TAG, "加密前字符串内容:" + content);
        Log.d(TAG, "加密前字节数组内容:" + PduUtil.bytes2HexString(data));

        try {

            String publicKeyPath = Environment.getExternalStorageDirectory().getPath() + "/public.pem";
            String publicKey = FileUtils.readString(new FileInputStream(publicKeyPath));

            String privateKeyPath = Environment.getExternalStorageDirectory().getPath() + "/private.pem";
            String privateKey = FileUtils.readString(new FileInputStream(privateKeyPath));

            byte[] test = RSAProvider.encryptPrivateKey(data, privateKey);

            String CryptTest = PduUtil.bytes2HexString(test);

            Log.d(TAG, "私钥加密后字节数组内容:" + CryptTest);

            byte[] test1 = RSAProvider.decryptPublicKey(test, publicKey);
            String decryptTest = PduUtil.bytes2HexString(test1);

            Log.d(TAG, "公钥解密后字节数组内容:" + decryptTest);
            Log.d(TAG, "公钥解密后字符串内容:" + new String(test1));

        } catch (Exception e) {
            Log.e(TAG, "error" + e.getMessage());
        }
    }


    private void testRsa1() {
        Log.d(TAG, "测试RAS 公钥加密 私钥解密:");

        String content = "ABCDEFG";
        byte[] data = content.getBytes();

        Log.d(TAG, "加密前字符串内容:" + content);
        Log.d(TAG, "加密前字节数组内容:" + PduUtil.bytes2HexString(data));

        try {

            String publicKeyPath = Environment.getExternalStorageDirectory().getPath() + "/public.pem";
            String publicKey = FileUtils.readString(new FileInputStream(publicKeyPath));

            String privateKeyPath = Environment.getExternalStorageDirectory().getPath() + "/private.pem";
            String privateKey = FileUtils.readString(new FileInputStream(privateKeyPath));

            byte[] test = RSAProvider.encryptPublicKey(data, publicKey);

            String CryptTest = PduUtil.bytes2HexString(test);

            Log.d(TAG, "公钥加密后字节数组内容:" + CryptTest);

            byte[] test1 = RSAProvider.decryptPrivateKey(test, privateKey);
            String decryptTest = PduUtil.bytes2HexString(test1);

            Log.d(TAG, "私钥解密后字节数组内容:" + decryptTest);
            Log.d(TAG, "私钥解密后字符串内容:" + new String(test1));

        } catch (Exception e) {
            Log.e(TAG, "error" + e.getMessage());
        }
    }


    private void testRsa2() {
        Log.d(TAG, "测试RAS 私钥签名 公钥验签:");

        String content = "ABCDEFG";
        byte[] data = content.getBytes();

        Log.d(TAG, "私钥签名前字符串内容:" + content);
        Log.d(TAG, "私钥签名前字节数组内容:" + PduUtil.bytes2HexString(data));

        try {

            String publicKeyPath = Environment.getExternalStorageDirectory().getPath() + "/public.pem";
            String publicKey = FileUtils.readString(new FileInputStream(publicKeyPath));

            String privateKeyPath = Environment.getExternalStorageDirectory().getPath() + "/private.pem";
            String privateKey = FileUtils.readString(new FileInputStream(privateKeyPath));

            String test = RSAProvider.sign(data, privateKey);
            Log.d(TAG, "私钥签名后内容:" + test);

            boolean test1 = RSAProvider.verify(data, publicKey, test);

            Log.d(TAG, "公钥验签结果:" + test1);

        } catch (Exception e) {
            Log.e(TAG, "error" + e.getMessage());
        }
    }

}
