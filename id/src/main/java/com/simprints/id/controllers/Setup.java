package com.simprints.id.controllers;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.simprints.id.R;
import com.simprints.id.data.analytics.AnalyticsManager;
import com.simprints.id.data.analytics.eventData.SessionEventsManager;
import com.simprints.id.data.analytics.eventData.models.events.CandidateReadEvent;
import com.simprints.id.data.analytics.eventData.models.events.ScannerConnectionEvent;
import com.simprints.id.data.db.DATA_ERROR;
import com.simprints.id.data.db.DataCallback;
import com.simprints.id.data.db.DbManager;
import com.simprints.id.data.loginInfo.LoginInfoManager;
import com.simprints.id.data.prefs.PreferencesManager;
import com.simprints.id.domain.ALERT_TYPE;
import com.simprints.id.exceptions.unsafe.NullScannerError;
import com.simprints.id.exceptions.unsafe.UnexpectedDataError;
import com.simprints.id.exceptions.unsafe.UninitializedDataManagerError;
import com.simprints.id.session.callout.CalloutAction;
import com.simprints.id.tools.AppState;
import com.simprints.id.tools.InternalConstants;
import com.simprints.id.tools.PermissionManager;
import com.simprints.id.tools.TimeHelper;
import com.simprints.id.tools.utils.SimNetworkUtils;
import com.simprints.libcommon.Person;
import com.simprints.libscanner.SCANNER_ERROR;
import com.simprints.libscanner.Scanner;
import com.simprints.libscanner.ScannerCallback;
import com.simprints.libscanner.ScannerUtils;
import com.simprints.libscanner.bluetooth.BluetoothComponentAdapter;

import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;

import static com.simprints.id.data.db.remote.tools.Utils.wrapCallback;
import static com.simprints.libscanner.ScannerUtils.convertAddressToSerial;

public class Setup {

    private final SimNetworkUtils simNetworkUtils;
    private BluetoothComponentAdapter bluetoothAdapter;
    private long startCandidateSearchTime = 0;

    public Setup(PreferencesManager preferencesManager,
                 DbManager dbManager,
                 LoginInfoManager loginInfoManager,
                 AnalyticsManager analyticsManager,
                 AppState appState,
                 SimNetworkUtils simNetworkUtils,
                 BluetoothComponentAdapter bluetoothAdapter,
                 SessionEventsManager sessionEventsManager,
                 TimeHelper timeHelper) {
        this.analyticsManager = analyticsManager;
        this.loginInfoManager = loginInfoManager;
        this.dbManager = dbManager;
        this.preferencesManager = preferencesManager;
        this.appState = appState;
        this.simNetworkUtils = simNetworkUtils;
        this.bluetoothAdapter = bluetoothAdapter;
        this.sessionEventsManager = sessionEventsManager;
        this.timeHelper = timeHelper;
    }


    private SetupCallback callback;

    // True iff it is not a verify intent || or the GUID in the verify intent was found
    private boolean guidExists = false;

    // True iff the UI of the scanner was reset since the last connection (enabling trigger button)
    private boolean uiResetSinceConnection = false;

    private volatile Boolean paused = false;

    private SessionEventsManager sessionEventsManager;
    private PreferencesManager preferencesManager;
    private LoginInfoManager loginInfoManager;
    private DbManager dbManager;
    private AnalyticsManager analyticsManager;
    private TimeHelper timeHelper;

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

    private void goOn(@NonNull final Activity activity) {
        if (this.paused)
            return;

        // Step 1: check permissions. These can be revoked, so it has to be done every time
        boolean permissionsReady = PermissionManager.checkAllPermissions(activity, preferencesManager.getCallingPackage());
        if (!permissionsReady) {
            this.requestPermissions(activity);
            return;
        }

        // Step 2: extractFrom scanner object.
        if (appState.getScanner() == null) {
            this.initScanner(activity);
            return;
        }

        // Step 3: connect with scanner. Must be done every time the scanner is not connected
        if (!appState.getScanner().isConnected()) {
            this.connectToScanner(activity);
            return;
        }

        // Step 4: check if it's a verify intent. If it is, check if the person is in the database.
        // If they are, check if connected to the internet.
        if (!guidExists) {
            this.checkIfVerifyAndGuidExists(activity);
            return;
        }


        // Step 5: reset the UI. This is necessary for the trigger button to work.
        if (!uiResetSinceConnection) {
            this.resetUi(activity);
            return;
        }

        // Step 6: turn on the un20 if needed.
        this.wakeUpUn20(activity);
    }

    // STEP 1
    private void requestPermissions(@NonNull Activity activity) {
        onProgress(15, R.string.launch_checking_permissions);
        PermissionManager.requestAllPermissions(activity, preferencesManager.getCallingPackage());
    }

    // STEP 2
    private void initScanner(@NonNull final Activity activity) {
        onProgress(45, R.string.launch_bt_connect);
        List<String> pairedScanners = ScannerUtils.getPairedScanners(bluetoothAdapter);
        if (pairedScanners.size() == 0) {
            onAlert(ALERT_TYPE.NOT_PAIRED);
            return;
        }
        if (pairedScanners.size() > 1) {
            onAlert(ALERT_TYPE.MULTIPLE_PAIRED_SCANNERS);
            return;
        }
        String macAddress = pairedScanners.get(0);
        preferencesManager.setMacAddress(macAddress);

        Scanner scanner;
        scanner = new Scanner(macAddress, bluetoothAdapter);
        appState.setScanner(scanner);

        preferencesManager.setLastScannerUsed(convertAddressToSerial(macAddress));

        Timber.d("Setup: Scanner initialized.");
        goOn(activity);
    }

