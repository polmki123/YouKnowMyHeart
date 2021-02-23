package com.example.camera3;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.ImageDecoder;
import android.graphics.Matrix;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.camera3.db.DataAdapter;
import com.example.camera3.db.FoodData;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.json.JSONObject;
import org.tensorflow.lite.Interpreter;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import com.daimajia.swipe.util.Attributes;
import com.example.camera3.util.ListAdapter;
import com.kakao.sdk.template.model.FeedTemplate;


public class ListActivity extends AppCompatActivity {

    final private static String TAG = "FOOD";
    public List<FoodData> foodList ;
    public String url;
    public LocationListener mLocationListener;
    public Double latitude, longitude;
    public int i_latitude, i_longitude;
    public String GENDER, AGE, BaseTime, BaseDate, WEATHER, TEMPERATURE, BaseMonth, PHOTOURL;
    public static int TO_GRID = 0;
    public static int TO_GPS = 1;
    public static emotionExpression emEx = new emotionExpression();
    ImageView listImageView;
    private static String IP_ADDRESS = "3.34.198.134:8080";
    private TextView resultText;


    public void initLoadDB() {

        DataAdapter mDbHelper = new DataAdapter(getApplicationContext());
        mDbHelper.createDatabase();
        mDbHelper.open();

        // db에 있는 값들을 model을 적용해서 넣는다.
        foodList = mDbHelper.getTableData();

        // db 닫기
        mDbHelper.close();
    }

