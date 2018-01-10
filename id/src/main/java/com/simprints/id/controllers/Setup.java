package com.simprints.id.controllers;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.simprints.id.R;
import com.simprints.id.data.DataManager;
import com.simprints.id.exceptions.unsafe.ApiKeyNotFoundError;
import com.simprints.id.exceptions.unsafe.NullScannerError;
import com.simprints.id.exceptions.unsafe.UnexpectedDataError;
import com.simprints.id.exceptions.unsafe.UninitializedDataManagerError;
import com.simprints.id.model.ALERT_TYPE;
import com.simprints.id.domain.callout.CalloutAction;
import com.simprints.id.tools.AppState;
import com.simprints.id.tools.InternalConstants;
import com.simprints.id.tools.PermissionManager;
import com.simprints.libcommon.Person;
import com.simprints.libdata.DATA_ERROR;
import com.simprints.libdata.DataCallback;
import com.simprints.libscanner.SCANNER_ERROR;
import com.simprints.libscanner.Scanner;
import com.simprints.libscanner.ScannerCallback;
import com.simprints.libscanner.ScannerUtils;

import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;

import static android.app.Activity.RESULT_CANCELED;
import static com.simprints.libdata.models.enums.VERIFY_GUID_EXISTS_RESULT.GUID_NOT_FOUND_OFFLINE;
import static com.simprints.libdata.models.enums.VERIFY_GUID_EXISTS_RESULT.GUID_NOT_FOUND_ONLINE;

public class Setup {

    private static Setup singleton;

    public synchronized static Setup getInstance(DataManager dataManager, AppState appState) {
        if (singleton == null) {
            singleton = new Setup(dataManager, appState);
        }
        return singleton;
    }

    private Setup(DataManager dataManager, AppState appState) {
        this.dataManager = dataManager;
        this.appState = appState;
    }


    private SetupCallback callback;

    // True iff the api key was validated successfully
    private boolean apiKeyValidated = false;

    // True iff it is not a verify intent || or the GUID in the verify intent was found
    private boolean guidExists = false;

    // True iff the UI of the scanner was reset since the last connection (enabling trigger button)
    private boolean uiResetSinceConnection = false;

    private volatile Boolean paused = false;

    private DataManager dataManager;

    // Singletons
    private AppState appState;

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
        boolean permissionsReady = PermissionManager.checkAllPermissions(activity, dataManager.getCallingPackage());
        if (!permissionsReady) {
            this.requestPermissions(activity);
            return;
        }

        // Step 2: extractFrom database context + port db from aa to realm if needed . Only has to be done once.
        if (!dataManager.isInitialized()) {
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

        // Step 4: extractFrom scanner object.
        if (appState.getScanner() == null) {
            this.initScanner(activity);
            return;
        }

        // Step 5: connect with scanner. Must be done every time the scanner is not connected
        if (!appState.getScanner().isConnected()) {
            this.connectToScanner(activity);
            return;
        }

        // Step 6: check if it's a verify intent. If it is, check if the person is in the database.
        // If they are, check if connected to the internet.
        if (!guidExists) {
            this.checkIfVerifyAndGuidExists(activity);
            return;
        }


        // Step 7: reset the UI. This is necessary for the trigger button to work.
        if (!uiResetSinceConnection) {
            this.resetUi(activity);
            return;
        }

        // Step 8: turn on the un20 if needed.
        this.wakeUpUn20(activity);
    }

    // STEP 1
    private void requestPermissions(@NonNull Activity activity) {
        onProgress(0, R.string.launch_checking_permissions);
        PermissionManager.requestAllPermissions(activity, dataManager.getCallingPackage());
    }

    // STEP 2
    private void initDbContext(@NonNull final Activity activity) {
        onProgress(10, R.string.updating_database);
        dataManager.initialize(new DataCallback() {
            @Override
            public void onSuccess() {
                Timber.d("Setup: Database context initialized.");
                goOn(activity);
            }

            @Override
            public void onFailure(DATA_ERROR dataError) {
                switch (dataError) {
                    case DATABASE_INIT_RESTART:
                        goOn(activity);
                        break;
                    case NOT_FOUND:
                        dataManager.logError(new ApiKeyNotFoundError("API Key was null in Setup.initDbContext()"));
                        onAlert(ALERT_TYPE.MISSING_API_KEY);
                        break;
                    default:
                        dataManager.logError(UnexpectedDataError.forDataError(dataError, "Setup.initDbContext()"));
                        onAlert(ALERT_TYPE.UNEXPECTED_ERROR);
                }
            }
        });
    }

    // STEP 3
    private void validateApiKey(@NonNull final Activity activity) {
        onProgress(20, R.string.launch_checking_api_key);

        try {
            dataManager.signIn(newSignInCallback(activity));
        } catch (UninitializedDataManagerError error) {
            dataManager.logError(error);
            onAlert(ALERT_TYPE.UNEXPECTED_ERROR);
        }
    }

