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
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.iflytek.cloud.ErrorCode;
import com.iflytek.cloud.InitListener;
import com.iflytek.cloud.RecognizerListener;
import com.iflytek.cloud.RecognizerResult;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechRecognizer;
import com.iflytek.cloud.SpeechUtility;
import com.iflytek.cloud.Setting;
import com.iflytek.cloud.ui.RecognizerDialog;
import com.iflytek.cloud.ui.RecognizerDialogListener;
import com.wpeng.smartrobot.util.JsonParser;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.LinkedHashMap;

public class MainActivity extends AppCompatActivity {
    static SQLite Data;
    static SQLiteDatabase Database;//数据库对象
    static String insert_sql;

    String sent_text="";//发送文本
    String return_text="";//机器返回文本

    LinearLayout talk_view;

    Handler handler=null;
    private String answer;
    private String url;

    private static final int SELECT_IMAGE = 1;
    private Bitmap bitmap = null;
    private Bitmap yourSelectedImage = null;
    public String imagePath="";
    public String imageName="";

    private static String TAG = MainActivity.class.getSimpleName();
    // 语音听写对象
    public SpeechRecognizer mIat;
    // 语音听写UI
    public RecognizerDialog mIatDialog;
    // 用HashMap存储听写结果
    private HashMap<String, String> mIatResults = new LinkedHashMap<>();
    private EditText sent_editText;
    private Toast mToast;
    public SharedPreferences mSharedPreferences;
    // 引擎类型
    private String mEngineType = SpeechConstant.TYPE_CLOUD;
    private String language = "zh_cn";
    private String resultType = "json";
    private StringBuffer buffer = new StringBuffer();

    @SuppressLint("HandlerLeak")
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        // 应用程序入口处调用，避免手机内存过小，杀死后台进程后通过历史intent进入Activity造成SpeechUtility对象为null
        // 如在Application中调用初始化，需要在Mainifest中注册该Applicaiton
        // 注意：此接口在非主进程调用会返回null对象，如需在非主进程使用语音功能，请增加参数：SpeechConstant.FORCE_LOGIN+"=true"
        // 参数间使用半角“,”分隔。
        // 设置你申请的应用appid,请勿在'='与appid之间添加空格及空转义符

        // 注意： appid 必须和下载的SDK保持一致，否则会出现10407错误
        SpeechUtility.createUtility(MainActivity.this, "appid=" + "cf2e2940");//输入讯飞appid
        // 以下语句用于设置日志开关（默认开启），设置成false时关闭语音云SDK日志打印
        Setting.setShowLog(true);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Data=new SQLite(this,"Database",null,1);//构造数据库
        Database = Data.getWritableDatabase();//获取可写数据库

        talk_view=findViewById(R.id.TalkView);//中间的交互界面
        Button sent_button=findViewById(R.id.sent_button);//发送按钮
        sent_editText=findViewById(R.id.sent_editText);//发送输入框

        handler=new android.os.Handler(){
            public void handleMessage(Message message){
                switch (message.what){
                    case 0x01:
                        if(!answer.contains("***")) {
                            RobotReturn(ReturnAnswer(answer));//机器人返回
                            AddUrl(talk_view, url , "图谱演示");//返回url跳转
                        }else {
                            RobotReturn(ReturnAnswer(answer).substring(0,ReturnAnswer(answer).indexOf('*')));//机器人返回
                        }
                        break;
                    case 0x02:
                        RobotReturn(answer);
                        AddUrl(talk_view,url,"图片实例");
                        break;
                    case 0x03:
                        Toast.makeText(MainActivity.this,"解析失败",Toast.LENGTH_SHORT).show();
                        break;
                    default:RobotReturn("解析失败");//机器人返回
                        break;
                }
            }
        };

        sent_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sent_text = sent_editText.getText().toString();//获得输入框的文本

