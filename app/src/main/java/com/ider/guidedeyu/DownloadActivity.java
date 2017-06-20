package com.ider.guidedeyu;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.ider.guidedeyu.util.CustomerHttpClient;
import com.ider.guidedeyu.util.HTTPFileDownloadTask;
import com.ider.guidedeyu.util.UpdataInfo;
import com.ider.guidedeyu.view.BaseRelative;

import org.apache.http.client.HttpClient;

import java.net.URI;
import java.net.URISyntaxException;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import static android.R.attr.data;
import static android.os.Build.VERSION_CODES.M;
import static android.print.PrintJobInfo.STATE_STARTED;

public class DownloadActivity extends FullscreenActivity implements View.OnClickListener,View.OnKeyListener{
    private String TAG = "DownloadActivity";
    private HTTPFileDownloadTask mHttpTask;
    private HttpClient mHttpClient;
    private TextView updating;
    private UpdataInfo info;
    private URI mHttpUri;
    private Context mContext;
    private BaseRelative toupdate;
    private DownloadActivity.HTTPdownloadHandler mHttpDownloadHandler;
    private ProgressBar mProgressBar;
    private TextView mCompletedTV,mDownloadRateTV,mRemainTimeTV;
    public static String FLASH_ROOT = Environment.getExternalStorageDirectory().getAbsolutePath();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_download);
        Intent intent = getIntent();
        String url = intent.getStringExtra("url");
        ActionBar actionBar= getSupportActionBar();
        if (actionBar!=null){
            actionBar.hide();
        }
        mContext = this;
        updating = (TextView) findViewById(R.id.updating);
        updating.setOnKeyListener(this);
        toupdate = (BaseRelative) findViewById(R.id.to_update);
        toupdate.setOnClickListener(this);

        mCompletedTV = (TextView)findViewById(R.id.progress_completed);
        mDownloadRateTV = (TextView)findViewById(R.id.download_info_rate);
        mRemainTimeTV = (TextView)findViewById(R.id.download_info_remaining);
        mProgressBar = (ProgressBar)findViewById(R.id.progress_horizontal);
        mHttpClient = CustomerHttpClient.getHttpClient();
        mHttpDownloadHandler = new DownloadActivity.HTTPdownloadHandler();

        try {
//            mHttpUri = new URI("http://192.168.2.20:8080/otaupdate/xml/download/up/1.zip");
            mHttpUri = new URI(url);
        } catch (URISyntaxException e) {
            Toast.makeText(DownloadActivity.this,"未能连接到服务器，请稍后再试",Toast.LENGTH_LONG).show();
            finish();
            e.printStackTrace();
        }
        Log.i(TAG,FLASH_ROOT);
        mHttpTask = new HTTPFileDownloadTask(mHttpClient, mHttpUri,"/mnt/internal_sd/", "update.zip", 4);
        mHttpTask.setProgressHandler(mHttpDownloadHandler);
        mHttpTask.start();
    }
    @Override
    public void onClick(View view){
        if (view.getId()==R.id.to_update){
            Intent intent = new Intent();
            intent.setComponent(new ComponentName("android.rockchip.update.service", "android.rockchip.update.service.UpdateAndRebootActivity"));
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra("android.rockchip.update.extra.IMAGE_PATH", "/mnt/internal_sd/update.zip");
            startActivity(intent);
        }
    }
    private void setDownloadInfoViews(long contentLength, long receivedCount, long receivedPerSecond) {
        int percent = (int)(receivedCount * 100 / contentLength);
        mCompletedTV.setText(String.valueOf(percent) + "%");

        String rate = "";
        if(receivedPerSecond < 1024) {
            rate = String.valueOf(receivedPerSecond) + "B/S";
        }else if(receivedPerSecond/1024 > 0 && receivedPerSecond/1024/1024 == 0) {
            rate = String.valueOf(receivedPerSecond/1024) + "KB/S";
        }else if(receivedPerSecond/1024/1024 > 0) {
            rate = String.valueOf(receivedPerSecond/1024/1024) + "MB/S";
        }

        mDownloadRateTV.setText(rate);

        int remainSecond = (receivedPerSecond == 0) ? 0 : (int)((contentLength - receivedCount) / receivedPerSecond);
        String remainSecondString = "";
        if(remainSecond < 60) {
            remainSecondString = String.valueOf(remainSecond) + "s";
        }else if(remainSecond/60 > 0 && remainSecond/60/60 == 0) {
            remainSecondString = String.valueOf(remainSecond/60) + "min";
        }else if(remainSecond/60/60 > 0) {
            remainSecondString = String.valueOf(remainSecond/60/60) + "h";
        }
        remainSecondString = mContext.getString(R.string.remain_time) + " " + remainSecondString;
        mRemainTimeTV.setText(remainSecondString);
    }

    @Override
    public boolean onKey(View view, int keyCode, KeyEvent keyEvent) {
        if(keyCode == KeyEvent.KEYCODE_BACK) {

            return true;
        }
        return false;
    }


    private class HTTPdownloadHandler extends Handler {

        @Override
        public void handleMessage(Message msg) {
            int whatMassage = msg.what;
            switch(whatMassage) {
                case HTTPFileDownloadTask.PROGRESS_UPDATE : {
                    Bundle b = msg.getData();
                    long receivedCount = b.getLong("ReceivedCount", 0);
                    long contentLength = b.getLong("ContentLength", 0);
                    long receivedPerSecond = b.getLong("ReceivedPerSecond", 0);
                    int percent = (int)(receivedCount * 100 / contentLength);
                    Log.d(TAG, "percent = " + percent);

                    setDownloadInfoViews(contentLength, receivedCount, receivedPerSecond);
                    mProgressBar.setProgress(percent);

                }
                break;
                case HTTPFileDownloadTask.PROGRESS_DOWNLOAD_COMPLETE : {
//                    toupdate.setVisibility(View.VISIBLE);
                    Intent intent = new Intent();
                    intent.setComponent(new ComponentName("android.rockchip.update.service", "android.rockchip.update.service.UpdateAndRebootActivity"));
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.putExtra("android.rockchip.update.extra.IMAGE_PATH", "/mnt/internal_sd/update.zip");
                    startActivity(intent);
                    finish();
                }
                break;
                case HTTPFileDownloadTask.PROGRESS_DOWNLOAD_FAILED : {
                    Toast.makeText(DownloadActivity.this,"未能连接到服务器，请稍后再试",Toast.LENGTH_LONG).show();
                    finish();
                }
                break;
//                case HTTPFileDownloadTask.PROGRESS_START_COMPLETE : {
//                    //mTxtState.setText("");
//                    mState = STATE_STARTED;
//                    mBtnControl.setText(getString(R.string.pause));
//                    mBtnControl.setClickable(true);
//                    mBtnControl.setFocusable(true);
//                    mBtnCancel.setClickable(true);
//                    mBtnCancel.setFocusable(true);
//                    setNotificationStrat();
//                    showNotification();
//                    mWakeLock.acquire();
//                }
//                break;
//                case HTTPFileDownloadTask.PROGRESS_STOP_COMPLETE : {
//                    Bundle b  = msg.getData();
//                    int errCode = b.getInt("err", HTTPFileDownloadTask.ERR_NOERR);
//                    if(errCode == HTTPFileDownloadTask.ERR_CONNECT_TIMEOUT) {
//                        //mTxtState.setText("State: ERR_CONNECT_TIMEOUT");
//                        Toast.makeText(getApplicationContext(), getString(R.string.error_display), Toast.LENGTH_LONG).show();
//                    }else if(errCode == HTTPFileDownloadTask.ERR_FILELENGTH_NOMATCH) {
//                        //mTxtState.setText("State: ERR_FILELENGTH_NOMATCH");
//                    }else if(errCode == HTTPFileDownloadTask.ERR_NOT_EXISTS) {
//                        //mTxtState.setText("State: ERR_NOT_EXISTS");
//                        Toast.makeText(getApplicationContext(), getString(R.string.error_display), Toast.LENGTH_LONG).show();
//                    }else if(errCode == HTTPFileDownloadTask.ERR_REQUEST_STOP) {
//                        //mTxtState.setText("State: ERR_REQUEST_STOP");
//                    }else if(errCode == HTTPFileDownloadTask.ERR_UNKNOWN) {
//                        Toast.makeText(getApplicationContext(), getString(R.string.error_display), Toast.LENGTH_LONG).show();
//                    }
//
//                    mState = STATE_STOPED;
//                    mRemainTimeTV.setText("");
//                    mDownloadRateTV.setText("");
//                    mBtnControl.setText(getString(R.string.retry));
//                    mBtnControl.setClickable(true);
//                    mBtnControl.setFocusable(true);
//                    mBtnCancel.setClickable(true);
//                    mBtnCancel.setFocusable(true);
//                    setNotificationPause();
//                    showNotification();
//                    if(mWakeLock.isHeld()){
//                        mWakeLock.release();
//                    }
//
//                    if(mIsCancelDownload) {
//                        finish();
//                    }
//                }
//                break;
                default:
                    break;
            }
        }
    }
}
