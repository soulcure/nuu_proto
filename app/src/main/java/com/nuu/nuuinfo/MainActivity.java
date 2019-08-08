package com.nuu.nuuinfo;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.nuu.MiFiManager;


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
                break;
            case R.id.btn_sim_data_req:
                break;
            case R.id.btn_release_sim_card_req:
                break;
            case R.id.btn_sim_auth_req:
                break;
            case R.id.btn_report_req:
                break;
            case R.id.btn_force_release_sim_card_req:
                break;
        }
    }


}
