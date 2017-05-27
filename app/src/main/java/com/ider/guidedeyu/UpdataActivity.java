package com.ider.guidedeyu;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.ider.guidedeyu.util.CustomerHttpClient;
import com.ider.guidedeyu.util.HTTPFileDownloadTask;
import com.ider.guidedeyu.util.MyApplication;
import com.ider.guidedeyu.util.UpdataInfo;
import com.ider.guidedeyu.view.BaseRelative;

import org.apache.http.client.HttpClient;

import java.net.URI;
import java.net.URISyntaxException;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import static android.R.attr.reparent;
import static android.R.attr.tag;

public class UpdataActivity extends FullscreenActivity implements View.OnClickListener{

    private String TAG = "UpdataActivity";
    private TextView textView ;
    private UpdataInfo info;
    private BaseRelative updata,pass,notice;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_updata);
        ActionBar actionBar= getSupportActionBar();
        if (actionBar!=null){
            actionBar.hide();
        }
        textView = (TextView) findViewById(R.id.remark);
        updata = (BaseRelative) findViewById(R.id.updata);
        pass = (BaseRelative) findViewById(R.id.pass);
        notice = (BaseRelative) findViewById(R.id.notice);
        updata.setOnClickListener(this);
        pass.setOnClickListener(this);
        notice.setOnClickListener(this);
        sendRequestWithOkHttp();
    }
    @Override
    public void onClick(View view){
        switch (view.getId()){
            case R.id.updata:
                Intent intent = new Intent(UpdataActivity.this,DownloadActivity.class);
                intent.putExtra("url",info.getUrl());
                startActivity(intent);
                finish();
                break;
            case R.id.pass:
                finish();
                break;
            case R.id.notice:
                SharedPreferences preferences = MyApplication.getContext().getSharedPreferences("guide_deyu", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = preferences.edit();
                editor.putBoolean("is_update_notice", false);
                editor.apply();
                Log.i(TAG,preferences.getBoolean("is_update_notice", true)+"");
                finish();
                break;
            default:
                break;
        }
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
                    Log.i(TAG,resposeData);
                    parseJSONWithGSON(resposeData);
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }).start();
    }
    private void parseJSONWithGSON(final String jsonData){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Gson gson = new Gson();
                info = gson.fromJson(jsonData,new TypeToken<UpdataInfo>(){}.getType());
                textView.setText(info.getRemark());
            }
        });

    }


}
