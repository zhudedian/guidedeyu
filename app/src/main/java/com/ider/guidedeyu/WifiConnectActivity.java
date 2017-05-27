package com.ider.guidedeyu;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.ider.guidedeyu.util.MyApplication;
import com.ider.guidedeyu.view.BaseRelative;

public class WifiConnectActivity extends FullscreenActivity implements View.OnClickListener{

    private String TAG = "WifiConnectActivity";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wifi_connect);
        ActionBar actionBar= getSupportActionBar();
        if (actionBar!=null){
            actionBar.hide();
        }
        BaseRelative set = (BaseRelative)findViewById(R.id.set);
        BaseRelative pass = (BaseRelative)findViewById(R.id.pass);
        BaseRelative nevernot = (BaseRelative) findViewById(R.id.notice);

        set.setOnClickListener(this);
        pass.setOnClickListener(this);
        nevernot.setOnClickListener(this);
    }
    @Override
    public void onClick(View view){
        Intent intent =new Intent();
        switch (view.getId()){
            case R.id.set:
                intent.setComponent(new ComponentName("com.rk_itvui.settings", "com.rk_itvui.settings.network_settingnew"));
                startActivity(intent);
                finish();
                break;
            case R.id.pass:
                finish();
                break;
            case R.id.notice:
                SharedPreferences preferences = MyApplication.getContext().getSharedPreferences("guide_deyu", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = preferences.edit();
                editor.putBoolean("is_wifi_notice", false);
                editor.apply();
                Log.i(TAG,preferences.getBoolean("is_wifi_notice", true)+"");
                finish();
                break;
            default:
                break;
        }
    }
    public boolean isWifiConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Service.CONNECTIVITY_SERVICE);
        NetworkInfo info = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        return info.isConnected() && info.isAvailable();

    }
}