    @SuppressLint({"WrongThread", "MissingPermission"})
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_list);

        ListView listview = (ListView)findViewById(R.id.listview);
        TextView emotion_expression = (TextView)findViewById(R.id.emotion_expression);

        Button btn_back = (Button)findViewById(R.id.btn_back);
        btn_back.setOnClickListener(new View.OnClickListener() {//버튼 이벤트 처리
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        Button btn_link = (Button)findViewById(R.id.btn_link);
        btn_link.setOnClickListener(new View.OnClickListener() {//버튼 이벤트 처리
            @Override
            public void onClick(View view) {
//                Intent msg = new Intent(Intent.ACTION_SEND);
//                msg.addCategory(Intent.CATEGORY_DEFAULT);
//                msg.putExtra(Intent.EXTRA_TEXT, "https://play.google.com/store/apps/details?id=com.example.camera3");
//                msg.putExtra(Intent.EXTRA_TITLE, "내맘알지?");
//                msg.setType("text/plain");
//                startActivity(Intent.createChooser(msg, "앱을 선택해 주세요"));

                View rootView = findViewById(R.id.rootLayout).getRootView();//findViewById를 통해서 루트뷰에 접근하고
                rootView.setDrawingCacheEnabled(true);  //화면에 뿌릴때 캐 시를 사 용하게 한다
                Bitmap screenBitmap = rootView.getDrawingCache();   //캐시를 비트맵으로 변환
                String strBitmap = BitmapToString(screenBitmap);
                InsertCapture captureTask = new InsertCapture();
                captureTask.execute("http://" + IP_ADDRESS + "/imageSave.php", strBitmap);

//                kakaolink();

            }
        });

        listImageView = findViewById(R.id.listImageView);

        Intent getMainIntent = getIntent();

        final Handler my_location_handler=new Handler();
        final LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        Log.i(TAG, "현재 위치 찾기 시작");
        Location location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);

        if(location==null){
            //gps를 이용한 좌표조회 실패시 network로 위치 조회
            location = lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        }
        if(location!=null) {
            longitude = location.getLongitude();
            latitude = location.getLatitude();
        }
        else
        {
            Log.i(TAG, "위치를 찾지 못했습니다.");
            longitude = 37.5980807;
            latitude = 126.9145672;
        }

//        latitude  = getMainIntent.getDoubleExtra("latitude" , 0.0);
//        longitude = getMainIntent.getDoubleExtra("longitude",0.0);
        AGE       = getMainIntent.getStringExtra("AGE");
        GENDER    = getMainIntent.getStringExtra("GENDER");
        PHOTOURL  = getMainIntent.getStringExtra("PHOTOURL");

        File file = new File(PHOTOURL);
        Bitmap bitmap;

        if (Build.VERSION.SDK_INT >= 29) {
            ImageDecoder.Source source = ImageDecoder.createSource(getContentResolver(), Uri.fromFile(file));
            try { bitmap = ImageDecoder.decodeBitmap(source);
                bitmap = rotateImage(bitmap, 0);
                if (bitmap != null) {
                    listImageView.setImageBitmap(bitmap);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else
        {
            try {
                bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), Uri.fromFile(file));
                bitmap = rotateImage(bitmap, 0);
                if (bitmap != null) {
                    listImageView.setImageBitmap(bitmap);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        long now = System.currentTimeMillis()-3600000;
        Date mdate = new Date(now);
        // 3600000 = 1시간
        SimpleDateFormat baseDate = new SimpleDateFormat("HHmm");
        Log.i(TAG, "시간 : "+mdate);
        String getBaseTime = baseDate.format(mdate);
        Log.i(TAG, "Base Time : "+getBaseTime);
        String base_time = getBaseTime;
        Log.i(TAG,"Base_Time : "+base_time);

        // 초기화
        WEATHER = "0";
        BaseMonth = "00";
        TEMPERATURE = "0.0";
        BaseTime = base_time;

        Log.i(TAG, "받은 값 : \n"
                +"위도 : "+latitude+"\n"
                +"경도 : "+longitude+"\n"
                +"나이 : "+AGE+"\n"
                +"성별 : "+GENDER);

        try {
            LatXLngY tmp = convertGRID_GPS(TO_GRID, latitude, longitude);
            Log.i(TAG, "격자 : "+Double.toString(tmp.x));
            Log.i(TAG, "격자 : "+Double.toString(tmp.y));
            Double d_xtmp, d_ytmp;

            d_xtmp = tmp.x;
            d_ytmp = tmp.y;
            i_latitude  = d_xtmp.intValue();
            i_longitude = d_ytmp.intValue();

            getWeather(i_latitude, i_longitude);
        } catch (AuthFailureError authFailureError) {
            authFailureError.printStackTrace();
            Log.i(TAG, "위/경도 숫자 변환 에러!");
        }

        //데이터를 저장하게 되는 리스트
        List list = new ArrayList<>();

        //리스트뷰와 리스트를 연결하기 위해 사용되는 어댑터
//        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
//                android.R.layout.simple_list_item_1, list);

//        ArrayAdapter<String> adapter = new ImageAdapter(this, list);



        // Input 데이터 담기
        float[][] input = new float[1][26];
        for(int x= 0 ; x<7 ; x++){
            emEx.setEmotion(x, getMainIntent.getDoubleExtra(Integer.toString(x), 0.0));
            if(getMainIntent.getDoubleExtra(Integer.toString(x), 0.0) > 1.0)
                input[0][x] = 1;
            else
                input[0][x] = 0;
        }

        // 감정설정 ( 7가지) 끝
        Log.i(TAG,"성별 : "+GENDER);
        if(GENDER.equals("MAN"))
        {
            input[0][7] = 1;
            input[0][8] = 0;
        }
        else
        {
            input[0][7] = 1;
            input[0][8] = 0;
        }
        // 성별 설정 ( 2가지) 끝
        //강수형태 : 비(1), 비/눈(2), 눈(3), 소나기(4), 빗방울(5), 빗방울/눈날림(6), 눈날림(7)
        Log.i(TAG,"강수형태 : "+WEATHER);
        if(WEATHER.equals("1") || WEATHER.equals("4") )
        {
            //비오는 날
            input[0][9]  = 0;
            input[0][10] = 0;
            input[0][11] = 1;
            input[0][12] = 0;
        }
        else if(WEATHER.equals("3") || WEATHER.equals("7"))
        {
            //눈오는 날
            input[0][9]  = 0;
            input[0][10] = 0;
            input[0][11] = 0;
            input[0][12] = 1;
        }
        else if (WEATHER.equals("2") || WEATHER.equals("6"))
        {
            //눈/비 오는 날
            input[0][9]  = 0;
            input[0][10] = 0;
            input[0][11] = 1;
            input[0][12] = 1;
        }
        else if(WEATHER.equals("5"))
        {
            //구름 낀 날
            input[0][9]  = 0;
            input[0][10] = 1;
            input[0][11] = 0;
            input[0][12] = 0;
        }
        else
        {
            //맑은 날
            input[0][9]  = 1;
            input[0][10] = 0;
            input[0][11] = 0;
            input[0][12] = 0;
        }
        // 날씨 설정 ( 맑음, 구름, 비 , 눈 - 4가지) 끝
        Log.i(TAG,"계절(월): "+BaseMonth);
        if(BaseMonth.equals("03") || BaseMonth.equals("04") || BaseMonth.equals("05"))
        {
            //봄
            input[0][13] = 1;
            input[0][14] = 0;
            input[0][15] = 0;
            input[0][16] = 0;
        }
        else if(BaseMonth.equals("06") || BaseMonth.equals("07") || BaseMonth.equals("08"))
        {
            //여름
            input[0][13] = 0;
            input[0][14] = 1;
            input[0][15] = 0;
            input[0][16] = 0;
        }
        else if(BaseMonth.equals("09") || BaseMonth.equals("10") || BaseMonth.equals("11"))
        {
            //가을
            input[0][13] = 0;
            input[0][14] = 0;
            input[0][15] = 1;
            input[0][16] = 0;
        }
        else
        {
            //겨울
            input[0][13] = 0;
            input[0][14] = 0;
            input[0][15] = 0;
            input[0][16] = 1;
        }
        //계절 설정 (봄, 여름, 가을, 겨울 - 4가지) 끝
        Log.i(TAG,"기온: "+TEMPERATURE);
        Double D_temp=Double.parseDouble(TEMPERATURE);
        if(D_temp <= 5.0)
        {
            input[0][17] = 1;
            input[0][18] = 0;
            input[0][19] = 0;
        }
        else if(D_temp >= 20)
        {
            input[0][17] = 0;
            input[0][18] = 0;
            input[0][19] = 1;
        }
        else {
            input[0][17] = 0;
            input[0][18] = 1;
            input[0][19] = 0;
        }

        //기온 3가지 (5도 이하, 5~20도, 20도 이상
        Log.i(TAG,"시간: "+BaseTime);
        int i_time = Integer.parseInt(BaseTime);

        if(i_time >= 500 && i_time < 1100)
        {
            //아침
            input[0][20] = 1;
            input[0][21] = 0;
            input[0][22] = 0;
            input[0][23] = 0;
        }
        else if(i_time >= 1100 && i_time <1700)
        {
            //점심
            input[0][20] = 0;
            input[0][21] = 1;
            input[0][22] = 0;
            input[0][23] = 0;
        }
        else if(i_time >= 1700 && i_time <2300)
        {
            //저녁
            input[0][20] = 0;
            input[0][21] = 0;
            input[0][22] = 1;
            input[0][23] = 0;
        }
        else
        {
            //야식
            input[0][20] = 0;
            input[0][21] = 0;
            input[0][22] = 0;
            input[0][23] = 1;
        }
        //시간대 4가지 ( 아침, 점심, 저녁 , 야식 )
        Log.i(TAG,"나이: "+AGE);

        int i_age = Integer.parseInt(AGE);

        if(i_age <40)
        {
            input[0][24] = 1;
            input[0][25] = 0;
        }
        else
        {
            input[0][24] = 0;
            input[0][25] = 1;
        }
        //연령 2가지 ( 40대 기준) 끝
        long current = System.currentTimeMillis();
        Date curDate = new Date(current);
        SimpleDateFormat baseCurDate = new SimpleDateFormat("HH");
        String getBaseCurTime = baseCurDate.format(curDate);

        String[] sEmotion = new String[2];
        sEmotion = emEx.getTopEmotion();
        Log.i(TAG, "문장 :"+ sEmotion[0]+"/"+sEmotion[1]);
        String korGender = " ";
        if(GENDER.equals("Man"))
        {
            korGender = "남성";
        }
        else
        {
            korGender = "여성";
        }

        String lstStatement = getBaseCurTime+"시 "+
                "\""+sEmotion[0]+"\""+" "+
                AGE+"세 "+
                "\""+sEmotion[1]+"\""+" "+
                korGender+"에게 추천하는 음식";

        emotion_expression.setText(lstStatement);
//        emotion_expression.setText("분노:"+Float.toString(input[0][0])+','
//                +"혐오:"+Float.toString(input[0][1])+','
//                +"공포:"+Float.toString(input[0][2])+','
//                +"행복:"+Float.toString(input[0][3])+','
//                +"보통:"+Float.toString(input[0][4])+','
//                +"슬픔:"+Float.toString(input[0][5])+','
//                +"놀람:"+Float.toString(input[0][6]));


        float[][] output = new float[1][160];

        int maxLen = output[0].length;

        float[][] output_idx = new float[2][maxLen];

        /* ModelClient.java */
        Interpreter tflite = getTfliteInterpreter("food_model.tflite");
        tflite.run(input,output);

        Log.i(TAG, "Data :" + Arrays.toString(output[0]));
        // Max 인덱스 가져오기

        Log.i(TAG, "길이 :" + Integer.toString(output[0].length));
        int max_idx = 0;
        float [] maxFood = new float[15];

        // 인덱스용 배열 초기화
        for(int i=0;i<maxLen;i++){
            output_idx[0][i] = output[0][i];
            output_idx[1][i] = i+1;
        }

        //output 결과 내림차순 정렬
        for(int i=0; i<maxLen; i++) {
            for(int j=i+1; j<maxLen; j++) {
                if(output[0][i] < output[0][j]) { //내림차순
                    float tmp = output[0][i];
                    output[0][i] = output[0][j];
                    output[0][j] = tmp;
                }
            }
        }

        //max15개 값 저장
        for(int i=0; i<maxFood.length; i++){
            maxFood[i] = output[0][i];
            Log.i(TAG, "최대값 : "+ Float.toString(maxFood[i]));
        }

        // 최대 (15개) 중 랜덤 값 만들기
        float [] maxHalfFood = new float[5];
        int [] randomNumber = new int[5];
        Random r = new Random();

        for(int i=0;i<5;i++)
        {
            randomNumber[i] = r.nextInt(maxFood.length);
            for(int j=0;j<i;j++)
            {
                if(randomNumber[i]==randomNumber[j]) {  i--; }
            }
        }

        Log.i(TAG,"랜덤 숫자 : ");

        for(int k=0; k<randomNumber.length; k++)
        {
            Log.i(TAG,Integer.toString(randomNumber[k])+" ");
        }

        for(int i=0;i<maxHalfFood.length;i++)
        {
            maxHalfFood[i] = maxFood[randomNumber[i]];
            Log.i(TAG, "랜덤 음식 확률 :"+ maxHalfFood[i]);
        }

        //food DB 가져오기
        this.initLoadDB();

        for(int i=0;i<maxHalfFood.length;i++) {
            for(int j=0;j<maxLen;j++)
            {

                if(maxHalfFood[i] == output_idx[0][j])
                {
                    Log.i(TAG, i+"차 성공");
                    Log.i(TAG, "인덱스 :"+j);
//                    list.add(foodList.get((int)output_idx[1][j]-1).getFoodName());
                    list.add(Integer.toString(foodList.get((int)output_idx[1][j]-1).getIdx()));
                    Log.i(TAG,"추천음식:"+list.get(i).toString());

                    break;
                }
            }
        }
        Log.i(TAG,"zzzzz" + Integer.toString(foodList.get(0).getIdx()));
        Log.i(TAG,"zzzzz" + foodList.get(0).getFoodName());
        Log.i(TAG, list.get(0).toString());
        ListAdapter  adapter = new ListAdapter(this, list, latitude, longitude );
        //리스트뷰의 어댑터를 지정해준다.
        listview.setAdapter(adapter);
        adapter.setMode(Attributes.Mode.Single);
        //리스트뷰의 아이템을 클릭시 해당 아이템의 문자열을 가져오기 위한 처리
        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> adapterView,
                                    View view, int position, long id) {

                //클릭한 아이템의 문자열을 가져옴
                //String selected_item = (String)adapterView.getItemAtPosition(position);

                url = "https://www.google.co.kr/maps/search/" + (String)adapterView.getItemAtPosition(position)
                        + "/@" + latitude + "," + longitude + ",16z?hl=ko";
                Log.i(TAG, "URL : "+url);

//                Intent intent = new Intent(getApplicationContext(), ViewActivity.class);
//                intent.putExtra("url", url);
//                startActivity(intent);

            }
        });
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

    /*날씨 정보 가져오기*/
    public void getWeather(int latitude, int longitude) throws AuthFailureError {
        // Instantiate the RequestQueue.
        RequestQueue queue = Volley.newRequestQueue(this);
        // 실시간 날씨 정보 획득 실패
//        String url ="http://apis.data.go.kr/1360000/VilageFcstInfoService/getVilageFcst?";
        String url ="http://apis.data.go.kr/1360000/VilageFcstInfoService/getUltraSrtNcst?";
//        String url ="http://apis.data.go.kr/1360000/VilageFcstInfoService/getFcstVersion?";

        String service_key = "Dkxm1ddffHy7wnm0vib%2Bu0%2FqjCnyklnU161UZFWqSumxJi6rd3m%2FPNyEfAg4IMOEu9jgbD7RwbiYJEmJkDKaIA%3D%3D";

        long now = System.currentTimeMillis()-3600000;
        Date mdate = new Date(now);
        // 3600000 = 1시간
        SimpleDateFormat baseDate = new SimpleDateFormat("yyyyMMdd");
        String getBaseDate = baseDate.format(mdate);
        String base_date = getBaseDate;

        SimpleDateFormat baseMonth = new SimpleDateFormat("MM");
        String getBaseMonth = baseMonth.format(mdate);
        Log.i(TAG,"월"+getBaseMonth);
        String base_month = getBaseMonth;
        Log.i(TAG,"월"+base_month);

        baseDate = new SimpleDateFormat("HHmm");
        Log.i(TAG, "시간 : "+mdate);
        String getBaseTime = baseDate.format(mdate);
        Log.i(TAG, "Base Time : "+getBaseTime);
        String base_time = getBaseTime;
        Log.i(TAG,"Base_Time : "+base_time);

        String nx = Integer.toString(latitude);
        String ny = Integer.toString(longitude);

        Log.i(TAG,"LATITUDE : "+nx);
        Log.i(TAG, "LONGITUDE : "+ny);

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
                        Log.i(TAG, "응답 : "+response);
                        JsonParser parser = new JsonParser();
                        JsonObject object = (JsonObject) parser.parse(response.toString());
                        JsonObject parse_response = (JsonObject) object.getAsJsonObject("response");

                        String res = parse_response.getAsJsonObject("header").get("resultMsg").toString();

                        Log.i(TAG,res);
                        if(res.equals("\"APPLICATION_ERROR\""))
                        {
                            Log.i(TAG, "응답 없음");
                        }
                        else {

                            String body = parse_response.getAsJsonObject("body").toString();

                            object = (JsonObject) parser.parse(body);

                            parse_response = (JsonObject) object.getAsJsonObject("items");

                            JsonArray jArray = (JsonArray) parse_response.getAsJsonArray("item");

                            String f_precipitation, f_temper;

                            f_precipitation = "";
                            f_temper = "";

                            for (int i = 0; i < jArray.size(); i++) {
                                JsonObject ob = jArray.get(i).getAsJsonObject();
                                // PTY = 강수형태 : 비(1), 비/눈(2), 눈(3), 소나기(4), 빗방울(5), 빗방울/눈날림(6), 눈날림(7)
                                //
                                if (jArray.get(i).getAsJsonObject().get("category").toString().equals("\"PTY\"")) {
                                    f_precipitation = jArray.get(i).getAsJsonObject().get("obsrValue").toString();
                                    f_precipitation = f_precipitation.replace("\"", "");
                                }

                                if (jArray.get(i).getAsJsonObject().get("category").toString().equals("\"T1H\"")) {
                                    f_temper = jArray.get(i).getAsJsonObject().get("obsrValue").toString();
                                    f_temper = f_temper.replace("\"", "");
                                }

//                                    Log.i("ASJ", jArray.get(i).getAsJsonObject().get("category").toString());
                            }

                            WEATHER = f_precipitation;
                            TEMPERATURE = f_temper;
                            BaseDate = base_date;
                            BaseMonth = base_month;
                            BaseTime = base_time;

                            Log.i(TAG, "강수타입 :" + WEATHER);
                            Log.i(TAG, "기온 : " + TEMPERATURE);
                            Log.i(TAG, "날짜 : " + BaseDate);
                            Log.i(TAG, "달(월) : " + BaseMonth);
                            Log.i(TAG, "시간 : " + BaseTime);
                        }
                    }
                },new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.i("실패","실패");
            }
        });

