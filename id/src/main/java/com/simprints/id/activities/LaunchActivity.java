package com.simprints.id.activities;

import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.appsee.Appsee;
import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.CustomEvent;
import com.simprints.id.R;
import com.simprints.id.backgroundSync.SyncSetup;
import com.simprints.id.tools.AppState;
import com.simprints.id.tools.Language;
import com.simprints.id.tools.Log;
import com.simprints.id.tools.PermissionManager;
import com.simprints.id.tools.PositionTracker;
import com.simprints.id.tools.SharedPrefHelper;
import com.simprints.libdata.DatabaseContext;
import com.simprints.libdata.DatabaseEventListener;
import com.simprints.libdata.Event;
import com.simprints.libscanner.Scanner;
import com.simprints.libsimprints.Constants;

import java.util.List;
import java.util.Locale;
import java.util.UUID;

import io.fabric.sdk.android.Fabric;

import static com.simprints.id.tools.InternalConstants.ALERT_ACTIVITY_REQUEST;
import static com.simprints.id.tools.InternalConstants.ALERT_TYPE_EXTRA;
import static com.simprints.id.tools.InternalConstants.COMMCARE_PACKAGE;
import static com.simprints.id.tools.InternalConstants.COMMCARE_PERMISSION;
import static com.simprints.id.tools.InternalConstants.DATABASE_VERSION_NUMBER;
import static com.simprints.id.tools.InternalConstants.GOOGLE_SERVICE_UPDATE_REQUEST;
import static com.simprints.id.tools.InternalConstants.LOCATION_PERMISSION_REQUEST;
import static com.simprints.id.tools.InternalConstants.MAIN_ACTIVITY_REQUEST;
import static com.simprints.id.tools.InternalConstants.RESOLUTION_REQUEST;
import static com.simprints.id.tools.InternalConstants.RESULT_TRY_AGAIN;

