package com.example.user.energysoft;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import me.relex.circleindicator.CircleIndicator;

public class FullNews extends AppCompatActivity implements Download_data.download_complete{
    String FULL_NEWS_URL = "http://10.0.0.15:8000/api/news/";
    Toolbar toolbar;
    private static ViewPager mPager;
    private static ScrollView mScrollView;
    TextView full_news_title, full_news_description, full_text_news_description;
    ImageView news_photo ;
    private static int currentPage = 0;
    private static final Integer[] XMEN= {};
    private ArrayList<Integer> XMENArray = new ArrayList<Integer>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_full_news);
        toolbar = (Toolbar)findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitleTextColor(0xFFFFFFFF);
        init();
        full_news_title = (TextView) findViewById(R.id.full_news_title);
        full_news_description = (TextView) findViewById(R.id.full_news_description);
        full_text_news_description = (TextView) findViewById(R.id.full_text_news_description);
        news_photo = (ImageView) findViewById(R.id.news_photo);
        int id = getIntent().getIntExtra("id",0);
        FULL_NEWS_URL = FULL_NEWS_URL+id+"/";
        Download_data download_data = new Download_data((Download_data.download_complete) this);
        download_data.download_data_from_link(FULL_NEWS_URL);
    }
    private void init() {
        for(int i=0;i<XMEN.length;i++){
            XMENArray.add(XMEN[i]);
        }
//        NestedScrollView scrollView = (NestedScrollView) findViewById (R.id.nest_scrollview);
//        scrollView.setFillViewport (true);
        mPager = (ViewPager) findViewById(R.id.pager);
        mPager.setAdapter(new MyAdapter(FullNews.this,XMENArray));
        mScrollView = (ScrollView) findViewById(R.id.news_scroll);
        mScrollView.setFillViewport(true);
        mPager.setOnTouchListener(new View.OnTouchListener() {
            int dragthreshold = 30;
            int downX;
            int downY;

            @Override
            public boolean onTouch(View v, MotionEvent event) {

                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        downX = (int) event.getRawX();
                        downY = (int) event.getRawY();
                        break;
                    case MotionEvent.ACTION_MOVE:
                        int distanceX = Math.abs((int) event.getRawX() - downX);
                        int distanceY = Math.abs((int) event.getRawY() - downY);

                        if (distanceY > distanceX && distanceY > dragthreshold) {
                            mPager.getParent().requestDisallowInterceptTouchEvent(false);
                            mScrollView.getParent().requestDisallowInterceptTouchEvent(true);
                        } else if (distanceX > distanceY && distanceX > dragthreshold) {
                            mPager.getParent().requestDisallowInterceptTouchEvent(true);
                            mScrollView.getParent().requestDisallowInterceptTouchEvent(false);
                        }
                        break;
                    case MotionEvent.ACTION_UP:
                        mScrollView.getParent().requestDisallowInterceptTouchEvent(false);
                        mPager.getParent().requestDisallowInterceptTouchEvent(false);
                        break;
                }
                return false;
            }
        });

        CircleIndicator indicator = (CircleIndicator) findViewById(R.id.indicator);
        indicator.setViewPager(mPager);

        // Auto start of viewpager
        final Handler handler = new Handler();
        final Runnable Update = new Runnable() {
            public void run() {
                if (currentPage == XMEN.length) {
                    currentPage = 0;
                }
                mPager.setCurrentItem(currentPage++, true);
            }
        };
        Timer swipeTimer = new Timer();
        swipeTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                handler.post(Update);
            }
        }, 2500, 2500);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu_main,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int res_id = item.getItemId();
        if(res_id == R.id.action_home)
        {
            Toast.makeText(getApplicationContext(),"You selet Home",Toast.LENGTH_SHORT).show();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void get_data(String data) {
        try {
            final JSONObject object = (JSONObject) new JSONTokener(data).nextValue();
            System.out.println("Object"+object);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    try {
                        full_news_title.setText(object.getString("news_title"));
                        full_news_description.setText(object.getString("news_description"));
                        full_text_news_description.setText(object.getString("news_description"));
                        String photo_link = object.getString("news_image");
                        String [] photo = photo_link.split("/");
                        for(int i = 0; i< photo.length ; i++){
                            System.out.println(photo[i]);
                        }
                        photo_link = "http://10.0.0.15:8000/media/images/"+photo[photo.length-1];
                        System.out.println(photo_link);
                        loadImageFromUrl(photo_link);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });


        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void loadImageFromUrl(String employee_photo) {
        Picasso.with(this).load(employee_photo).placeholder(R.mipmap.ic_launcher).error(R.mipmap.ic_launcher)
                .into(news_photo, new com.squareup.picasso.Callback(){

                    @Override
                    public void onSuccess() {

                    }

                    @Override
                    public void onError() {

                    }
                });
    }
}