//        // queue에 Request를 추가해준다.
        queue.add(jsonObjectRequest);

        System.out.println("응답"+jsonObjectRequest.getBody());
    }

    private LatXLngY convertGRID_GPS(int mode, double lat_X, double lng_Y )
    {
        double RE = 6371.00877; // 지구 반경(km)
        double GRID = 5.0; // 격자 간격(km)
        double SLAT1 = 30.0; // 투영 위도1(degree)
        double SLAT2 = 60.0; // 투영 위도2(degree)
        double OLON = 126.0; // 기준점 경도(degree)
        double OLAT = 38.0; // 기준점 위도(degree)
        double XO = 43; // 기준점 X좌표(GRID)
        double YO = 136; // 기1준점 Y좌표(GRID)

        //
        // LCC DFS 좌표변환 ( code : "TO_GRID"(위경도->좌표, lat_X:위도,  lng_Y:경도), "TO_GPS"(좌표->위경도,  lat_X:x, lng_Y:y) )
        //

        double DEGRAD = Math.PI / 180.0;
        double RADDEG = 180.0 / Math.PI;

        double re = RE / GRID;
        double slat1 = SLAT1 * DEGRAD;
        double slat2 = SLAT2 * DEGRAD;
        double olon = OLON * DEGRAD;
        double olat = OLAT * DEGRAD;

        double sn = Math.tan(Math.PI * 0.25 + slat2 * 0.5) / Math.tan(Math.PI * 0.25 + slat1 * 0.5);
        sn = Math.log(Math.cos(slat1) / Math.cos(slat2)) / Math.log(sn);
        double sf = Math.tan(Math.PI * 0.25 + slat1 * 0.5);
        sf = Math.pow(sf, sn) * Math.cos(slat1) / sn;
        double ro = Math.tan(Math.PI * 0.25 + olat * 0.5);
        ro = re * sf / Math.pow(ro, sn);
        LatXLngY rs = new LatXLngY();

        if (mode == TO_GRID) {
            rs.lat = lat_X;
            rs.lng = lng_Y;
            double ra = Math.tan(Math.PI * 0.25 + (lat_X) * DEGRAD * 0.5);
            ra = re * sf / Math.pow(ra, sn);
            double theta = lng_Y * DEGRAD - olon;
            if (theta > Math.PI) theta -= 2.0 * Math.PI;
            if (theta < -Math.PI) theta += 2.0 * Math.PI;
            theta *= sn;
            rs.x = Math.floor(ra * Math.sin(theta) + XO + 0.5);
            rs.y = Math.floor(ro - ra * Math.cos(theta) + YO + 0.5);
        }
        else {
            rs.x = lat_X;
            rs.y = lng_Y;
            double xn = lat_X - XO;
            double yn = ro - lng_Y + YO;
            double ra = Math.sqrt(xn * xn + yn * yn);
            if (sn < 0.0) {
                ra = -ra;
            }
            double alat = Math.pow((re * sf / ra), (1.0 / sn));
            alat = 2.0 * Math.atan(alat) - Math.PI * 0.5;

            double theta = 0.0;
            if (Math.abs(xn) <= 0.0) {
                theta = 0.0;
            }
            else {
                if (Math.abs(yn) <= 0.0) {
                    theta = Math.PI * 0.5;
                    if (xn < 0.0) {
                        theta = -theta;
                    }
                }
                else theta = Math.atan2(xn, yn);
            }
            double alon = theta / sn + olon;
            rs.lat = alat * RADDEG;
            rs.lng = alon * RADDEG;
        }
        return rs;
    }

    class LatXLngY
    {
        public double lat;
        public double lng;

        public double x;
        public double y;
    }

    public static Bitmap rotateImage(Bitmap source, float angle) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(),
                matrix, true);
    }

    public static String BitmapToString(Bitmap bitmap) {

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 70, baos);

        byte[] bytes = baos.toByteArray();
        //String temp = Base64.encodeToString(bytes, Base64.DEFAULT);
        String temp = Base64.encodeToString(bytes, Base64.URL_SAFE);
        return temp;
    }

    class InsertCapture extends AsyncTask<String, Void, String> {

        ProgressDialog progressDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            progressDialog = ProgressDialog.show(ListActivity.this,
                    "좋아요 중 입니다.", null, true, true);
        }


        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            progressDialog.dismiss();
            Log.d(TAG, "POST response  - " + result);
        }


        // AsyncTask 첫번째 인자 값 (전달값)
        @Override
        protected String doInBackground(String... params) {

            // 서버 URL
            String serverURL = (String)params[0];

            // 이미지 String
            String strBitmap = (String)params[1];


            // 이름=값&이름=값...
            String postParameters = "strBitmap=" + strBitmap;

            try {
                URL url = new URL(serverURL);
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();

                // 5초안에 응답안올경우 예외발생
                httpURLConnection.setReadTimeout(5000);
                // 5초안에 연결안될경우 예외발생
                httpURLConnection.setConnectTimeout(5000);
                // POST
                httpURLConnection.setRequestMethod("POST");
                httpURLConnection.connect();


                OutputStream outputStream = httpURLConnection.getOutputStream();
                // 전송할 데이터 및 인코딩
                outputStream.write(postParameters.getBytes("UTF-8"));
                outputStream.flush();
                outputStream.close();

                // 응답
                int responseStatusCode = httpURLConnection.getResponseCode();
                Log.d(TAG, "POST response code - " + responseStatusCode);

                InputStream inputStream;
                if(responseStatusCode == HttpURLConnection.HTTP_OK) {
                    // 정상 응답
                    inputStream = httpURLConnection.getInputStream();
                }
                else{
                    // 오류 응답
                    inputStream = httpURLConnection.getErrorStream();
                }


                InputStreamReader inputStreamReader = new InputStreamReader(inputStream, "UTF-8");
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

                StringBuilder sb = new StringBuilder();
                String line = null;

                while((line = bufferedReader.readLine()) != null){
                    sb.append(line);
                }


                bufferedReader.close();


                return sb.toString();


            } catch (Exception e) {

                Log.d(TAG, "InsertData: Error ", e);

                return new String("Error: " + e.getMessage());
            }

        }
    }

