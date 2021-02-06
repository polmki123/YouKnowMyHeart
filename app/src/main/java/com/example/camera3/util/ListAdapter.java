package com.example.camera3.util;

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

    public ListAdapter(@NonNull Context context, List  list ) {
        this.mContext = context;
        this.list    = list;
        this.latitude = latitude;
        this.longitude = longitude;
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

        String item = list.get(position).toString();

//        swipeLayout.addDrag(SwipeLayout.DragEdge.Left, swipeLayout.findViewById(R.id.bottom_wrapper));
        swipeLayout.addDrag(SwipeLayout.DragEdge.Right, swipeLayout.findViewById(R.id.bottom_wrapper_2));
//        swipeLayout.addDrag(SwipeLayout.DragEdge.Top, starBottView);
//        swipeLayout.addDrag(SwipeLayout.DragEdge.Bottom, starBottView);

//        String item = getItem(position);

        this.initLoadDB();

        TextView txt_Rank = (TextView) v.findViewById(R.id.txt_Rank);
        TextView txt_Text = (TextView) v.findViewById(R.id.txt_Text);
//        ImageView image_Food = (ImageView) v.findViewById(R.id.image_Food);
        Button btn_good = (Button) v.findViewById(R.id.btn_Good);
        Button btn_Search = (Button) v.findViewById(R.id.btn_Search);

        int rank = 0;
        int index = 0;
        String image_url;

        //인덱스처리
        rank = position + 1;
        index = Integer.parseInt(item);
        String sItem = Integer.toString(index+1);

        image_url = "FoodImage/" + sItem + ".jpg";

        Log.i("FOOD", "test" + item);
        Log.i("FOOD", "test" + foodList.get(index).getFoodName());

        //커스텀리스트뷰 텍스트, 이미지, 버튼 처리
        txt_Rank.setText(Integer.toString(rank));
        txt_Text.setText(foodList.get(index).getFoodName());

        AssetManager am = mContext.getAssets();
        BufferedInputStream buf = null;

        try {
            buf = new BufferedInputStream(am.open(image_url));
            Bitmap bitmap = BitmapFactory.decodeStream(buf);
            txt_Text.setBackground(new BitmapDrawable(mContext.getResources(), bitmap));
        } catch (Exception e) {
            e.printStackTrace();
        }

        //버튼처리
        btn_good.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                Log.i("URL", "onClick: btn_Good");

                if(btn_good.getTextColors() == ColorStateList.valueOf(Color.WHITE))
                    btn_good.setTextColor(Color.RED);
                else
                    btn_good.setTextColor(Color.WHITE);
            }
        });

        btn_Search.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {

                Log.i(TAG, (String) txt_Text.getText());

                Log.i("URL", "onClick: btn_Search");
                url = "https://www.google.co.kr/maps/search/" + (String)txt_Text.getText()
                        + "/@" + latitude + "," + longitude + ",16z?hl=ko";

                Intent intent = new Intent(mContext.getApplicationContext(), ViewActivity.class);
                intent.putExtra("url", url);
                mContext.startActivity(intent);
            }
        });

        return v;
    }


    @Override
    public void fillValues(int position, View convertView) {
        Log.i("URL", "onClick: btn_Good");
    }

    private void initLoadDB() {
        DataAdapter mDbHelper = new DataAdapter(mContext.getApplicationContext());
        mDbHelper.createDatabase();
        mDbHelper.open();

        // db에 있는 값들을 model을 적용해서 넣는다.
        foodList = mDbHelper.getTableData();

        // db 닫기
        mDbHelper.close();
    }
    @Override
    public int getCount() {
        return 5;
    }

    @Override
    public String getItem(int position) {
        return  Integer.toString(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

}