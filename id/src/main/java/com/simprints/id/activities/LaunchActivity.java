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

import java.util.List;
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

    private boolean isExiting = false;

    private Scanner scanner;

    private Data data;

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

        // set scanner instance and set in singleton
        scanner = new Scanner();
        scanner.setScannerListener(this);
        BaseApplication.setScanner(scanner);

        // get data instance and set in singleton
        data = new Data(context);
        data.setDataListener(this);
        BaseApplication.setData(data);

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

        private long startTime;
        private long currentTime;

        @Override
        protected void onPreExecute() {
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected Integer doInBackground(Void... arg0) {

            startTime = System.currentTimeMillis();

            /*
            // look for paired scanners
            int noOfPairedScanners = 0;
            String macAddress = null;
            List<String> pairedScanners = Scanner.getPairedScanners();
            for (String pairedScanner : pairedScanners) {
                if (Scanner.isScannerAddress(pairedScanner)) {
                    Log.w("Simprints", "paired mac address = " + pairedScanner);
                    macAddress = pairedScanner;
                    noOfPairedScanners += 1;
                }
            }
            */

            // initial display minimum
            currentTime = System.currentTimeMillis();
            if (currentTime - startTime < INITIAL_DISPLAY_MINIMUM) {
                try {
                    Thread.sleep(INITIAL_DISPLAY_MINIMUM - currentTime - startTime);
                }
                catch (InterruptedException e) {
                    e.printStackTrace();
                    finish();
                }
            }

            // check for mandatory parameters
            if (isExiting == false && apiKey == null) {
                Intent intent = new Intent(context, AlertActivity.class);
                intent.putExtra("alertType", BaseApplication.MISSING_API_KEY);
                startActivity(intent);
                finish();
                return 0;
            }
            else {
                BaseApplication.getData().validateApiKey(apiKey);
            }

            /*
            // check for no scanner found
            if (noOfPairedScanners == 0) {
                Intent intent = new Intent(context, AlertActivity.class);
                intent.putExtra("alertType", BaseApplication.NO_SCANNER_FOUND);
                startActivity(intent);
                finish();
                return 0;
            }

            // check for multiple scanners found
            if (noOfPairedScanners > 1) {
                Intent intent = new Intent(context, AlertActivity.class);
                intent.putExtra("alertType", BaseApplication.MULTIPLE_SCANNERS_FOUND);
                startActivity(intent);
                finish();
                return 0;
            }
            */

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
                isExiting = true;
                finish();
                return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }
}
