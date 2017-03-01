package com.simprints.id.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;

import com.crashlytics.android.Crashlytics;
import com.simprints.id.R;
import com.simprints.id.backgroundSync.SyncSetup;
import com.simprints.id.tools.Analytics;
import com.simprints.id.tools.AppState;
import com.simprints.id.tools.Language;
import com.simprints.id.tools.PositionTracker;
import com.simprints.id.tools.RemoteConfig;
import com.simprints.id.tools.SharedPref;
import com.simprints.id.tools.launch.LaunchProcess;
import com.simprints.libdata.DatabaseEventListener;
import com.simprints.libdata.Event;
import com.simprints.libscanner.ButtonListener;
import com.simprints.libscanner.ResultListener;
import com.simprints.libscanner.SCANNER_ERROR;
import com.simprints.libsimprints.Constants;

import java.util.UUID;

import io.fabric.sdk.android.Fabric;

import static android.provider.Settings.Secure;
import static com.simprints.id.tools.AppState.Callout;
import static com.simprints.id.tools.AppState.getInstance;
import static com.simprints.id.tools.InternalConstants.ALERT_ACTIVITY_REQUEST;
import static com.simprints.id.tools.InternalConstants.ALERT_TYPE_EXTRA;
import static com.simprints.id.tools.InternalConstants.COMMCARE_PACKAGE;
import static com.simprints.id.tools.InternalConstants.COMMCARE_PERMISSION;
import static com.simprints.id.tools.InternalConstants.GOOGLE_SERVICE_UPDATE_REQUEST;
import static com.simprints.id.tools.InternalConstants.LOCATION_PERMISSION_REQUEST;
import static com.simprints.id.tools.InternalConstants.MAIN_ACTIVITY_REQUEST;
import static com.simprints.id.tools.InternalConstants.REFUSAL_ACTIVITY_REQUEST;
import static com.simprints.id.tools.InternalConstants.RESOLUTION_REQUEST;
import static com.simprints.id.tools.InternalConstants.RESULT_TRY_AGAIN;

@SuppressWarnings("deprecation")
@SuppressLint("HardwareIds")
public class LaunchActivity extends AppCompatActivity implements DatabaseEventListener {

    public boolean waitingForConfirmation;
    public AppState appState;
    public Analytics analytics;
    private PositionTracker positionTracker;
    private LaunchProcess launchProcess;
    private boolean launchOutOfFocus;
    private ButtonListener scannerButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getBaseContext().getResources().updateConfiguration(Language.selectLanguage(
                getApplicationContext()), getBaseContext().getResources().getDisplayMetrics());
        setContentView(R.layout.activity_launch);
        Fabric.with(this, new Crashlytics());

        //initialize remote config
        RemoteConfig.init();

        analytics = Analytics.getInstance(getApplicationContext());
        appState = getInstance();

        String deviceID = Secure.getString(getApplicationContext().getContentResolver(),
                Secure.ANDROID_ID);
        appState.setDeviceId(deviceID);
        analytics.setDeviceId(appState.getDeviceId());

        //Set position tracker
        positionTracker = new PositionTracker(this);
        positionTracker.start();

        waitingForConfirmation = false;

        //Start the background sync service in case it has failed for some reason
        new SyncSetup(getApplicationContext()).initialize();

        scannerButton = new ButtonListener() {
            @Override
            public void onClick() {
                finishLaunch();
            }
        };

