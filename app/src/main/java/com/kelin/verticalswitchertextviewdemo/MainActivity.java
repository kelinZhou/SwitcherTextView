package com.kelin.verticalswitchertextviewdemo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//        final TextView tvText = findViewById(R.id.tvText);
//        tvText.post(new Runnable() {
//            @Override
//            public void run() {
//                int measuredWidth = tvText.getMeasuredWidth();
//                float textWidth = tvText.getPaint().measureText("这是一条很长很长的文字不知道能不能在一行之内放得下。如果放不下则另起一行显示！");
//
//                Log.i("============", "onCreate: measuredWidth=" + measuredWidth);
//                Log.i("============", "onCreate: realMeasuredWidth=" + (measuredWidth - tvText.getPaddingLeft() - tvText.getPaddingRight()));
//                Log.i("============", "onCreate: textWidth=" + textWidth);
//            }
//        });
    }
}
