package com.example.asaem.dadm;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.ArrayList;

/**
 * Created by Songbum on 2016-11-28.
 */
public class StackMessageHandler {
    // Debugging
    private static final String TAG = "StackMessageHandler";

    // object of DBHelper class
    private StackMessageHelper mStackMessageHelper;

    public StackMessageHandler(Context context){
        mStackMessageHelper = new StackMessageHelper(context);
    }

    // db.insert
    public int insertToStack(StackMessage mStackMessage){
        Log.d(TAG, "insertToStack()");
        SQLiteDatabase db = mStackMessageHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(StackMessage.KEY_SENDER, mStackMessage.sender);
        values.put(StackMessage.KEY_MSG, mStackMessage.message);

        long stack_id = db.insert(StackMessage.TABLE, null, values);
        db.close();
        return (int)stack_id;
    }

    // db.delete
    public void deleteFromStack(int stack_id){
        Log.d(TAG, "deleteFromStack()");
        SQLiteDatabase db = mStackMessageHelper.getWritableDatabase();
        db.delete(StackMessage.TABLE, StackMessage.KEY_ID + "= ?", new String[]{String.valueOf(stack_id)});
        db.close();
    }

    /*
       get stack message list
       number, message
       */
    public ArrayList<ArrayList<String>> getStackList(){
        Log.d(TAG, "getStackList()");
        SQLiteDatabase db = mStackMessageHelper.getReadableDatabase();

        String selectQuery = "SELECT "
                + StackMessage.KEY_ID + ", "
                + StackMessage.KEY_SENDER + ", "
                + StackMessage.KEY_MSG + " FROM "
                + StackMessage.TABLE;

        ArrayList<ArrayList<String>> stackList = new ArrayList<>();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()){
            do {
                ArrayList<String> stackListValues = new ArrayList<>();
                stackListValues.add(cursor.getString(cursor.getColumnIndex(StackMessage.KEY_SENDER)));
                stackListValues.add(cursor.getString(cursor.getColumnIndex(StackMessage.KEY_MSG)));
                stackList.add(stackListValues);
            } while (cursor.moveToNext());
        }

        if (cursor != null && !cursor.isClosed()){
            cursor.close();
        }
        db.close();
        return stackList;
    }
}
