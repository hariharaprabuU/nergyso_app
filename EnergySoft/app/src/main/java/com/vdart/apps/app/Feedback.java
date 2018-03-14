package com.vdart.apps.app;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;

import static com.vdart.apps.app.MainActivity.MyPREFERENCES;

public class Feedback extends AppCompatActivity {
    Toolbar toolbar;
    int ONE_TIME = 0;
    String SERVER_URL ;
    String FEEDBACK_URL ;
    Menu menuInflate;
    String notification_count = "";
    String NOTIFICATION_COUNT_URL = "api/notification/notification_employee_unread_count";
    int NOTIFICATION_COUNT = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feedback);
        SERVER_URL = getString(R.string.service_url);
        FEEDBACK_URL = SERVER_URL+ "api/feedback/";
        toolbar = (Toolbar)findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
//        toolbar.setTitleTextColor(0xFFFFFFFF);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        SharedPreferences shared = getSharedPreferences(MyPREFERENCES, MODE_PRIVATE);
        System.out.println("USer : "+shared.getInt("id",0));
        final int id = shared.getInt("id",0);
//        ImageView home = (ImageView) findViewById(R.id.action_home);
//        home.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Intent intent = new Intent(Feedback.this, GridList.class);
//                startActivity(intent);
//            }
//        });

        notification_count = getSharedPreferences(MyPREFERENCES, MODE_PRIVATE).getString("nc","");

        NOTIFICATION_COUNT_URL = SERVER_URL + NOTIFICATION_COUNT_URL;
        getNotificationCount();

        registerReceiver(myReceiver, new IntentFilter(MyAndroidFirebaseMsgService.INTENT_FILTER));

        final EditText Feedback = (EditText) findViewById(R.id.feedback);
        final EditText Query = (EditText) findViewById(R.id.query);
        final RatingBar mBar = (RatingBar) findViewById(R.id.ratingBar);
        Button newservices = (Button) findViewById(R.id.submit);
        newservices.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String rating = String.valueOf(mBar.getRating());
                ONE_TIME++;
                if (ONE_TIME == 1) {
                    String feedback = Feedback.getText().toString();
                    String query = Query.getText().toString();
                    System.out.println("Feedback:" + feedback + "Query:" + query);
                    String data = null;
                    try {
                        data = URLEncoder.encode("feedback_description", "UTF-8")
                                + "=" + URLEncoder.encode(feedback, "UTF-8");
                        data += "&" + URLEncoder.encode("feedback_rating_count", "UTF-8") + "="
                                + URLEncoder.encode(rating, "UTF-8");
                        data += "&" + URLEncoder.encode("feedback_employee", "UTF-8") + "="
                                + id;
                        data += "&" + URLEncoder.encode("feedback_queries", "UTF-8") + "="
                                + URLEncoder.encode(query, "UTF-8");

                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }

                    final BufferedReader[] reader = {null};

                    // Send data
                    final String finalData = data;
                    AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>() {
                        @Override
                        protected Void doInBackground(Void... params) {
                            try {
                                ONE_TIME = 0;
                                // Defined URL  where to send data
                                URL url = new URL(FEEDBACK_URL);
                                // Send POST data request
                                System.out.println("URL:" + FEEDBACK_URL);
                                URLConnection conn = url.openConnection();
                                conn.setDoOutput(true);
                                OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
                                wr.write(finalData);
                                wr.flush();

                                // Get the server response

                                reader[0] = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                                StringBuilder sb = new StringBuilder();
                                String line = null;

                                // Read Server Response
                                while ((line = reader[0].readLine()) != null) {
                                    // Append server response in string
                                    sb.append(line + "\n");
                                }
                                System.out.println(sb.toString());
                                JSONObject object = new JSONObject(sb.toString());
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        createAndShowDialog("Thanks for submitting your feedback with patience. We will look into them to serve you better.", "Success");
                                        Feedback.setText("");
                                        Query.setText("");
                                    }
                                });
                            } catch (Exception ex) {
                                System.out.println(ex);
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        createAndShowDialog("Coming soon", "Not Ready");
                                    }
                                });
                            } finally {
                                try {

                                    reader[0].close();
                                } catch (Exception ex) {
//                                    runOnUiThread(new Runnable() {
//                                        @Override
//                                        public void run() {
//                                            createAndShowDialog("Check your connection","No connection");
//                                        }
//                                    });

                                }
                            }


                            return null;
                        }

                    };
                    runAsyncTask(task);

                }
            }

        });
        Button reset = (Button) findViewById(R.id.reset);
        reset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Feedback.setText("");
                        Query.setText("");
                    }
                });
            }
        });
        //        Click Logo to home screen

        ImageView imageButton = (ImageView) toolbar.findViewById(R.id.vdart_logo);

        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Feedback.this, BannerActivity.class);
                startActivity(intent);
            }
        });
    }

    private BroadcastReceiver myReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            getNotificationCount();
        }
    };

    public void onDestroy() {

        unregisterReceiver(myReceiver);
        super.onDestroy();

    }

    public void getNotificationCount(){
        String data = null;
        try {

            data = URLEncoder.encode("notification_employee", "UTF-8")
                    + "=" + getSharedPreferences(MyPREFERENCES, MODE_PRIVATE).getInt("id",0);;
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        final BufferedReader[] reader = {null};

        // Send data
        final String finalData = data;
        AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                try {
                    // Defined URL  where to send data
                    URL url = new URL(NOTIFICATION_COUNT_URL);

                    // Send POST data request
                    System.out.println("URL:" + NOTIFICATION_COUNT_URL);
                    URLConnection conn = url.openConnection();
                    conn.setDoOutput(true);
                    OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
                    wr.write(finalData);
                    wr.flush();
                    System.out.println(finalData);
                    // Get the server response

                    reader[0] = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    StringBuilder sb = new StringBuilder();
                    String line = null;

                    // Read Server Response
                    while ((line = reader[0].readLine()) != null) {
                        // Append server response in string
                        sb.append(line + "\n");
                    }
                    System.out.println("Output" + sb.toString());
                    JSONObject obj = new JSONObject(sb.toString());
                    if(obj.has("unread")){
                        NOTIFICATION_COUNT = obj.getInt("unread");
                    }
                    setCount(Feedback.this, String.valueOf(NOTIFICATION_COUNT));
                }catch (Exception ex) {
                    System.out.println(ex);
                } finally {
                    try {
                        reader[0].close();
                    } catch (Exception ex) {
                    }
                }
                return null;
            }

        };
        runAsyncTask(task);

