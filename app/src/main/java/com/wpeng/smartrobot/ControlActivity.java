package com.wpeng.smartrobot;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class ControlActivity extends AppCompatActivity {

    Handler handler;
    JSONObject jsonObject;
    String data;


    @SuppressLint("HandlerLeak")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_control);

        TextView hum=findViewById(R.id.hum);
        handler=new android.os.Handler(){
            @SuppressLint("SetTextI18n")
            public void handleMessage(Message message){
                switch (message.what){
                    case 0x01:
                        hum.setText("湿度为:"+data);
                        break;
                    case 0x02:
                        Toast.makeText(ControlActivity.this,"获取失败，请检查服务器连接!",Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        };

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                GetData();//获取服务器数据
                handler.postDelayed(this,6000);//60 second delay
            }
        };
        handler.postDelayed(runnable,0);//一个0秒的定时器（因为获取数据需要时间，索性就不写延时了）

    }

    public void GetData(){
        try {
            new Thread(new Runnable() {
                Message message=new Message();
                String url="http://81.69.222.73:8080/refresh";
                @Override
                public void run() {
                    try {
                        message.what=0x01;
                        String html = getHtml(url);
                        jsonObject=new JSONObject(html);
                        data=jsonObject.getJSONArray("data").getJSONObject(1).getString("data");
                        Log.e("GetData","刷新数据,数据为:"+data);
                        handler.sendMessage(message);
                    } catch (Exception e) {
                        message.what=0x02;
                        handler.sendMessage(message);
                        e.printStackTrace();
                    }
                }
            }).start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String getHtml(String path) throws Exception {
        Log.i("path",path);
        URL url = new URL(path);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setConnectTimeout(5 * 1000);
        InputStream inStream = conn.getInputStream();//通过输入流获取html数据
        byte[] data = readInputStream(inStream);//得到html的二进制数据
        String html = new String(data, "UTF-8");
        return html;
    }

    public static byte[] readInputStream(InputStream inStream) throws Exception {
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int len = 0;
        while ((len = inStream.read(buffer)) != -1) {
            outStream.write(buffer, 0, len);
        }
        inStream.close();
        return outStream.toByteArray();
    }
}