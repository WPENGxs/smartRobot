package com.wpeng.smartrobot;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.ImageDecoder;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Message;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class ChatActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        LinearLayout chat_view=findViewById(R.id.chat_TalkView);//中间的交互界面
        Cursor cursor=MainActivity.Database.rawQuery("select * from user", null);
        while (cursor.moveToNext()) {
            String character=cursor.getString(cursor.getColumnIndex("character"));
            String type=cursor.getString(cursor.getColumnIndex("type"));
            String contents=cursor.getString(cursor.getColumnIndex("contents"));
            if(character.equals("user")){//用户
                if(type.equals("text")){
                    AddText(chat_view,contents,true);
                }else if(type.equals("photo")){
                    Bitmap bitmap=BitmapFactory.decodeFile(contents);
                    AddImage(chat_view,bitmap);
                }
            }else {//机器人
                if(type.equals("text")){
                    AddText(chat_view,contents,false);
                }else if(type.equals("url")){
                    AddUrl(chat_view,contents,"跳转链接");
                }
            }
        }
        cursor.close();
    }

    public void DelAllChat(View view){
        AlertDialog.Builder dialog=new AlertDialog.Builder(ChatActivity.this);
        dialog.setTitle("警告");
        dialog.setMessage("要删除所有聊天记录么？");
        dialog.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                MainActivity.Database.execSQL("delete from user");
                Toast.makeText(ChatActivity.this,"已删除",Toast.LENGTH_SHORT).show();
                refresh();
            }
        });
        dialog.show();

    }

    public void refresh(){//刷新函数——通过结束当前界面并新建界面的方式刷新
        finish();
        Intent intent = new Intent(this, ChatActivity.class);
        startActivity(intent);
    }



    /*
    下为败笔,当时没想到有这么多功能,临时加的,直接怼上去了,懒
     */

    /**
     * 在LinearLayout中插入一条带头像的对话框
     * right_direction为true时为用户
     * @param linearLayout
     *              布局文件
     * @param str
     *              显示的文本
     * @param right_direction
     *              方向,false为头像在左,true为头像在右
     */
    public void AddText(LinearLayout linearLayout,String str,boolean right_direction){
        LinearLayout talk_list=new LinearLayout(ChatActivity.this);
        talk_list.setGravity(LinearLayout.HORIZONTAL);//设置方向
        talk_list.setLeftTopRightBottom(50,50,50,50);

        ImageView imageView=new ImageView(ChatActivity.this);//设置头像
        imageView.setMaxHeight(160);
        imageView.setMaxWidth(160);
        imageView.setAdjustViewBounds(true);

        TextView textView=new TextView(ChatActivity.this);//设置文本
        textView.setTextSize(15);
        textView.setLayoutDirection(View.LAYOUT_DIRECTION_LTR);//使文本靠左布局

        if(right_direction){//为true时先文本再头像
            imageView.setImageResource(R.drawable.me);
            talk_list.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
            talk_list.setPadding(20,10,0,10);
            textView.setBackgroundResource(R.drawable.talk_text);
            textView.setTextColor(Color.rgb(0, 0, 0));//设置文本颜色为黑色
        }
        else{//为false时先头像再文本
            imageView.setImageResource(R.drawable.miao);
            talk_list.setLayoutDirection(View.LAYOUT_DIRECTION_LTR);
            talk_list.setPadding(0,10,20,10);
            textView.setBackgroundResource(R.drawable.talk_text_blue);
            textView.setTextColor(Color.rgb(255, 255, 255));//设置文本颜色为白色
        }

        talk_list.addView(imageView);//向布局中添加头像
        talk_list.addView(textView);//向布局中添加文本
        textView.setText(str);
        linearLayout.addView(talk_list);
    }

    /**
     * 插入一条图片（用户角度）
     * @param linearLayout
     *              布局
     * @param bitmap
     *              发送的位图
     */
    public void AddImage(LinearLayout linearLayout,Bitmap bitmap){
        LinearLayout talk_list=new LinearLayout(ChatActivity.this);
        talk_list.setGravity(LinearLayout.HORIZONTAL);//设置方向
        talk_list.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        talk_list.setLeftTopRightBottom(50,50,50,50);

        ImageView imageView_head=new ImageView(ChatActivity.this);//设置头像
        imageView_head.setMaxHeight(160);
        imageView_head.setMaxWidth(160);
        imageView_head.setAdjustViewBounds(true);
        imageView_head.setImageResource(R.drawable.me);

        ImageView imageView_bitmap=new ImageView(ChatActivity.this);//设置发送的图片
        imageView_bitmap.setMaxWidth(400);
        imageView_bitmap.setMaxHeight(400);
        imageView_bitmap.setAdjustViewBounds(true);
        imageView_bitmap.setImageBitmap(bitmap);
        imageView_bitmap.setClickable(true);
        imageView_bitmap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Toast.makeText(ChatActivity.this,"你点击了这个图片",Toast.LENGTH_SHORT).show();
                OnScreenShow(imageView_bitmap);
            }
        });

        talk_list.addView(imageView_head);//向布局中添加头像
        talk_list.addView(imageView_bitmap);//向布局中添加文本
        talk_list.setPadding(20,10,0,10);
        linearLayout.addView(talk_list);
    }

    /**
     * 通过url获得bitmap
     * @param url_str
     *              bitmap的链接
     */
    public Bitmap GetBitmap(String url_str) throws IOException, InterruptedException {
        final Bitmap[] bitmap_img = {null};
        Thread thread=new Thread(){
            @Override
            public void run(){
                try{
                    URL url=new URL(url_str);
                    HttpURLConnection httpURLConnection=(HttpURLConnection) url.openConnection();
                    httpURLConnection.setRequestMethod("GET");
                    httpURLConnection.setReadTimeout(2000);
                    httpURLConnection.connect();
                    InputStream inputStream=httpURLConnection.getInputStream();
                    if(inputStream!=null){
                        bitmap_img[0] = BitmapFactory.decodeStream(inputStream);//输入流转换为bitmap
                    }
                }
                catch (Exception e){
                    e.printStackTrace();
                }

            }
        };
        thread.start();
        thread.join(1000);

        return bitmap_img[0];
    }

    /**
     * 插入一条带链接的可点击的按钮，点击后跳转到此链接
     * @param linearLayout
     *              布局
     * @param url
     *              链接
     */
    @SuppressLint("SetTextI18n")
    public void AddUrl(LinearLayout linearLayout, String url,String str){
        LinearLayout url_list=new LinearLayout(ChatActivity.this);
        url_list.setGravity(LinearLayout.HORIZONTAL);//设置方向
        url_list.setLayoutDirection(View.LAYOUT_DIRECTION_LTR);//设置子布局排列方向为从左向右
        url_list.setLeftTopRightBottom(50,50,50,50);

        ImageView imageView_head=new ImageView(ChatActivity.this);//设置头像
        imageView_head.setMaxHeight(160);
        imageView_head.setMaxWidth(160);
        imageView_head.setAdjustViewBounds(true);
        imageView_head.setImageResource(R.drawable.miao);

        Button url_button=new Button(ChatActivity.this);//设置跳转按钮
        url_button.setText(str);//设置按钮文本
        url_button.setTextSize(15);
        url_button.setTextColor(Color.rgb(255, 255, 255));//设置文本颜色为白色
        url_button.setBackgroundResource(R.drawable.talk_text_blue);//设置按钮样式
        url_button.setOnClickListener(new View.OnClickListener() {//设置按钮监听
            @Override
            public void onClick(View v) {
                //WebViewDialog(ChatActivity.this,url);//弹出WebView弹窗
                ToWebActivity(url);
            }
        });

        url_list.addView(imageView_head);//向布局中添加头像
        url_list.addView(url_button);//向布局中添加文本
        url_list.setPadding(0,10,20,10);
        linearLayout.addView(url_list);
    }

    /**
     * 将图片全屏显示
     * @param imageView
     *              ImageView
     */
    public void OnScreenShow(ImageView imageView){
        Dialog dialog = new Dialog(ChatActivity.this);

        ImageView image=new ImageView(ChatActivity.this);
        BitmapDrawable bitmapDrawable= (BitmapDrawable) imageView.getDrawable();
        Bitmap bitmap=bitmapDrawable.getBitmap();
        image.setImageBitmap(bitmap);
        image.setScaleType(ImageView.ScaleType.MATRIX);


        WindowManager.LayoutParams attributes = getWindow().getAttributes();
        attributes.width = WindowManager.LayoutParams.MATCH_PARENT;
        attributes.height = WindowManager.LayoutParams.MATCH_PARENT;
        dialog.getWindow().setAttributes(attributes);

        //image = getImageView();
        dialog.setContentView(image);
        dialog.show();
        //大图的点击事件（点击让他消失）
        image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
    }

    /**
     * 从当前页面跳转到WebView界面
     * @param Url
     *              跳转的Url
     */
    public void ToWebActivity(String Url){
        Intent intent=new Intent(ChatActivity.this,WebActivity.class);
        intent.putExtra("Url",Url);//向WebActivity传值
        startActivity(intent);
    }
}