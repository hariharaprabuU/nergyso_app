package com.example.user.energysoft;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Timer;
import java.util.TimerTask;

import static android.content.Intent.getIntent;
import static com.google.android.gms.internal.zzahf.runOnUiThread;

/**
 * Created by ets-prabhu on 8/1/18.
 */

public class ImageFragment extends Fragment implements Download_data.download_complete{

    ImageFragment.MyCustomPagerAdapter myCustomPagerAdapter;
    ViewPager viewPager;
    String SERVER_URL;
    String IMAGE_URL;
    View view;
    String[] images = new String[20];
    int currentPage = 0;
    Timer timer;
    final long DELAY_MS = 500;//delay in milliseconds before task is to be executed
    final long PERIOD_MS = 3000; // time in milliseconds between successive task executions.
    public static int NUM_PAGES = 0;



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        SERVER_URL = getString(R.string.service_url);
        int id = getActivity().getIntent().getIntExtra("id", 0);
        String check = getActivity().getIntent().getStringExtra("check");
        if(check.equals("NEWS")){
            IMAGE_URL = SERVER_URL + "api/news" + "/" + id;
        }else if(check.equals("EVENTS")){
            IMAGE_URL = SERVER_URL + "api/events" + "/" + id;
        }

        view = inflater.inflate(R.layout.activity_full_event, container, false);


        viewPager = (ViewPager) view.findViewById(R.id.viewPagerdash);

        myCustomPagerAdapter = new ImageFragment.MyCustomPagerAdapter();
        viewPager.setAdapter(myCustomPagerAdapter);

        /*After setting the adapter use the timer */
        final Handler handler = new Handler();
        final Runnable Update = new Runnable() {

            public void run() {
                if (currentPage == NUM_PAGES) {
                    currentPage = 0;
                }
                viewPager.setCurrentItem(currentPage++, true);
            }
        };

        timer = new Timer(); // This will create a new Thread
        timer .schedule(new TimerTask() { // task to be scheduled

            @Override
            public void run() {
                handler.post(Update);
            }
        }, DELAY_MS, PERIOD_MS);

        Download_data download_data = new Download_data((Download_data.download_complete) this);
        download_data.download_data_from_link(IMAGE_URL);

        return null;
    }

    public void get_data(String data)
    {
        try {
            JSONObject data_array = new JSONObject(data);
            System.out.println("Object"+data_array);
//            NUM_PAGES = data_array.length();
            NUM_PAGES = 1;
            images[0] = SERVER_URL + data_array.getString("events_image");
            images[1] = SERVER_URL + data_array.getString("events_image");
            System.out.println("Coming from data "+ images[0]);
//            for (int i = 0 ; i < 1 ; i++)
//            {
//                images[i] = SERVER_URL+obj.getString("events_image");
//            }
        } catch (JSONException e) {
            createAndShowDialog(e,"No connection");
            e.printStackTrace();
        }

    }

    private void createAndShowDialog(final String message, final String title) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        builder.setMessage(message);
        builder.setTitle(title);
        builder.create().show();
    }

    private void createAndShowDialog(Exception exception, String title) {
        Throwable ex = exception;
        if(exception.getCause() != null){
            ex = exception.getCause();
        }
        createAndShowDialog(ex.getMessage(), title);
    }

    public class MyCustomPagerAdapter extends FragmentPagerAdapter {
        Context context;
        String images[];
        LayoutInflater layoutInflater;

        public MyCustomPagerAdapter(FragmentManager fm) {
            super(fm);
        }


//        public MyCustomPagerAdapter(Context context, String images[]) {
//            this.context = context;
//            this.images = images;
//            layoutInflater = LayoutInflater.from(context);
//        }

        @Override
        public int getCount() {
            return images.length;
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == ((LinearLayout) object);
        }

        @Override
        public android.support.v4.app.Fragment getItem(int position) {

            return null;
        }

        @Override
        public Object instantiateItem(ViewGroup container, final int position) {
            View itemView = layoutInflater.inflate(R.layout.activity_banner_item, container, false);

            ImageView imageView = (ImageView) itemView.findViewById(R.id.imageViewdash);
//        imageView.setImageResource(images[position]);
            System.out.println(" in");
            loadImageFromUrl(imageView,images[position]);
            container.addView(itemView);

            //listening to image click
            imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
//                    Toast.makeText(context, "you clicked image " + (position + 1), Toast.LENGTH_LONG).show();
                }
            });

            return itemView;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((LinearLayout) object);
        }
    }

    private void loadImageFromUrl(ImageView myImage,String employee_photo) {
        System.out.println("In LoadImageFromURL");
        Picasso.with(getActivity()).load(employee_photo).placeholder(R.mipmap.ic_launcher).error(R.mipmap.ic_launcher)
                .into(myImage, new com.squareup.picasso.Callback(){

                    @Override
                    public void onSuccess() {

                    }

                    @Override
                    public void onError() {

                    }
                });
    }
}
