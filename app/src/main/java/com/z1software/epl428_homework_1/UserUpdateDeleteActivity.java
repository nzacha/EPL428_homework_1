package com.z1software.epl428_homework_1;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.app.AlertDialog;

import androidx.appcompat.app.AppCompatActivity;

import helper.NetworkStatusCheck;
import helper.HttpJsonParser;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class UserUpdateDeleteActivity extends AppCompatActivity {
    private static String STRING_EMPTY = "";
    private static final String KEY_SUCCESS = "success";
    private static final String KEY_DATA = "data";
    private static final String KEY_ID = "ID";
    private static final String KEY_FNAME = "FName";
    private static final String KEY_LNAME = "LName";
    private static final String BASE_URL = "http://40.115.18.125/db/";
    private String userId;
    private TextView userIdEditText;
    private EditText fnameEditText;
    private EditText lnameEditText;
    private String fname;
    private String lname;
    private Button deleteButton;
    private Button updateButton;
    private int success;
    private ProgressDialog pDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_update_delete);
        Intent intent = getIntent();
        userIdEditText = findViewById(R.id.txtUserIDUpdate);
        fnameEditText = findViewById(R.id.txtFnameUpdate);
        lnameEditText = findViewById(R.id.txtLnameUpdate);

        userId = intent.getStringExtra(KEY_ID);
        new FetchUserDetailsAsyncTask().execute();
        deleteButton = findViewById(R.id.btnDelete);
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                confirmDelete();
            }
        });
        updateButton = findViewById(R.id.btnUpdate);
        updateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (NetworkStatusCheck.isNetworkAvailable(getApplicationContext())) {
                    updateUser();
                } else {
                    Toast.makeText(UserUpdateDeleteActivity.this,
                            "Unable to connect to internet",
                            Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    /**
     * Fetches single movie details from the server
     */
    private class FetchUserDetailsAsyncTask extends AsyncTask<String, String, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            //Display progress bar
            pDialog = new ProgressDialog(UserUpdateDeleteActivity.this);
            pDialog.setMessage("Loading User Details. Please wait...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(false);
            pDialog.show();
        }

        @Override
        protected String doInBackground(String... params) {
            HttpJsonParser httpJsonParser = new HttpJsonParser();
            Map<String, String> httpParams = new HashMap<>();
            httpParams.put(KEY_ID, userId);
            JSONObject jsonObject = httpJsonParser.makeHttpRequest(
                    BASE_URL + "get_user_details.php", "GET", httpParams);
            try {
                int success = jsonObject.getInt(KEY_SUCCESS);
                JSONObject user;
                if (success == 1) {
                    //Parse the JSON response
                    user = jsonObject.getJSONObject(KEY_DATA);
                    userId = user.getString(KEY_ID);
                    fname = user.getString(KEY_FNAME);
                    lname = user.getString(KEY_LNAME);

                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        protected void onPostExecute(String result) {
            pDialog.dismiss();
            runOnUiThread(new Runnable() {
                public void run() {
                    //Populate the Edit Texts once the network activity is finished executing
                    userIdEditText.setText(userId);
                    fnameEditText.setText(fname);
                    lnameEditText.setText(lname);
                }
            });
        }
    }

    /**
     * Displays an alert dialogue to confirm the deletion
     */
    private void confirmDelete() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                UserUpdateDeleteActivity.this);
        alertDialogBuilder.setMessage("Are you sure, you want to delete this user?");
        alertDialogBuilder.setPositiveButton("Delete",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {
                        if (NetworkStatusCheck.isNetworkAvailable(getApplicationContext())) {
                            //If the user confirms deletion, execute DeleteMovieAsyncTask
                            new DeleteUserAsyncTask().execute();
                        } else {
                            Toast.makeText(UserUpdateDeleteActivity.this,
                                    "Unable to connect to internet",
                                    Toast.LENGTH_LONG).show();
                        }
                    }
                });

        alertDialogBuilder.setNegativeButton("Cancel", null);

        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    /**
     * AsyncTask to delete a movie
     */
    private class DeleteUserAsyncTask extends AsyncTask<String, String, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            //Display progress bar
            pDialog = new ProgressDialog(UserUpdateDeleteActivity.this);
            pDialog.setMessage("Deleting User. Please wait...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(false);
            pDialog.show();
        }

        @Override
        protected String doInBackground(String... params) {
            HttpJsonParser httpJsonParser = new HttpJsonParser();
            Map<String, String> httpParams = new HashMap<>();
            //Set movie_id parameter in request
            httpParams.put(KEY_ID, userId);
            JSONObject jsonObject = httpJsonParser.makeHttpRequest(
                    BASE_URL + "delete_user.php", "POST", httpParams);
            System.err.println(httpParams.toString());
            try {
                success = jsonObject.getInt(KEY_SUCCESS);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        protected void onPostExecute(String result) {
            pDialog.dismiss();
            runOnUiThread(new Runnable() {
                public void run() {
                    if (success == 1) {
                        //Display success message
                        Toast.makeText(UserUpdateDeleteActivity.this,
                                "User Deleted", Toast.LENGTH_LONG).show();
                        Intent i = getIntent();
                        //send result code 20 to notify about movie deletion
                        setResult(20, i);
                        finish();
                    } else {
                        Toast.makeText(UserUpdateDeleteActivity.this,
                                "Some error occurred while deleting user",
                                Toast.LENGTH_LONG).show();
                    }
                }
            });
        }
    }

    /**
     * Checks whether all files are filled. If so then calls UpdateMovieAsyncTask.
     * Otherwise displays Toast message informing one or more fields left empty
     */
    private void updateUser() {
        if (!STRING_EMPTY.equals(userIdEditText.getText().toString()) &&
                !STRING_EMPTY.equals(fnameEditText.getText().toString()) &&
                !STRING_EMPTY.equals(lnameEditText.getText().toString())) {

            userId = userIdEditText.getText().toString();
            fname = fnameEditText.getText().toString();
            lname = lnameEditText.getText().toString();
            new UpdateUserAsyncTask().execute();
        } else {
            Toast.makeText(UserUpdateDeleteActivity.this,
                    "One or more fields left empty!",
                    Toast.LENGTH_LONG).show();
        }
    }
    /**
     * AsyncTask for updating a users details
     */

    private class UpdateUserAsyncTask extends AsyncTask<String, String, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            //Display progress bar
            pDialog = new ProgressDialog(UserUpdateDeleteActivity.this);
            pDialog.setMessage("Updating User. Please wait...");
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
                    BASE_URL + "update_user.php", "POST", httpParams);
            try {
                success = jsonObject.getInt(KEY_SUCCESS);
                System.err.println(jsonObject.toString());
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        protected void onPostExecute(String result) {
            pDialog.dismiss();
            runOnUiThread(new Runnable() {
                public void run() {
                    if (success == 1) {
                        //Display success message
                        Toast.makeText(UserUpdateDeleteActivity.this,
                                "User Updated", Toast.LENGTH_LONG).show();
                        Intent i = getIntent();
                        //send result code 20 to notify about movie update
                        setResult(20, i);
                        finish();
                    } else {
                        Toast.makeText(UserUpdateDeleteActivity.this,
                                "Some error occurred while updating user",
                                Toast.LENGTH_LONG).show();
                    }
                }
            });
        }
    }
}