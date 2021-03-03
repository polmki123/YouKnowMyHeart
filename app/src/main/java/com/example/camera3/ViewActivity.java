package com.example.camera3;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.WindowManager;
import android.webkit.GeolocationPermissions;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

public class ViewActivity extends AppCompatActivity {

    private WebView webView;
    String keyword;
    private static final int MY_PERMISSION_REQUEST_LOCATION = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_view);

        Intent getIntent = getIntent();
        keyword = getIntent.getStringExtra("url");
        webView = (WebView) findViewById(R.id.webView);

        permissionCheck();
    }

    private void initWebView(){
        webView.getSettings().setJavaScriptEnabled(true);
        webView.setWebViewClient(new WebViewClient());
        webView.setWebViewClient(new WebViewClientClass());
        // 새로운 창을 띄우지 않고 내부에서 웹뷰를 실행시킨다.
        webView.setWebChromeClient(new WebChromeClient(){
            @Override
            public void onGeolocationPermissionsShowPrompt(String origin, GeolocationPermissions.Callback callback) {
                super.onGeolocationPermissionsShowPrompt(origin, callback);
                callback.invoke(origin, true, false);
            }
        });

        webView.loadUrl(keyword);
    }

    private void permissionCheck(){
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            //Manifest.permission.ACCESS_FINE_LOCATION 접근 승낙 상태 일때
            initWebView();
        } else {
            //Manifest.permission.ACCESS_FINE_LOCATION 접근 거절 상태 일때
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION},MY_PERMISSION_REQUEST_LOCATION);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == MY_PERMISSION_REQUEST_LOCATION) {
            initWebView();
        }
    }

    @Override
    public void onBackPressed() {
        if(webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
        }
    }

    private class WebViewClientClass extends WebViewClient {
        @Override
        public void onPageFinished(WebView view, String url) {
            getChoice();
        }
    }

    public void getChoice(){
        //url get
        String geturl = webView.getUrl();
        //url decode
        String decodeUrl = null;
        try {
            decodeUrl = URLDecoder.decode(geturl , "UTF-8" );
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        Log.i("URL", "주소 : " + decodeUrl);
        String[] choice = decodeUrl.split("/");

        Log.i("URL", "장소 : " + choice[4]);
        if( choice[4].toString().equals("place"))
        {
            //url 표시
            Toast.makeText(getApplicationContext(), choice[5], Toast.LENGTH_SHORT).show();
            Log.i("URL", "끝--------: " + choice[5]);
        }
    }
}