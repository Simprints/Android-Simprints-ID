package com.simprints.id.controllers;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.simprints.id.R;
import com.simprints.id.model.ALERT_TYPE;
import com.simprints.id.tools.AppState;
import com.simprints.id.tools.InternalConstants;
import com.simprints.id.tools.PermissionManager;
import com.simprints.id.tools.RemoteConfig;
import com.simprints.libdata.DatabaseContext;
import com.simprints.libdata.DatabaseEventListener;
import com.simprints.libdata.Event;
import com.simprints.libscanner.ResultListener;
import com.simprints.libscanner.SCANNER_ERROR;
import com.simprints.libscanner.Scanner;
import com.simprints.libscanner.ScannerUtils;

import java.util.List;

import static android.app.Activity.RESULT_CANCELED;
import static com.simprints.id.tools.InternalConstants.COMMCARE_PACKAGE;

public class Setup {

    private static Setup singleton;

    public synchronized static Setup getInstance() {
        if (singleton == null) {
            singleton = new Setup();
        }
        return singleton;
    }

    // TODO: not store the activity in the static singleton (doable when libdata will use individual callbacks)
    private Activity activity;
    private DatabaseEventListener listener;
    private SetupCallback callback;

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


    private Setup() {
        ccDbResolved = !RemoteConfig.get().getBoolean(RemoteConfig.ENABLE_CCDBR_ON_LOADING) ||
                !COMMCARE_PACKAGE.equalsIgnoreCase(appState.getCallingPackage());
    }

    public void stop() {
        paused = true;
    }

    public void start(@NonNull Activity activity, @NonNull DatabaseEventListener listener, @Nullable SetupCallback callback) {
        this.activity = activity;
        this.listener = listener;
        this.callback = callback;
        paused = false;
        goOn();
    }

    public void destroy() {
        activity = null;
        singleton = null;
    }


