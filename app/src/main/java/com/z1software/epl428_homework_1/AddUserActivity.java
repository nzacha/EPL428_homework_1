package com.z1software.epl428_homework_1;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import helper.NetworkStatusCheck;
import helper.HttpJsonParser;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class AddUserActivity extends AppCompatActivity {
    private static final String KEY_SUCCESS = "success";
    private static final String KEY_ID = "ID";
    private static final String KEY_FNAME = "FName";
    private static final String KEY_LNAME = "LName";
    private static final String BASE_URL = "http://40.115.18.125/db/";
    private static String STRING_EMPTY = "";
    private EditText userIdEditText;
    private EditText userFnameEditText;
    private EditText userLnameEditText;
    private String userId;
    private String fname;
    private String lname;
    private Button addButton;
    private int success;
    private ProgressDialog pDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_user);
        userIdEditText = findViewById(R.id.txtUserIdAdd);
        userFnameEditText = findViewById(R.id.txtFnameAdd);
        userLnameEditText = findViewById(R.id.txtLnameAdd);
        addButton = findViewById(R.id.btnAdd);
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (NetworkStatusCheck.isNetworkAvailable(getApplicationContext())) {
                    addUser();
                } else {
                    Toast.makeText(AddUserActivity.this,
                            "Unable to connect to internet",
                            Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    /**
     * Checks whether all files are filled. If so then calls AddMovieAsyncTask.
     * Otherwise displays Toast message informing one or more fields left empty
     */
    private void addUser() {
        if (!STRING_EMPTY.equals(userIdEditText.getText().toString()) &&
                !STRING_EMPTY.equals(userFnameEditText.getText().toString()) &&
                !STRING_EMPTY.equals(userLnameEditText.getText().toString())) {

            userId = userIdEditText.getText().toString();
            fname = userFnameEditText.getText().toString();
            lname = userLnameEditText.getText().toString();
            new AddMovieAsyncTask().execute();
        } else {
            Toast.makeText(AddUserActivity.this,
                    "One or more fields left empty!",
                    Toast.LENGTH_LONG).show();
        }
    }

    /**
     * AsyncTask for adding a movie
     */
    private class AddMovieAsyncTask extends AsyncTask<String, String, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            //Display proggress bar
            pDialog = new ProgressDialog(AddUserActivity.this);
            pDialog.setMessage("Adding User. Please wait...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(false);
            pDialog.show();
        }

        @Override
        protected String doInBackground(String... params) {
            HttpJsonParser httpJsonParser = new HttpJsonParser();
            Map<String, String> httpParams = new HashMap<>();
            //Populating request parameters
            httpParams.put(KEY_ID, userId);
            httpParams.put(KEY_FNAME, fname);
            httpParams.put(KEY_LNAME, lname);
            JSONObject jsonObject = httpJsonParser.makeHttpRequest(
                    BASE_URL + "add_user.php", "POST", httpParams);
            System.err.println(httpParams.toString());
            try {
                success = jsonObject.getInt(KEY_SUCCESS);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            System.err.println(jsonObject.toString());
            return null;
        }

        protected void onPostExecute(String result) {
            pDialog.dismiss();
            runOnUiThread(new Runnable() {
                public void run() {
                    if (success == 1) {
                        //Display success message
                        Toast.makeText(AddUserActivity.this,
                                "User Added", Toast.LENGTH_LONG).show();
                        Intent i = getIntent();
                        //send result code 20 to notify about movie update
                        setResult(20, i);
                        //Finish ths activity and go back to listing activity
                        finish();

                    } else {
                        Toast.makeText(AddUserActivity.this,
                                "Some error occurred while adding user",
                                Toast.LENGTH_LONG).show();
                    }
                }
            });
        }
    }
}