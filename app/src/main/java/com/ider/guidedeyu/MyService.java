package com.ider.guidedeyu;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.ider.guidedeyu.util.MyApplication;
import com.ider.guidedeyu.util.UpdataInfo;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MyService extends Service {
    private String TAG = "MyService";
    private UpdataInfo info;

    public MyService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId){
        if (!isWifiConnected()&&!isEthernetConnected()&&isWifiNotice()){
            Intent intent1 = new Intent(MyApplication.getContext(),WifiConnectActivity.class);
            intent1.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            MyApplication.getContext().startActivity(intent1);
        }else if (isUpdateNotice()){
            sendRequestWithOkHttp();
        }
        return super.onStartCommand(intent,flags,startId);
    }
    private boolean isWifiNotice(){
        SharedPreferences preferences = MyApplication.getContext().getSharedPreferences("guide_deyu", Context.MODE_PRIVATE);
        boolean isWifiNotice = preferences.getBoolean("is_wifi_notice", true);
        return isWifiNotice;
    }
    private boolean isUpdateNotice(){
        SharedPreferences preferences = MyApplication.getContext().getSharedPreferences("guide_deyu", Context.MODE_PRIVATE);
        boolean isUpdateNotice = preferences.getBoolean("is_update_notice", true);
        return isUpdateNotice;
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
        info = gson.fromJson(jsonData,new TypeToken<UpdataInfo>(){}.getType());
        int serversion = Integer.parseInt(info.getVersioncode());
        int localversion = Integer.parseInt(getVersion());
        Log.i(TAG,info.getVersioncode()+getVersion());
        if (localversion<serversion){
            Intent intent = new Intent(MyApplication.getContext(),UpdataActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            MyApplication.getContext().startActivity(intent);
        }else if (localversion==serversion){
            File f = new File("mnt/internal_sd/update.zip");
            if (f.exists()){
                f.delete();
            }
        }


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
    public boolean isEthernetConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Service.CONNECTIVITY_SERVICE);
        NetworkInfo info = cm.getNetworkInfo(ConnectivityManager.TYPE_ETHERNET);
        return info.isConnected() && info.isAvailable();
    }
    public boolean isWifiConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Service.CONNECTIVITY_SERVICE);
        NetworkInfo info = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        return info.isConnected() && info.isAvailable();

    }
}
