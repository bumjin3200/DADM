package com.example.asaem.dadm;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by Songbum on 2016-12-04.
 */
public class ReportHelper extends SQLiteOpenHelper {
    // debugging
    private static final String TAG = "ReportHelper";

    // db version and db name
    private static final int DB_VERSION = 5;
    private static final String DB_NAME = "reportdb";

    public ReportHelper(Context context){
        super(context, DB_NAME, null, DB_VERSION);
    }

    // create new db table
    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        Log.d(TAG, "onCreate() DB version: " + DB_VERSION);
        String createStatement = "CREATE TABLE "+Report.TABLE+"("
                +Report.KEY_ID+" INTEGER PRIMARY KEY AUTOINCREMENT, "
                +Report.KEY_NUMBER+" TEXT, "
                +Report.KEY_MSG+" TEXT)";
        sqLiteDatabase.execSQL(createStatement);

        String msg = "교통 사고 발생";
        this.addSetting(sqLiteDatabase, 0, "01076549816", msg);
    }

    // upgrade db table
    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        Log.d(TAG, "onUpgrade() DB version: " + DB_VERSION);
        String dropStatement = "DROP TABLE IF EXISTS "+Report.TABLE;
        sqLiteDatabase.execSQL(dropStatement);
        onCreate(sqLiteDatabase);
    }

    public void addSetting(SQLiteDatabase db, int key, String num, String msg){
        ContentValues values = new ContentValues();
        values.put(Report.KEY_ID, key);
        values.put(Report.KEY_NUMBER, num);
        values.put(Report.KEY_MSG, msg);
        db.insert(Report.TABLE, null, values);
    }
}
