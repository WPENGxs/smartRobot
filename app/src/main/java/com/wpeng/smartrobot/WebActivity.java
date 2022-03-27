package com.wpeng.smartrobot;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class WebActivity extends AppCompatActivity {

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web);

        Bundle bundle=this.getIntent().getExtras();//接受来自MainActivity的值
        String Url=bundle.getString("Url");

        WebView webView=findViewById(R.id.webView);
        webView.setClickable(true);//设置可点击
        webView.getSettings().setJavaScriptEnabled(true);//支持JS
        webView.getSettings().setSupportZoom(true);//设置可以支持缩放
        webView.getSettings().setBuiltInZoomControls(true);//设置出现缩放工具
        webView.getSettings().setDomStorageEnabled(true);//设置为使用webView推荐的窗口，主要是为了配合下一个属性
        webView.getSettings().setUseWideViewPort(true);//扩大缩放比例
        webView.getSettings().setLoadWithOverviewMode(true);

        webView.loadUrl(Url);

        //webView.getSettings().setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);
        //webView.getSettings().setLoadWithOverviewMode(true);

        /*webView.setWebViewClient(new WebViewClient(){//保证网页只在软件中打开而不是跳转到浏览器
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                // TODO Auto-generated method stub
                view.loadUrl(url);
                return true;
            }
        });*/
    }
}