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
import com.simprints.id.model.ALERT_TYPE;
import com.simprints.id.tools.AppState;
import com.simprints.id.tools.Language;
import com.simprints.id.tools.PositionTracker;
import com.simprints.id.tools.RemoteConfig;
import com.simprints.id.controllers.Setup;
import com.simprints.libdata.DatabaseEventListener;
import com.simprints.libdata.Event;
import com.simprints.libscanner.ButtonListener;
import com.simprints.libscanner.ResultListener;
import com.simprints.libscanner.SCANNER_ERROR;

import io.fabric.sdk.android.Fabric;

import static com.simprints.id.tools.InternalConstants.ALERT_ACTIVITY_REQUEST;
import static com.simprints.id.tools.InternalConstants.ALERT_TYPE_EXTRA;
import static com.simprints.id.tools.InternalConstants.GOOGLE_SERVICE_UPDATE_REQUEST;
import static com.simprints.id.tools.InternalConstants.MAIN_ACTIVITY_REQUEST;
import static com.simprints.id.tools.InternalConstants.REFUSAL_ACTIVITY_REQUEST;
import static com.simprints.id.tools.InternalConstants.RESOLUTION_REQUEST;
import static com.simprints.id.tools.InternalConstants.RESULT_TRY_AGAIN;
import static com.simprints.id.tools.Vibrate.vibrate;

@SuppressWarnings("deprecation")
@SuppressLint("HardwareIds")
public class LaunchActivity extends AppCompatActivity implements DatabaseEventListener {

    // Application state (singleton containing scanner, database context, analytics, ...)
    private AppState appState = AppState.getInstance();

    // Position tracker, used to locate the user
    private PositionTracker positionTracker;

    // Scanner button callback
    private ButtonListener scannerButton = new ButtonListener() {
        @Override
        public void onClick() {
            if (!launchOutOfFocus) {
                finishLaunch();
            }
        }
    };

    // True iff the user confirmed consent
    private boolean consentConfirmed = false;

    // True iff the app is waiting for the user to confirm consent
    private boolean waitingForConfirmation = false;

    // True iff another activity launched by this activity is running
    private boolean launchOutOfFocus = false;


    private Setup setup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getBaseContext().getResources().updateConfiguration(Language.selectLanguage(
                getApplicationContext()), getBaseContext().getResources().getDisplayMetrics());
        setContentView(R.layout.activity_launch);
        Fabric.with(this, new Crashlytics());

        // Initialize remote config
        RemoteConfig.init();

        // Parse/verify callout, initialize app state
        ALERT_TYPE alert = appState.init(getIntent(), getApplicationContext());
        if (alert != null) {
            launchAlert(alert);
            return;
        }

        // Initialize position tracker
        positionTracker = new PositionTracker(this);
        positionTracker.start();

        // Start the background sync service in case it has failed for some reason
        new SyncSetup(getApplicationContext()).initialize();

        // Start the launch process
        setup = new Setup(LaunchActivity.this, new Runnable() {
            @Override
            public void run() {
                // If it is the first time the launch process finishes, wait for consent confirmation
                // Else, go directly to the main activity
                if (!consentConfirmed) {
                    waitingForConfirmation = true;
                    appState.getScanner().registerButtonListener(scannerButton);
                    vibrate(LaunchActivity.this, 100);
                } else {
                    finishLaunch();
                }
            }
        });
        setup.start();
    }

    /**
     * Start alert activity
     */
    public void launchAlert(ALERT_TYPE alertType) {
        if (launchOutOfFocus)
            return;

        launchOutOfFocus = true;
        setup.pause();
        Intent intent = new Intent(this, AlertActivity.class);
        intent.putExtra(ALERT_TYPE_EXTRA, alertType);
        startActivityForResult(intent, ALERT_ACTIVITY_REQUEST);
    }

    /**
     * Start refusal form activity
     */
    public void launchRefusal() {
        launchOutOfFocus = true;
        setup.pause();
        startActivityForResult(new Intent(this, RefusalActivity.class), REFUSAL_ACTIVITY_REQUEST);
    }

    /**
     * Close Simprints ID
     */
    public void finishWith(final int resultCode, final Intent resultData) {
        waitingForConfirmation = false;
        setResult(resultCode, resultData);
        finish();
    }


    private void tryAgain() {
        launchOutOfFocus = false;
        setup.resume();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (waitingForConfirmation) {
            finishLaunch();
            return true;
        }
        return super.onTouchEvent(event);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        if (positionTracker != null)
            positionTracker.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (setup != null)
            setup.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        switch (requestCode) {
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
                    case RESULT_TRY_AGAIN:
                        tryAgain();
                        break;
                }
                break;
            case ALERT_ACTIVITY_REQUEST:
            case REFUSAL_ACTIVITY_REQUEST:
                switch (resultCode) {
                    case RESULT_TRY_AGAIN:
                        tryAgain();
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

        if (positionTracker != null)
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
        if (setup != null) {
            setup.onDataEvent(event);
        }

        switch (event) {
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
        consentConfirmed = true;
        waitingForConfirmation = false;
        appState.getScanner().unregisterButtonListener(scannerButton);
        startActivityForResult(new Intent(LaunchActivity.this, MainActivity.class), MAIN_ACTIVITY_REQUEST);
    }
}