    private DataCallback newSignInCallback(@NonNull final Activity activity) {
        return new DataCallback() {
            @Override
            public void onSuccess() {
                if (!apiKeyValidated) {
                    apiKeyValidated = true;
                    Timber.d("Setup: Api key validated.");
                    goOn(activity);
                }
            }

            @Override
            public void onFailure(DATA_ERROR dataError) {
                switch (dataError) {
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
                        dataManager.logError(UnexpectedDataError.forDataError(dataError, "Setup.validateApiKey()"));
                        onAlert(ALERT_TYPE.UNEXPECTED_ERROR);
                }
            }
        };
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
        dataManager.setMacAddress(macAddress);
        appState.setScanner(new Scanner(macAddress));

        Timber.d("Setup: Scanner initialized.");
        goOn(activity);
    }

    // STEP 5
    private void connectToScanner(@NonNull final Activity activity) {
        callback.onProgress(50, R.string.launch_bt_connect);

        appState.getScanner().connect(new ScannerCallback() {
            @Override
            public void onSuccess() {
                if (appState != null && appState.getScanner() != null) {
                    Timber.d("Setup: Connected to Vero.");
                    uiResetSinceConnection = false;
                    dataManager.setScannerId(appState.getScanner().getScannerId());
                    dataManager.logScannerProperties();
                    goOn(activity);
                } else {
                    dataManager.logError(new NullScannerError("Null values in onSuccess Setup.connectToScanner()"));
                    onAlert(ALERT_TYPE.UNEXPECTED_ERROR);
                }
            }

            @Override
            public void onFailure(SCANNER_ERROR scanner_error) {
                switch (scanner_error) {
                    case INVALID_STATE: // Already connected, considered as a success
                        Timber.d("Setup: Connected to Vero.");
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
    private void checkIfVerifyAndGuidExists(@NonNull final Activity activity) {
        if (dataManager.getCalloutAction() != CalloutAction.VERIFY) {
            guidExists = true;
            goOn(activity);
            return;
        }

        callback.onProgress(60, R.string.launch_checking_person_in_db);

        List<Person> loadedPerson = new ArrayList<>();
        final String guid = dataManager.getPatientId();
        try {
            dataManager.loadPerson(loadedPerson, guid, newLoadPersonCallback(activity, guid));
        } catch (UninitializedDataManagerError error) {
            dataManager.logError(error);
            onAlert(ALERT_TYPE.UNEXPECTED_ERROR);
        }
    }

    private DataCallback newLoadPersonCallback(@NonNull final Activity activity, final String guid) {
        return new DataCallback() {
            @Override
            public void onSuccess() {
                Timber.d("Setup: GUID found.");
                guidExists = true;
                goOn(activity);
            }

            @Override
            public void onFailure(DATA_ERROR data_error) {
                switch (data_error) {
                    case NOT_FOUND:
                        Person probe = new Person(guid);
                        try {
                            saveNotFoundVerification(probe);
                        } catch (UninitializedDataManagerError error) {
                            dataManager.logError(error);
                            onAlert(ALERT_TYPE.UNEXPECTED_ERROR);
                        }
                        break;
                    default:
                        dataManager.logError(UnexpectedDataError.forDataError(data_error, "Setup.checkIfVerifyAndGuidExists()"));
                        onAlert(ALERT_TYPE.UNEXPECTED_ERROR);
                }
            }
        };
    }

    private void saveNotFoundVerification(Person probe) {
        if (dataManager.isConnected()) {
            // We've synced with the online db and they're not in the db
            dataManager.saveVerification(probe, null, GUID_NOT_FOUND_ONLINE);
            onAlert(ALERT_TYPE.GUID_NOT_FOUND_ONLINE);
        } else {
            // We're offline but might find the person if we sync
            dataManager.saveVerification(probe, null, GUID_NOT_FOUND_OFFLINE);
            onAlert(ALERT_TYPE.GUID_NOT_FOUND_OFFLINE);
        }
    }

    // STEP 7
    private void resetUi(@NonNull final Activity activity) {
        callback.onProgress(70, R.string.launch_setup);

        appState.getScanner().resetUI(new ScannerCallback() {
            @Override
            public void onSuccess() {
                Timber.d("Setup: UI reset.");
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

    // STEP 8
    private void wakeUpUn20(@NonNull final Activity activity) {
        callback.onProgress(85, R.string.launch_wake_un20);

        appState.getScanner().un20Wakeup(new ScannerCallback() {
            @Override
            public void onSuccess() {
                if (appState != null && appState.getScanner() != null) {
                    Timber.d("Setup: UN20 ready.");
                    dataManager.setHardwareVersion(appState.getScanner().getUcVersion());
                    Setup.this.onSuccess();
                } else {
                    dataManager.logError(new NullScannerError("Null values in onSuccess Setup.wakeUpUn20()"));
                    onAlert(ALERT_TYPE.UNEXPECTED_ERROR);
                }
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
            Timber.d("Setup: Permissions granted.");
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
