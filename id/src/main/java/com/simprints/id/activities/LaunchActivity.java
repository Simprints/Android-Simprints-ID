package com.simprints.id.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.simprints.id.Application;
import com.simprints.id.R;
import com.simprints.id.controllers.Setup;
import com.simprints.id.controllers.SetupCallback;
import com.simprints.id.data.DataManager;
import com.simprints.id.exceptions.unsafe.UninitializedDataManagerError;
import com.simprints.id.model.ALERT_TYPE;
import com.simprints.id.tools.AppState;
import com.simprints.id.tools.LanguageHelper;
import com.simprints.id.tools.Log;
import com.simprints.id.tools.PositionTracker;
import com.simprints.id.tools.RemoteConfig;
import com.simprints.libscanner.ButtonListener;
import com.simprints.libscanner.SCANNER_ERROR;
import com.simprints.libscanner.ScannerCallback;

import static com.simprints.id.tools.InternalConstants.ALERT_ACTIVITY_REQUEST;
import static com.simprints.id.tools.InternalConstants.GOOGLE_SERVICE_UPDATE_REQUEST;
import static com.simprints.id.tools.InternalConstants.MAIN_ACTIVITY_REQUEST;
import static com.simprints.id.tools.InternalConstants.REFUSAL_ACTIVITY_REQUEST;
import static com.simprints.id.tools.InternalConstants.RESOLUTION_REQUEST;
import static com.simprints.id.tools.InternalConstants.RESULT_TRY_AGAIN;
import static com.simprints.id.tools.Vibrate.vibrate;

@SuppressWarnings("deprecation")
@SuppressLint("HardwareIds")
public class LaunchActivity extends AppCompatActivity {


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

    // Setup callback
    private SetupCallback setupCallback;

    // True iff the user confirmed consent
    private boolean consentConfirmed = false;

    // True iff the app is waiting for the user to confirm consent
    private boolean waitingForConfirmation = false;

    // True iff another activity launched by this activity is running
    private boolean launchOutOfFocus = false;


    private DataManager dataManager;
    // Singletons
    private AppState appState;
    private Setup setup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Application app = ((Application) getApplication());
        dataManager = app.getDataManager();
        appState = app.getAppState();
        setup = app.getSetup();

        LanguageHelper.setLanguage(this, dataManager.getLanguage());
        setContentView(R.layout.activity_launch);

        // Keep screen from going to sleep
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        // Initialize remote config
        RemoteConfig.init();

        // Parse/verify callout, initialize app state
        ALERT_TYPE alert = appState.init(getIntent());
        if (alert != null) {
            launchAlert(alert);
            return;
        }
        // Log some attributes to analytics
        dataManager.logUserProperties();
        dataManager.logLogin();


        // Initialize position tracker
        positionTracker = new PositionTracker(this, appState);
        positionTracker.start();


        final ProgressBar launchProgress = findViewById(R.id.pb_launch_progress);
        final TextView loadingInfoTextView = findViewById(R.id.tv_loadingInfo);
        final TextView confirmConsentTextView = findViewById(R.id.confirm_consent_text_view);

        confirmConsentTextView.setVisibility(View.INVISIBLE);
        loadingInfoTextView.setVisibility(View.VISIBLE);

        setupCallback = new SetupCallback() {
            @Override
            public void onSuccess() {
                appState.logLoadEnd();
                // If it is the first time the launch process finishes, wait for consent confirmation
                // Else, go directly to the main activity
                if (!consentConfirmed) {
                    launchProgress.setProgress(100);
                    confirmConsentTextView.setVisibility(View.VISIBLE);
                    loadingInfoTextView.setVisibility(View.INVISIBLE);
                    waitingForConfirmation = true;
                    appState.getScanner().registerButtonListener(scannerButton);
                    vibrate(LaunchActivity.this, dataManager.getVibrateMode(), 100);
                } else {
                    finishLaunch();
                }
            }

            @Override
            public void onProgress(int progress, int detailsId) {
                Log.INSTANCE.d(LaunchActivity.this, "onprogress");
                launchProgress.setProgress(progress);
                loadingInfoTextView.setText(detailsId);
            }

            @Override
            public void onError(int resultCode, Intent resultData) {
                finishWith(resultCode, resultData);
            }

            @Override
            public void onAlert(@NonNull ALERT_TYPE alertType) {
                launchAlert(alertType);
            }
        };


        // Start the launch process
        setup.start(this, setupCallback);
    }

    /**
     * Start alert activity
     */
    private void launchAlert(ALERT_TYPE alertType) {
        if (launchOutOfFocus)
            return;

        launchOutOfFocus = true;
        setup.stop();
        Intent intent = new Intent(this, AlertActivity.class);
        intent.putExtra(IntentKeys.alertActivityAlertTypeKey, alertType);
        startActivityForResult(intent, ALERT_ACTIVITY_REQUEST);
    }

    private void tryAgain() {
        launchOutOfFocus = false;
        setup.start(this, setupCallback);
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
            setup.onRequestPermissionsResult(this, requestCode, permissions, grantResults);
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
                    case RESULT_TRY_AGAIN:
                        tryAgain();
                        break;

                    case RESULT_CANCELED:
                    case RESULT_OK:
                        finishWith(resultCode, data);
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
        launchOutOfFocus = true;
        setup.stop();
        startActivityForResult(new Intent(this, RefusalActivity.class), REFUSAL_ACTIVITY_REQUEST);
    }

    /**
     * Close Simprints ID
     */
    private void finishWith(final int resultCode, final Intent resultData) {
        waitingForConfirmation = false;
        setResult(resultCode, resultData);
        finish();
    }

    @Override
    protected void onDestroy() {
        appState.logSessionEnd();
        if (dataManager.isInitialized()) {
            try {
                dataManager.finish();
            } catch (UninitializedDataManagerError error) {
                dataManager.logError(error);
            }
        }

        if (positionTracker != null)
            positionTracker.finish();

        if (appState.getScanner() != null) {
            appState.getScanner().disconnect(new ScannerCallback() {
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

        setup.destroy();
        super.onDestroy();
    }

    public void finishLaunch() {
        consentConfirmed = true;
        waitingForConfirmation = false;
        appState.getScanner().unregisterButtonListener(scannerButton);
        startActivityForResult(new Intent(LaunchActivity.this, MainActivity.class), MAIN_ACTIVITY_REQUEST);
    }
}
