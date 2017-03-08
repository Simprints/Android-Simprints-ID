package com.simprints.id.controllers;

import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.simprints.id.R;
import com.simprints.id.model.ALERT_TYPE;
import com.simprints.id.activities.LaunchActivity;
import com.simprints.id.tools.AppState;
import com.simprints.id.tools.InternalConstants;
import com.simprints.id.tools.Log;
import com.simprints.id.tools.PermissionManager;
import com.simprints.id.tools.RemoteConfig;
import com.simprints.libdata.DatabaseContext;
import com.simprints.libdata.Event;
import com.simprints.libscanner.ResultListener;
import com.simprints.libscanner.SCANNER_ERROR;
import com.simprints.libscanner.Scanner;
import com.simprints.libscanner.ScannerUtils;
import com.simprints.libscanner.enums.UN20_STATE;

import java.util.List;

import static android.app.Activity.RESULT_CANCELED;
import static com.simprints.id.tools.InternalConstants.COMMCARE_PACKAGE;

public class Setup {

    private LaunchActivity launchActivity;
    private ProgressBar launchProgress;
    private TextView confirmConsentTextView;
    private TextView loadingInfoTextView;
    private AppState appState = AppState.getInstance();


    // True iff migrating the database from ActiveAndroid to Realm was not required, or
    // was done successfully
    private boolean databaseUpdated = false;

    // True iff the api key was validated successfully
    private boolean apiKeyValidated = false;

    // True iff calling package is commcare AND commcare db resolution on loading is enable
    // AND commcare's database was resolved successfully
    private boolean ccDbResolved = false;

    // True iff the UI of the scanner was reset since the last connection (enabling trigger button)
    private boolean uiResetSinceConnection = false;

    private volatile Boolean paused = false;

    private Runnable onReady;


    public Setup(LaunchActivity launchActivity, Runnable onReady) {
        this.launchActivity = launchActivity;
        this.onReady = onReady;

        ccDbResolved = !RemoteConfig.get().getBoolean(RemoteConfig.ENABLE_CCDBR_ON_LOADING) ||
                !COMMCARE_PACKAGE.equalsIgnoreCase(appState.getCallingPackage());

        launchProgress = (ProgressBar) launchActivity.findViewById(R.id.pb_launch_progress);
        confirmConsentTextView = (TextView) launchActivity.findViewById(R.id.confirm_consent_text_view);
        loadingInfoTextView = (TextView) launchActivity.findViewById(R.id.tv_loadingInfo);

        confirmConsentTextView.setVisibility(View.INVISIBLE);
        loadingInfoTextView.setVisibility(View.VISIBLE);
        setProgress(0);
    }

    public void pause() {
        paused = true;
    }

    public void resume() {
        paused = false;
        start();
    }

