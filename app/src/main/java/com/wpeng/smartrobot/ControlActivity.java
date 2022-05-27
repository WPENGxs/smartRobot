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

@SuppressLint("UseSwitchCompatOrMaterialCode")
public class ControlActivity extends AppCompatActivity {

    Handler handler;
    JSONObject jsonObject;

    String data="0";
    String tem_data="15";
    String light_data="625";

    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;

    Switch hum_switch;
    Switch light_switch;
    Switch tem_switch_1;
    Switch tem_switch_2;

    String hum_up_str="70";
    String hum_down_str="50";
    String tem_up_str="70";
    String tem_down_str="50";
    String light_up_str="70";
    String light_down_str="50";

    String tem=null;
    String hum=null;
    String light=null;

    @SuppressLint({"HandlerLeak", "UseSwitchCompatOrMaterialCode"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_control);

        TextView hum=findViewById(R.id.hum);
        TextView tem=findViewById(R.id.tem);
        TextView light=findViewById(R.id.light);
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
                    case 0x03:
                        tem.setText(tem_data+"℃");
                        break;
                    case 0x04:
                        light.setText(light_data+"Lx");
                        break;
                }
            }
        };

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                GetData();//获取服务器数据
                KeepHum();
                GetTem();
                GetLight();
                handler.postDelayed(this,1000);//60 second delay
            }
        };
        handler.postDelayed(runnable,0);//一个0秒的定时器

        sharedPreferences=getSharedPreferences("hum_d",MODE_PRIVATE);
        editor=sharedPreferences.edit();

        hum_switch=findViewById(R.id.hum_switch);
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
                        hum_up_str=up.getText().toString();
                        hum_down_str=down.getText().toString();
                        hum_btu.setText(hum_down_str+" - "+hum_up_str);
                        Toast.makeText(ControlActivity.this,"设置完毕",Toast.LENGTH_SHORT).show();
                    }
                });
                dialog.show();
            }
        });

        sharedPreferences=getSharedPreferences("light_d",MODE_PRIVATE);

        light_switch=findViewById(R.id.light_switch);
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
                        light_up_str=up.getText().toString();
                        light_down_str=down.getText().toString();
                        light_btu.setText(light_down_str+" - "+light_up_str);
                        Toast.makeText(ControlActivity.this,"设置完毕",Toast.LENGTH_SHORT).show();
                    }
                });
                dialog.show();
            }
        });

        sharedPreferences=getSharedPreferences("tem_d_1",MODE_PRIVATE);

        tem_switch_1=findViewById(R.id.tem_switch_1);
        tem_switch_1.setChecked(sharedPreferences.getBoolean("tem_d_1",false));
        tem_switch_1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editor.putBoolean("tem_d_1", !hum_switch.isSelected());
                editor.apply();
            }
        });

        sharedPreferences=getSharedPreferences("tem_d_2",MODE_PRIVATE);

        tem_switch_2=findViewById(R.id.tem_switch_2);
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
                        tem_up_str=up.getText().toString();
                        tem_down_str=down.getText().toString();
                        tem_btu.setText(tem_down_str+" - "+tem_up_str);
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

    public void KeepHum(){
        if(Integer.parseInt(hum_down_str)>Integer.parseInt(data)){
            hum_switch.setChecked(true);
            hum="true";
        }
        else if(Integer.parseInt(hum_up_str)<Integer.parseInt(data)){
            hum_switch.setChecked(false);
            hum="false";
        }
        if(Integer.parseInt(data)==Integer.parseInt(hum_down_str)||
                Integer.parseInt(data)==Integer.parseInt(hum_up_str)){
            hum_switch.setChecked(false);
            hum="keep";
        }
    }

    public void GetTem(){
        if(Integer.parseInt(tem_data)>=Integer.parseInt(tem_down_str)&&
                Integer.parseInt(tem_data)<=Integer.parseInt(tem_up_str)){
            tem_switch_1.setChecked(false);
            tem_switch_2.setChecked(false);
            tem="keep";
        }
        else if(Integer.parseInt(tem_down_str)>Integer.parseInt(tem_data)){
            tem_switch_2.setChecked(true);
            tem_switch_1.setChecked(false);
            tem="true";
        }
        else if(Integer.parseInt(tem_up_str)<Integer.parseInt(tem_data)){
            tem_switch_1.setChecked(true);
            tem_switch_2.setChecked(false);
            tem="false";
        }


        try {
            new Thread(new Runnable() {
                Message message=new Message();
                String url="http://81.69.222.73:8080/tem?old_tem="+tem_data+"&raise="+ tem;
                @Override
                public void run() {
                    try {
                        message.what=0x03;
                        tem_data = getHtml(url);
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

    public void GetLight(){
        if(Integer.parseInt(light_data)>=Integer.parseInt(light_down_str)&&
                Integer.parseInt(light_data)<=Integer.parseInt(light_up_str)){
            light_switch.setChecked(false);
            light="keep";
        }
        else if(Integer.parseInt(light_down_str)>Integer.parseInt(light_data)){
            light_switch.setChecked(true);
            light="true";
        }
        else if(Integer.parseInt(light_up_str)<Integer.parseInt(light_data)){
            light_switch.setChecked(false);
            light="false";
        }


        try {
            new Thread(new Runnable() {
                Message message=new Message();
                String url="http://81.69.222.73:8080/light?old_light="+light_data+"&raise="+light;
                @Override
                public void run() {
                    try {
                        message.what=0x04;
                        light_data = getHtml(url);
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