//        Download_data download_data = new Download_data((Download_data.download_complete) this);
//        download_data.download_data_from_link(NOTIFICATION_URL);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        setCount(this, String.valueOf(NOTIFICATION_COUNT));
        return  true;
    }

    private void createAndShowDialog(Exception exception, String title) {
        Throwable ex = exception;
        if(exception.getCause() != null){
            ex = exception.getCause();
        }
        createAndShowDialog(ex.getMessage(), title);
    }

    /**
     * Creates a dialog and shows it
     *
     * @param message
     *            The dialog message
     * @param title
     *            The dialog title
     */
    private void createAndShowDialog(final String message, final String title) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setMessage(message);
        builder.setTitle(title);
        builder.create().show();
    }

    private void createAndShowDialogFromTask(final Exception exception, String title) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                createAndShowDialog(exception, "Error");
            }
        });
    }

    private AsyncTask<Void, Void, Void> runAsyncTask(AsyncTask<Void, Void, Void> task) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            return task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } else {
            return task.execute();
        }
    }

    // Initiating Menu XML file (menu.xml)
    public void setCount(Context context, String count) {
        MenuItem menuItem = (MenuItem) menuInflate.findItem(R.id.action_notification);
        LayerDrawable icon = (LayerDrawable) menuItem.getIcon();

        CountDrawable badge;

        // Reuse drawable if possible
        Drawable reuse = icon.findDrawableByLayerId(R.id.ic_group_count);
        if (reuse != null && reuse instanceof CountDrawable) {
            badge = (CountDrawable) reuse;
        } else {
            badge = new CountDrawable(context);
        }

        badge.setCount(count);
        icon.mutate();
        icon.setDrawableByLayerId(R.id.ic_group_count, badge);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        menuInflate = menu;
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu_main, menu);
        setCount(this,notification_count);
        return true;
    }

    /**
     * Event Handling for Individual menu item selected
     * Identify single menu item by it's id
     * */
    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        Intent intent;
        switch (item.getItemId())
        {
            case R.id.action_home:
                intent = new Intent(Feedback.this,BannerActivity.class);
                startActivity(intent);
                return true;

            case R.id.profile:
                intent = new Intent(Feedback.this,ProfileActivity.class);
                startActivity(intent);
                return true;

            case R.id.feedback:
                intent = new Intent(Feedback.this,Feedback.class);
                startActivity(intent);
                return true;

            case R.id.action_search:
                intent = new Intent(Feedback.this,SearchActivity.class);
                startActivity(intent);
                return true;

            case R.id.events:
                intent = new Intent(Feedback.this,EventMain.class);
                startActivity(intent);
                return true;

            case R.id.news:
                intent = new Intent(Feedback.this,NewsMain.class);
                startActivity(intent);
                return true;

            case R.id.shoutout:
                intent = new Intent(Feedback.this,Shoutout.class);
                startActivity(intent);
                return true;

            case R.id.gallery:
                intent = new Intent(Feedback.this,EventGallery.class);
                startActivity(intent);
                return true;

            case R.id.facebook:
                intent = new Intent(Feedback.this,FacebookActivity.class);
                startActivity(intent);
                return true;

            case R.id.twitter:
                intent = new Intent(Feedback.this,TwitterActivity.class);
                startActivity(intent);
                return true;


            case R.id.info:
                Toast.makeText(Feedback.this, "Coming soon", Toast.LENGTH_SHORT).show();
                return true;

            case R.id.settings:
                intent = new Intent(Feedback.this,Changepassword_Activity.class);
                startActivity(intent);
                return true;

            case R.id.logout:
                intent = new Intent(Feedback.this,MainActivity.class);
                startActivity(intent);
                return true;

            case R.id.ceomsg:
                intent = new Intent(Feedback.this,CeomessageActivity.class);
                startActivity(intent);
                return true;

            case R.id.action_notification:
                intent = new Intent(Feedback.this,NotificationMain.class);
                startActivity(intent);
                return true;

            case R.id.live:
                intent = new Intent(this,LiveTelecast.class);
                startActivity(intent);
                return true;

            case R.id.polls:
                intent = new Intent(this,quiz_activity_frag.class);
                startActivity(intent);
                return true;

            case R.id.action_refresh:
//                intent = new Intent(this,BannerActivity.class);
                startActivity(getIntent());
                return true;


            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
