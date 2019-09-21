package com.z1software.epl428_homework_1;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SyncStatusObserver;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

import helper.HttpJsonParser;
import helper.NetworkStatusCheck;

public class UserListingActivity extends AppCompatActivity {
    private static final String KEY_SUCCESS = "success";
    private static final String KEY_DATA = "data";
    private static final String KEY_ID = "ID";
    private static final String KEY_FNAME = "FName";
    private static final String KEY_LNAME = "LName";
    private static final String BASE_URL = "http://40.115.18.125/db/";
    private ArrayList<HashMap<String, String>> userList;
    private ListView userListView;
    private ProgressDialog pDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_listing);
        userListView = findViewById(R.id.userList);
        new FetchUsersAsyncTask().execute();

    }

    /**
     * Fetches the list of movies from the server
     */
    private class FetchUsersAsyncTask extends AsyncTask<String, String, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            //Display progress bar
            pDialog = new ProgressDialog(UserListingActivity.this);
            pDialog.setMessage("Loading users. Please wait...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(false);
            pDialog.show();
        }

        @Override
        protected String doInBackground(String... params) {
            HttpJsonParser httpJsonParser = new HttpJsonParser();
            JSONObject jsonObject = httpJsonParser.makeHttpRequest(
                    BASE_URL + "fetch_all_users.php", "GET", null);
            try {
                int success = jsonObject.getInt(KEY_SUCCESS);
                JSONArray users;
                if (success == 1) {
                    userList = new ArrayList<>();
                    users = jsonObject.getJSONArray(KEY_DATA);
                    //Iterate through the response and populate movies list
                    for (int i = 0; i < users.length(); i++) {
                        JSONObject user = users.getJSONObject(i);
                        Integer userId = user.getInt(KEY_ID);
                        String userFname = user.getString(KEY_FNAME);
                        String userLname = user.getString(KEY_LNAME);
                        HashMap<String, String> map = new HashMap<String, String>();
                        map.put(KEY_ID, userId.toString());
                        map.put(KEY_FNAME, userFname);
                        map.put(KEY_LNAME, userLname);
                        userList.add(map);
                    }
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
                    populateUserList();
                }
            });
        }

    }

    /**
     * Updating parsed JSON data into ListView
     * */
    private void populateUserList() {
        ListAdapter adapter = new SimpleAdapter(UserListingActivity.this, userList,
                R.layout.list_item, new String[]{ KEY_ID, KEY_FNAME, KEY_LNAME}, new int[]{R.id.userId, R.id.fname, R.id.lname});
        // updating listview
        userListView.setAdapter(adapter);
        //Call MovieUpdateDeleteActivity when a movie is clicked
        userListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                //Check for network connectivity
                if (NetworkStatusCheck.isNetworkAvailable(getApplicationContext())) {
                    String userId = ((TextView) view.findViewById(R.id.userId))
                            .getText().toString();
                    Intent intent = new Intent(getApplicationContext(),
                            UserUpdateDeleteActivity.class);
                    intent.putExtra(KEY_ID, userId);
                    startActivityForResult(intent, 20);
                } else {
                    Toast.makeText(UserListingActivity.this,
                            "Unable to connect to internet",
                            Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == 20) {
            // If the result code is 20 that means that
            // the user has deleted/updated the user.
            // So refresh the user listing
            Intent intent = getIntent();
            finish();
            startActivity(intent);
        }
    }

}