    // STEP 3
    private void connectToScanner(@NonNull final Activity activity) {
        onProgress(60, R.string.launch_bt_connect);

        appState.getScanner().connect(new ScannerCallback() {
            @Override
            public void onSuccess() {
                if (appState != null && appState.getScanner() != null) {
                    Timber.d("Setup: Connected to Vero.");
                    uiResetSinceConnection = false;
                    preferencesManager.setScannerId(appState.getScanner().getScannerId());
                    analyticsManager.logScannerProperties();

                    sessionEventsManager.addEventForScannerConnectivityInBackground(
                        new ScannerConnectionEvent.ScannerInfo(
                            preferencesManager.getScannerId(),
                            preferencesManager.getMacAddress(),
                            preferencesManager.getHardwareVersionString()));

                    goOn(activity);
                } else {
                    analyticsManager.logError(new NullScannerError("Null values in onSuccess Setup.connectToScanner()"));
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

    // STEP 4
    @SuppressLint("CheckResult")
    private void checkIfVerifyAndGuidExists(@NonNull final Activity activity) {
        if (preferencesManager.getCalloutAction() != CalloutAction.VERIFY) {
            guidExists = true;
            goOn(activity);
            return;
        }

        onProgress(70, R.string.launch_checking_person_in_db);
        startCandidateSearchTime = timeHelper.msSinceBoot();

        List<Person> loadedPerson = new ArrayList<>();
        final String guid = preferencesManager.getPatientId();
        try {
            dbManager.loadPerson(loadedPerson, loginInfoManager.getSignedInProjectId(), guid, wrapCallback("loading people from dbManager", newLoadPersonCallback(activity, guid)));
        } catch (UninitializedDataManagerError error) {
            analyticsManager.logError(error);
            onAlert(ALERT_TYPE.UNEXPECTED_ERROR);
        }
    }

    private DataCallback newLoadPersonCallback(@NonNull final Activity activity, final String guid) {
        return new DataCallback() {
            @Override
            public void onSuccess(boolean isDataFromRemote) {
                Timber.d("Setup: GUID found.");
                guidExists = true;
                goOn(activity);

                sessionEventsManager.addEventForCandidateReadInBackground(
                    guid,
                    startCandidateSearchTime,
                    isDataFromRemote ? CandidateReadEvent.LocalResult.NOT_FOUND : CandidateReadEvent.LocalResult.FOUND,
                    isDataFromRemote ? CandidateReadEvent.RemoteResult.FOUND : CandidateReadEvent.RemoteResult.NOT_FOUND);
            }

            @Override
            public void onFailure(DATA_ERROR data_error) {
                switch (data_error) {
                    case NOT_FOUND:
                        Person probe = new Person(guid);
                        try {
                            saveNotFoundVerification(probe);
                        } catch (UninitializedDataManagerError error) {
                            analyticsManager.logError(error);
                            onAlert(ALERT_TYPE.UNEXPECTED_ERROR);
                        }
                        break;
                    default:
                        analyticsManager.logError(UnexpectedDataError.forDataError(data_error, "Setup.checkIfVerifyAndGuidExists()"));
                        onAlert(ALERT_TYPE.UNEXPECTED_ERROR);
                }
            }
        };
    }

    private void saveNotFoundVerification(final Person probe) {
        if (simNetworkUtils.isConnected()) {
            // We've synced with the online dbManager and they're not in the dbManager
            onAlert(ALERT_TYPE.GUID_NOT_FOUND_ONLINE);
            sessionEventsManager.addEventForCandidateReadInBackground(
                probe.getGuid(),
                startCandidateSearchTime,
                CandidateReadEvent.LocalResult.NOT_FOUND,
                CandidateReadEvent.RemoteResult.NOT_FOUND);

        } else {
            // We're offline but might find the person if we sync
            onAlert(ALERT_TYPE.GUID_NOT_FOUND_OFFLINE);
            sessionEventsManager.addEventForCandidateReadInBackground(
                probe.getGuid(), startCandidateSearchTime,
                CandidateReadEvent.LocalResult.NOT_FOUND,
                CandidateReadEvent.RemoteResult.OFFLINE);
        }
    }

    // STEP 5
    private void resetUi(@NonNull final Activity activity) {
        onProgress(80, R.string.launch_setup);

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

    // STEP 6
    private void wakeUpUn20(@NonNull final Activity activity) {
        onProgress(90, R.string.launch_wake_un20);

        appState.getScanner().un20Wakeup(new ScannerCallback() {
            @Override
            public void onSuccess() {
                if (appState != null && appState.getScanner() != null) {
                    Timber.d("Setup: UN20 ready.");
                    preferencesManager.setHardwareVersion(appState.getScanner().getUcVersion());
                    sessionEventsManager.updateHardwareVersionInScannerConnectivityEvent(preferencesManager.getHardwareVersionString());
                    Setup.this.onSuccess();
                } else {
                    analyticsManager.logError(new NullScannerError("Null values in onSuccess Setup.wakeUpUn20()"));
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
                    onError();
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

    private void onError() {
        paused = true;
        if (callback != null)
            callback.onError(Activity.RESULT_CANCELED);
    }

    private void onAlert(@NonNull ALERT_TYPE alertType) {
        paused = true;
        if (callback != null)
            callback.onAlert(alertType);
    }
}
