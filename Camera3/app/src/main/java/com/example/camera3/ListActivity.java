package com.example.camera3;

import com.example.camera3.db.*;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
//
//import com.android.volley.AuthFailureError;
//import com.android.volley.Request;
//import com.android.volley.RequestQueue;
//import com.android.volley.Response;
//import com.android.volley.VolleyError;
//import com.android.volley.toolbox.JsonObjectRequest;
//import com.android.volley.toolbox.Volley;

import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;

import org.json.JSONException;
import org.json.JSONObject;
import org.tensorflow.lite.Interpreter;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Random;


public class ListActivity extends AppCompatActivity {

    public List<FoodData> foodList ;
    public String url;
    public LocationListener mLocationListener;

    private void initLoadDB() {

        DataAdapter mDbHelper = new DataAdapter(getApplicationContext());
        mDbHelper.createDatabase();
        mDbHelper.open();

        // db에 있는 값들을 model을 적용해서 넣는다.
        foodList = mDbHelper.getTableData();

        // db 닫기
        mDbHelper.close();
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);
        ListView listview = (ListView)findViewById(R.id.listview);
        TextView selected_item_textview = (TextView)findViewById(R.id.selected_item_textview);

        Intent getMainIntent = getIntent();

        //데이터를 저장하게 되는 리스트
        List<String> list = new ArrayList<>();

        //리스트뷰와 리스트를 연결하기 위해 사용되는 어댑터
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1, list);

        //리스트뷰의 어댑터를 지정해준다.
        listview.setAdapter(adapter);

        //리스트뷰의 아이템을 클릭시 해당 아이템의 문자열을 가져오기 위한 처리
        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> adapterView,
                                    View view, int position, long id) {

                //클릭한 아이템의 문자열을 가져옴
                //String selected_item = (String)adapterView.getItemAtPosition(position);
                Double latitude  = getMainIntent.getDoubleExtra("latitude" , 0.0);
                Double longitude = getMainIntent.getDoubleExtra("longitude",0.0);

                url = "https://www.google.co.kr/maps/search/" + (String)adapterView.getItemAtPosition(position)
                        + "/@" + latitude + "," + longitude + ",16z?hl=ko";
                Log.i("FOOD", "URL : "+url);

                Intent intent = new Intent(getApplicationContext(), ViewActivity.class);
                intent.putExtra("url", url);
                startActivity(intent);

            }
        });

        Random ran = new Random();

        float[][] input = new float[1][7];
        for(int x= 0 ; x<7 ; x++){
            if(getMainIntent.getDoubleExtra(Integer.toString(x), 0.0) > 1.0)
                input[0][x] = 1;
            else
                input[0][x] = 0;
        }

        selected_item_textview.setText("분노:"+Float.toString(input[0][0])+','
                +"혐오:"+Float.toString(input[0][1])+','
                +"공포:"+Float.toString(input[0][2])+','
                +"행복:"+Float.toString(input[0][3])+','
                +"보통:"+Float.toString(input[0][4])+','
                +"슬픔:"+Float.toString(input[0][5])+','
                +"놀람:"+Float.toString(input[0][6]));

        float[][] output = new float[1][160];

        int maxLeng = output[0].length;

        float[][] output_idx = new float[2][maxLeng];

        /* ModelClient.java */
        Interpreter tflite = getTfliteInterpreter("food_model.tflite");
        tflite.run(input,output);

        Log.i("FOOD", "Data :" + Arrays.toString(output[0]));
        // Max 인덱스 가져오기
        float max = output[0][0];
        Log.i("FOOD", "길이 :" + Integer.toString(output[0].length));
        int max_idx = 0;
        float [] maxFood = new float[5];

        // 인덱스용 배열
        for(int i=0;i<maxLeng;i++){
            output_idx[0][i] = output[0][i];
            output_idx[1][i] = i;
        }

        //output 결과 내림차순 정렬
        for(int i=0; i<maxLeng; i++) {
            for(int j=i+1; j<maxLeng; j++) {
                if(output[0][i] < output[0][j]) { //내림차순
                    float tmp = output[0][i];
                    output[0][i] = output[0][j];
                    output[0][j] = tmp;
                }
            }
        }

        //max5개 값 저장
        for(int i=0; i<5; i++){
            maxFood[i] = output[0][i];
            Log.i("FOOD", "최대값 : "+ Float.toString(maxFood[i]));
        }
        //food DB 가져오기
        this.initLoadDB();

        for(int i=0;i<5;i++) {
            for(int j=0;j<maxLeng;j++)
            {
                if(maxFood[i] == output_idx[0][j])
                {
                    list.add(foodList.get((int)output_idx[1][j]).getFoodName());

                    break;
                }
            }
        }
        Log.i("결과", foodList.get(1).getFoodName());
//        Log.d("DB",Integer.toString(foodList.size()));

    }

    public Interpreter getTfliteInterpreter(String modelPath) {
        try {
            return new Interpreter(loadModelFile(ListActivity.this, modelPath));
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }



    /** Load TF Lite model from assets. */

    public MappedByteBuffer loadModelFile(Activity activity, String modelPath) throws IOException {
        AssetFileDescriptor fileDescriptor = activity.getAssets().openFd(modelPath);
        FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
        FileChannel fileChannel = inputStream.getChannel();
        long startOffset = fileDescriptor.getStartOffset();
        long declaredLength = fileDescriptor.getDeclaredLength();

        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
    }

}