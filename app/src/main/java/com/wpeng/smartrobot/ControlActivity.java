package com.wpeng.smartrobot;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
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

    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;


    @SuppressLint({"HandlerLeak", "UseSwitchCompatOrMaterialCode"})
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
                        hum.setText(data);
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

        sharedPreferences=getSharedPreferences("hum_d",MODE_PRIVATE);
        editor=sharedPreferences.edit();

        Switch hum_switch=findViewById(R.id.hum_switch);
        hum_switch.setChecked(sharedPreferences.getBoolean("hum_d",false));
        hum_switch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editor.putBoolean("hum_d", !hum_switch.isSelected());
                editor.apply();
            }
        });

        Button hum_btu=findViewById(R.id.hum_btu);
        hum_btu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder dialog=new AlertDialog.Builder(ControlActivity.this);
                dialog.setTitle("设置范围");

                LinearLayout linearLayout=new LinearLayout(ControlActivity.this);
                linearLayout.setOrientation(LinearLayout.VERTICAL);
                EditText up=new EditText(ControlActivity.this);
                up.setHint("上限");
                EditText down=new EditText(ControlActivity.this);
                down.setHint("下限");
                linearLayout.addView(up);
                linearLayout.addView(down);

                dialog.setView(linearLayout);
                dialog.setPositiveButton("确认", new DialogInterface.OnClickListener() {
                    @SuppressLint("SetTextI18n")
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String up_str=up.getText().toString();
                        String down_str=down.getText().toString();
                        hum_btu.setText(up_str+" - "+down_str);
                        Toast.makeText(ControlActivity.this,"设置完毕",Toast.LENGTH_SHORT).show();
                    }
                });
                dialog.show();
            }
        });

        sharedPreferences=getSharedPreferences("light_d",MODE_PRIVATE);

        Switch light_switch=findViewById(R.id.light_switch);
        light_switch.setChecked(sharedPreferences.getBoolean("light_d",false));
        light_switch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editor.putBoolean("light_d", !hum_switch.isSelected());
                editor.apply();
            }
        });

        Button light_btu=findViewById(R.id.light_btu);
        light_btu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder dialog=new AlertDialog.Builder(ControlActivity.this);
                dialog.setTitle("设置范围");

                LinearLayout linearLayout=new LinearLayout(ControlActivity.this);
                linearLayout.setOrientation(LinearLayout.VERTICAL);
                EditText up=new EditText(ControlActivity.this);
                up.setHint("上限");
                EditText down=new EditText(ControlActivity.this);
                down.setHint("下限");
                linearLayout.addView(up);
                linearLayout.addView(down);

                dialog.setView(linearLayout);
                dialog.setPositiveButton("确认", new DialogInterface.OnClickListener() {
                    @SuppressLint("SetTextI18n")
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String up_str=up.getText().toString();
                        String down_str=down.getText().toString();
                        light_btu.setText(up_str+" - "+down_str);
                        Toast.makeText(ControlActivity.this,"设置完毕",Toast.LENGTH_SHORT).show();
                    }
                });
                dialog.show();
            }
        });

        sharedPreferences=getSharedPreferences("tem_d_1",MODE_PRIVATE);

        Switch tem_switch_1=findViewById(R.id.tem_switch_1);
        tem_switch_1.setChecked(sharedPreferences.getBoolean("tem_d_1",false));
        tem_switch_1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editor.putBoolean("tem_d_1", !hum_switch.isSelected());
                editor.apply();
            }
        });

        sharedPreferences=getSharedPreferences("tem_d_2",MODE_PRIVATE);

        Switch tem_switch_2=findViewById(R.id.tem_switch_2);
        tem_switch_2.setChecked(sharedPreferences.getBoolean("tem_d_2",false));
        tem_switch_2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editor.putBoolean("tem_d_2", !hum_switch.isSelected());
                editor.apply();
            }
        });

        Button tem_btu=findViewById(R.id.tem_btu);
        tem_btu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder dialog=new AlertDialog.Builder(ControlActivity.this);
                dialog.setTitle("设置范围");

                LinearLayout linearLayout=new LinearLayout(ControlActivity.this);
                linearLayout.setOrientation(LinearLayout.VERTICAL);
                EditText up=new EditText(ControlActivity.this);
                up.setHint("上限");
                EditText down=new EditText(ControlActivity.this);
                down.setHint("下限");
                linearLayout.addView(up);
                linearLayout.addView(down);

                dialog.setView(linearLayout);
                dialog.setPositiveButton("确认", new DialogInterface.OnClickListener() {
                    @SuppressLint("SetTextI18n")
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String up_str=up.getText().toString();
                        String down_str=down.getText().toString();
                        tem_btu.setText(up_str+" - "+down_str);
                        Toast.makeText(ControlActivity.this,"设置完毕",Toast.LENGTH_SHORT).show();
                    }
                });
                dialog.show();
            }
        });

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