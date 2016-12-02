package com.simprints.id.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;

import com.appsee.Appsee;
import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.CustomEvent;
import com.simprints.id.LaunchProcess;
import com.simprints.id.R;
import com.simprints.id.backgroundSync.SyncSetup;
import com.simprints.id.tools.AppState;
import com.simprints.id.tools.Language;
import com.simprints.id.tools.PositionTracker;
import com.simprints.libdata.DatabaseEventListener;
import com.simprints.libdata.Event;
import com.simprints.libscanner.Scanner;
import com.simprints.libsimprints.Constants;

import java.util.UUID;

import io.fabric.sdk.android.Fabric;

import static com.simprints.id.tools.InternalConstants.ALERT_ACTIVITY_REQUEST;
import static com.simprints.id.tools.InternalConstants.ALERT_TYPE_EXTRA;
import static com.simprints.id.tools.InternalConstants.COMMCARE_PACKAGE;
import static com.simprints.id.tools.InternalConstants.COMMCARE_PERMISSION;
import static com.simprints.id.tools.InternalConstants.GOOGLE_SERVICE_UPDATE_REQUEST;
import static com.simprints.id.tools.InternalConstants.LOCATION_PERMISSION_REQUEST;
import static com.simprints.id.tools.InternalConstants.MAIN_ACTIVITY_REQUEST;
import static com.simprints.id.tools.InternalConstants.RESOLUTION_REQUEST;
import static com.simprints.id.tools.InternalConstants.RESULT_TRY_AGAIN;

public class LaunchActivity extends AppCompatActivity
        implements Scanner.ScannerListener, DatabaseEventListener {

    private final static int MINIMUM_DISPLAY_DURATION = 2500;
    public static Handler handler;
    public boolean waitingForConfirmation;

    public AppState appState;
    private PositionTracker positionTracker;
    private String callingPackage;
    private long minEndTime;

    private LaunchProcess launchProcess;

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

        Bundle extras = getIntent().getExtras();
        if (extras == null) {
            finishWith(Constants.SIMPRINTS_INVALID_API_KEY, null, true);
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
                finishWith(Constants.SIMPRINTS_INVALID_INTENT_ACTION, null, true);
                return;
        }

        // Sets apiKey
        String apiKey = extras.getString(Constants.SIMPRINTS_API_KEY);
        if (apiKey == null) {
            finishWith(Constants.SIMPRINTS_INVALID_API_KEY, null, true);
            Answers.getInstance().logCustom(new CustomEvent("Missing API Key"));
            return;
        }
        appState.setApiKey(apiKey);

        // Sets guid (to specified value, or random one)
        String guid = extras.getString(Constants.SIMPRINTS_GUID);
        if (guid == null) {
            guid = UUID.randomUUID().toString();
        }
        appState.setGuid(guid);

        // Sets userId
        String userId = extras.getString(Constants.SIMPRINTS_USER_ID);
        appState.setUserId(userId);

        // Sets deviceId
        String deviceId = extras.getString(Constants.SIMPRINTS_DEVICE_ID);
        appState.setDeviceId(deviceId);

        // Sets calling package
        callingPackage = extras.getString(Constants.SIMPRINTS_CALLING_PACKAGE);

        //Start the background sync service in case it has failed for some reason
        new SyncSetup(getApplicationContext()).initialize();

        //Start the launching process
        launchProcess = new LaunchProcess(callingPackage, this);
        launchProcess.launch();
    }

    private void finishWith(final int resultCode, final Intent resultData, boolean delay) {
        handler.removeCallbacksAndMessages(null);

        // The activity must last at least MINIMUM_DISPLAY_DURATION
        // to avoid disorienting the user.
        long remainingTime = 0;
        if (delay) {
            remainingTime = Math.max(0, minEndTime - SystemClock.elapsedRealtime());
        }

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                setResult(resultCode, resultData);
                waitingForConfirmation = false;
                finish();
            }
        }, remainingTime);
    }

    private void launch(@NonNull final Intent intent, boolean delayed) {
        handler.removeCallbacksAndMessages(null);

        // The activity must last at least MINIMUM_DISPLAY_DURATION
        // Before throwing an alert screen to avoid disorienting the user.
        long remainingTime = 0;
        if (delayed) {
            remainingTime = Math.max(0, minEndTime - SystemClock.elapsedRealtime());
        }

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                startActivityForResult(intent, MAIN_ACTIVITY_REQUEST);
            }
        }, remainingTime);

    }

    public void launchAlert(ALERT_TYPE alertType) {
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
                    finishWith(RESULT_CANCELED, null, false);
                    return;
                } else {
                    if (callingPackage != null && callingPackage.equalsIgnoreCase(COMMCARE_PACKAGE)) {
                        finishWith(RESULT_CANCELED, null, false);
                        return;
                    }
                }
            }
        }

        launchProcess.permissions = true;
        launchProcess.launch();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

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
                        minEndTime = SystemClock.elapsedRealtime() + MINIMUM_DISPLAY_DURATION;

                        if (appState.getScanner() != null) {
                            appState.getScanner().destroy();
                            appState.setScanner(null);
                        }

                        launchProcess = new LaunchProcess(callingPackage, this);
                        launchProcess.launch();
                        break;

                    case RESULT_OK:
                    case RESULT_CANCELED:
                        finishWith(resultCode, data, false);
                        break;
                }
                break;
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onBackPressed() {
        finishWith(RESULT_CANCELED, null, false);
        super.onBackPressed();
    }

    @Override
    public void onDestroy() {
        handler.removeCallbacksAndMessages(null);
        if (appState.getData() != null && appState.getReadyToSendSession() != null) {
            appState.getData().saveSession(appState.getReadyToSendSession());
        }
        positionTracker.finish();

        try {
            appState.getScanner().destroy();
            appState.setScanner(null);
        } catch (Exception ignored) {
        }

        super.onDestroy();
    }

    @Override
    public void onDataEvent(Event event) {
        switch (event) {
            case API_KEY_VALID:
                launchProcess.apiKey = true;
                launchProcess.dataLunch();
                break;
            case API_KEY_UNVERIFIED:
                launchAlert(ALERT_TYPE.UNVERIFIED_API_KEY);
                break;
            case API_KEY_INVALID:
                launchAlert(ALERT_TYPE.INVALID_API_KEY);
                break;
            case DATABASE_RESOLVED:
                launchProcess.ccResolver = true;
                launchProcess.dataLunch();
                break;
        }
    }

    @Override
    public void onScannerEvent(com.simprints.libscanner.EVENT event) {

        switch (event) {
            case CONNECTION_SUCCESS:
            case CONNECTION_ALREADY_CONNECTED:
                launchProcess.btConnection = true;
                launchProcess.scannerLaunch();
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

            case SEND_REQUEST_SUCCESS:
                break;

            case DISCONNECTION_IO_ERROR:
            case SET_SENSOR_CONFIG_FAILURE:
            case PAIR_FAILURE:
            case UN20_SHUTDOWN_INVALID_STATE:
            case UN20_SHUTDOWN_FAILURE:
            case EXTRACT_CRASH_LOG_FAILURE:
            case SET_HARDWARE_CONFIG_INVALID_STATE:
            case SET_HARDWARE_CONFIG_INVALID_CONFIG:
            case SET_HARDWARE_CONFIG_FAILURE:
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
                launchProcess.un20WakeUp = true;
                launchProcess.scannerLaunch();
                break;

            case TRIGGER_PRESSED:
                if (waitingForConfirmation) {
                    launch(new Intent(LaunchActivity.this, MainActivity.class), false);
                }
                break;
        }
    }
}