        //Validate the callout
        validateCalloutAndLaunch(getIntent());
    }

    public void finishWith(final int resultCode, final Intent resultData) {
        setResult(resultCode, resultData);
        waitingForConfirmation = false;
        finish();
    }

    public void launchAlert(ALERT_TYPE alertType) {
        if (launchOutOfFocus)
            return;

        launchOutOfFocus = true;

        Intent intent = new Intent(this, AlertActivity.class);
        intent.putExtra(ALERT_TYPE_EXTRA, alertType);
        startActivityForResult(intent, ALERT_ACTIVITY_REQUEST);
    }

    public void launchRefusal() {
        launchOutOfFocus = true;
        startActivityForResult(new Intent(this, RefusalActivity.class), REFUSAL_ACTIVITY_REQUEST);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (waitingForConfirmation) {
            finishLaunch();
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
                    String callPack = appState.getCallingPackage();
                    if (callPack != null && callPack.equalsIgnoreCase(COMMCARE_PACKAGE)) {
                        finishWith(RESULT_CANCELED, null);
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
                switch (resultCode) {
                    case RESULT_CANCELED:
                        launchRefusal();
                        break;
                    case RESULT_OK:
                        finishWith(resultCode, data);
                        break;
                }
                break;
            case ALERT_ACTIVITY_REQUEST:
            case REFUSAL_ACTIVITY_REQUEST:
                switch (resultCode) {
                    case RESULT_TRY_AGAIN:
                        launchOutOfFocus = false;

                        if (appState.getScanner() != null) {
                            appState.getScanner().disconnect(new ResultListener() {
                                @Override
                                public void onSuccess() {
                                    appState.setScanner(null);
                                    launchProcess = new LaunchProcess(LaunchActivity.this);
                                    launchProcess.launch();
                                }

                                @Override
                                public void onFailure(SCANNER_ERROR scanner_error) {
                                    appState.setScanner(null);
                                    launchProcess = new LaunchProcess(LaunchActivity.this);
                                    launchProcess.launch();
                                }
                            });

                        }
                        break;

                    default:
                        finishWith(resultCode, data);
                        break;
                }
                break;
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onBackPressed() {
        launchRefusal();
    }

    @Override
    public void onDestroy() {
        if (appState.getData() != null && appState.getReadyToSendSession() != null) {
            if (appState.getRefusalForm() != null)
                appState.getData().saveSession(appState.getReadyToSendSession(), appState.getRefusalForm());
            else
                appState.getData().saveSession(appState.getReadyToSendSession());
            appState.getData().destroy();
        }

        positionTracker.finish();

        if (appState.getScanner() != null) {
            appState.getScanner().disconnect(new ResultListener() {
                @Override
                public void onSuccess() {
                    appState.destroy();
                }

                @Override
                public void onFailure(SCANNER_ERROR scanner_error) {
                    appState.destroy();
                }
            });
            appState.setScanner(null);
        }

        super.onDestroy();
    }

    @Override
    public void onDataEvent(final Event event) {
        switch (event) {
            case API_KEY_VALID:
                launchProcess.apiKey = true;
                launchProcess.updateData();
                break;
            case API_KEY_UNVERIFIED:
                launchAlert(ALERT_TYPE.UNVERIFIED_API_KEY);
                break;
            case API_KEY_INVALID:
                launchAlert(ALERT_TYPE.INVALID_API_KEY);
                break;
            case DATABASE_INIT_SUCCESS:
                launchProcess.databaseUpdate = true;
                launchProcess.launch();
                break;
            case DATABASE_INIT_RESTART:
                if (appState.getData() != null) {
                    appState.getData().destroy();
                    appState.setData(null);
                }

                launchProcess = new LaunchProcess(this);
                launchProcess.launch();
                break;
            case DATABASE_RESOLVED:
                launchProcess.ccResolver = true;
                launchProcess.updateData();
                break;
            case CONNECTED:
                appState.setConnected(true);
                if (!appState.getSignedIn())
                    appState.getData().signIn();
                break;
            case DISCONNECTED:
                appState.setConnected(false);
                break;
            case SIGNED_IN:
                appState.setSignedIn(true);
                break;
            case SIGNED_OUT:
                appState.setSignedIn(false);
                break;
        }
    }

    public void finishLaunch() {
        appState.getScanner().unregisterButtonListener(scannerButton);
        waitingForConfirmation = false;
        analytics.setScannerMac(appState.getMacAddress());
        startActivityForResult(new Intent(LaunchActivity.this, MainActivity.class),
                MAIN_ACTIVITY_REQUEST);
    }

    public void setButton() {
        scannerButton = new ButtonListener() {
            @Override
            public void onClick() {
                finishLaunch();
            }
        };

        appState.getScanner().registerButtonListener(scannerButton);
    }

    private void validateCalloutAndLaunch(Intent intent) {

        // Check bundle
        Bundle extras = intent.getExtras();
        if (extras == null || extras.isEmpty()) {
            launchAlert(ALERT_TYPE.MISSING_API_KEY);
            return;
        }

        // Check action
        switch (intent.getAction()) {
            case Constants.SIMPRINTS_IDENTIFY_INTENT:
                appState.setCallout(Callout.IDENTIFY);
                break;
            case Constants.SIMPRINTS_REGISTER_INTENT:
                appState.setCallout(Callout.REGISTER);
                appState.setGuid(UUID.randomUUID().toString());
                break;
            case Constants.SIMPRINTS_UPDATE_INTENT:
                String updateId = extras.getString(Constants.SIMPRINTS_UPDATE_GUID);
                if (updateId == null || updateId.isEmpty()) {
                    launchAlert(ALERT_TYPE.MISSING_UPDATE_GUID);
                    return;
                }
                appState.setGuid(updateId);
                appState.setCallout(Callout.UPDATE);
                break;
            case Constants.SIMPRINTS_VERIFY_INTENT:
                String verifyId = extras.getString(Constants.SIMPRINTS_VERIFY_GUID);
                if (verifyId == null || verifyId.isEmpty()) {
                    launchAlert(ALERT_TYPE.MISSING_VERIFY_GUID);
                    return;
                }
                appState.setGuid(verifyId);
                appState.setCallout(Callout.VERIFY);
                break;
            default:
                finishWith(Constants.SIMPRINTS_INVALID_INTENT_ACTION, null);
                return;
        }
        analytics.setLogin(appState.getCallout());

        // Check apiKey
        String apiKey = extras.getString(Constants.SIMPRINTS_API_KEY);
        if (apiKey == null || apiKey.isEmpty()) {
            launchAlert(ALERT_TYPE.MISSING_API_KEY);
            return;
        }
        appState.setApiKey(apiKey);
        appState.setAppKey(apiKey.substring(0, 8));
        new SharedPref(getApplicationContext()).setAppKeyString(appState.getAppKey());

        // Check userId
        String userId = extras.getString(Constants.SIMPRINTS_USER_ID);
        if (userId == null || userId.isEmpty()) {
            launchAlert(ALERT_TYPE.MISSING_USER_ID);
            return;
        }
        appState.setUserId(userId);
        analytics.setUser(appState.getUserId(), appState.getApiKey());

        // Check moduleId
        String moduleId = extras.getString(Constants.SIMPRINTS_MODULE_ID);
        if (moduleId == null || moduleId.isEmpty()) {
            launchAlert(ALERT_TYPE.MISSING_MODULE_ID);
            return;
        }

        // Sets calling package
        appState.setCallingPackage(null);
        appState.setCallingPackage(extras.getString(Constants.SIMPRINTS_CALLING_PACKAGE));

        launchProcess = new LaunchProcess(LaunchActivity.this);
        launchProcess.launch();
    }
}
