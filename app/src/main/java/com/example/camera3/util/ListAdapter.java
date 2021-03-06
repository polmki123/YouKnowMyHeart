package com.example.camera3.util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.VectorEnabledTintResources;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.camera3.R;
import com.example.camera3.ViewActivity;
import com.example.camera3.db.DataAdapter;
import com.example.camera3.db.FoodData;

import java.io.BufferedInputStream;
import java.util.List;

import com.daimajia.swipe.SwipeLayout;
import com.daimajia.swipe.adapters.BaseSwipeAdapter;
import com.example.camera3.R;

public class ListAdapter extends BaseSwipeAdapter {

    private Context mContext;
    public String url;
    public List  list ;
    public List<FoodData> foodList ;
    public Double latitude, longitude;

    public ListAdapter(@NonNull Context context, List  items, double latitude, double longitude) {
        this.mContext = context;
        this.list    = items;
        this.latitude = latitude;
        this.longitude = longitude;

        Log.i(TAG, "ListAdapter: " + latitude);
        Log.i(TAG, "ListAdapter: " + longitude);
        Log.i(TAG, "ListAdapter: this " + this.list.size());
        this.initLoadDB();
    }

    final private static String TAG = "FOOD";

    @Override
    public int getSwipeLayoutResourceId(int position) {
        return R.id.list_view_item;
    }

    @Override
    public View generateView(int position, ViewGroup parent) {
        View v = LayoutInflater.from(mContext).inflate(R.layout.list_view_item, null);
        SwipeLayout swipeLayout = (SwipeLayout)v.findViewById(getSwipeLayoutResourceId(position));
        swipeLayout.setShowMode(SwipeLayout.ShowMode.PullOut);
//        View starBottView = swipeLayout.findViewById(R.id.starbott);

        Log.i(TAG, "ListAdapter: position " + position);

//        swipeLayout.addDrag(SwipeLayout.DragEdge.Left, swipeLayout.findViewById(R.id.bottom_wrapper));
//        swipeLayout.addDrag(SwipeLayout.DragEdge.Right, swipeLayout.findViewById(R.id.bottom_wrapper_2));
//        swipeLayout.addDrag(SwipeLayout.DragEdge.Top, starBottView);
//        swipeLayout.addDrag(SwipeLayout.DragEdge.Bottom, starBottView);

//        String item = getItem(position);
//        ImageView image_Food = (ImageView) v.findViewById(R.id.image_Food);
        Button btn_good = (Button) v.findViewById(R.id.btn_Good);
        Button btn_Search = (Button) v.findViewById(R.id.btn_Search);

        swipeLayout.setOnClickListener( new View.OnClickListener()
            {
                @Override
                public void onClick(View view) {
                    Log.i("FOOD", "onClick: " + position);
                    if(swipeLayout.getOpenStatus().equals(SwipeLayout.Status.Close))
                    {
                        swipeLayout.open(SwipeLayout.DragEdge.Right);
                    }
                    else {
                        swipeLayout.close();
                    }
                }
            }
        );
        //버튼처리
        btn_good.setOnClickListener(new Button.OnClickListener() {
            @SuppressLint("UseCompatLoadingForDrawables")
            public void onClick(View v) {
                Log.i("URL", "onClick: btn_Good");

                if(btn_good.getBackground().getConstantState().equals( mContext.getResources().getDrawable(R.drawable.good_1).getConstantState()))
                    btn_good.setBackground(ContextCompat.getDrawable(mContext, R.drawable.good_2));
                else
                    btn_good.setBackground(ContextCompat.getDrawable(mContext, R.drawable.good_1));
            }
        });

//        btn_Search.setOnClickListener(new Button.OnClickListener() {
//            public void onClick(View v) {
//
//                Log.i(TAG, (String) txt_Text.getText());
//
//                Log.i("URL", "onClick: btn_Search");
//                url = "https://www.google.co.kr/maps/search/" + (String)txt_Text.getText()
//                        + "/@" + latitude + "," + longitude + ",16z?hl=ko";
//
//                Intent intent = new Intent(mContext.getApplicationContext(), ViewActivity.class);
//                intent.putExtra("url", url);
//                mContext.startActivity(intent);
//            }
//        });

        return v;
    }


    @Override
    public void fillValues(int position, View convertView) {
        String item = this.list.get(position).toString();

        TextView txt_Rank = (TextView) convertView.findViewById(R.id.txt_Rank);
        TextView txt_Text = (TextView) convertView.findViewById(R.id.txt_Text);
        ImageView foodimage = (ImageView) convertView.findViewById(R.id.foodimage);
        Log.i("URL", "onClick: " + position);

        int rank = 0;
        int index = 0;
        String image_url;

        //인덱스처리
        rank = position + 1;
        index = Integer.parseInt(item);
        String sItem = Integer.toString(index+1);

        image_url = "FoodImage/" + sItem + ".jpg";

        Log.i("FOOD", "test" + item);
        Log.i("FOOD", "test" + this.foodList.get(index).getFoodName());

        //커스텀리스트뷰 텍스트, 이미지, 버튼 처리
        txt_Rank.setText(Integer.toString(rank));
        txt_Text.setText(this.foodList.get(index).getFoodName());

        AssetManager am = this.mContext.getAssets();
        BufferedInputStream buf = null;

        try {
            buf = new BufferedInputStream(am.open(image_url));
            Bitmap bitmap = BitmapFactory.decodeStream(buf);
//            txt_Text.setBackground(new BitmapDrawable(mContext.getResources(), bitmap));
            foodimage.setBackground(new BitmapDrawable(this.mContext.getResources(), bitmap));
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void initLoadDB() {
        DataAdapter mDbHelper = new DataAdapter(mContext.getApplicationContext());
        mDbHelper.createDatabase();
        mDbHelper.open();

        // db에 있는 값들을 model을 적용해서 넣는다.
        this.foodList = mDbHelper.getTableData();

        // db 닫기
        mDbHelper.close();
    }
    @Override
    public int getCount() {
        return 5;
    }

    @Override
//    public String getItem(int position) {
//        return  Integer.toString(position);
//    }
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

}