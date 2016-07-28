package com.simprints.id.activities;

import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.appsee.Appsee;
import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.CustomEvent;
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

import io.fabric.sdk.android.Fabric;

public class LaunchActivity extends AppCompatActivity
        implements Scanner.ScannerListener, Data.DataListener/*,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener*/ {

    private final static int MINIMUM_DISPLAY_DURATION = 2500;
    private final static int CONNECTION_AND_VALIDATION_TIMEOUT = 10000;
    private final static int COMMCARE_PERMISSION_REQUEST = 0;

    private AppState appState;
    private static Handler handler;

    private String callingPackage;
    private boolean isDataReady;
    private long minEndTime;
    private boolean childActivityLaunched;
    boolean finishing;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launch);
        Fabric.with(this, new Crashlytics());
        Appsee.start(getString(R.string.com_appsee_apikey));

        appState = AppState.getInstance();
        handler = new Handler();
        isDataReady = false;
        minEndTime = SystemClock.elapsedRealtime() + MINIMUM_DISPLAY_DURATION;
        childActivityLaunched = false;
        finishing = false;
        callingPackage = null;

        Bundle extras = getIntent().getExtras();
        if (extras == null) {
            Log.d(this, "finishing with SIMPRINTS_INVALID_API_KEY");
            finishWith(Constants.SIMPRINTS_INVALID_API_KEY, null);
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
                finishWith(Constants.SIMPRINTS_INVALID_INTENT_ACTION, null);
                return;
        }

        // Sets apiKey
        String apiKey = extras.getString(Constants.SIMPRINTS_API_KEY);
        if (apiKey == null) {
            launchAlert(ALERT_TYPE.MISSING_API_KEY);
            Answers.getInstance().logCustom(new CustomEvent("Missing API Key"));
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

        // Sets calling package
        callingPackage = extras.getString(Constants.SIMPRINTS_CALLING_PACKAGE);
        Log.d(this, String.format(Locale.UK, "callingPackage = %s", callingPackage));


        // Initializes result
        appState.setResultCode(RESULT_CANCELED);
        appState.setResultData(new Intent(appState.isEnrol()
                ? Constants.SIMPRINTS_REGISTER_INTENT
                : Constants.SIMPRINTS_IDENTIFY_INTENT));

        /*// Initializes the google API client
        appState.setGoogleApiClient(
                new GoogleApiClient.Builder(this)
                        .addConnectionCallbacks(this)
                        .addOnConnectionFailedListener(this)
                        .addApi(LocationServices.API)
                        .build());*/

        // Initializes the session Data object
        appState.setData(new Data(getApplicationContext()));
        appState.getData().setDataListener(this);
        Log.d(this, "Data object initialised");
        Log.d(this, "Validating apiKey");
        appState.getData().validateApiKey(appState.getApiKey());




        connect();
    }

    private void connect() {
        // Initializes the session Scanner object if necessary
        if (appState.getScanner() == null) {
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
                launchAlert(ALERT_TYPE.NOT_PAIRED);
                return;
            }
            if (pairedScanners.size() > 1) {
                launchAlert(ALERT_TYPE.MULTIPLE_PAIRED_SCANNERS);
                return;
            }
            String macAddress = pairedScanners.get(0);
            appState.setMacAddress(macAddress);
            appState.setScanner(new Scanner(macAddress));
            Log.d(this, String.format("Scanner object initialised (MAC address = %s)",
                    macAddress));
        }

        // Initiate scanner connection and apiKey validation
        Log.d(this, "Initiating scanner connection");
        appState.getScanner().setScannerListener(this);
        appState.getScanner().connect();

        // Program a timeout event after CONNECTION_AND_VALIDATION_TIMEOUT ms
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                boolean connected = appState.getScanner().getConnectionStatus() == BluetoothCom.BLUETOOTH_STATUS.CONNECTED;
                Log.d(LaunchActivity.this, String.format(Locale.UK,
                        "TIMEOUT, Data object ready: %s, connected to scanner: %s",
                        isDataReady ? "YES" : "NO", connected ? "YES" : "NO"));

                if (!isDataReady || !connected) {
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
                "continueIfReady, Data object ready: %s, connected to scanner: %s",
                isDataReady ? "YES" : "NO", connected ? "YES" : "NO"));

        if (isDataReady && connected) {
            handler.removeCallbacksAndMessages(null);
            appState.getScanner().un20Wakeup();
        }
    }

    private void finishWith(final int resultCode, final Intent resultData) {
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
                setResult(resultCode, resultData);
                finishing = true;
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
        handler.removeCallbacksAndMessages(null);
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
                launchAlert(ALERT_TYPE.NOT_PAIRED);
                break;
            case CONNECTION_SCANNER_UNREACHABLE:
                launchAlert(ALERT_TYPE.SCANNER_UNREACHABLE);
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
                appState.getScanner().setUI(true, null, (short)-1);
                break;

            case SET_UI_SUCCESS:
                Intent intent = new Intent(this, ConsentActivity.class);
                launch(intent);
                break;

            case SET_UI_FAILURE:
                launchAlert(ALERT_TYPE.UNEXPECTED_ERROR);
                break;
        }
    }

    @Override
    public void onDataEvent(EVENT event) {
        Log.d(this, String.format(Locale.UK, "onDataEvent %s, %s", event.name(), event.details()));

        switch (event) {
            case API_KEY_VALID:
                if (appState.isEnrol()) {
                    Answers.getInstance().logCustom(new CustomEvent("Login")
                            .putCustomAttribute("API Key", appState.getApiKey())
                            .putCustomAttribute("Type", "Enrol"));
                } else {
                    Answers.getInstance().logCustom(new CustomEvent("Login")
                            .putCustomAttribute("API Key", appState.getApiKey())
                            .putCustomAttribute("Type", "Identify"));
                }
                if (InternalConstants.COMMCARE_PACKAGE.equals(callingPackage)) {
                    resolveCommCareDatabase();
                } else {
                    isDataReady = true;
                    continueIfReady();
                }
                break;

            case API_KEY_INVALID:
                launchAlert(ALERT_TYPE.INVALID_API_KEY);
                Answers.getInstance().logCustom(new CustomEvent("Invalid API Key")
                        .putCustomAttribute("API Key", appState.getApiKey()));
                break;

            case DATABASE_RESOLVER_FAILURE:
                Toast.makeText(this, "Warning: could not synchronize with CommCare",
                        Toast.LENGTH_LONG).show();
                Crashlytics.log(0, "CommCare DB resolution", "Failure");
            case DATABASE_RESOLVER_SUCCESS:
                isDataReady = true;
                continueIfReady();
                break;

            case NETWORK_FAILURE:
                launchAlert(ALERT_TYPE.NETWORK_FAILURE);
                break;
        }
    }

    private void resolveCommCareDatabase() {
        if (ContextCompat.checkSelfPermission(this, "org.commcare.dalvik.provider.cases.read")
                != PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(this,
                    new String[]{"org.commcare.dalvik.provider.cases.read"},
                    COMMCARE_PERMISSION_REQUEST);
        } else {
            appState.getData().commCareDatabaseResolver(getContentResolver(), appState.getApiKey());
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[],
                                           @NonNull int[] grantResults)
    {
        switch (requestCode) {
            case COMMCARE_PERMISSION_REQUEST:
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    resolveCommCareDatabase();
                } else {
                    Toast.makeText(this, "Warning: could not synchronize with CommCare",
                            Toast.LENGTH_LONG).show();
                    Crashlytics.log(0, "CommCare DB resolution", "No permission");
                    isDataReady = true;
                    continueIfReady();
                }
                break;
        }
    }

    /*@Override
    public void onConnected(@Nullable Bundle bundle) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    COMMCARE_PERMISSION_REQUEST);
        } else {
            appState.getData().commCareDatabaseResolver(getContentResolver(), appState.getApiKey());
        }
        Location lastLocation = LocationServices.FusedLocationApi.getLastLocation(
                appState.getGoogleApiClient());

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }*/

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
                minEndTime = SystemClock.elapsedRealtime() + MINIMUM_DISPLAY_DURATION;
                childActivityLaunched = false;
                finishing = false;
                connect();
            } else {
                finishWith(appState.getResultCode(), appState.getResultData());
            }
        }

    }


//    @Override
//    public boolean onKeyDown(int keyCode, KeyEvent event) {
//        Log.d(this, String.format(Locale.UK, "onKeyDown, keyCode %d", keyCode));
//        if (keyCode != KeyEvent.KEYCODE_BACK) {
//            return true;
//        } else {
//            return super.onKeyDown(keyCode, event);
//        }
//    }

    @Override
    public void onBackPressed() {
        // Neutralize back press
    }

    @Override
    public void onDestroy() {
        Log.d(this, "onDestroy");
        handler.removeCallbacksAndMessages(null);
        if (finishing && appState.getScanner() != null) {
            appState.getScanner().destroy();
            appState.setScanner(null);
        }
        super.onDestroy();
    }
}
