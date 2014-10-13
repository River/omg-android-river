package com.riverjiang.omgandroid;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ShareActionProvider;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;


public class MyActivity extends Activity implements View.OnClickListener, AdapterView.OnItemClickListener {

    TextView mainTextView;
    Button mainButton;
    EditText mainEditText;
    ListView mainListView;

    JSONAdapter mJSONAdapter;
    ShareActionProvider mShareActionProvider;

    private static final String PREFS = "prefs";
    private static final String PREF_NAME = "name";
    SharedPreferences mSharedPreferences;

    private static final String QUERY_URL = "http://openlibrary.org/search.json?q=";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setProgressBarIndeterminateVisibility(false);

        setContentView(R.layout.activity_my);


        mainTextView = (TextView) findViewById(R.id.main_textview);

        mainButton = (Button) findViewById(R.id.main_button);
        mainButton.setOnClickListener(this);

        mainEditText = (EditText) findViewById(R.id.main_edittext);

        mainListView = (ListView) findViewById(R.id.main_listview);

        mJSONAdapter = new JSONAdapter(this, getLayoutInflater());

        mainListView.setAdapter(mJSONAdapter);

        mainListView.setOnItemClickListener(this);

        mSharedPreferences = getSharedPreferences(PREFS, MODE_PRIVATE);

        String name = mSharedPreferences.getString(PREF_NAME, "");
        if (name.length() > 0) {
            Toast.makeText(this, "Welcome back, " + name + "!", Toast.LENGTH_LONG).show();
        } else {
            AlertDialog.Builder alert = new AlertDialog.Builder(this);

            alert.setTitle("Hi!");
            alert.setMessage("What's your name?");

            final EditText input = new EditText(this);
            alert.setView(input);

            alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    String inputName = input.getText().toString();

                    SharedPreferences.Editor e = mSharedPreferences.edit();
                    e.putString(PREF_NAME, inputName);
                    e.commit();

                    Toast.makeText(getApplicationContext(), "Welcome, " + inputName + "!", Toast.LENGTH_LONG).show();
                }
            });

            alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {

                }
            });

            alert.show();
        }

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.my, menu);

        MenuItem shareItem = menu.findItem(R.id.menu_item_share);

        if (shareItem != null) mShareActionProvider = (ShareActionProvider) shareItem.getActionProvider();

        setShareIntent();

        return true;
    }

    private void setShareIntent() {
        if (mShareActionProvider != null) {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Android Development");
            shareIntent.putExtra(Intent.EXTRA_TEXT, mainEditText.getText().toString());
            mShareActionProvider.setShareIntent(shareIntent);
        }
    }

    @Override
    public void onClick(View view) {
        // Hide software keyboard
        hideKeyboard();

        queryBooks(mainEditText.getText().toString());
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        JSONObject jsonObject = (JSONObject) mJSONAdapter.getItem(position);

        Intent detailIntent = new Intent(this, DetailActivity.class);

        detailIntent.putExtra("coverID", jsonObject.optString("cover_i", ""));
        detailIntent.putExtra("bookTitle", jsonObject.optString("title", ""));
        // put any extra information to be sent to the intent here

        startActivity(detailIntent);
    }

    private void queryBooks(String searchString) {
        String urlString = "";
        try {
            urlString = URLEncoder.encode(searchString, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
        }

        setProgressBarIndeterminateVisibility(true);

        AsyncHttpClient client = new AsyncHttpClient();
        client.get(QUERY_URL + urlString, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(JSONObject jsonObject) {
                mJSONAdapter.updateData(jsonObject.optJSONArray("docs"));
                setProgressBarIndeterminateVisibility(false);
            }
            @Override
            public void onFailure(int statusCode, Throwable throwable, JSONObject error)
            {
                Toast.makeText(getApplicationContext(),
                        "Error: " + statusCode + " " + throwable.getMessage(), Toast.LENGTH_LONG).show();
                Log.e("OMGAndroid", "Error: " + statusCode + " " + throwable.getMessage());
                setProgressBarIndeterminateVisibility(false);
            }
        });
    }

    private void hideKeyboard() {
        InputMethodManager inputManager = (InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE);

        // check if no view has focus:
        View view = this.getCurrentFocus();
        if (view != null) {
            inputManager.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }
}