public class LaunchActivity extends AppCompatActivity
        implements Scanner.ScannerListener, DatabaseEventListener {

    private final static int MINIMUM_DISPLAY_DURATION = 2500;
    private final static int CONNECTION_AND_VALIDATION_TIMEOUT = 15000;
    private static Handler handler;
    boolean waitingForConfirmation;
    boolean finishing;
    ProgressBar progressBar;
    TextView confirmConsentTextView;
    TextView loadingInfoTextView;
    private AppState appState;
    private PositionTracker positionTracker;
    private String callingPackage;
    private long minEndTime;

    /**
     * Launch booleans
     */
    Boolean apiKey = false;
    Boolean ccResolver = false;
    Boolean btConnection = false;
    Boolean un20WakeUp = false;
    Boolean permissions = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getBaseContext().getResources().updateConfiguration(Language.selectLanguage(
                getApplicationContext()), getBaseContext().getResources().getDisplayMetrics());
        setContentView(R.layout.activity_launch);
        Fabric.with(this, new Crashlytics());
        Appsee.start(getString(R.string.com_appsee_apikey));

        appState = AppState.getInstance();
        positionTracker = new PositionTracker(this);
        positionTracker.start();
        handler = new Handler();

        callingPackage = null;
        minEndTime = SystemClock.elapsedRealtime() + MINIMUM_DISPLAY_DURATION;
        waitingForConfirmation = false;
        finishing = false;

        progressBar = (ProgressBar) findViewById(R.id.progress_bar);
        confirmConsentTextView = (TextView) findViewById(R.id.confirm_consent_text_view);
        loadingInfoTextView = (TextView) findViewById(R.id.tv_loadingInfo);

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


        //Start the background sync service in case it has failed for some reason
        new SyncSetup(getApplicationContext()).initialize();

        //Start the launching process
        launch();
    }

    private void launch() {
        if (!permissions) {
            loadingInfoTextView.setText(R.string.launch_checking_permissions);
            Boolean permReady = PermissionManager.requestPermissions(LaunchActivity.this);

            if (!permReady) {
                return;
            } else {
                permissions = true;
            }
        }

        if (!apiKey) {
            loadingInfoTextView.setText(R.string.launch_loading_database);
            DatabaseContext.initActiveAndroid(getApplicationContext());

            SharedPrefHelper sharedPrefHelper = new SharedPrefHelper(getApplicationContext());
            int dbVersion = sharedPrefHelper.getDbVersionInt();
            if (dbVersion == 0) {
                DatabaseContext.reset(getApplicationContext());

                sharedPrefHelper.setDbVersionInt(DATABASE_VERSION_NUMBER);
            }

            appState.setData(new DatabaseContext(appState.getApiKey(), getApplicationContext(), this));

            loadingInfoTextView.setText(R.string.launch_checking_api_key);
            appState.getData().validateApiKey();

            return;
        }

        if (!ccResolver) {
            if (COMMCARE_PACKAGE.equals(callingPackage)) {
                loadingInfoTextView.setText(R.string.launch_cc_resolve);
                appState.getData().resolveCommCare(getContentResolver());
                return;
            }
        }

        if (!btConnection) {
            setupTimeOut();

            loadingInfoTextView.setText(R.string.launch_bt_connect);
            btConnect();
            return;
        }

        if (!un20WakeUp) {
            handler.removeCallbacksAndMessages(null);
            appState.getScanner().un20Wakeup();
            loadingInfoTextView.setText(R.string.launch_wake_un20);
            return;
        }

        appState.setHardwareVersion(appState.getScanner().getUcVersion());

        waitingForConfirmation = true;
        progressBar.setVisibility(View.GONE);
        confirmConsentTextView.setVisibility(View.VISIBLE);
        loadingInfoTextView.setVisibility(View.INVISIBLE);
    }

    private void btConnect() {
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
        }

        appState.setScanner(new Scanner(appState.getMacAddress()));

        // Initiate scanner connection
        Log.d(LaunchActivity.this, "Initiating scanner connection");
        appState.getScanner().setScannerListener(LaunchActivity.this);
        appState.getScanner().connect();
    }

    private void setupTimeOut() {
        // Program a timeout event after CONNECTION_AND_VALIDATION_TIMEOUT ms
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                // The user can't do anything anyway, so unexpected error
                launchAlert(ALERT_TYPE.UNEXPECTED_ERROR);
            }
        }, CONNECTION_AND_VALIDATION_TIMEOUT);
    }

    private void finishWith(final int resultCode, final Intent resultData) {
        // The activity must last at least MINIMUM_DISPLAY_DURATION
        // to avoid disorienting the user.
        final long remainingTime = Math.max(0, minEndTime - SystemClock.elapsedRealtime());

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                handler.removeCallbacksAndMessages(null);
                setResult(resultCode, resultData);
                waitingForConfirmation = false;
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
                    startActivityForResult(intent, MAIN_ACTIVITY_REQUEST);
                }
            }, remainingTime);
        } else {
            Log.d(LaunchActivity.this, String.format(Locale.UK,
                    "Starting child activity %s", intent.getAction()));
            startActivityForResult(intent, MAIN_ACTIVITY_REQUEST);
        }

    }

    private void launchAlert(ALERT_TYPE alertType) {
        if (appState.getScanner() != null) {
            appState.getScanner().destroy();
            appState.setScanner(null);
        }
        handler.removeCallbacksAndMessages(null);
        Intent intent = new Intent(this, AlertActivity.class);
        intent.putExtra(ALERT_TYPE_EXTRA, alertType);
        launch(intent, true);
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

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String rtnPermissions[],
                                           @NonNull int[] grantResults) {
        positionTracker.onRequestPermissionsResult(requestCode, rtnPermissions, grantResults);

        if (requestCode == 11)
            return;

        for (int x = 0; x < rtnPermissions.length; x++) {
            if (grantResults[x] == -1) {
                if (!rtnPermissions[x].equalsIgnoreCase(COMMCARE_PERMISSION)) {
                    finishWith(RESULT_CANCELED, null);
                    return;
                } else {
                    if (callingPackage != null && callingPackage.equalsIgnoreCase(COMMCARE_PACKAGE)) {
                        finishWith(RESULT_CANCELED, null);
                        return;
                    }
                }
            }
        }

        permissions = true;
        launch();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(this, String.format(Locale.UK,
                "onActivityResult, resultCode = %d, requestCode = %d", resultCode, requestCode));

        switch (requestCode) {
            case LOCATION_PERMISSION_REQUEST:
                break;
            case RESOLUTION_REQUEST:
            case GOOGLE_SERVICE_UPDATE_REQUEST:
                positionTracker.onActivityResult(requestCode, resultCode, data);
                break;
            case MAIN_ACTIVITY_REQUEST:
            case ALERT_ACTIVITY_REQUEST:
                switch (resultCode) {
                    case RESULT_TRY_AGAIN:
                        progressBar.setVisibility(View.VISIBLE);
                        confirmConsentTextView.setVisibility(View.GONE);
                        minEndTime = SystemClock.elapsedRealtime() + MINIMUM_DISPLAY_DURATION;
                        finishing = false;
                        launch();
                        break;

                    case RESULT_OK:
                    case RESULT_CANCELED:
                        finishWith(resultCode, data);
                        break;
                }
                break;
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
                apiKey = true;
                launch();
                break;
            case API_KEY_UNVERIFIED:
            case API_KEY_INVALID:
                launchAlert(ALERT_TYPE.INVALID_API_KEY);
                Answers.getInstance().logCustom(new CustomEvent("Invalid API Key")
                        .putCustomAttribute("API Key", appState.getApiKey()));
                break;
            case DATABASE_RESOLVED:
                ccResolver = true;
                launch();
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
                btConnection = true;
                launch();
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
                un20WakeUp = true;
                launch();
                break;

            case TRIGGER_PRESSED:
                if (waitingForConfirmation) {
                    launch(new Intent(LaunchActivity.this, MainActivity.class), false);
                }
                break;
        }
    }
}
