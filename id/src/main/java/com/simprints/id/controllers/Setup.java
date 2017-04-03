package com.simprints.id.controllers;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.simprints.id.BuildConfig;
import com.simprints.id.R;
import com.simprints.id.model.ALERT_TYPE;
import com.simprints.id.tools.AppState;
import com.simprints.id.tools.InternalConstants;
import com.simprints.id.tools.PermissionManager;
import com.simprints.libdata.AuthListener;
import com.simprints.libdata.ConnectionListener;
import com.simprints.libdata.DATA_ERROR;
import com.simprints.libdata.DataCallback;
import com.simprints.libdata.DatabaseContext;
import com.simprints.libscanner.SCANNER_ERROR;
import com.simprints.libscanner.Scanner;
import com.simprints.libscanner.ScannerCallback;
import com.simprints.libscanner.ScannerUtils;

import java.util.List;

import static android.app.Activity.RESULT_CANCELED;

public class Setup {

    private static Setup singleton;

    public synchronized static Setup getInstance() {
        if (singleton == null) {
            singleton = new Setup();
        }
        return singleton;
    }

    private SetupCallback callback;

    private AppState appState = AppState.getInstance();


    // True iff the database context was initialized
    private boolean databaseInitialized = false;

    // True iff the api key was validated successfully
    private boolean apiKeyValidated = false;

    // True iff the UI of the scanner was reset since the last connection (enabling trigger button)
    private boolean uiResetSinceConnection = false;

    private volatile Boolean paused = false;

    public void stop() {
        paused = true;
    }

    public void start(@NonNull Activity activity, @Nullable SetupCallback callback) {
        this.callback = callback;
        paused = false;
        goOn(activity);
    }

    public void destroy() {
        singleton = null;
    }


    private void goOn(@NonNull final Activity activity) {
        if (this.paused)
            return;

        // Step 1: check permissions. These can be revoked, so it has to be done every time
        boolean permissionsReady = PermissionManager.checkAllPermissions(activity);
        if (!permissionsReady) {
            this.requestPermissions(activity);
            return;
        }

        // Step 2: initialize database context + port db from aa to realm if needed . Only has to be done once.
        if (!databaseInitialized) {
            this.initDbContext(activity);
            return;
        }

        // Step 3: check the api key. This only has to be done once.
        // Note: LibData checks a cached value and callback right away if it is valid
        // (To make the launch faster).
        // Then it re-validate the key with the server, resulting in a second callback.
        if (!apiKeyValidated) {
            this.validateApiKey(activity);
            return;
        }

        // Step 4: initialize scanner object.
        if (appState.getScanner() == null) {
            this.initScanner(activity);
            return;
        }

        // Step 5: connect with scanner. Must be done everytime the scanner is not connected
        if (!appState.getScanner().isConnected()) {
            this.connectToScanner(activity);
            return;
        }

        // Step 6: reset the UI. This is necessary for the trigger button to work.
        if (!uiResetSinceConnection) {
            this.resetUi(activity);
            return;
        }

        // Step 7: turn on the un20 if needed.
        this.wakeUpUn20(activity);
    }

    // STEP 1
    private void requestPermissions(@NonNull Activity activity) {
        onProgress(0, R.string.launch_checking_permissions);
        PermissionManager.requestAllPermissions(activity);
    }

