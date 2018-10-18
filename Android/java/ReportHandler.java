package com.example.asaem.dadm;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

/**
 * Created by Songbum on 2016-12-04.
 */
public class ReportHandler {
    // debugging
    private static final String TAG = "ReportHandler";

    // object of DBHelper class
    private ReportHelper mReportHelper;

    public ReportHandler(Context context){
        mReportHelper = new ReportHelper(context);
    }

    // db.insert
    public int insertReport(Report mReport){
        Log.d(TAG, "insertReport()");
        SQLiteDatabase db = mReportHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(Report.KEY_NUMBER, mReport.number);
        values.put(Report.KEY_MSG, mReport.message);

        long report_id = db.insert(Report.TABLE, null, values);
        db.close();
        return (int)report_id;
    }

    // db.update
    public void updateReport(Report mReport){
        Log.d(TAG, "updateReport()");
        SQLiteDatabase db = mReportHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(Report.KEY_NUMBER, mReport.number);
        values.put(Report.KEY_MSG, mReport.message);

        db.update(Report.TABLE, values, Report.KEY_ID+"= ?", new String[]{String.valueOf(mReport.id)});
        db.close();
    }

    // select query
    public Report getReportById(int report_id){
        SQLiteDatabase db = mReportHelper.getReadableDatabase();
        String selectQuery = "SELECT "
                + Report.KEY_ID + ", "
                + Report.KEY_NUMBER + ", "
                + Report.KEY_MSG + " FROM "
                + Report.TABLE + " WHERE "
                + Report.KEY_ID + "= ?";

        Report mReport = new Report();
        @SuppressLint("Recycle") Cursor cursor = db.rawQuery(selectQuery, new String[]{String.valueOf(report_id)});

        if (cursor.moveToFirst()){
            do {
                mReport.id = cursor.getInt(cursor.getColumnIndex(Report.KEY_ID));
                mReport.number = cursor.getString(cursor.getColumnIndex(Report.KEY_NUMBER));
                mReport.message = cursor.getString(cursor.getColumnIndex(Report.KEY_MSG));
            } while (cursor.moveToNext());
        }
        if (cursor != null && !cursor.isClosed()){
            cursor.close();
        }
        db.close();
        return  mReport;
    }
}
