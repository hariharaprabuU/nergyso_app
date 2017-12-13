package com.example.user.energysoft;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;

public class Resetpassword_Activity extends AppCompatActivity {
    EditText new_password, confirm_password, uid, token;
    String SERVER_URL;
    String RESET_PASSWORD_URL ;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_resetpassword);
        SERVER_URL = getString(R.string.service_url);
        RESET_PASSWORD_URL = SERVER_URL+ "api/rest-auth/password/reset/confirm/";
        new_password = (EditText) findViewById(R.id.new_password);
        confirm_password = (EditText) findViewById(R.id.confirm_password);
        uid = (EditText) findViewById(R.id.uid);
        token = (EditText) findViewById(R.id.token);
        Button reset = (Button) findViewById(R.id.cancel);
        reset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        new_password.setText("");
                        confirm_password.setText("");
                        uid.setText("");
                        token.setText("");
                    }
                });
            }
        });
        Button submit = (Button) findViewById(R.id.submit);
        submit.setOnClickListener(new View.OnClickListener() {
            int ONE_TIME = 0;
            @Override
            public void onClick(View view) {
                ONE_TIME++;
                if (ONE_TIME == 1) {
                    String New_password = new_password.getText().toString();
                    String Confirm_password = confirm_password.getText().toString();
                    String Uid = uid.getText().toString();
                    String Token = token.getText().toString();
                    String data = null;
                    try {
                        data = URLEncoder.encode("new_password1", "UTF-8")
                                + "=" + URLEncoder.encode(New_password, "UTF-8");
                        data += "&" +URLEncoder.encode("new_password2", "UTF-8")
                                + "=" + URLEncoder.encode(Confirm_password, "UTF-8");
                        data += "&" +URLEncoder.encode("uid", "UTF-8")
                                + "=" + URLEncoder.encode(Uid, "UTF-8");
                        data += "&" +URLEncoder.encode("token", "UTF-8")
                                + "=" + URLEncoder.encode(Token, "UTF-8");
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
                                URL url = new URL(RESET_PASSWORD_URL);

                                // Send POST data request
                                System.out.println("URL:" + RESET_PASSWORD_URL);
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
                                if(object.has("detail")){
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            Toast.makeText(Resetpassword_Activity.this, "Password reset e-mail has been sent to your mail.", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                    Intent intent = new Intent(Resetpassword_Activity.this, MainActivity.class);
//                                    intent.putExtra("key", object.getString("key"));
//                                    finish();
                                    startActivity(intent);
                                }else{
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            createAndShowDialog("Please check your Email ID", "Error");
                                        }
                                    });
                                }
                            } catch (Exception ex) {
                                System.out.println(ex);
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        createAndShowDialog("No internet connection", "Error");
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
}
