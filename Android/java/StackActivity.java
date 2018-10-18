package com.example.asaem.dadm;

import android.app.ListActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;

/**
 * Created by Songbum on 2016-11-28.
 */
public class StackActivity extends ListActivity {
    // Debugging
    private static final String TAG = "StackActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate()");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stack);

        final StackMessageHandler mStackMessageHandler = new StackMessageHandler(this);
        ArrayList<ArrayList<String>> stackList = mStackMessageHandler.getStackList();

        ArrayAdapter<ArrayList<String>> stackAdapter;
        stackAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, stackList);

        ListView listView = (ListView) findViewById(android.R.id.list);
        listView.setAdapter(stackAdapter);
    }
}
