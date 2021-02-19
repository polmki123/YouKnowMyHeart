package com.example.camera3;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.Handler;

public class LoadingActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loading);
        
        //gif 이미지뷰에 실행
        ImageView loading = (ImageView) findViewById(R.id.imageView);
        Glide.with(this).load(R.drawable.loading_a).into(loading);
        
        startLoading();
    }

    private void startLoading() {
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                finish();
            }
        }, 8000);
    }
}