    private void goOn() {
        if (this.paused)
            return;

        // Step 1: check permissions. These can be revoked, so it has to be done every time
        boolean permissionsReady = PermissionManager.checkAllPermissions(activity);
        if (!permissionsReady) {
            onProgress(0, R.string.launch_checking_permissions);
            PermissionManager.requestAllPermissions(activity);
            return;
            // The launch process will be resumed by onRequestPermissionResult
        }

        // Step 2: initialize database context. This only has to be done once.
        if (!databaseUpdated) {
            onProgress(10, R.string.updating_database);
            appState.setData(new DatabaseContext(appState.getApiKey(), appState.getUserId(), appState.getDeviceId(), activity));
            appState.getData().setListener(listener); // TODO: move from global listener to callback for each request in libdata
            appState.getData().initDatabase();
            return;
            // The launch process will be resumed when getting a DATABASE_INIT_SUCCESS/RESTART event
        }

        // Step 3: check the api key. This only has to be done once.
        // Note: LibData checks a cached value and callback right away if it is valid
        // (To make the launch faster).
        // Then it re-validate the key with the server, resulting in a second callback.
        if (!apiKeyValidated) {
            onProgress(20, R.string.launch_checking_api_key);
            appState.getData().signIn();
            return;
            // The launch process will be resumed when getting an API_KEY_VALID/INVALID/UNVERIFIED event
        }

        // Step 4: resolve commcare database. Optional, and only has to be done once.
        if (!ccDbResolved) {
            callback.onProgress(30, R.string.launch_cc_resolve);
            appState.getData().resolveCommCare(activity.getContentResolver());
            return;
            // The launch process will be resumed when getting a DATABASE_RESOLVED event
        }

        // Step 5: initialize scanner object.
        if (appState.getScanner() == null) {
            callback.onProgress(40, R.string.launch_bt_connect);
            List<String> pairedScanners = ScannerUtils.getPairedScanners();
            if (pairedScanners.size() == 0) {
                onAlert(ALERT_TYPE.NOT_PAIRED);
                return;
            }
            if (pairedScanners.size() > 1) {
                onAlert(ALERT_TYPE.MULTIPLE_PAIRED_SCANNERS);
                return;
            }
            String macAddress = pairedScanners.get(0);
            appState.setMacAddress(macAddress);
            appState.setScanner(new Scanner(macAddress));

            Log.d("Setup", "Scanner initialized.");
        }

        // Step 6: connect with scanner. Must be done everytime the scanner is not connected
        if (!appState.getScanner().isConnected()) {
            callback.onProgress(50, R.string.launch_bt_connect);

            appState.getScanner().connect(new ResultListener() {
                @Override
                public void onSuccess() {
                    Log.d("Setup", "Connected to Vero.");
                    uiResetSinceConnection = false;
                    Setup.this.goOn();
                }

                @Override
                public void onFailure(SCANNER_ERROR scanner_error) {
                    switch (scanner_error) {
                        case INVALID_STATE: // Already connected, considered as a success
                            Log.d("Setup", "Connected to Vero.");
                            uiResetSinceConnection = false;
                            Setup.this.goOn();
                            break;
                        case BLUETOOTH_DISABLED:
                            onAlert(ALERT_TYPE.BLUETOOTH_NOT_ENABLED);
                            break;
                        case BLUETOOTH_NOT_SUPPORTED:
                            onAlert(ALERT_TYPE.BLUETOOTH_NOT_SUPPORTED);
                            break;
                        case SCANNER_UNBONDED:
                            onAlert(ALERT_TYPE.NOT_PAIRED);
                            break;

                        case BUSY:
                        case IO_ERROR:
                        default:
                            onAlert(ALERT_TYPE.DISCONNECTED);
                    }
                }
            });
            return;
            // The launch process will be resumed by the connect request callback
        }

        // Step 8: reset the UI. This is necessary for the trigger button to work.
        if (!uiResetSinceConnection) {
            callback.onProgress(60, R.string.launch_setup);

            appState.getScanner().resetUI(new ResultListener() {
                @Override
                public void onSuccess() {
                    Log.d("Setup", "UI reset.");
                    uiResetSinceConnection = true;
                    Setup.this.goOn();
                }

                @Override
                public void onFailure(final SCANNER_ERROR scanner_error) {
                    switch (scanner_error) {
                        case BUSY:
                        case INVALID_STATE:
                            Setup.this.goOn(); // try again
                            break;
                        default:
                            onAlert(ALERT_TYPE.DISCONNECTED);
                    }
                }
            });
            return;
        }

        // Step 8: turn on the un20 if needed.
        callback.onProgress(80, R.string.launch_wake_un20);
        appState.getScanner().un20Wakeup(new ResultListener() {
            @Override
            public void onSuccess() {
                Log.d("Setup", "UN20 ready.");
                appState.setHardwareVersion(appState.getScanner().getUcVersion());
                Setup.this.onSuccess();
            }

            @Override
            public void onFailure(final SCANNER_ERROR scanner_error) {
                switch (scanner_error) {
                    case BUSY:
                    case INVALID_STATE:
                        Setup.this.goOn();
                        break;
                    case UN20_LOW_VOLTAGE:
                        onAlert(ALERT_TYPE.LOW_BATTERY);
                    default:
                        onAlert(ALERT_TYPE.DISCONNECTED);
                }
            }
        });
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
                    onError(RESULT_CANCELED, null);
                    return;
                }
            }
            Log.d("Setup", "Permissions granted.");
            goOn();
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
                Log.d("Setup", "Database context initialized.");
                goOn();
                break;

            // The database context must be reset, then the launch process can continue (re-doing step 2)
            case DATABASE_INIT_RESTART:
                if (appState.getData() != null) {
                    appState.getData().destroy();
                    appState.setData(null);
                }
                goOn();
                break;

            // Step 3
            case API_KEY_VALID:
                if (!apiKeyValidated) {
                    apiKeyValidated = true;
                    Log.d("Setup", "Api key validated.");
                    goOn();
                }
                break;
            case API_KEY_UNVERIFIED:
            case API_KEY_INVALID:
                apiKeyValidated = false;
                paused = true;
                callback.onAlert(event == Event.API_KEY_UNVERIFIED ? ALERT_TYPE.UNVERIFIED_API_KEY : ALERT_TYPE.INVALID_API_KEY);
                break;

            // Step 4
            case DATABASE_RESOLVED:
                ccDbResolved = true;
                Log.d("Setup", "CC database resolved.");
                goOn();
                break;
        }
    }


    private void onSuccess() {
        paused = true;
        if (callback != null)
            callback.onSuccess();
    }

    private void onProgress(int progress, int detailsId) {
        if (callback != null)
            callback.onProgress(progress, detailsId);
    }

    private void onError(int resultCode, Intent resultData) {
        paused = true;
        if (callback != null)
            callback.onError(resultCode, resultData);
    }

    private void onAlert(@NonNull ALERT_TYPE alertType) {
        paused = true;
        if (callback != null)
            callback.onAlert(alertType);
    }
}