                if(!sent_text.equals("")){
                    new Thread(){
                        public void run() {
                            Message message=new Message();
                            try {
                                Data_socket socket = new Data_socket("81.69.222.73", 39015);
                                socket.SentData(sent_text);

                                url = socket.GetData();
                                Log.e("url", url);
                                answer = socket.GetData();
                                Log.e("answer", answer);

                                message.what=0x01;
                                handler.sendMessage(message);
                                socket.Close();
                            } catch (IOException e) {
                                e.printStackTrace();
                                message.what=0x03;
                                handler.sendMessage(message);
                            }
                        }
                    }.start();
                    AddText(talk_view, sent_text,true);//插入对话框
                    sent_editText.setText("");//对话框置空

                    //RobotReturn("");//机器人返回
                }
            }
        });

        // 初始化识别无UI识别对象
        // 使用SpeechRecognizer对象，可根据回调消息自定义界面；
        mIat = SpeechRecognizer.createRecognizer(MainActivity.this, mInitListener);

        // 初始化听写Dialog，如果只使用有UI听写功能，无需创建SpeechRecognizer
        // 使用UI听写功能，请根据sdk文件目录下的notice.txt,放置布局文件和图片资源
        //mIatDialog = new RecognizerDialog(MainActivity.context, mInitListener);
        mIatDialog = new RecognizerDialog(MainActivity.this,mInitListener);

        mSharedPreferences = MainActivity.this.getSharedPreferences("com.iflytek.setting",
                Activity.MODE_PRIVATE);

        Button ifly_button=findViewById(R.id.ifly_button);
        ifly_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ifly();
            }
        });

        Button select_img=findViewById(R.id.select_image_button);
        select_img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {//选择图片
                Intent i = new Intent(Intent.ACTION_PICK);
                i.setType("image/*");
                startActivityForResult(i, SELECT_IMAGE);
            }
        });
    }


    /**
     * 用户发送文本时机器人返回函数
     */
    public void RobotReturn(String str){
        AddText(talk_view, str,false);//小喵的回话;
    }

    /**
     * 用户发送图片时机器人返回函数
     */
    public void RobotReturn(LinearLayout linearLayout,Bitmap bitmap,String path) throws IOException{
        //AddText(talk_view, miao.toString(),false);//小喵的回话;
        AddImage(linearLayout,bitmap);//在用户角度插入一张图片

        SentFile(path);//发送文件

        //AddUrl(talk_view,"https://www.wpengxs.cn");
        //AddUrlImage(talk_view,"https://www.wpengxs.cn/shu_meng_ge/shu_meng_ge_background/兰.jpg");
    }

    /**
     * 返回字符串的处理函数
     * @param answer
     *              输入的字符串
     * @return
     */
    public String ReturnAnswer(String answer){
        String ReturnStr;
        ReturnStr=answer.substring(answer.lastIndexOf('[')+1,answer.indexOf(']'));
        ReturnStr=ReturnStr.replace('\'',' ');

        return ReturnStr;
    }


     public void SentFile(String path) throws IOException {
        new Thread(){
            Message message=new Message();
            @Override
            public void run() {
                try {
                    Data_socket socket = new Data_socket("81.69.222.73", 39100);
                    socket.sendFile(path);

                    String str=socket.GetData();
                    System.out.println(str);
                    answer=str.substring(0,str.indexOf('$'));
                    url=str.substring(str.indexOf('$')+1);

                    socket.Close();
                    message.what=0x02;
                    handler.sendMessage(message);
                } catch (Exception e) {
                    e.printStackTrace();
                    message.what=0x03;
                    handler.sendMessage(message);
                }
            }

        }.start();

     }

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
        LinearLayout talk_list=new LinearLayout(MainActivity.this);
        talk_list.setGravity(LinearLayout.HORIZONTAL);//设置方向
        talk_list.setLeftTopRightBottom(50,50,50,50);

        ImageView imageView=new ImageView(MainActivity.this);//设置头像
        imageView.setMaxHeight(160);
        imageView.setMaxWidth(160);
        imageView.setAdjustViewBounds(true);

        TextView textView=new TextView(MainActivity.this);//设置文本
        textView.setTextSize(15);
        textView.setTextColor(Color.rgb(0, 0, 0));//设置文本颜色为黑色
        textView.setBackgroundResource(R.drawable.talk_text);
        textView.setLayoutDirection(View.LAYOUT_DIRECTION_LTR);//使文本靠左布局

        //向数据库插入数据
        if(right_direction){//为true时先文本再头像
            imageView.setImageResource(R.drawable.me);
            talk_list.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);

            insert_sql="insert into user(Character,type,contents)" +
                    "values('user','text','" +
                    str + "')";
        }
        else{//为false时先头像再文本
            imageView.setImageResource(R.drawable.miao);
            talk_list.setLayoutDirection(View.LAYOUT_DIRECTION_LTR);

            insert_sql="insert into user(Character,type,contents)" +
                    "values('robot','text','" +
                    str + "')";
        }
        Database.execSQL(insert_sql);//向数据库插入数据

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
        LinearLayout talk_list=new LinearLayout(MainActivity.this);
        talk_list.setGravity(LinearLayout.HORIZONTAL);//设置方向
        talk_list.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        talk_list.setLeftTopRightBottom(50,50,50,50);

        ImageView imageView_head=new ImageView(MainActivity.this);//设置头像
        imageView_head.setMaxHeight(160);
        imageView_head.setMaxWidth(160);
        imageView_head.setAdjustViewBounds(true);
        imageView_head.setImageResource(R.drawable.me);

        ImageView imageView_bitmap=new ImageView(MainActivity.this);//设置发送的图片
        imageView_bitmap.setMaxWidth(400);
        imageView_bitmap.setMaxHeight(400);
        imageView_bitmap.setAdjustViewBounds(true);
        imageView_bitmap.setImageBitmap(bitmap);
        imageView_bitmap.setClickable(true);
        imageView_bitmap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Toast.makeText(MainActivity.this,"你点击了这个图片",Toast.LENGTH_SHORT).show();
                OnScreenShow(imageView_bitmap);
            }
        });

        talk_list.addView(imageView_head);//向布局中添加头像
        talk_list.addView(imageView_bitmap);//向布局中添加文本
        linearLayout.addView(talk_list);
    }

    /**
     *
     * @param linearLayout
     *              布局
     * @param url_str
     *              url
     */
    public void AddUrlImage(LinearLayout linearLayout,String url_str) throws IOException, InterruptedException {
        Bitmap bitmap_img = null;
        bitmap_img=GetBitmap(url_str);

        LinearLayout talk_list=new LinearLayout(MainActivity.this);
        talk_list.setGravity(LinearLayout.HORIZONTAL);//设置方向
        talk_list.setLayoutDirection(View.LAYOUT_DIRECTION_LTR);
        talk_list.setLeftTopRightBottom(50,50,50,50);

        ImageView imageView_head=new ImageView(MainActivity.this);//设置头像
        imageView_head.setMaxHeight(160);
        imageView_head.setMaxWidth(160);
        imageView_head.setAdjustViewBounds(true);
        imageView_head.setImageResource(R.drawable.miao);

        ImageView imageView_image=new ImageView(MainActivity.this);//设置发送的图片
        imageView_image.setMaxWidth(400);
        imageView_image.setMaxHeight(400);
        imageView_image.setAdjustViewBounds(true);
        imageView_image.setImageBitmap(bitmap_img);
        imageView_image.setClickable(true);//设置该图片可点击
        imageView_image.setOnClickListener(new View.OnClickListener() {//设置图片点击监听
            @Override
            public void onClick(View v) {
                //Toast.makeText(MainActivity.this,"你点击了这个图片",Toast.LENGTH_SHORT).show();
                OnScreenShow(imageView_image);
            }
        });

        talk_list.addView(imageView_head);//向布局中添加头像
        talk_list.addView(imageView_image);//向布局中添加文本
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
                        bitmap_img[0] =BitmapFactory.decodeStream(inputStream);//输入流转换为bitmap
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

        insert_sql="insert into user(Character,type,contents)" +
                "values('robot','url','" +
                url + "')";
        Database.execSQL(insert_sql);//向数据库插入数据

        LinearLayout url_list=new LinearLayout(MainActivity.this);
        url_list.setGravity(LinearLayout.HORIZONTAL);//设置方向
        url_list.setLayoutDirection(View.LAYOUT_DIRECTION_LTR);//设置子布局排列方向为从左向右
        url_list.setLeftTopRightBottom(50,50,50,50);

        ImageView imageView_head=new ImageView(MainActivity.this);//设置头像
        imageView_head.setMaxHeight(160);
        imageView_head.setMaxWidth(160);
        imageView_head.setAdjustViewBounds(true);
        imageView_head.setImageResource(R.drawable.miao);

        Button url_button=new Button(MainActivity.this);//设置跳转按钮
        url_button.setText(str);//设置按钮文本
        url_button.setTextSize(15);
        url_button.setTextColor(Color.BLUE);
        url_button.setBackgroundResource(R.drawable.general_button);//设置按钮样式
        url_button.setOnClickListener(new View.OnClickListener() {//设置按钮监听
            @Override
            public void onClick(View v) {
                //WebViewDialog(MainActivity.this,url);//弹出WebView弹窗
                ToWebActivity(url);
            }
        });

        url_list.addView(imageView_head);//向布局中添加头像
        url_list.addView(url_button);//向布局中添加文本
        linearLayout.addView(url_list);
    }

    /**
     * 弹窗显示一张图片，传入值为图片的链接
     * @param url
     *              图片的链接
     */
    public void DialogImageShow(String url){
        WebView img=new WebView(MainActivity.this);
        img.loadUrl(url);

        WebSettings imgSetting=img.getSettings();//获得WebView的设置
        imgSetting.setUseWideViewPort(true);//页面大小自适应
        imgSetting.setLoadWithOverviewMode(true);//加载完全缩小的WebView

        AlertDialog.Builder show_image=new AlertDialog.Builder(MainActivity.this);
        show_image.setView(img);
        show_image.setPositiveButton("关闭", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        show_image.show();
    }

    /**
     * 将图片全屏显示
     * @param imageView
     *              ImageView
     */
    public void OnScreenShow(ImageView imageView){
        Dialog dialog = new Dialog(MainActivity.this);

        ImageView image=new ImageView(MainActivity.this);
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
     * 创建一个WebView弹窗
     * @param context
     *              界面
     * @param Url
     *              访问的链接,格式为字符串
     */
    public void WebViewDialog(Context context,String Url){
        WebView webView=new WebView(context);

        webView.loadUrl(Url);

        WebSettings webSettings=webView.getSettings();//获得WebView的设置
        webSettings.setUseWideViewPort(true);//页面大小自适应
        webSettings.setLoadWithOverviewMode(true);//加载完全缩小的WebView

        AlertDialog.Builder web_dialog=new AlertDialog.Builder(context);
        web_dialog.setView(webView);
        web_dialog.setPositiveButton("关闭", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        web_dialog.show();
    }

    /**
     * 从当前页面跳转到WebView界面
     * @param Url
     *              跳转的Url
     */
    public void ToWebActivity(String Url){
        Intent intent=new Intent(MainActivity.this,WebActivity.class);
        intent.putExtra("Url",Url);//向WebActivity传值
        startActivity(intent);
    }

    public void ToChatActivity(View view){
        Intent intent=new Intent(MainActivity.this,ChatActivity.class);
        startActivity(intent);
    }

    /*
    图片回调函数
     */
    @SuppressLint("SetTextI18n")
    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && null != data) {
            Uri selectedImage = data.getData();

            try
            {
                if (requestCode == SELECT_IMAGE) {
                    bitmap = decodeUri(selectedImage);
                    yourSelectedImage = bitmap.copy(Bitmap.Config.ARGB_8888, true);
                    //imageView.setImageBitmap(bitmap);

                    String[] proj = {MediaStore.Images.Media.DATA};
                    Cursor cursor = managedQuery(selectedImage, proj, null, null, null);
                    //获得用户选择的图片的索引值
                    int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                    //将光标移至开头 ，这个很重要，不小心很容易引起越界
                    cursor.moveToFirst();
                    //最后根据索引值获取图片路径
                    imagePath = cursor.getString(column_index);
                    imageName = imagePath.substring(imagePath.lastIndexOf("/")+1);


                    /*
                    在此处理发送图片的逻辑和机器人返回的值
                     */
                    insert_sql="insert into user(Character,type,contents)" +
                            "values('user','photo','" +
                            imagePath + "')";
                    Database.execSQL(insert_sql);//向数据库插入数据

                    RobotReturn(talk_view,bitmap,imagePath);//机器人返回
                }
            }
            catch (FileNotFoundException e)
            {
                Log.e("MainActivity", "FileNotFoundException");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private Bitmap decodeUri(Uri selectedImage) throws FileNotFoundException
    {
        // Decode image size
        BitmapFactory.Options o = new BitmapFactory.Options();
        o.inJustDecodeBounds = true;
        BitmapFactory.decodeStream(getContentResolver().openInputStream(selectedImage), null, o);

        // The new size we want to scale to
        final int REQUIRED_SIZE = 640;

        // Find the correct scale value. It should be the power of 2.
        int width_tmp = o.outWidth, height_tmp = o.outHeight;
        int scale = 1;
        while (true) {
            if (width_tmp / 2 < REQUIRED_SIZE
                    || height_tmp / 2 < REQUIRED_SIZE) {
                break;
            }
            width_tmp /= 2;
            height_tmp /= 2;
            scale *= 2;
        }

        // Decode with inSampleSize
        BitmapFactory.Options o2 = new BitmapFactory.Options();
        o2.inSampleSize = scale;
        Bitmap bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(selectedImage), null, o2);

        // Rotate according to EXIF
        int rotate = 0;
        try
        {
            ExifInterface exif = new ExifInterface(getContentResolver().openInputStream(selectedImage));
            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_270:
                    rotate = 270;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    rotate = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_90:
                    rotate = 90;
                    break;
            }
        }
        catch (IOException e)
        {
            Log.e("MainActivity", "ExifInterface IOException");
        }

        Matrix matrix = new Matrix();
        matrix.postRotate(rotate);
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }

    int ret = 0; // 函数调用返回值

    public void ifly() {
        buffer.setLength(0);
        sent_editText.setText(null);// 清空显示内容
        mIatResults.clear();
        // 设置参数
        setParam();

        //boolean isShowDialog = mSharedPreferences.getBoolean(
        //getString(R.string.pref_key_iat_show), true);

        // 显示听写对话框
        mIatDialog.setListener(mRecognizerDialogListener);
        mIatDialog.show();
        showTip("请开始说话");
    }

    /**
     * 参数设置
     *
     * @return
     */
    public void setParam() {
        // 清空参数
        mIat.setParameter(SpeechConstant.PARAMS, null);
        // 设置听写引擎
        mIat.setParameter(SpeechConstant.ENGINE_TYPE, mEngineType);
        // 设置返回结果格式
        mIat.setParameter(SpeechConstant.RESULT_TYPE, resultType);

        if (language.equals("zh_cn")) {
            String lag = mSharedPreferences.getString("iat_language_preference",
                    "mandarin");
            // 设置语言
            Log.e(TAG, "language = " + language);
            mIat.setParameter(SpeechConstant.LANGUAGE, "zh_cn");
            // 设置语言区域
            mIat.setParameter(SpeechConstant.ACCENT, lag);
        } else {
            mIat.setParameter(SpeechConstant.LANGUAGE, language);
        }
        Log.e(TAG, "last language:" + mIat.getParameter(SpeechConstant.LANGUAGE));

        //此处用于设置dialog中不显示错误码信息
        //mIat.setParameter("view_tips_plain","false");

        // 设置语音前端点:静音超时时间，即用户多长时间不说话则当做超时处理
        mIat.setParameter(SpeechConstant.VAD_BOS, mSharedPreferences.getString("iat_vadbos_preference", "4000"));

        // 设置语音后端点:后端点静音检测时间，即用户停止说话多长时间内即认为不再输入， 自动停止录音
        mIat.setParameter(SpeechConstant.VAD_EOS, mSharedPreferences.getString("iat_vadeos_preference", "1000"));

        // 设置标点符号,设置为"0"返回结果无标点,设置为"1"返回结果有标点
        mIat.setParameter(SpeechConstant.ASR_PTT, mSharedPreferences.getString("iat_punc_preference", "0"));

        // 设置音频保存路径，保存音频格式支持pcm、wav.
        mIat.setParameter(SpeechConstant.AUDIO_FORMAT, "wav");
        mIat.setParameter(SpeechConstant.ASR_AUDIO_PATH,
                MainActivity.this.getExternalFilesDir("msc").getAbsolutePath() + "/iat.wav");
    }

    /**
     * 听写UI监听器
     */
    private RecognizerDialogListener mRecognizerDialogListener = new RecognizerDialogListener() {
        // 返回结果
        public void onResult(RecognizerResult results, boolean isLast) {
            printResult(results);
        }

        // 识别回调错误
        public void onError(SpeechError error) {
            showTip(error.getPlainDescription(true));
        }

    };

    private void showTip(final String str) {
        if (mToast != null) {
            mToast.cancel();
        }
        mToast = Toast.makeText(MainActivity.this, str, Toast.LENGTH_SHORT);
        mToast.show();
    }

    /**
     * 听写监听器。
     */
    private RecognizerListener mRecognizerListener = new RecognizerListener() {

        @Override
        public void onBeginOfSpeech() {
            // 此回调表示：sdk内部录音机已经准备好了，用户可以开始语音输入
            showTip("开始说话");
        }

        @Override
        public void onError(SpeechError error) {
            // Tips：
            // 错误码：10118(您没有说话)，可能是录音机权限被禁，需要提示用户打开应用的录音权限。
            Log.d(TAG, "onError " + error.getPlainDescription(true));
            showTip(error.getPlainDescription(true));

        }

        @Override
        public void onEndOfSpeech() {
            // 此回调表示：检测到了语音的尾端点，已经进入识别过程，不再接受语音输入
            showTip("结束说话");
        }

        @Override
        public void onResult(RecognizerResult results, boolean isLast) {
            Log.d(TAG, results.getResultString());
            if (isLast) {
                Log.d(TAG, "onResult 结束");
            }
            if (resultType.equals("json")) {
                printResult(results);
                return;
            }
            if (resultType.equals("plain")) {
                buffer.append(results.getResultString());
                sent_editText.setText(buffer.toString());
                sent_editText.setSelection(sent_editText.length());
            }
        }

        @Override
        public void onVolumeChanged(int volume, byte[] data) {
            showTip("当前正在说话，音量大小 = " + volume + " 返回音频数据 = " + data.length);
        }

        @Override
        public void onEvent(int eventType, int arg1, int arg2, Bundle obj) {
            // 以下代码用于获取与云端的会话id，当业务出错时将会话id提供给技术支持人员，可用于查询会话日志，定位出错原因
            // 若使用本地能力，会话id为null
            //	if (SpeechEvent.EVENT_SESSION_ID == eventType) {
            //		String sid = obj.getString(SpeechEvent.KEY_EVENT_SESSION_ID);
            //		Log.d(TAG, "session id =" + sid);
            //	}
        }
    };
    /**
     * 显示结果
     */
    private void printResult(RecognizerResult results) {
        String text = JsonParser.parseIatResult(results.getResultString());
        String sn = null;
        // 读取json结果中的sn字段
        try {
            JSONObject resultJson = new JSONObject(results.getResultString());
            sn = resultJson.optString("sn");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        mIatResults.put(sn, text);

        StringBuffer resultBuffer = new StringBuffer();
        for (String key : mIatResults.keySet()) {
            resultBuffer.append(mIatResults.get(key));
        }
        sent_editText.setText(resultBuffer.toString());
        sent_editText.setSelection(sent_editText.length());
    }
    /**
     * 初始化监听器。
     */
    private InitListener mInitListener = new InitListener() {

        @Override
        public void onInit(int code) {
            Log.d(TAG, "SpeechRecognizer init() code = " + code);
            if (code != ErrorCode.SUCCESS) {
                showTip("初始化失败，错误码：" + code + ",请点击网址https://www.xfyun.cn/document/error-code查询解决方案");
            }
        }
    };
}