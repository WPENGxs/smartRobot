package com.wpeng.smartrobot;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

public class SQLite extends SQLiteOpenHelper {
    public SQLite(@Nullable Context context, @Nullable String name, @Nullable SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }
    /*
    * 建立SQLite数据库
    * */
    @Override
    public void onCreate(SQLiteDatabase Database) {
        String sql="CREATE TABLE user(userid integer primary key autoincrement,"+
                "character text not null,"+//角色
                //user robot
                "type text not null,"+//类型
                //text photo url
                "contents text"+//内容
                //text uri url
                ")";
        Database.execSQL(sql);
    }
    @Override
    public void onUpgrade(SQLiteDatabase Database, int oldVersion, int newVersion) {
    }

}
