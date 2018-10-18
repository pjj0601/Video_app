package com.example.administrator.video_app;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.MediaController;
import android.widget.Toast;
import android.widget.VideoView;

import com.example.administrator.video_app.load_util.Loading_view;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.entity.StringEntity;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URL;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends Activity {

    int i = 0;
    private String urlStream;
    private String token;
    private String video_id;
    private String replace;
    private String rtsp_name;
    private String rtsp_path;
    private String msg;

    private EditText ed_rtsp;
    private Button btn_submit;
    private VideoView myVideoView;

    private Handler mhandler;
    private Timer timer;

    private Loading_view loading;

    private URL url;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        myVideoView = findViewById(R.id.myVideoView);
        ed_rtsp = findViewById(R.id.ed_rtsp);
        btn_submit = findViewById(R.id.btn_submit);
        loading = new Loading_view(this,R.style.CustomDialog);

//        btn_get_token.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                get_token();
//
//            }
//        });
//        btn_get_m3u8.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                try {
//                    rtsp_2_m3u8(ed_rtsp.getText().toString());
//
//
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//            }
//        });
//        btn_keep.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//
//                mhandler = new Handler() {
//                    @Override
//                    public void handleMessage(Message msg) {
//                        if (msg.what == 1){
//                            //do something
//                            keep_contact(video_id);
//                        }
//                        super.handleMessage(msg);
//                    }
//                };
//
//                Timer timer = new Timer();
//                TimerTask timerTask = new TimerTask() {
//                    @Override
//                    public void run() {
//                        Message message = new Message();
//                        message.what = 1;
//                        mhandler.sendMessage(message);
//                    }
//                };
//
//                timer.schedule(timerTask,0,10000);
//                //视频播放
//                Handler handler = new Handler();
//                handler.postDelayed(new Runnable() {
//                    @Override
//                    public void run() {
//                        /**
//                         *要执行的操作
//                         */
//                        //视频播放
//                        MediaController mc = new MediaController(MainActivity.this,false);
//                        myVideoView.setMediaController(mc);
//                        urlStream = "http://tvideo.fsyzt.cn:84/video/"+rtsp_path;
//                        runOnUiThread(new Runnable() {
//                            @Override
//                            public void run() {
//
//                                myVideoView.setVideoURI(Uri.parse(urlStream));
//                                myVideoView.requestFocus();
//                                myVideoView.start();
//
//                            }
//                        });
//                    }
//                }, 8000);//8秒后执行Runnable中的run方法
//            }
//        });

        btn_submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                get_token();
                loading.show();
            }
        });

    }

    private void get_token(){
        String login_url = "http://tvideo.fsyzt.cn:84/api/login?account=admin&password=c7cef7c64a6e705362143c161490f751 " ;
        RequestParams requestParams = new RequestParams();
        AsyncHttpClient token_Client = new AsyncHttpClient();
        token_Client.get(login_url,requestParams,new JsonHttpResponseHandler(){
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                try {
                    token = response.getString("token");
                    rtsp_path = ed_rtsp.getText().toString();
                    replace = rtsp_path.substring(7,20);
                    if (replace.equals("58.252.72.172")){
                        rtsp_path = rtsp_path.replace(replace,"192.168.2.3");
                    }
                    if (rtsp_path.indexOf("hikvision") != -1){
                        rtsp_path = rtsp_path.replace("0?","1?");
                    }
//                    Toast.makeText(MainActivity.this,rtsp_path,Toast.LENGTH_LONG).show();
                    rtsp_2_m3u8(rtsp_path);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                super.onFailure(statusCode, headers, throwable, errorResponse);
            }
        });
    }
    private void rtsp_2_m3u8(String path) throws Exception{

        String convert_url = "http://tvideo.fsyzt.cn:84/api/video/open-rtsp" ;
        RequestParams requestParams = new RequestParams();
        requestParams.setUseJsonStreamer(true);
        String convert_path = "{\n" +
                "      \"path\": \""+path+"\"\n" +
                "  }";
        JSONObject body = new JSONObject(convert_path);
        StringEntity stringEntity = new StringEntity(body.toString());

        AsyncHttpClient convert_Client = new AsyncHttpClient();
//        requestParams.add("path","rtsp://58.252.72.172:554/hikvision://10.200.189.87:8000:0:1?cnid=5&pnid=4&username=admin&password=wang982207");
        convert_Client.addHeader("Content-Type","application/json");
        convert_Client.addHeader("Authorization",token);
        convert_Client.post(null,convert_url,stringEntity,"application/json",new JsonHttpResponseHandler(){
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                try {
                    Toast.makeText(MainActivity.this,"正在转码，8秒后自动播放...",Toast.LENGTH_LONG).show();
                    rtsp_name = response.getString("path");
                    video_id = response.getString("time");
                    mhandler = new Handler() {
                        @Override
                        public void handleMessage(Message msg) {
                            if (msg.what == 1){
                                //do something
                                keep_contact(video_id);
                            }
                            super.handleMessage(msg);
                        }
                    };

                    timer = new Timer();
                    TimerTask timerTask = new TimerTask() {
                        @Override
                        public void run() {
                            Message message = new Message();
                            message.what = 1;
                            mhandler.sendMessage(message);
                        }
                    };

                    timer.schedule(timerTask,0,10000);
                    //视频播放
                    Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            /**
                             *要执行的操作
                             */
                            //视频播放
                            MediaController mc = new MediaController(MainActivity.this,false);
                            myVideoView.setMediaController(mc);
                            urlStream = "http://tvideo.fsyzt.cn:84/video/"+rtsp_name;
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    loading.dismiss();
                                    myVideoView.setVideoURI(Uri.parse(urlStream));
                                    myVideoView.requestFocus();

                                    myVideoView.start();

                                }
                            });
                        }
                    }, 8000);//8秒后执行Runnable中的run方法

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                loading.dismiss();
                Toast.makeText(MainActivity.this,"加载超时...",Toast.LENGTH_LONG).show();
            }
        });
    }
    private void keep_contact(String id){
        String keep_url = "http://tvideo.fsyzt.cn:84/api/video/update-last-pull-date?id="+id;
        RequestParams requestParams = new RequestParams();
        AsyncHttpClient keep_Client = new AsyncHttpClient();
        keep_Client.addHeader("Authorization",token);
        keep_Client.get(keep_url,requestParams,new JsonHttpResponseHandler(){
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                try {
                    msg = response.getString("msg");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                super.onFailure(statusCode, headers, throwable, errorResponse);
            }
        });
    }

    @Override
    public void onBackPressed() {
        timer.cancel();
        super.onBackPressed();
    }
}
