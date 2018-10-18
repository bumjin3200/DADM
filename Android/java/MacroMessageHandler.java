package com.example.asaem.dadm;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

/**
 * Created by Songbum on 2016-11-28.
 */
public class MacroMessageHandler {
    // Debugging
    private static final String TAG = "MacroMessageHandler";

    // object of DBHelper class
    private MacroMessageHelper mMacroMessageHelper;

    public MacroMessageHandler(Context context){
        mMacroMessageHelper = new MacroMessageHelper(context);
    }

    // db.insert
    public int insertMacroMessage(MacroMessage mMacroMessage){
        Log.d(TAG, "insertMacroMessage()");

        SQLiteDatabase db = mMacroMessageHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(MacroMessage.KEY_MSG, mMacroMessage.message);

        long message_id = db.insert(MacroMessage.TABLE, null, values);
        db.close();
        return (int)message_id;
    }

    // db.delete
    public void deleteMacroMessage(int message_id){
        Log.d(TAG, "deleteMacroMessage()");

        SQLiteDatabase db = mMacroMessageHelper.getWritableDatabase();
        db.delete(MacroMessage.TABLE, MacroMessage.KEY_ID + "= ?", new String[]{String.valueOf(message_id)});
        db.close();
    }

    // db.update
    public void updateMacroMessage(MacroMessage mMacroMessage){
        Log.d(TAG, "updateMacroMessage()");

        SQLiteDatabase db = mMacroMessageHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(MacroMessage.KEY_MSG, mMacroMessage.message);

        db.update(MacroMessage.TABLE, values, MacroMessage.KEY_ID + "= ?", new String[]{String.valueOf(mMacroMessage.id)});
        db.close();
    }

    /*
    select query
    parameter : key id of macro db
    return : macro message
    */
    public MacroMessage getMacroMessageById(int message_id){
        SQLiteDatabase db = mMacroMessageHelper.getReadableDatabase();
        String selectQuery = "SELECT "
                + MacroMessage.KEY_ID + ", "
                + MacroMessage.KEY_MSG + " FROM "
                + MacroMessage.TABLE + " WHERE "
                + MacroMessage.KEY_ID + "= ?";

        MacroMessage mMacroMessage = new MacroMessage();
        @SuppressLint("Recycle") Cursor cursor = db.rawQuery(selectQuery, new String[]{String.valueOf(message_id)});

        if (cursor.moveToFirst()){
            do {
                mMacroMessage.id = cursor.getInt(cursor.getColumnIndex(MacroMessage.KEY_ID));
                mMacroMessage.message = cursor.getString(cursor.getColumnIndex(MacroMessage.KEY_MSG));
            } while (cursor.moveToNext());
        }
        if (cursor != null && !cursor.isClosed()){
            cursor.close();
        }
        db.close();
        return  mMacroMessage;
    }
}

