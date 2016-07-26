package com.simprints.id.activities;

import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;

import com.simprints.id.AppState;
import com.simprints.id.R;
import com.simprints.id.tools.InternalConstants;
import com.simprints.id.tools.Log;
import com.simprints.libdata.Data;
import com.simprints.libdata.EVENT;
import com.simprints.libscanner.BluetoothCom;
import com.simprints.libscanner.Scanner;
import com.simprints.libsimprints.Constants;

import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class LaunchActivity extends AppCompatActivity implements Scanner.ScannerListener, Data.DataListener {

    private final static int MINIMUM_DISPLAY_DURATION = 2500;
    private final static int CONNECTION_AND_VALIDATION_TIMEOUT = 10000;


    private AppState appState;
    private static Handler handler;

    private boolean validApiKey;
    private long minEndTime;
    private boolean childActivityLaunched;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launch);
        //Fabric.with(this, new Crashlytics());

        appState = AppState.getInstance();
        handler = new Handler();
        validApiKey = false;
        minEndTime = SystemClock.elapsedRealtime() + MINIMUM_DISPLAY_DURATION;
        childActivityLaunched = false;

        Bundle extras = getIntent().getExtras();
        if (extras == null) {
            Log.d(this, "finishing with SIMPRINTS_INVALID_API_KEY");
            finishWith(Constants.SIMPRINTS_INVALID_API_KEY);
            return;
        }

        switch(getIntent().getAction()) {
            case Constants.SIMPRINTS_IDENTIFY_INTENT:
                appState.setEnrol(false);
                break;
            case Constants.SIMPRINTS_REGISTER_INTENT:
                appState.setEnrol(true);
                break;
            default:
                finishWith(Constants.SIMPRINTS_INVALID_INTENT_ACTION);
                return;
        }

        // Sets apiKey
        String apiKey = extras.getString(Constants.SIMPRINTS_API_KEY);
        if (apiKey == null) {
            launchAlert(ALERT_TYPE.MISSING_API_KEY);
            return;
        }
        Log.d(this, String.format(Locale.UK, "apiKey = %s", apiKey));
        appState.setApiKey(apiKey);

        // Sets guid (to specified value, or random one)
        String guid = extras.getString(Constants.SIMPRINTS_GUID);
        Log.d(this, String.format(Locale.UK, "guid = %s", guid));
        if (guid == null) {
            guid = UUID.randomUUID().toString();
            Log.d(this, String.format(Locale.UK, "using random guid = %s", guid));
        }
        appState.setGuid(guid);

        // Sets userId
        String userId = extras.getString(Constants.SIMPRINTS_USER_ID);
        Log.d(this, String.format(Locale.UK, "userId = %s", userId));
        appState.setUserId(userId);

        // Sets deviceId
        String deviceId = extras.getString(Constants.SIMPRINTS_DEVICE_ID);
        Log.d(this, String.format(Locale.UK, "deviceId = %s", deviceId));
        appState.setDeviceId(deviceId);

        // Initializes result
        appState.setResultCode(RESULT_CANCELED);
        appState.setResultData(new Intent(appState.isEnrol()
                ? Constants.SIMPRINTS_REGISTER_INTENT
                : Constants.SIMPRINTS_IDENTIFY_INTENT));

        // Initializes the session Data object
        appState.setData(new Data(getApplicationContext()));
        appState.getData().setDataListener(this);
        Log.d(this, "Data object initialised");

        // Initializes the session Scanner object
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        if (adapter == null) {
            launchAlert(ALERT_TYPE.BLUETOOTH_NOT_SUPPORTED);
            return;
        }
        if (!adapter.isEnabled()) {
            launchAlert(ALERT_TYPE.BLUETOOTH_NOT_ENABLED);
            return;
        }
        List<String> pairedScanners = Scanner.getPairedScanners();
        if (pairedScanners.size() == 0) {
            launchAlert(ALERT_TYPE.NO_PAIRED_SCANNER);
            return;
        }
        if (pairedScanners.size() > 1) {
            launchAlert(ALERT_TYPE.MULTIPLE_PAIRED_SCANNERS);
            return;
        }
        String macAddress = pairedScanners.get(0);
        appState.setMacAddress(macAddress);
        appState.setScanner(new Scanner(macAddress));
        appState.getScanner().setScannerListener(this);
        Log.d(this, String.format("Scanner object initialised (MAC address = %s)",
                macAddress));

        validateAndConnect();
    }

    private void validateAndConnect() {
        // Initiate scanner connection and apiKey validation
        Log.d(this, "Initiating scanner connection and apiKey validation");
        appState.getScanner().connect();
        appState.getData().validateApiKey(appState.getApiKey());

        // Program a timeout event after CONNECTION_AND_VALIDATION_TIMEOUT ms
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                boolean connected = appState.getScanner().getConnectionStatus() == BluetoothCom.BLUETOOTH_STATUS.CONNECTED;
                Log.d(LaunchActivity.this, String.format(Locale.UK,
                        "TIMEOUT, apiKey validated: %s, connected to scanner: %s",
                        validApiKey ? "YES" : "NO", connected ? "YES" : "NO"));

                if (!validApiKey || !connected) {
                    if (connected) {
                        appState.getScanner().disconnect();
                    }
                    // The user can't do anything anyway, so unexpected error
                    launchAlert(ALERT_TYPE.UNEXPECTED_ERROR);
                }
            }
        }, CONNECTION_AND_VALIDATION_TIMEOUT);
        Log.d(this, "Timeout set");
    }

    private void continueIfReady() {
        boolean connected = appState.getScanner().getConnectionStatus() == BluetoothCom.BLUETOOTH_STATUS.CONNECTED;
        Log.d(LaunchActivity.this, String.format(Locale.UK,
                "continueIfReady, apiKey validated: %s, connected to scanner: %s",
                validApiKey ? "YES" : "NO", connected ? "YES" : "NO"));

        if (validApiKey && connected) {
            handler.removeCallbacksAndMessages(null);
            appState.getScanner().un20Wakeup();
        }
    }

    private void finishWith(final int resultCode) {
        // The activity must last at least MINIMUM_DISPLAY_DURATION
        // to avoid disorienting the user.
        final long remainingTime = Math.max(0, minEndTime - SystemClock.elapsedRealtime());
        Log.d(this, String.format(Locale.UK, "Waiting %d ms to finish with result code %d",
                resultCode, remainingTime));

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Log.d(LaunchActivity.this, String.format(Locale.UK,
                        "Finishing with result code %d", resultCode));
                setResult(resultCode);
                finish();
            }
        }, remainingTime);
    }
    
    private void launch(@NonNull final Intent intent) {
        // The activity must last at least MINIMUM_DISPLAY_DURATION
        // to avoid disorienting the user.
        final long remainingTime = Math.max(0, minEndTime - SystemClock.elapsedRealtime());
        Log.d(this, String.format(Locale.UK, "Waiting %d ms to start child activity %s",
                remainingTime, intent.getAction()));

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Log.d(LaunchActivity.this, String.format(Locale.UK,
                        "Starting child activity %s", intent.getAction()));
                childActivityLaunched = true;
                startActivity(intent);
            }
        }, remainingTime);
        
    }

    private void launchAlert(ALERT_TYPE alertType) {
        Intent intent = new Intent(this, AlertActivity.class);
        intent.putExtra(InternalConstants.ALERT_TYPE_EXTRA, alertType);
        launch(intent);
    }


    @Override
    public void onScannerEvent(com.simprints.libscanner.EVENT event) {
        Log.d(this, String.format(Locale.UK,
                "onScannerEvent %s, %s", event.name(), event.details()));

        switch (event) {
            case CONNECTION_SUCCESS:
            case CONNECTION_ALREADY_CONNECTED:
                continueIfReady();
                break;

            case CONNECTION_BLUETOOTH_DISABLED:
                launchAlert(ALERT_TYPE.BLUETOOTH_NOT_ENABLED);
                break;
            case CONNECTION_SCANNER_UNBONDED:
                launchAlert(ALERT_TYPE.BLUETOOTH_UNBONDED_SCANNER);
                break;
            case NOT_CONNECTED:
            case NO_RESPONSE:
            case SEND_REQUEST_IO_ERROR:
            case CONNECTION_IO_ERROR:
            case CONNECTION_BAD_SCANNER_FEATURE:
            case UN20_WAKEUP_FAILURE:
            case SCANNER_BUSY:
            case UN20_CANNOT_CHECK_STATE:
                launchAlert(ALERT_TYPE.UNEXPECTED_ERROR);
                break;

            case UN20_WAKEUP_SUCCESS:
            case UN20_WAKEUP_INVALID_STATE:
                Intent intent = new Intent(this, ConsentActivity.class);
                launch(intent);
                break;
        }
    }

    @Override
    public void onDataEvent(EVENT event) {
        Log.d(this, String.format(Locale.UK, "onDataEvent %s, %s", event.name(), event.details()));

        switch (event) {
            case API_KEY_VALID:
                validApiKey = true;
                continueIfReady();
                break;

            case API_KEY_INVALID:
                validApiKey = false;
                launchAlert(ALERT_TYPE.INVALID_API_KEY);
                break;

            case NETWORK_FAILURE:
                launchAlert(ALERT_TYPE.NETWORK_FAILURE);
                break;
        }
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.d(this, String.format(Locale.UK,
                "onRestart, returning from child activity ? %s, resultCode = %d, resultData = %s",
                childActivityLaunched ? "yes" : "no",
                appState.getResultCode(), appState.getResultData()));

        // If just went back from a child activity
        if (childActivityLaunched) {
            if (appState.getResultCode() == InternalConstants.RESULT_TRY_AGAIN) {
                validateAndConnect();
            } else {
                setResult(appState.getResultCode(), appState.getResultData());
                finish();
            }
        }

    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        Log.d(this, String.format(Locale.UK, "onKeyDown, keyCode %d", keyCode));
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            setResult(RESULT_CANCELED);
            finish();
            return true;
        } else {
            return super.onKeyDown(keyCode, event);
        }
    }


    @Override
    public void onDestroy() {
        handler.removeCallbacksAndMessages(null);
        Scanner scanner = appState.getScanner();
        if (scanner != null) {
            scanner.destroy();
            appState.setScanner(null);
        }
        super.onDestroy();
    }

}
