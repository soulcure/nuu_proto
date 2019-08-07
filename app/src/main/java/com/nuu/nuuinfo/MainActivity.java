package com.nuu.nuuinfo;

import android.app.Activity;
import android.content.ContentValues;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.nuu.MiFiManager;
import com.nuu.http.IGetListener;
import com.nuu.http.IPostListener;
import com.nuu.http.OkHttpConnector;
import com.nuu.util.ShellUtils;


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
        findViewById(R.id.btn_install).setOnClickListener(this);
        findViewById(R.id.btn_pm).setOnClickListener(this);
        findViewById(R.id.btn_reboot).setOnClickListener(this);
        findViewById(R.id.btn_close).setOnClickListener(this);
        findViewById(R.id.btn_test).setOnClickListener(this);
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
            case R.id.btn_install:
                break;
            case R.id.btn_pm:
                break;
            case R.id.btn_reboot:
                ShellUtils.execCmd("reboot", false);
                break;
            case R.id.btn_close:
                MiFiManager.instance().close();
                break;
            case R.id.btn_test:
                /*String ciStr = MiFiManager.instance().getProp("getprop gsm.cell.cid");
                int a = HexUtil.convertHexStringToInt(ciStr);
                Log.d(TAG, "cid from linux prop:" + a);

                String imsi = MiFiManager.instance().getProp("getprop gsm.imsi.m2.slot0");
                Log.d(TAG, "imsi from linux prop:" + imsi);*/

                //webTest1();

                webTest2();
                break;

        }
    }



    private void webTest2() {

    }

    private void webTest3() {
        String url = "http://localhost:8088/transfer";

        ContentValues params = new ContentValues();
        params.put("itf_name", "query_device_package_info");  //API name
        params.put("trans_serial", "1234cde");  //API name
        params.put("login", "tuser");
        params.put("auth_code", "abcd456");
        params.put("device_sn", "354243074362656");


        OkHttpConnector.httpPost(url, params, new IPostListener() {
            @Override
            public void httpReqResult(String response) {
                Log.d("TcpClient", "webTest3 transfer:" + response);
            }
        });
    }


    private void webTest4() {
        String url = "http://localhost:8088/transfer";

        ContentValues params = new ContentValues();
        params.put("itf_name", "query_device_package_info");  //API name
        params.put("trans_serial", "1234cde");  //API name
        params.put("login", "tuser");
        params.put("auth_code", "abcd456");
        params.put("device_sn", "354243074362656");

        OkHttpConnector.httpPost(url, params, new IPostListener() {
            @Override
            public void httpReqResult(String response) {
                Log.d("TcpClient", "webTest4 transfer:" + response);
            }
        });

    }


    private void webTest5() {
        String url = "http://localhost:8088/transfer";
        //String url = "http://192.168.43.1:8088/transfer";

        OkHttpConnector.httpGet(url, new IGetListener() {
            @Override
            public void httpReqResult(String response) {
                Log.d("TcpClient", "webTest5 transfer:" + response);
            }
        });
    }


}
