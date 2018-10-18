package com.example.asaem.dadm;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

/**
 * Created by Songbum on 2016-11-28.
 */
public class IntroActivity extends AppCompatActivity{
    //Debugging
    private static final String TAG = "IntroActivity";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intro);

        // Hide action bar
        ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.hide();

        Handler handler = new Handler();
        // Show intro screen for 2 seconds, start MainActivity
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "run()");
                Intent intent = new Intent(IntroActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        }, 2000);
    }
}
