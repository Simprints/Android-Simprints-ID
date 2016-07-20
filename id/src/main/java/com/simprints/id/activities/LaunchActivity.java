package com.simprints.id.activities;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.simprints.id.BaseApplication;
import com.simprints.id.R;
import com.simprints.libdata.Data;
import com.simprints.libdata.EVENT;
import com.simprints.libscanner.Message;
import com.simprints.libscanner.Scanner;
import com.simprints.libsimprints.Constants;

import java.util.List;
import java.util.UUID;

public class LaunchActivity extends AppCompatActivity implements Scanner.ScannerListener, Data.DataListener {

    private Context context;
    private ProgressBar progressBar;

    private static int INITIAL_DISPLAY_MINIMUM = 2500;
    private static int SUBSEQUENT_DISPLAY_MAXIMUM = 10000;
    private static Handler handler;
    private static Runnable runnable;

    private String userId = null;
    private String deviceId = null;
    private String apiKey = null;
    private String callingPackage = null;
    private String guid = null;

    private int mode;
    private boolean isExiting = false;

    private Scanner scanner = null;
    public final Scanner.ScannerListener scannerListener = this;
    private boolean scannerConnected = false;
    private static int noOfPairedScanners = 0;
    private static String macAddress = null;

    private Data data = null;
    public final Data.DataListener dataListener = this;
    private boolean apiKeyValid = false;

