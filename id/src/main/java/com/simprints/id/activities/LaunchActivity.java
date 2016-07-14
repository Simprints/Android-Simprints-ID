package com.simprints.id.activities;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ProgressBar;

import com.simprints.id.BaseApplication;
import com.simprints.id.R;
import com.simprints.libdata.Data;
import com.simprints.libdata.EVENT;
import com.simprints.libscanner.Scanner;
import com.simprints.libsimprints.Constants;

import java.util.UUID;

import io.fabric.sdk.android.Fabric;
import com.crashlytics.android.Crashlytics;

public class LaunchActivity extends AppCompatActivity implements Scanner.ScannerListener, Data.DataListener {

    private Context context;
    private ProgressBar progressBar;

    private static int INITIAL_DISPLAY_MINIMUM = 2000;
    private static int SUBSEQUENT_DISPLAY_MAXIMUM = 5000;

    private String userId = null;
    private String deviceId = null;
    private String apiKey = null;
    private String callingPackage = null;
    private String guid = null;

    private int mode;

    private LoadingTask loadingTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launch);
        context = this;
        BaseApplication.setContext(context);

        //Fabric.with(this, new Crashlytics());

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            userId = extras.getString(Constants.SIMPRINTS_USER_ID);
            deviceId = extras.getString(Constants.SIMPRINTS_DEVICE_ID);
            apiKey = extras.getString(Constants.SIMPRINTS_API_KEY);
            callingPackage = extras.getString(Constants.SIMPRINTS_CALLING_PACKAGE);
            guid = extras.getString(Constants.SIMPRINTS_GUID);
        }

        BaseApplication.setUserId(userId);
        BaseApplication.setDeviceId(deviceId);
        BaseApplication.setApiKey(apiKey);
        BaseApplication.setCallingPackage(callingPackage);
        BaseApplication.setGuid(guid);

        if (getIntent().getAction().equals(Constants.SIMPRINTS_IDENTIFY_INTENT)) {
            mode = BaseApplication.IDENTIFY_SUBJECT;
            BaseApplication.setMode(mode);
        }
        else
        if (getIntent().getAction().equals(Constants.SIMPRINTS_REGISTER_INTENT)) {
            mode = BaseApplication.REGISTER_SUBJECT;
            BaseApplication.setMode(mode);
            if (guid == null) {
                guid = UUID.randomUUID().toString();
                BaseApplication.setGuid(guid);
            }
        }
        else {
            //finish();
        }

        progressBar = (ProgressBar) findViewById(R.id.progress_bar);
        loadingTask = new LoadingTask();
        loadingTask.execute();
    }

    @Override
    public void onScannerEvent(com.simprints.libscanner.EVENT event) {
        Log.w("Simprints", "ID: onScannerEvent event name = " + event.name() + " detail = " + event.details());
    }

    @Override
    public void onDataEvent(EVENT event) {
        Log.w("Simprints", "ID: onDataEvent event name = " + event.name() + " details = " + event.details());
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
                Intent intent = new Intent(context, AlertActivity.class);
                intent.putExtra("alertType", BaseApplication.MISSING_API_KEY);
                startActivity(intent);
                finish();
                return 0;
            }
            else {
                BaseApplication.getData().validateApiKey(apiKey);
            }

            // subsequent display maximum
            try {
                Thread.sleep(SUBSEQUENT_DISPLAY_MAXIMUM);
            }
            catch (InterruptedException e) {
                e.printStackTrace();
                finish();
            }

            return 1;
        }

        @Override
        protected void onPostExecute(Integer result) {
            progressBar.setVisibility(View.INVISIBLE);
            if (result == 1) {
                Intent intent = new Intent(context, ConsentActivity.class);
                startActivity(intent);
            }
            finish();
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK: {
                loadingTask.cancel(true);
                finish();
                return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }
}