    // STEP 2
    private void initDbContext(@NonNull final Activity activity) {
        onProgress(10, R.string.updating_database);
        DatabaseContext dbContext = new DatabaseContext(appState.getApiKey(), appState.getUserId(), appState.getModuleId(), appState.getDeviceId(), activity);
        appState.setData(dbContext);

        dbContext.registerAuthListener(new AuthListener() {
            @Override
            public void onSignIn() {
                appState.setSignedIn(true);
            }

            @Override
            public void onSignOut() {
                appState.setSignedIn(false);
            }
        });

        dbContext.registerConnectionListener(new ConnectionListener() {
            @Override
            public void onConnection() {
                if (!appState.getSignedIn()) {
                    apiKeyValidated = false;
                    goOn(activity);
                }
            }

            @Override
            public void onDisconnection() {
            }
        });

        dbContext.initDatabase(new DataCallback() {
            @Override
            public void onSuccess() {
                Log.d("Setup", "Database context initialized.");
                databaseInitialized = true;
                goOn(activity);
            }

            @Override
            public void onFailure(DATA_ERROR error) {
                switch (error) {
                    case DATABASE_INIT_RESTART:
                        if (appState.getData() != null) {
                            appState.getData().destroy();
                            appState.setData(null);
                        }
                        goOn(activity);
                        break;
                    default:
                        throw new RuntimeException();
                }
            }
        });
    }

    // STEP 3
    private void validateApiKey(@NonNull final Activity activity) {
        onProgress(20, R.string.launch_checking_api_key);

        appState.getData().signIn(BuildConfig.DEBUG, new DataCallback() {
            @Override
            public void onSuccess() {
                if (!apiKeyValidated) {
                    apiKeyValidated = true;
                    Log.d("Setup", "Api key validated.");
                    goOn(activity);
                }
            }

            @Override
            public void onFailure(DATA_ERROR error) {
                switch (error) {
                    case UNVERIFIED_API_KEY:
                        apiKeyValidated = false;
                        paused = true;
                        callback.onAlert(ALERT_TYPE.UNVERIFIED_API_KEY);
                        break;
                    case INVALID_API_KEY:
                        apiKeyValidated = false;
                        paused = true;
                        callback.onAlert(ALERT_TYPE.INVALID_API_KEY);
                        break;
                    default:
                        throw new RuntimeException();
                }
            }
        });
    }

    // STEP 4
    private void initScanner(@NonNull final Activity activity) {
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

        goOn(activity);
        Log.d("Setup", "Scanner initialized.");
        goOn(activity);
    }

    // STEP 5
    private void connectToScanner(@NonNull final Activity activity) {
        callback.onProgress(50, R.string.launch_bt_connect);

        appState.getScanner().connect(new ScannerCallback() {
            @Override
            public void onSuccess() {
                Log.d("Setup", "Connected to Vero.");
                uiResetSinceConnection = false;
                goOn(activity);
            }

            @Override
            public void onFailure(SCANNER_ERROR scanner_error) {
                switch (scanner_error) {
                    case INVALID_STATE: // Already connected, considered as a success
                        Log.d("Setup", "Connected to Vero.");
                        uiResetSinceConnection = false;
                        goOn(activity);
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
    }

    // STEP 6
    private void resetUi(@NonNull final Activity activity) {
        callback.onProgress(60, R.string.launch_setup);

        appState.getScanner().resetUI(new ScannerCallback() {
            @Override
            public void onSuccess() {
                Log.d("Setup", "UI reset.");
                uiResetSinceConnection = true;
                goOn(activity);
            }

            @Override
            public void onFailure(final SCANNER_ERROR scanner_error) {
                switch (scanner_error) {
                    case BUSY:
                    case INVALID_STATE:
                        goOn(activity); // try again
                        break;
                    default:
                        onAlert(ALERT_TYPE.DISCONNECTED);
                }
            }
        });
    }

    // STEP 7
    private void wakeUpUn20(@NonNull final Activity activity) {
        callback.onProgress(80, R.string.launch_wake_un20);
        appState.getScanner().un20Wakeup(new ScannerCallback() {
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
                        goOn(activity);
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
    public void onRequestPermissionsResult(Activity activity, int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        // Step 1
        if (requestCode == InternalConstants.ALL_PERMISSIONS_REQUEST) {
            for (int i = 0; i < permissions.length; i++) {
                if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
                    onError(RESULT_CANCELED, null);
                    return;
                }
            }
            Log.d("Setup", "Permissions granted.");
            goOn(activity);
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