    public void start() {
        if (this.paused)
            return;

        // Step 1: check permissions. These can be revoked, so it has to be done every time
        boolean permissionsReady = PermissionManager.checkAllPermissions(launchActivity);
        if (!permissionsReady) {
            Log.d(launchActivity, "(1/8) Requesting permissions.");
            loadingInfoTextView.setText(R.string.launch_checking_permissions);
            PermissionManager.requestAllPermissions(launchActivity);
            return;
            // The launch process will be resumed by onRequestPermissionResult
        }

        // Step 2: initialize database context. This only has to be done once.
        if (!databaseUpdated) {
            Log.d(launchActivity, "(2/8) Initializing database context.");
            loadingInfoTextView.setText(R.string.updating_database);
            appState.setData(new DatabaseContext(appState.getApiKey(), appState.getUserId(), appState.getDeviceId(), launchActivity));
            appState.getData().setListener(launchActivity);
            appState.getData().initDatabase();
            return;
            // The launch process will be resumed when getting a DATABASE_INIT_SUCCESS/RESTART event
        }

        // Step 3: check the api key. This only has to be done once.
        // Note: LibData checks a cached value and callback right away if it is valid
        // (To make the launch faster).
        // Then it re-validate the key with the server, resulting in a second callback.
        if (!apiKeyValidated) {
            Log.d(launchActivity, "(3/8) Validating api key.");
            loadingInfoTextView.setText(R.string.launch_checking_api_key);
            appState.getData().signIn();
            return;
            // The launch process will be resumed when getting an API_KEY_VALID/INVALID/UNVERIFIED event
        }

        // Step 4: resolve commcare database. Optional, and only has to be done once.
        if (!ccDbResolved) {
            Log.d(launchActivity, "(4/8) Resolving CC database.");
            loadingInfoTextView.setText(R.string.launch_cc_resolve);
            appState.getData().resolveCommCare(launchActivity.getContentResolver());
            return;
            // The launch process will be resumed when getting a DATABASE_RESOLVED event
        }

        // Step 5: initialize scanner object.
        if (appState.getScanner() == null) {
            Log.d(launchActivity, "(5/8) Initializing Scanner.");
            loadingInfoTextView.setText(R.string.launch_bt_connect);
            List<String> pairedScanners = ScannerUtils.getPairedScanners();
            if (pairedScanners.size() == 0) {
                launchActivity.launchAlert(ALERT_TYPE.NOT_PAIRED);
                return;
            }
            if (pairedScanners.size() > 1) {
                launchActivity.launchAlert(ALERT_TYPE.MULTIPLE_PAIRED_SCANNERS);
                return;
            }
            String macAddress = pairedScanners.get(0);
            appState.setMacAddress(macAddress);
            appState.setScanner(new Scanner(macAddress));

            Log.d(launchActivity, "Scanner initialized.");
            setProgress(50);
        }

        // Step 6: connect with scanner. Must be done everytime the scanner is not connected
        if (!appState.getScanner().isConnected()) {
            Log.d(launchActivity, "(6/8) Connecting to Vero.");
            loadingInfoTextView.setText(R.string.launch_bt_connect);

            appState.getScanner().connect(new ResultListener() {
                @Override
                public void onSuccess() {
                    Log.d(launchActivity, "Connected to Vero.");
                    setProgress(60);
                    uiResetSinceConnection = false;
                    Setup.this.start();
                }

                @Override
                public void onFailure(SCANNER_ERROR scanner_error) {
                    switch (scanner_error) {
                        case INVALID_STATE: // Already connected, considered as a success
                            Log.d(launchActivity, "Connected to Vero.");
                            setProgress(60);
                            uiResetSinceConnection = false;
                            Setup.this.start();
                            break;
                        case BLUETOOTH_DISABLED:
                            launchActivity.launchAlert(ALERT_TYPE.BLUETOOTH_NOT_ENABLED);
                            break;
                        case BLUETOOTH_NOT_SUPPORTED:
                            launchActivity.launchAlert(ALERT_TYPE.BLUETOOTH_NOT_SUPPORTED);
                            break;
                        case SCANNER_UNBONDED:
                            launchActivity.launchAlert(ALERT_TYPE.NOT_PAIRED);
                            break;

                        case BUSY:
                        case IO_ERROR:
                        default:
                            launchActivity.launchAlert(ALERT_TYPE.DISCONNECTED);
                    }
                }
            });
            return;
            // The launch process will be resumed by the connect request callback
        }

        // Step 8: reset the UI. This is necessary for the trigger button to work.
        if (!uiResetSinceConnection) {
            Log.d(launchActivity, "(7/8) Resetting UI.");

            appState.getScanner().resetUI(new ResultListener() {
                @Override
                public void onSuccess() {
                    Log.d(launchActivity, "UI Reset");
                    uiResetSinceConnection = true;
                    setProgress(70);
                    Setup.this.start();
                }

                @Override
                public void onFailure(final SCANNER_ERROR scanner_error) {
                    switch (scanner_error) {
                        case BUSY:
                        case INVALID_STATE:
                            Setup.this.start();
                            break;
                        default:
                            launchActivity.launchAlert(ALERT_TYPE.DISCONNECTED);
                    }
                }
            });

        }

        // Step 8: wake up the un20. Must be done every time the un20 is sleeping
        if (appState.getScanner().getUn20State() != UN20_STATE.READY) {
            Log.d(launchActivity, "(8/8) Waking up UN20.");
            loadingInfoTextView.setText(R.string.launch_wake_un20);

            appState.getScanner().un20Wakeup(new ResultListener() {
                @Override
                public void onSuccess() {
                    Log.d(launchActivity, "UN20 ready.");
                    setProgress(100);
                    Setup.this.start();
                }

                @Override
                public void onFailure(final SCANNER_ERROR scanner_error) {
                    switch (scanner_error) {
                        case BUSY:
                        case INVALID_STATE:
                            Setup.this.start();
                            break;
                        case UN20_LOW_VOLTAGE:
                            launchActivity.launchAlert(ALERT_TYPE.LOW_BATTERY);
                        default:
                            launchActivity.launchAlert(ALERT_TYPE.DISCONNECTED);
                    }
                }
            });
            return;
            // The launch process will be resumed by the un20Wakeup request callback
        }

        // Step 8.5: update the hardware version in the app state,
        // as it was obtained by polling the un20
        appState.setHardwareVersion(appState.getScanner().getUcVersion());

        confirmConsentTextView.setVisibility(View.VISIBLE);
        loadingInfoTextView.setVisibility(View.INVISIBLE);

        onReady.run();
    }

    /**
     * Handles the results of the permission requests issued by the launch process
     * Must be called by the launch activity
     */
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        // Step 1
        if (requestCode == InternalConstants.ALL_PERMISSIONS_REQUEST) {
            for (int i = 0; i < permissions.length; i++) {
                if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
                    launchActivity.finishWith(RESULT_CANCELED, null);
                    return;
                }
            }
            Log.d(launchActivity, "Launch: Permissions granted.");
            setProgress(10);
            start();
        }
    }

    /**
     * Handles the results of the database requests issued by the launch process
     * Must be called by the launch activity
     */
    public void onDataEvent(final Event event) {
        switch (event) {
            // Step 2
            // The database context was initialized successfuly, the launch process can continue (validating step 2)
            case DATABASE_INIT_SUCCESS:
                databaseUpdated = true;
                Log.d(launchActivity, "Database context initialized.");
                setProgress(20);
                start();
                break;

            // The database context must be reset, then the launch process can continue (re-doing step 2)
            case DATABASE_INIT_RESTART:
                if (appState.getData() != null) {
                    appState.getData().destroy();
                    appState.setData(null);
                }
                start();
                break;

            // Step 3
            case API_KEY_VALID:
                if (!apiKeyValidated) {
                    apiKeyValidated = true;
                    Log.d(launchActivity, "Api key validated.");
                    setProgress(30);
                    start();
                }
                break;
            case API_KEY_UNVERIFIED:
                apiKeyValidated = false;
                launchActivity.launchAlert(ALERT_TYPE.UNVERIFIED_API_KEY);
                break;
            case API_KEY_INVALID:
                apiKeyValidated = false;
                launchActivity.launchAlert(ALERT_TYPE.INVALID_API_KEY);
                break;

            // Step 4
            case DATABASE_RESOLVED:
                ccDbResolved = true;
                Log.d(launchActivity, "CC database resolved.");
                setProgress(40);
                start();
                break;
        }
    }

    private void setProgress(int progress) {
        if (launchProgress != null) {
            launchProgress.setProgress(progress);
        }
    }

}
