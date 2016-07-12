package com.simprints.id.activities;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.view.View;
import android.widget.ProgressBar;

import com.simprints.id.BaseApplication;
import com.simprints.id.R;
import com.simprints.libsimprints.Constants;

import java.util.UUID;

import io.fabric.sdk.android.Fabric;
import com.crashlytics.android.Crashlytics;

public class LaunchActivity extends BaseNavigationActivity {

    private Context context;
    private ProgressBar progressBar;

    private static int INITIAL_DISPLAY_MINIMUM = 3000;
    private static int SUBSEQUENT_DISPLAY_MAXIMUM = 5000;

    private String userId = null;
    private String deviceId = null;
    private String apiKey = null;
    private String callingPackage = null;
    private String guid = null;

    private int operation;

    private LoadingTask loadingTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launch);
        context = this;

        Fabric.with(this, new Crashlytics());

        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            userId = extras.getString(Constants.SIMPRINTS_USER_ID);
            deviceId = extras.getString(Constants.SIMPRINTS_DEVICE_ID);
            apiKey = extras.getString(Constants.SIMPRINTS_API_KEY);
            callingPackage = extras.getString(Constants.SIMPRINTS_CALLING_PACKAGE);
            guid = extras.getString(Constants.SIMPRINTS_GUID);

            BaseApplication.setUserId(userId);
            BaseApplication.setDeviceId(deviceId);
            BaseApplication.setApiKey(apiKey);
            BaseApplication.setCallingPackage(callingPackage);
            BaseApplication.setGuid(guid);
        }

        if (getIntent().getAction().equals("com.simprints.id.IDENTIFY")) {
            operation = BaseApplication.IDENTIFY_SUBJECT;
        }
        else {
            operation = BaseApplication.REGISTER_SUBJECT;
            if (guid == null) {
                guid = UUID.randomUUID().toString();
                BaseApplication.setGuid(guid);
            }
        }

        progressBar = (ProgressBar) findViewById(R.id.progress_bar);
        loadingTask = new LoadingTask();
        loadingTask.execute();
    }

    private class LoadingTask extends AsyncTask<Void, Void, Integer> {
        @Override
        protected void onPreExecute() {
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected Integer doInBackground(Void... arg0) {

            // initial display minimum
            try {
                Thread.sleep(INITIAL_DISPLAY_MINIMUM);
            }
            catch (InterruptedException e) {
                e.printStackTrace();
                finish();
            }

            // check for mandatory parameters
            if (apiKey == null) {

            }

            // subsequent display maximum
            try {
                Thread.sleep(SUBSEQUENT_DISPLAY_MAXIMUM);
            }
            catch (InterruptedException e) {
                e.printStackTrace();
                finish();
            }

            return 0;
        }

        @Override
        protected void onPostExecute(Integer result) {
            progressBar.setVisibility(View.INVISIBLE);
            Intent intent = new Intent(context, ConsentActivity.class);
            intent.putExtra("operation", operation);
            if (operation == BaseApplication.REGISTER_SUBJECT) {
                intent.putExtra("guid", guid);
            }
            startActivity(intent);
            finish();
        }
    }
}