//    public void kakaolink() {
//        Log.i(TAG, "kakaolink: ");
//        FeedTemplate params = FeedTemplate
//                .newBuilder(ContentObject.newBuilder("추천메뉴",
//                        "http://3.34.198.134:8080/tmp.png",
//                        LinkObject.newBuilder().setWebUrl("https://developers.kakao.com")
//                                .setMobileWebUrl("https://developers.kakao.com").build())
////                        .setDescrption("햄버거")
//                        .build())
//                .addButton(new ButtonObject("앱에서 보기", LinkObject.newBuilder()
//                        .setWebUrl("'https://developers.kakao.com")
//                        .setMobileWebUrl("'https://developers.kakao.com")
//                        .setAndroidExecutionParams("key1=value1")
//                        .setIosExecutionParams("key1=value1")
//                        .build()))
//                .build();
//
//        Map<String, String> serverCallbackArgs = new HashMap<String, String>();
//        serverCallbackArgs.put("user_id", "${current_user_id}");
//        serverCallbackArgs.put("product_id", "${shared_product_id}");
//
//        KakaoLinkService.getInstance().sendDefault(this, params, serverCallbackArgs, new ResponseCallback<KakaoLinkResponse>() {
//            @Override
//            public void onFailure(ErrorResult errorResult) {
//                Logger.e(errorResult.toString());
//            }
//
//            @Override
//            public void onSuccess(KakaoLinkResponse result) {
//                // 템플릿 밸리데이션과 쿼터 체크가 성공적으로 끝남. 톡에서 정상적으로 보내졌는지 보장은 할 수 없다. 전송 성공 유무는 서버콜백 기능을 이용하여야 한다.
//            }
//        });
//    }
}