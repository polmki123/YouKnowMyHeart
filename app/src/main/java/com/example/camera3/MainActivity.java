package com.example.camera3;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.ImageDecoder;
import android.graphics.Matrix;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.loader.content.CursorLoader;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity {
    final private static String TAG = "FOOD";
    Button btn_photo, btn_send, btn_gall;
    ImageView iv_photo;
    FrameLayout iv_back;
    String imgPath, mCurrentPhotoPath;
    private static final int MY_PERMISSION_REQUEST_LOCATION = 0;

    double longitude, latitude;

    final static int REQUEST_TAKE_PHOTO = 1;

    LocationListener mLocationListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);

        Intent intent = new Intent(this, MainLoadingActivity.class);
        startActivity(intent);

        iv_photo = findViewById(R.id.iv_photo);
        iv_back = findViewById(R.id.iv_back);

        btn_photo = findViewById(R.id.btn_photo);
        btn_send = findViewById(R.id.btn_send);
        btn_gall = findViewById(R.id.btn_gall);

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
        {
            if(checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
                    && checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)
            {
                Log.i(TAG, "권한 설정 완료");
            }
            else
            {
                Log.i(TAG, "권한 설정 요청");
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
            }
        }

        //GPS 상태가 꺼져있으면 팝업 발생
        LocationManager locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
        if(!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER))
        {
            createGpsDisabledAlert();
        }
        else
        {
            mLocationListener = new LocationListener() {
                public void onLocationChanged(Location location) {
                    longitude = location.getLongitude();    //경도
                    latitude = location.getLatitude();         //위도
                    Log.i(TAG, longitude + " " + latitude);
                    LocationManager mLM = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                    mLM.removeUpdates(mLocationListener);
                }
            };

            permissionCheck();
        }

        class progressThread extends Thread {

            public progressThread()
            {
                // 초기화 작업
            }

            public void run()
            {
                Intent intent = new Intent(MainActivity.this, LoadingActivity.class);
                startActivity(intent);
            }
        }

        class sendSeverThread extends Thread {

            String thpath = " ";

            public sendSeverThread(String path)
            {
                thpath = path;
            }

            public void run()
            {
                FileUploadUtils.send2Server(thpath);
//
//                Handler handler = new Handler();
//
//                handler.post(new Runnable() {
//                    @Override
//                    public void run() {
//                        Intent intent = new Intent(MainActivity.this, LoadingActivity.class);
//                        startActivity(intent);                    }
//                });
                while(true)
                {
                    if(FileUploadUtils.NETOK.equals("200"))
                    {
                        //네트워크 상태값을 마지막에 세팅해야 값 세팅 가능함
                        Log.i(TAG, "NETOK"+FileUploadUtils.NETOK);

                        Intent intent = new Intent(MainActivity.this, ListActivity.class);
                        intent.putExtra("longitude", longitude);
                        intent.putExtra("latitude" , latitude  );

                        intent.putExtra("AGE"      , FileUploadUtils.AGE    );
                        intent.putExtra("0"    , FileUploadUtils.ANGRY  );
                        intent.putExtra("1"  , FileUploadUtils.DISGUST);
                        intent.putExtra("2"     , FileUploadUtils.FEAR   );
                        intent.putExtra("3"    , FileUploadUtils.HAPPY  );
                        intent.putExtra("4"  , FileUploadUtils.NEUTRAL);
                        intent.putExtra("5"      , FileUploadUtils.SAD     );
                        intent.putExtra("6" , FileUploadUtils.SURPRISE);
                        intent.putExtra("GENDER"   , FileUploadUtils.GENDER  );

                        intent.putExtra("PHOTOURL", mCurrentPhotoPath);

                        Log.i(TAG, "보내는 값 : \n"+"NETWORK : "+FileUploadUtils.NETOK+"   "+
                                "\n AGE : "+FileUploadUtils.AGE+"   "+
                                "\n ANGRY : "+ FileUploadUtils.ANGRY+"   "+
                                "\n DISGUST : "+FileUploadUtils.DISGUST+"   "+
                                "\n FEAR : "+FileUploadUtils.FEAR+"   "+
                                "\n HAPPY : "+FileUploadUtils.HAPPY+"   "+
                                "\n NEUTRAL : "+FileUploadUtils.NEUTRAL+"   "+
                                "\n SAD : "+FileUploadUtils.SAD+"   "+
                                "\n SURPRISE : "+FileUploadUtils.SURPRISE+"   "+
                                "\n GENDER : "+FileUploadUtils.GENDER+"   "+
                                "\n latitude : "+latitude+"   "+
                                "longitude :"+longitude+"    "+
                                "photo URL : ");

                        startActivity(intent);

                        Log.i("FOOD", "oncllick send end");
                        break;
                    }
                    if(FileUploadUtils.NETOK.equals("0"))
                    {
                        Log.i(TAG, "NETOK"+FileUploadUtils.NETOK);

//                        Toast.makeText(MainActivity.this, "얼굴을 인식할 수 없습니다.", Toast.LENGTH_LONG).show();
                        break;
                    }
                }
            }
        }

        btn_send.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v)
            {
                Log.i(TAG, "oncllick send");
                /* 추가 */
                if(mCurrentPhotoPath == null){
                    Toast.makeText(MainActivity.this, "분석할 수 있는 이미지가 없습니다.", Toast.LENGTH_LONG).show();
                    return;
                }
                Log.i(TAG, "파일 경로2"+mCurrentPhotoPath);

                progressThread pth   = new progressThread();
                sendSeverThread ssth = new sendSeverThread(mCurrentPhotoPath);

                pth.start();
                ssth.start();
            }
        });

        btn_photo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                switch (v.getId())
                {
                    case R.id.btn_photo:
                        dispatchTakePictureIntent();
                        break;
                }
            }
        });

        btn_gall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                switch (v.getId())
                {
                    case R.id.btn_gall:
                        getGall();
                        break;
                }
            }
        });
    }

    private void permissionCheck() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            //Manifest.permission.ACCESS_FINE_LOCATION 접근 승낙 상태 일때
            LocationManager mLM = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            mLM.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                    1000, 1, mLocationListener);
            Log.i(TAG, "퍼미션O");
        } else {
            //Manifest.permission.ACCESS_FINE_LOCATION 접근 거절 상태 일때
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, MY_PERMISSION_REQUEST_LOCATION);
            Log.i(TAG, "onCreate: 퍼미션x");
        }
    }

    // GPS Disabled Alert
    private void createGpsDisabledAlert() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("GPS가 꺼져있습니다. 켜시겠습니까?")
                .setCancelable(false)
                .setPositiveButton("GPS 켜기",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                showGpsOptions();
                            }
                        })
                .setNegativeButton("취소",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });
        AlertDialog alert = builder.create();
        alert.show();
    }

    // show GPS Options
    private void showGpsOptions() {
        Intent gpsOptionsIntent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
        startActivity(gpsOptionsIntent);
    }

    // 카메라로 촬영한 사진의 썸네일을 가져와 이미지뷰에 띄워줌
    @Override protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        try
        {
            switch (requestCode) {
                case REQUEST_TAKE_PHOTO: {
                    if (resultCode == RESULT_OK) {
                        File file = new File(mCurrentPhotoPath);
                        Bitmap bitmap;

                        if (Build.VERSION.SDK_INT >= 29) {
                            ImageDecoder.Source source = ImageDecoder.createSource(getContentResolver(), Uri.fromFile(file));
                            try { bitmap = ImageDecoder.decodeBitmap(source);
                                bitmap = rotateImage(bitmap, 0);
                                if (bitmap != null) {
                                    //배경제거
                                    iv_back.setBackground(new ColorDrawable(ContextCompat.getColor(getApplicationContext(), R.color.white)));
                                    iv_photo.setImageBitmap(bitmap);
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
                                    //배경제거
                                    iv_back.setBackground(new ColorDrawable(ContextCompat.getColor(getApplicationContext(), R.color.white)));
                                    iv_photo.setImageBitmap(bitmap);
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    else{ }
                    break;
                }

                case 10:
                    if(resultCode==RESULT_OK){
                        //선택한 사진의 경로(Uri)객체 얻어오기
                        Uri uri= intent.getData();

                        Log.i(TAG, "갤러리 경로 : "+ uri);

                        if(uri!=null){
                            //배경제거
                            iv_back.setBackground(new ColorDrawable(ContextCompat.getColor(getApplicationContext(), R.color.white)));
                            iv_photo.setImageURI(uri);

                            //갤러리앱에서 관리하는 DB정보가 있는데, 그것이 나온다 [실제 파일 경로가 아님!!]
                            //얻어온 Uri는 Gallery앱의 DB번호임. (content://-----/2854)
                            //업로드를 하려면 이미지의 절대경로(실제 경로: file:// -------/aaa.png 이런식)가 필요함
                            //Uri -->절대경로(String)로 변환
                            imgPath= getRealPathFromUri(uri);   //임의로 만든 메소드 (절대경로를 가져오는 메소드)
                            Log.i(TAG,"갤러리 이미지 경로 : "+imgPath);
                            //이미지 경로 uri 확인해보기
                            //new AlertDialog.Builder(this).setMessage(uri.toString()+"\n"+imgPath).create().show();
//                            mCurrentPhotoPath = uri.toString()+"\n"+imgPath;
                            mCurrentPhotoPath = imgPath;
                        }

                    }else
                    {
                        Toast.makeText(this, "이미지 선택을 하지 않았습니다.", Toast.LENGTH_SHORT).show();
                    }
                    break;
            }
        } catch (Exception error) {
            error.printStackTrace();
        }
    }

    // 사진 촬영 후 썸네일만 띄워줌. 이미지를 파일로 저장해야 함
    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName, ".jpg", storageDir );
        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }

    // 카메라 인텐트 실행하는 부분
    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if(takePictureIntent.resolveActivity(getPackageManager()) != null) {
            File photoFile = null;

            try {
                photoFile = createImageFile();
            }
            catch (IOException ex) { }
            if(photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this, "com.example.camera3.fileprovider", photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);

                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
            }
        }
    }

    private void getGall() {

        //갤러리 or 사진 앱 실행하여 사진을 선택하도록..
        Intent intent= new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent,10);
    }

    //Uri -- > 절대경로로 바꿔서 리턴시켜주는 메소드
    String getRealPathFromUri(Uri uri){
        String[] proj= {MediaStore.Images.Media.DATA};
        Log.i(TAG,"갤러리경로 :"+proj[0]);
        CursorLoader loader= new CursorLoader(this, uri, proj, null, null, null);
        Cursor cursor= loader.loadInBackground();
        int column_index= cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        String result= cursor.getString(column_index);
        cursor.close();
        return  result;
    }

    public static Bitmap rotateImage(Bitmap source, float angle) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(),
                matrix, true);
    }
}
