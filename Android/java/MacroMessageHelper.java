package com.example.asaem.dadm;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by Songbum on 2016-11-28.
 */
public class MacroMessageHelper extends SQLiteOpenHelper {
    // Debugging
    private static final String TAG = "MacroMessageHelper";

    // db version and db name
    private static final int DB_VERSION = 5;
    private static final String DB_NAME = "macrodb";

    public MacroMessageHelper(Context context){
        super(context, DB_NAME, null, DB_VERSION);
    }

    // create database table
    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        Log.d(TAG, "onCreate() DB version: " + DB_VERSION);
        String createStatement = "CREATE TABLE "+MacroMessage.TABLE+"("
                +MacroMessage.KEY_ID+" INTEGER PRIMARY KEY AUTOINCREMENT, "
                +MacroMessage.KEY_MSG+" TEXT)";
        sqLiteDatabase.execSQL(createStatement);

        String msg = "운전중입니다.\n나중에 연락드리겠습니다.";
        this.addSetting(sqLiteDatabase, 0, msg);
    }

    /*
    upgrade db table
    if db_version up then call this method
    drop exist table and create new table
    */
    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        Log.d(TAG, "onUpgrade() DB version: " + DB_VERSION);
        String dropStatement = "DROP TABLE IF EXISTS "+MacroMessage.TABLE;
        sqLiteDatabase.execSQL(dropStatement);
        onCreate(sqLiteDatabase);
    }

    public void addSetting(SQLiteDatabase db, int key, String msg){
        ContentValues values = new ContentValues();
        values.put(MacroMessage.KEY_ID, key);
        values.put(MacroMessage.KEY_MSG, msg);
        db.insert(MacroMessage.TABLE, null, values);
    }
}