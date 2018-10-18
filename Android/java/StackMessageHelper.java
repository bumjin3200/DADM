package com.example.asaem.dadm;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Songbum on 2016-11-28.
 */
public class StackMessageHelper extends SQLiteOpenHelper {
    // Debugging
    private static final String TAG = "StackMessageHelper";

    // db version and db name
    private static final int DB_VERSION = 3;
    private static final String DB_NAME = "stackdb";

    public StackMessageHelper(Context context){
        super(context, DB_NAME, null, DB_VERSION);
    }

    // create new db table
    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        String createStatement = "CREATE TABLE "
                +StackMessage.TABLE + "("
                +StackMessage.KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                +StackMessage.KEY_SENDER + " TEXT, "
                +StackMessage.KEY_MSG + " TEXT)";
        sqLiteDatabase.execSQL(createStatement);
    }

    // upgrade db table
    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        String dropStatement = "DROP TABLE IF EXISTS "+StackMessage.TABLE;
        sqLiteDatabase.execSQL(dropStatement);
        onCreate(sqLiteDatabase);
    }
}
