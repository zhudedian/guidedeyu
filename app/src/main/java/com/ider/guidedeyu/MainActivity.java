package com.ider.guidedeyu;

import android.app.Service;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.util.Log;
import android.view.View;

import com.google.gson.Gson;
import com.ider.guidedeyu.view.BaseRelative;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends FullscreenActivity implements View.OnClickListener{
    private String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ActionBar actionBar= getSupportActionBar();
        if (actionBar!=null){
            actionBar.hide();
        }
        BaseRelative set = (BaseRelative)findViewById(R.id.set);
        BaseRelative pass = (BaseRelative)findViewById(R.id.pass);
        BaseRelative notice = (BaseRelative) findViewById(R.id.notice);

        set.setOnClickListener(this);
        pass.setOnClickListener(this);
        notice.setOnClickListener(this);
    }
    @Override
    public void onClick(View view){
        Intent intent ;
        switch (view.getId()){
            case R.id.set:
                intent = new Intent(MainActivity.this,WifiConnectActivity.class);
                startActivity(intent);
                break;
            case R.id.pass:
                intent = new Intent(MainActivity.this,UpdataActivity.class);
                startActivity(intent);
                break;
            case R.id.notice:
                Log.i(TAG,getVersion());
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
    private void sendRequestWithOkHttp(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                try{
                    OkHttpClient client = new OkHttpClient();
                    Request request = new Request.Builder()
                                                  .url("http://api.dy-icloud.com/system/romupdate")
                                                  .build();
                    Response response = client.newCall(request).execute();
                    String resposeData = response.body().string();
                    parseJSONWithGSON(resposeData);
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }).start();
    }
    private void parseJSONWithGSON(String jsonData){
        Gson gson = new Gson();
//        List<String> infos = gson.fromJson(jsonData,new TypeToken<List<String>>(){}.getType());
        Log.i(TAG,jsonData);
    }


    private void showResponse(final String response){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Log.i(TAG,response);
            }
        });
    }
    private String getVersion(){
        String version = null;
        try {
            Method method = Build.class.getDeclaredMethod("getString", String.class);
            method.setAccessible(true);
            version = (String) method.invoke(new Build(), "ro.product.firmware");
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        Log.d(TAG, "get display:" + version);
        return version;
    }
}
