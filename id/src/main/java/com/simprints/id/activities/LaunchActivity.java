package com.simprints.id.activities;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ProgressBar;

import com.simprints.id.AppState;
import com.simprints.id.R;
import com.simprints.libdata.Data;
import com.simprints.libdata.EVENT;
import com.simprints.libscanner.BluetoothCom;
import com.simprints.libscanner.Scanner;
import com.simprints.libsimprints.Constants;

import java.util.List;
import java.util.UUID;

public class LaunchActivity extends AppCompatActivity implements Scanner.ScannerListener, Data.DataListener {

    private final static int INITIAL_DISPLAY_MINIMUM = 2500;
    private final static int SUBSEQUENT_DISPLAY_MAXIMUM = 10000;


    private AppState appState;
    private static Handler handler;
    private boolean validApiKey;
    private long startTime;
    private boolean stayConnected;

    private Runnable timeout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launch);
        //Fabric.with(this, new Crashlytics());

        appState = AppState.getInstance();
        ProgressBar progressBar = (ProgressBar) findViewById(R.id.progress_bar);
        progressBar.setVisibility(View.VISIBLE);
        handler = new Handler();
        validApiKey = false;
        startTime = SystemClock.elapsedRealtime();
        stayConnected = false;

        Bundle extras = getIntent().getExtras();
        if (extras == null) {
            Log.d("Simprints", "Trace LaunchActivity: no extras");
            finishWith(Constants.SIMPRINTS_INVALID_API_KEY, null);
            return;
        }
        // If the intent contain a valid random code, then it is an internal
        // call from AlertActivity and we don't have to set the AppState singleton
        // again.
        // Else, we have to
        else {
            switch(getIntent().getAction()) {
                case Constants.SIMPRINTS_IDENTIFY_INTENT:
                    appState.setMode(MODE.IDENTIFY_SUBJECT);
                    break;
                case Constants.SIMPRINTS_REGISTER_INTENT:
                    appState.setMode(MODE.REGISTER_SUBJECT);
                    break;
                default:
                    finishWith(Constants.SIMPRINTS_INVALID_INTENT_ACTION, null);
                    return;
            }

            if (!appState.checkRandomCode(extras.getString(AppState.randomCodeKey))) {
                Log.d("Simprints", "Trace LaunchActivity: external call");
                // Sets apiKey
                String apiKey = extras.getString(Constants.SIMPRINTS_API_KEY);
                if (apiKey == null) {
                    startAlert(ALERT_TYPE.MISSING_API_KEY);
                    return;
                }
                appState.setApiKey(apiKey);
                Log.d("Simprints", String.format("Trace LaunchActivity: apiKey = %s", apiKey));

                // Sets guid (to specified value, or random one)
                String guid = extras.getString(Constants.SIMPRINTS_GUID);
                appState.setGuid(guid == null ? UUID.randomUUID().toString() : guid);
                Log.d("Simprints", String.format("Trace LaunchActivity: guid = %s", guid));

                // These 3 fields are set but not used for the time being
                appState.setUserId(extras.getString(Constants.SIMPRINTS_USER_ID));
                appState.setDeviceId(extras.getString(Constants.SIMPRINTS_DEVICE_ID));
                appState.setCallingPackage(extras.getString(Constants.SIMPRINTS_CALLING_PACKAGE));

                // Set the persistant data object
                appState.setData(new Data(getApplicationContext()));
                appState.getData().setDataListener(this);
                Log.d("Simprints", "Trace LaunchActivity: data object created, listener set");

                // Sets the persistant scanner object
                BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
                if (adapter == null) {
                    Log.d("Simprints", "Trace LaunchActivity: bluetooth not supported");
                    startAlert(ALERT_TYPE.BLUETOOTH_NOT_SUPPORTED);
                    return;
                } else if (!adapter.isEnabled()) {
                    Log.d("Simprints", "Trace LaunchActivity: bluetooth not enabled");
                    startAlert(ALERT_TYPE.BLUETOOTH_NOT_ENABLED);
                    return;
                } else {
                    List<String> pairedScanners = Scanner.getPairedScanners();
                    switch (pairedScanners.size()) {
                        case 0:
                            Log.d("Simprints", "Trace LaunchActivity: no scanner found");
                            startAlert(ALERT_TYPE.NO_SCANNER_FOUND);
                            return;
                        case 1:
                            String macAddress = pairedScanners.get(0);
                            Log.d("Simprints",
                                    String.format("Trace LaunchActivity: 1 scanner found with mac address %s",
                                            macAddress));
                            appState.setScanner(new Scanner(macAddress));
                            appState.getScanner().setScannerListener(this);
                            break;
                        default:
                            Log.d("Simprints", "Trace LaunchActivity: multiple scanners found");
                            startAlert(ALERT_TYPE.MULTIPLE_SCANNERS_FOUND);
                            return;
                    }
                }
            }
        }

        // Initiate scanner connection and apiKey validation
        Log.d("Simprints", "Trace LaunchActivity: initiating scanner connection");
        appState.getScanner().connect();
        Log.d("Simprints", "Trace LaunchActivity: initiating apiKey validation");
        appState.getData().validateApiKey(appState.getApiKey());


        timeout = new Runnable() {
            @Override
            public void run() {
                Scanner scanner = appState.getScanner();
                Log.d("Simprints", String.format(
                        "Trace LaunchActivity: TIMEOUT. validApiKey=%s, scanner=%s, connectionStatus=%s",
                        validApiKey, scanner, (scanner == null) ? null : scanner.getConnectionStatus().name()));
                if (!validApiKey || scanner == null || scanner.getConnectionStatus() != BluetoothCom.BLUETOOTH_STATUS.CONNECTED)
                {
                    // The user can't do anything anyway, so unexpected error
                    startAlert(ALERT_TYPE.UNEXPECTED_ERROR);
                } else {
                    checkApiKeyAndScannerConnected();
                }
            }
        };
        // Just in case, program a timeout event
        handler.postDelayed(timeout, SUBSEQUENT_DISPLAY_MAXIMUM);
        Log.d("Simprints", "Trace LaunchActivity: timeout set");

    }

    private void finishWith(final int resultCode, final Intent intent) {
        if (resultCode >= 0) {
            setResult(resultCode);
        }
        final long remainingTime = Math.max(0, startTime + INITIAL_DISPLAY_MINIMUM - SystemClock.elapsedRealtime());
        Log.d("Simprints", String.format(
                "Trace LaunchActivity: finish with result code %d and intent %s programmed in %d ms",
                resultCode, (intent == null) ? null : intent.toString(), remainingTime));

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Log.d("Simprints", String.format(
                        "Trace LaunchActivity: finish with result code %d and intent %s",
                        resultCode, (intent == null) ? null : intent.toString()));
                if (intent != null) {
                    startActivity(intent);
                }
                finish();
            }
        }, remainingTime);
    }

    private void startAlert(ALERT_TYPE alertType) {
        Intent intent = new Intent(this, AlertActivity.class);
        intent.putExtra("alertType", alertType);
        finishWith(-1, intent);
    }



    @Override
    public void onScannerEvent(com.simprints.libscanner.EVENT event) {
        Log.d("Simprints", String.format("Trace LaunchActivity: onScannerEvent %s, %s",
                event.name(), event.details()));
        switch (event) {
            case CONNECTION_SUCCESS:
            case CONNECTION_ALREADY_CONNECTED:
                checkApiKeyAndScannerConnected();
                break;
            case CONNECTION_BLUETOOTH_DISABLED:
                startAlert(ALERT_TYPE.BLUETOOTH_NOT_ENABLED);
                break;
            case CONNECTION_SCANNER_UNBONDED:
                startAlert(ALERT_TYPE.BLUETOOTH_UNBONDED_SCANNER);
                break;
            // THESE SHOULD NOT HAPPEN, BUT YOU ARE NEVER TOO PRUDENT
            case CONNECTION_IO_ERROR:
            case CONNECTION_BAD_SCANNER_FEATURE:
                startAlert(ALERT_TYPE.UNEXPECTED_ERROR);
                break;
            //

            case UN20_WAKEUP_SUCCESS:
            case UN20_WAKEUP_INVALID_STATE:
                stayConnected = true;
                Intent intent = new Intent(this, ConsentActivity.class);
                finishWith(-1, intent);
                break;
                // THESE SHOULD NOT HAPPEN, BUT YOU ARE NEVER TOO PRUDENT
            case UN20_WAKEUP_FAILURE:
                startAlert(ALERT_TYPE.UNEXPECTED_ERROR);
                break;
        }
    }

    @Override
    public void onDataEvent(EVENT event) {
        Log.d("Simprints", String.format("Trace LaunchActivity: onDataEvent %s, %s",
                event.name(), event.details()));
        switch (event) {
            case API_KEY_VALID:
                validApiKey = true;
                checkApiKeyAndScannerConnected();
                break;
            case API_KEY_INVALID:
                validApiKey = false;
                startAlert(ALERT_TYPE.INVALID_API_KEY);
                break;
        }
    }

    private void checkApiKeyAndScannerConnected() {
        Scanner scanner = appState.getScanner();
        Log.d("Simprints", String.format(
                "Trace LaunchActivity: checkApiKeyAndScannerConnected, validApiKey=%s, scanner=%s, connStatus=%s",
                validApiKey, scanner, (scanner == null) ? null : scanner.getConnectionStatus().name()));
        if (validApiKey && scanner != null && scanner.getConnectionStatus() == BluetoothCom.BLUETOOTH_STATUS.CONNECTED)
        {
            Log.d("Simprints",
                    "Trace LaunchActivity: checkApiKeyAndScannerConnected, un20wakeup and remove timeout callback");
            scanner.un20Wakeup();
            handler.removeCallbacks(timeout);
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        Log.d("Simprints", String.format(
                "Trace LaunchActivity: onKeyDown, keyCode %d", keyCode));
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK: {
                finishWith(Activity.RESULT_CANCELED, null);
                return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        handler.removeCallbacksAndMessages(null);
        Log.d("Simprints", String.format(
                "Trace LaunchActivity: onDestroy, stayConnected=%s", stayConnected));
        if (!stayConnected) {
            Scanner scanner = AppState.getInstance().getScanner();
            if (scanner != null) {
                scanner.disconnect();
            }
        }
    }
}