    private long startTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launch);
        context = this;
        BaseApplication.setContext(context);

        //Fabric.with(this, new Crashlytics());

        // get parameters
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            userId = extras.getString(Constants.SIMPRINTS_USER_ID);
            deviceId = extras.getString(Constants.SIMPRINTS_DEVICE_ID);
            apiKey = extras.getString(Constants.SIMPRINTS_API_KEY);
            callingPackage = extras.getString(Constants.SIMPRINTS_CALLING_PACKAGE);
            guid = extras.getString(Constants.SIMPRINTS_GUID);

            // persist in singleton
            BaseApplication.setUserId(userId);
            BaseApplication.setDeviceId(deviceId);
            BaseApplication.setApiKey(apiKey);
            BaseApplication.setCallingPackage(callingPackage);
            BaseApplication.setGuid(guid);

            if (getIntent().getAction().equals(null)) {
                mode = BaseApplication.getMode();
            }
            else
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
        }

        // start loading task
        startTime = System.currentTimeMillis();
        progressBar = (ProgressBar) findViewById(R.id.progress_bar);
        progressBar.setVisibility(View.VISIBLE);

        // check for mandatory apiKey parameter
        if (isExiting == false && apiKey == null) {
            Intent intent = new Intent(context, AlertActivity.class);
            intent.putExtra("alertType", BaseApplication.MISSING_API_KEY);
            intent.putExtra("userId", userId);
            intent.putExtra("deviceId", deviceId);
            intent.putExtra("apiKey", apiKey);
            intent.putExtra("callingPackage", callingPackage);
            intent.putExtra("guid", guid);
            startActivity(intent);
            finish();
        }

        // set timeout for api key validation and/or scanner connected
        runnable = new Runnable() {
            @Override
            public void run() {
                if (!apiKeyValid || !scannerConnected) {
                    Intent intent = new Intent(context, AlertActivity.class);
                    intent.putExtra("alertType", BaseApplication.NO_SCANNER_FOUND);
                    startActivity(intent);
                    isExiting = true;
                    finish();
                }
            }
        };
        handler = new Handler();
        handler.postDelayed(runnable, SUBSEQUENT_DISPLAY_MAXIMUM);

        // set data instance and persist in singleton
        data = new Data(context);
        data.setDataListener(dataListener);
        BaseApplication.setData(data);
        BaseApplication.getData().validateApiKey(apiKey);

        DelayAndContinueTask delayAndContinueTask = new DelayAndContinueTask();
        delayAndContinueTask.execute();

    }

    @Override
    public void onScannerEvent(com.simprints.libscanner.EVENT event) {
        Log.w("Simprints", "ID: onScannerEvent event name = " + event.name() + " detail = " + event.details());
        if (event.equals(com.simprints.libscanner.EVENT.CONNECTION_SUCCESS)) {
            scannerConnected = true;
            checkApiKeyAndScannerConnected();
        }
        if (event.equals(com.simprints.libscanner.EVENT.UN20_WAKEUP_SUCCESS)) {
            Intent intent = new Intent(context, ConsentActivity.class);
            startActivity(intent);
            finish();
        }
    }

    @Override
    public void onDataEvent(EVENT event) {
        Log.w("Simprints", "ID: onDataEvent event name = " + event.name() + " details = " + event.details());
        if (event.equals(com.simprints.libdata.EVENT.API_KEY_VALID)) {
            apiKeyValid = true;
            checkApiKeyAndScannerConnected();
        }
        else
        if (event.equals(com.simprints.libdata.EVENT.API_KEY_INVALID)) {
            apiKeyValid = false;
            Intent intent = new Intent(context, AlertActivity.class);
            intent.putExtra("alertType", BaseApplication.INVALID_API_KEY);
            intent.putExtra("userId", userId);
            intent.putExtra("deviceId", deviceId);
            intent.putExtra("apiKey", apiKey);
            intent.putExtra("callingPackage", callingPackage);
            intent.putExtra("guid", guid);
            startActivity(intent);
            isExiting = true;
            finish();
        }
    }

    private void checkApiKeyAndScannerConnected() {
        if (apiKeyValid && scannerConnected) {
            scanner.un20Wakeup();
            handler.removeCallbacksAndMessages(null);
        }
    }

    private class DelayAndContinueTask extends AsyncTask<Void, Void, Void> {

        private boolean bluetoothNotSupported = false;
        private boolean bluetoothNotEnabled = false;

        @Override
        protected void onPreExecute() {
            progressBar.setVisibility(View.VISIBLE);

            // check bluetooth supported
            BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            if (mBluetoothAdapter == null) {
                bluetoothNotSupported = true;
            }

            // check bluetooth enabled
            else if (!mBluetoothAdapter.isEnabled()) {
                bluetoothNotEnabled = true;
            }

            // get list of paired scanners
            else {
                noOfPairedScanners = 0;
                List<String> pairedScanners = Scanner.getPairedScanners();
                for (String pairedScanner : pairedScanners) {
                    if (Scanner.isScannerAddress(pairedScanner)) {
                        macAddress = pairedScanner;
                        noOfPairedScanners += 1;
                    }
                }
            }
        }

        @Override
        protected Void doInBackground(Void... voids) {
            long currentTime = System.currentTimeMillis();
            if (currentTime - startTime < INITIAL_DISPLAY_MINIMUM) {
                try {
                    Thread.sleep(INITIAL_DISPLAY_MINIMUM - (currentTime - startTime));
                }
                catch (InterruptedException e) { }
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            // bluetooth not supported
            if (bluetoothNotSupported == true) {
                Intent intent = new Intent(context, AlertActivity.class);
                intent.putExtra("alertType", BaseApplication.BLUETOOTH_NOT_SUPPORTED);
                intent.putExtra("userId", userId);
                intent.putExtra("deviceId", deviceId);
                intent.putExtra("apiKey", apiKey);
                intent.putExtra("callingPackage", callingPackage);
                intent.putExtra("guid", guid);
                startActivity(intent);
                isExiting = true;
                finish();
            }

            // bluetooth not enabled
            else if (bluetoothNotEnabled == true) {
                Intent intent = new Intent(context, AlertActivity.class);
                intent.putExtra("alertType", BaseApplication.BLUETOOTH_NOT_ENABLED);
                intent.putExtra("userId", userId);
                intent.putExtra("deviceId", deviceId);
                intent.putExtra("apiKey", apiKey);
                intent.putExtra("callingPackage", callingPackage);
                intent.putExtra("guid", guid);
                startActivity(intent);
                isExiting = true;
                finish();
            }

            // check for no scanner found
            else if (noOfPairedScanners == 0) {
                Intent intent = new Intent(context, AlertActivity.class);
                intent.putExtra("alertType", BaseApplication.NO_SCANNER_FOUND);
                intent.putExtra("userId", userId);
                intent.putExtra("deviceId", deviceId);
                intent.putExtra("apiKey", apiKey);
                intent.putExtra("callingPackage", callingPackage);
                intent.putExtra("guid", guid);
                startActivity(intent);
                isExiting = true;
                finish();
            }

            // check for multiple scanners found
            else if (noOfPairedScanners > 1) {
                Intent intent = new Intent(context, AlertActivity.class);
                intent.putExtra("alertType", BaseApplication.MULTIPLE_SCANNERS_FOUND);
                intent.putExtra("userId", userId);
                intent.putExtra("deviceId", deviceId);
                intent.putExtra("apiKey", apiKey);
                intent.putExtra("callingPackage", callingPackage);
                intent.putExtra("guid", guid);
                startActivity(intent);
                isExiting = true;
                finish();
            }

            // get scanner instance and set in singleton and then try connection
            else {
                scanner = new Scanner(macAddress);
                scanner.setScannerListener(scannerListener);
                BaseApplication.setScanner(scanner);
                scanner.connect();
            }
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK: {
                isExiting = true;
                finish();
                return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (isExiting == true) {
            if (scanner != null) {
                scanner.un20Shutdown();
                scanner.disconnect();
            }
            handler.removeCallbacksAndMessages(null);
        }
    }
}
