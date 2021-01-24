package com.example.weather;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        try {
            getWeather();
        } catch (AuthFailureError authFailureError) {
            authFailureError.printStackTrace();
        }
    }

    public void getWeather() throws AuthFailureError {
        // Instantiate the RequestQueue.
        RequestQueue queue = Volley.newRequestQueue(this);
        // 실시간 날씨 정보 획득 실패
//        String url ="http://apis.data.go.kr/1360000/VilageFcstInfoService/getVilageFcst?";
        String url ="http://apis.data.go.kr/1360000/VilageFcstInfoService/getUltraSrtNcst?";
//        String url ="http://apis.data.go.kr/1360000/VilageFcstInfoService/getFcstVersion?";

        String service_key = "Dkxm1ddffHy7wnm0vib%2Bu0%2FqjCnyklnU161UZFWqSumxJi6rd3m%2FPNyEfAg4IMOEu9jgbD7RwbiYJEmJkDKaIA%3D%3D";

        long now = System.currentTimeMillis();
        Date mdate = new Date(now);

        SimpleDateFormat baseDate = new SimpleDateFormat("yyyyMMdd");
        String getBaseDate = baseDate.format(mdate);
        String base_date = getBaseDate;

        baseDate = new SimpleDateFormat("HHmm");
        Log.i("ASJ", "시간 : "+mdate);
        String getBaseTime = baseDate.format(mdate);
//        getBaseTime.
        Log.i("ASJ", "Base Time : "+getBaseTime);
//        String base_time = getBaseTime;
        String base_time = "2144";
//        base_time = getBaseTime;
        Log.i("ASJ","Base_Time : "+base_time);

        String nx = "60";
        String ny = "128";

        String payload = "serviceKey=" + service_key + "&" +
                "dataType=json" + "&" +
                "base_date=" + base_date + "&" +
                "base_time=" + base_time + "&" +
                "nx=" + nx + "&" +
                "ny=" + ny;

        url = url + payload;
        Log.i("ASJ", "URL : "+url);

        JsonObjectRequest jsonObjectRequest =new JsonObjectRequest(Request.Method.GET, url,null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {

                                JsonParser parser = new JsonParser();
                                JsonObject object = (JsonObject)parser.parse(response.toString());
                                JsonObject parse_response = (JsonObject) object.getAsJsonObject("response");

                                String body = parse_response.getAsJsonObject("body").toString();

                                object = (JsonObject)parser.parse(body);

                                parse_response = (JsonObject)object.getAsJsonObject("items");

                                JsonArray jArray = (JsonArray)parse_response.getAsJsonArray("item");

                                String baseDate = new String();
                                String baseTime = new String();
                                String precipitation = new String(); // 강수타입
                                 String temper = new String(); // 기온

                                for(int i = 0; i<jArray.size(); i++)
                                {
                                    JsonObject ob = jArray.get(i).getAsJsonObject();
                                    // PTY = 강수형태 : 비(1), 비/눈(2), 눈(3), 소나기(4), 빗방울(5), 빗방울/눈날림(6), 눈날림(7)
                                    //
                                    baseDate = jArray.get(i).getAsJsonObject().get("baseDate").toString();
                                    baseTime = jArray.get(i).getAsJsonObject().get("baseTime").toString();
                                    if(jArray.get(i).getAsJsonObject().get("category").toString().equals("\"PTY\""))
                                    {
                                        precipitation = jArray.get(i).getAsJsonObject().get("obsrValue").toString();
                                        precipitation = precipitation.replace("\"","");
                                    }

                                    if(jArray.get(i).getAsJsonObject().get("category").toString().equals("\"T1H\""))
                                    {
                                        temper = jArray.get(i).getAsJsonObject().get("obsrValue").toString();
                                        temper = temper.replace("\"","");
                                    }

//                                    Log.i("ASJ", jArray.get(i).getAsJsonObject().get("category").toString());
                                }


                        Log.i("ASJ", "강수타입 :"+precipitation);
                        Log.i("ASJ", "기온 :"+temper);
                    }
                },new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.i("실패","실패");
            }
        });

//        // queue에 Request를 추가해준다.
        queue.add(jsonObjectRequest);

//        System.out.println("응답"+jsonObjectRequest.getBody());
    }
}