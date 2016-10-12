package com.simprints.id.activities;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.appsee.Appsee;
import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.CustomEvent;
import com.facebook.stetho.Stetho;
import com.simprints.id.R;
import com.simprints.id.backgroundSync.SyncSetup;
import com.simprints.id.tools.AppState;
import com.simprints.id.tools.InternalConstants;
import com.simprints.id.tools.Language;
import com.simprints.id.tools.Log;
import com.simprints.id.tools.PermissionManager;
import com.simprints.id.tools.PositionTracker;
import com.simprints.libdata.DatabaseContext;
import com.simprints.libdata.DatabaseEventListener;
import com.simprints.libdata.Event;
import com.simprints.libscanner.BluetoothCom;
import com.simprints.libscanner.Scanner;
import com.simprints.libsimprints.Constants;

import java.util.List;
import java.util.Locale;
import java.util.UUID;

import io.fabric.sdk.android.Fabric;
import io.fabric.sdk.android.services.concurrency.AsyncTask;

public class LaunchActivity extends AppCompatActivity
        implements Scanner.ScannerListener, DatabaseEventListener {

    private final static int MINIMUM_DISPLAY_DURATION = 2500;
    private final static int CONNECTION_AND_VALIDATION_TIMEOUT = 15000;
    private static Handler handler;
    boolean waitingForConfirmation;
    boolean finishing;
    ProgressBar progressBar;
    TextView confirmConsentTextView;
    private AppState appState;
    private PositionTracker positionTracker;
    private String callingPackage;
    private boolean isDataReady;
    private long minEndTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getBaseContext().getResources().updateConfiguration(Language.selectLanguage(
                getApplicationContext()), getBaseContext().getResources().getDisplayMetrics());
        setContentView(R.layout.activity_launch);
        Fabric.with(this, new Crashlytics());
        Appsee.start(getString(R.string.com_appsee_apikey));
        Stetho.initializeWithDefaults(this);

        appState = AppState.getInstance();
        positionTracker = new PositionTracker(this);
        positionTracker.start();
        handler = new Handler();

        callingPackage = null;
        isDataReady = false;
        minEndTime = SystemClock.elapsedRealtime() + MINIMUM_DISPLAY_DURATION;
        waitingForConfirmation = false;
        finishing = false;

        progressBar = (ProgressBar) findViewById(R.id.progress_bar);
        confirmConsentTextView = (TextView) findViewById(R.id.confirm_consent_text_view);

        Bundle extras = getIntent().getExtras();
        if (extras == null) {
            Log.d(this, "finishing with SIMPRINTS_INVALID_API_KEY");
            finishWith(Constants.SIMPRINTS_INVALID_API_KEY, null);
            Answers.getInstance().logCustom(new CustomEvent("Missing API Key"));
            return;
        }

        switch (getIntent().getAction()) {
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
            Log.d(this, "finishing with SIMPRINTS_INVALID_API_KEY");
            finishWith(Constants.SIMPRINTS_INVALID_API_KEY, null);
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

        // Initializes the session Data object
        initDatabase(appState.getApiKey());

        //Start the background sync service in case it has failed for some reason
        new SyncSetup(getApplicationContext()).initialize();

        //Check the android permissions
        PermissionManager.requestPermissions(LaunchActivity.this);

        backgroundConnect();
    }

    private void initDatabase(String apiKey) {
        DatabaseContext.initActiveAndroid(getApplicationContext());

        SharedPreferences sharedPref = getApplicationContext().getSharedPreferences(
                getApplicationContext().getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        int dbVersion = sharedPref.getInt(getApplicationContext().getString(R.string.db_version_int), 0);

        if (dbVersion == 0) {
            DatabaseContext.reset(getApplicationContext());

            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putInt(getString(R.string.db_version_int), InternalConstants.DATABASE_VERSION_NUMBER);
            editor.apply();
        }

        appState.setData(new DatabaseContext(apiKey, getApplicationContext(), this));
        appState.getData().validateApiKey();
    }

    private void backgroundConnect() {
        new AsyncTask<Void, Void, Boolean>() {
            @Override
            protected Boolean doInBackground(Void... voids) {
                // Initializes the session Scanner object if necessary
                if (appState.getScanner() == null) {
                    BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
                    if (adapter == null) {
                        launchAlert(ALERT_TYPE.BLUETOOTH_NOT_SUPPORTED);
                        return false;
                    }
                    if (!adapter.isEnabled()) {
                        launchAlert(ALERT_TYPE.BLUETOOTH_NOT_ENABLED);
                        return false;
                    }
                    List<String> pairedScanners = Scanner.getPairedScanners();
                    if (pairedScanners.size() == 0) {
                        launchAlert(ALERT_TYPE.NOT_PAIRED);
                        return false;
                    }
                    if (pairedScanners.size() > 1) {
                        launchAlert(ALERT_TYPE.MULTIPLE_PAIRED_SCANNERS);
                        return false;
                    }
                    String macAddress = pairedScanners.get(0);
                    appState.setMacAddress(macAddress);
                }
                return true;
            }

            @Override
            protected void onPostExecute(Boolean result) {
                if (result) {
                    appState.setScanner(new Scanner(appState.getMacAddress()));
                    // Initiate scanner connection
                    Log.d(LaunchActivity.this, "Initiating scanner connection");
                    appState.getScanner().setScannerListener(LaunchActivity.this);
                    appState.getScanner().connect();
                    setupTimeOut();
                }
            }
        }.execute();
    }

    private void setupTimeOut() {
        // Program a timeout event after CONNECTION_AND_VALIDATION_TIMEOUT ms
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                boolean connected = appState.getScanner().getConnectionStatus() == BluetoothCom.BLUETOOTH_STATUS.CONNECTED;
                Log.d(LaunchActivity.this, String.format(Locale.UK,
                        "TIMEOUT, Data object ready: %s, connected to scanner: %s",
                        isDataReady ? "YES" : "NO", connected ? "YES" : "NO"));

                if (!isDataReady || !connected) {
                    // The user can't do anything anyway, so unexpected error
                    launchAlert(ALERT_TYPE.UNEXPECTED_ERROR);
                }
            }
        }, CONNECTION_AND_VALIDATION_TIMEOUT);
        Log.d(this, "Timeout set");
    }

    private void continueIfReady() {
        Scanner scanner = appState.getScanner();
        boolean connected = (scanner != null &&
                scanner.getConnectionStatus() == BluetoothCom.BLUETOOTH_STATUS.CONNECTED);
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

    private void launch(@NonNull final Intent intent, boolean delayed) {
        handler.removeCallbacksAndMessages(null);
        // The activity must last at least MINIMUM_DISPLAY_DURATION
        // Before throwing an alert screen to avoid disorienting the user.
        if (delayed) {
            final long remainingTime = Math.max(0, minEndTime - SystemClock.elapsedRealtime());
            Log.d(this, String.format(Locale.UK, "Waiting %d ms to start child activity %s",
                    remainingTime, intent.getAction()));
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    Log.d(LaunchActivity.this, String.format(Locale.UK,
                            "Starting child activity %s", intent.getAction()));
                    startActivityForResult(intent, 1);
                }
            }, remainingTime);
        } else {
            Log.d(LaunchActivity.this, String.format(Locale.UK,
                    "Starting child activity %s", intent.getAction()));
            startActivityForResult(intent, 1);
        }

    }

    private void launchAlert(ALERT_TYPE alertType) {
        if (appState.getScanner() != null) {
            appState.getScanner().destroy();
            appState.setScanner(null);
        }
        handler.removeCallbacksAndMessages(null);
        Intent intent = new Intent(this, AlertActivity.class);
        intent.putExtra(InternalConstants.ALERT_TYPE_EXTRA, alertType);
        launch(intent, true);
    }

    private void waitForConfirmation() {
        waitingForConfirmation = true;
        progressBar.setVisibility(View.GONE);
        confirmConsentTextView.setVisibility(View.VISIBLE);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (waitingForConfirmation) {
            waitingForConfirmation = false;
            launch(new Intent(LaunchActivity.this, MainActivity.class), false);
            return true;
        } else {
            return super.onTouchEvent(event);
        }
    }

    private void resolveCommCareDatabase() {
        if (ContextCompat.checkSelfPermission(this, "org.commcare.dalvik.provider.cases.read")
                == PackageManager.PERMISSION_GRANTED) {
            appState.getData().resolveCommCare(getContentResolver());
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[],
                                           @NonNull int[] grantResults) {

        //If a permission is denied fail out safely
        for (int permissionResult : grantResults) {
            if (permissionResult == -1) {
                waitingForConfirmation = false;
                finishing = true;
                setResult(RESULT_CANCELED);
                finish();
            }
        }

        positionTracker.onRequestPermissionsResult(requestCode, permissions, grantResults);
        resolveCommCareDatabase();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(this, String.format(Locale.UK,
                "onActivityResult, resultCode = %d", resultCode));

        if (requestCode == InternalConstants.RESOLUTION_REQUEST || requestCode == InternalConstants.GOOGLE_SERVICE_UPDATE_REQUEST) {
            positionTracker.onActivityResult(requestCode, resultCode, data);
        } else {
            if (resultCode == InternalConstants.RESULT_TRY_AGAIN) {
                progressBar.setVisibility(View.VISIBLE);
                confirmConsentTextView.setVisibility(View.GONE);
                minEndTime = SystemClock.elapsedRealtime() + MINIMUM_DISPLAY_DURATION;
                finishing = false;
                backgroundConnect();
            } else {
                finishWith(0, data);
            }
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onBackPressed() {
        // Neutralize back press
        if (waitingForConfirmation) {
            waitingForConfirmation = false;
            finishing = true;
            setResult(RESULT_CANCELED);
            finish();
            super.onBackPressed();
        }
    }

    @Override
    public void onDestroy() {
        Log.d(this, "onDestroy");
        handler.removeCallbacksAndMessages(null);
        if (appState.getData() != null && appState.getReadyToSendSession() != null) {
            appState.getData().saveSession(appState.getReadyToSendSession());
        }
        positionTracker.finish();
        android.util.Log.d(this.getLocalClassName(), "finishing = " + finishing);
        if (finishing && appState.getScanner() != null) {
            appState.getScanner().destroy();
            appState.setScanner(null);
        }
        super.onDestroy();
    }

    @Override
    public void onDataEvent(Event event) {
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
            case API_KEY_UNVERIFIED:
            case API_KEY_INVALID:
                launchAlert(ALERT_TYPE.INVALID_API_KEY);
                Answers.getInstance().logCustom(new CustomEvent("Invalid API Key")
                        .putCustomAttribute("API Key", appState.getApiKey()));
                break;
            case DATABASE_RESOLVED:
                isDataReady = true;
                continueIfReady();
                break;
        }
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
                launchAlert(ALERT_TYPE.DISCONNECTED);
                break;

            case NOT_CONNECTED:
            case NO_RESPONSE:
            case SEND_REQUEST_IO_ERROR:
            case CONNECTION_IO_ERROR:
            case CONNECTION_BAD_SCANNER_FEATURE:
            case UN20_WAKEUP_FAILURE:
            case SCANNER_BUSY:
            case UN20_CANNOT_CHECK_STATE:
            case SET_UI_FAILURE:
            case UPDATE_SENSOR_INFO_FAILURE:
                launchAlert(ALERT_TYPE.UNEXPECTED_ERROR);
                break;

            case UN20_WAKEUP_SUCCESS:
            case UN20_WAKEUP_INVALID_STATE:
                appState.getScanner().setUI(true, null, (short) -1);
                break;

            case SET_UI_SUCCESS:
                appState.getScanner().updateSensorInfo();
                break;

            case UPDATE_SENSOR_INFO_SUCCESS:
                appState.setHardwareVersion(appState.getScanner().getUcVersion());
                waitForConfirmation();
                break;

            case TRIGGER_PRESSED:
                if (waitingForConfirmation) {
                    launch(new Intent(LaunchActivity.this, MainActivity.class), false);
                }
                break;
        }
    }
}
