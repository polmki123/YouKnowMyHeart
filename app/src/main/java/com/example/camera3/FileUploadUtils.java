package com.example.camera3;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
//import android.support.v7.app.AppCompatActivity;
//import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.animation.Animation;
import android.widget.ImageView;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;

public class FileUploadUtils {
    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    public static String NETOK, AGE, GENDER;
    public static Double ANGRY, DISGUST, FEAR, HAPPY, NEUTRAL, SAD, SURPRISE;

    public static void send2Server(String  file_path){

        String TAG = "FOOD";
        Log.i(TAG, "sned2Server");
        NETOK = "-1";
//        ImageView image =(ImageView)findViewById(R.id.image);

        //encode image to base64 string
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
//        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.logo);
//        String file_path = mSaveBit.getPath(); // 만약 path로 안오고 file로 올 경우 path를 얻는다.
        Bitmap bitmap = BitmapFactory.decodeFile(file_path);
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] imageBytes = baos.toByteArray();
        String imageString = Base64.encodeToString(imageBytes, Base64.DEFAULT);

        //decode base64 string to image
//        imageBytes = Base64.decode(imageString, Base64.DEFAULT);
//        Bitmap decodedImage = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
//        image.setImageBitmap(decodedImage);
        JSONObject jsonInput = new JSONObject();
        JSONObject jsonInput2 = new JSONObject();
        JSONArray jarray = new JSONArray();
        try {
            jsonInput2.put( TAG, "data:image/jpeg;base64," + imageString);
        } catch (Exception e) {
            e.printStackTrace();
            NETOK = "0";
            return;
        }
        jarray.put("data:image/jpeg;base64," + imageString);
        try {
            jsonInput.put("img",    jarray );
        } catch (Exception e) {
            e.printStackTrace();
            NETOK = "0";
            return;
        }
        RequestBody requestBody = RequestBody.create(JSON , jsonInput.toString() );
        Log.i("FOOD", "JSON INPUT : "+jsonInput.toString() );
        Request request = new Request.Builder()
//                .url("http://172.16.0.98:18080/upload")
//                .url("http://10.0.2.2:8080/upload")
//                .url("http://3.35.234.219:5000/analyze")
//                .url("http://ec2-3-35-234-219.ap-northeast-2.compute.amazonaws.com:5000/analyze")
//                .url("http://b190d080e05c.ngrok.io/analyze")
                .url("http://3.34.198.134:5000/analyze")

                .post(requestBody)
                .build();

//                OkHttpClient client = new OkHttpClient();

        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build();

        client.newCall(request).enqueue(new Callback() {

            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try {
                    String isOk = response.networkResponse().toString();
                    String[] isOkArray = isOk.split("code=");
                    isOkArray = isOkArray[1].split(",");
                    isOk = isOkArray[0];
                    if (isOk.equals("200"))
                    {
                        JSONObject jObj = new JSONObject(response.body().string());
                        AGE      = jObj.getJSONObject("instance_1").getString("age");
                        ANGRY    = jObj.getJSONObject("instance_1").getJSONObject("emotion").getDouble("angry");
                        DISGUST  = jObj.getJSONObject("instance_1").getJSONObject("emotion").getDouble("disgust");
                        FEAR     = jObj.getJSONObject("instance_1").getJSONObject("emotion").getDouble("fear");
                        HAPPY    = jObj.getJSONObject("instance_1").getJSONObject("emotion").getDouble("happy");
                        NEUTRAL  = jObj.getJSONObject("instance_1").getJSONObject("emotion").getDouble("neutral");
                        SAD      = jObj.getJSONObject("instance_1").getJSONObject("emotion").getDouble("sad");
                        SURPRISE = jObj.getJSONObject("instance_1").getJSONObject("emotion").getDouble("surprise");
                        GENDER   = jObj.getJSONObject("instance_1").getString("gender");

                        NETOK    = isOk;

                        Log.i(TAG, "NETWORK : "+NETOK+"   "+
                                "AGE : "+AGE+"   "+
                                "ANGRY : "+Double.toString(ANGRY)+"   "+
                                "DISGUST : "+Double.toString(DISGUST)+"   "+
                                "FEAR : "+Double.toString(FEAR)+"   "+
                                "HAPPY : "+Double.toString(HAPPY)+"   "+
                                "NEUTRAL : "+Double.toString(NEUTRAL)+"   "+
                                "SAD : "+Double.toString(SAD)+"   "+
                                "SURPRISE : "+Double.toString(SURPRISE)+"   "+
                                "GENDER : "+GENDER);

                    }
                    else
                    {
                        Log.i(TAG, "얼굴을 인식할 수 없습니다.");
                        NETOK = "0";
                    }
//                    JSONObject jObj = new JSONObject(response.body().string());

                } catch (JSONException e) {
                    NETOK = "0";
                    e.printStackTrace();
                }

//                Log.i(TAG, "결과 :"+response.body().string());
            }
        });
    }
}

