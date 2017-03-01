package com.simprints.id.tools.launch;

import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.simprints.id.R;
import com.simprints.id.activities.ALERT_TYPE;
import com.simprints.id.activities.LaunchActivity;
import com.simprints.id.tools.AppState;
import com.simprints.id.tools.PermissionManager;
import com.simprints.id.tools.RemoteConfig;
import com.simprints.libdata.DatabaseContext;
import com.simprints.libscanner.ResultListener;
import com.simprints.libscanner.SCANNER_ERROR;
import com.simprints.libscanner.Scanner;
import com.simprints.libscanner.ScannerUtils;

import java.util.List;

import static com.simprints.id.tools.InternalConstants.COMMCARE_PACKAGE;
import static com.simprints.id.tools.Vibrate.vibrate;

public class LaunchProcess {
    private LaunchActivity launchActivity;
    private ProgressBar launchProgress;
    private TextView confirmConsentTextView;
    private TextView loadingInfoTextView;
    private AppState appState;

    private Boolean asyncLaunched = false;

    public Boolean apiKey = false;
    public Boolean ccResolver = false;
    private Boolean btConnection = false;
    private Boolean un20WakeUp = false;
    public Boolean permissions = false;
    public Boolean databaseUpdate = false;
    private Boolean vib = false;
    private Boolean registerButton = false;


    public LaunchProcess(LaunchActivity launchActivity) {
        this.launchActivity = launchActivity;

        appState = AppState.getInstance();

        launchProgress = (ProgressBar) launchActivity.findViewById(R.id.pb_launch_progress);
        confirmConsentTextView = (TextView) launchActivity.findViewById(R.id.confirm_consent_text_view);
        loadingInfoTextView = (TextView) launchActivity.findViewById(R.id.tv_loadingInfo);

        confirmConsentTextView.setVisibility(View.INVISIBLE);
        loadingInfoTextView.setVisibility(View.VISIBLE);
        launchProgress.setProgress(0);
    }

    public void launch() {
        if (!permissions) {
            this.loadingInfoTextView.setText(R.string.launch_checking_permissions);
            Boolean permReady = PermissionManager.requestPermissions(launchActivity);

            if (!permReady) {
                return;
            } else {
                permissions = true;
            }
        }

        launchProgress.setProgress(10);
        loadingInfoTextView.setText(R.string.updating_database);

        if (!databaseUpdate) {
            appState.setData(new DatabaseContext(appState.getApiKey(),
                    appState.getUserId(),
                    appState.getDeviceId(),
                    launchActivity));
            appState.getData().setListener(launchActivity);
            appState.getData().initDatabase();
            return;
        }

        if (!asyncLaunched) {
            asyncLaunched = true;
            updateData();
            updateScanner();
        }

        this.launchProgress.setProgress(20);
        this.loadingInfoTextView.setText(R.string.launch_checking_api_key);

        if (!apiKey) {
            return;
        }

        launchProgress.setProgress(40);
        loadingInfoTextView.setText(R.string.launch_cc_resolve);

        if (!ccResolver) {
            return;
        }

        launchProgress.setProgress(60);
        loadingInfoTextView.setText(R.string.launch_bt_connect);

        if (!btConnection) {
            btConnection = true;
            updateScanner();
            return;
        }

        launchProgress.setProgress(80);
        loadingInfoTextView.setText(R.string.launch_wake_un20);

        if (!un20WakeUp) {
            un20WakeUp = true;
            updateScanner();
            return;
        }

        launchProgress.setProgress(100);

        launchActivity.waitingForConfirmation = true;
        confirmConsentTextView.setVisibility(View.VISIBLE);
        loadingInfoTextView.setVisibility(View.INVISIBLE);

        if (!vib) {
            vib = true;
            vibrate(launchActivity, 100);
        }

        if (!registerButton) {
            registerButton = true;
            launchActivity.setButton();
        }
    }

    public void updateData() {
        if (!apiKey) {
            appState.getData().signIn();
            return;
        }

        launch();

        if (!ccResolver) {
            if (RemoteConfig.get().getBoolean(RemoteConfig.ENABLE_CCDBR_ON_LOADING)) {
                if (COMMCARE_PACKAGE.equals(appState.getCallingPackage())) {
                    appState.getData().resolveCommCare(launchActivity.getContentResolver());
                    return;
                }
            }
            ccResolver = true;
        }

        launch();
    }

    private void updateScanner() {
        if (!btConnection) {
            // Initializes the session Scanner object if necessary
            if (launchActivity.appState.getScanner() == null) {
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
            }

            launchActivity.appState.setScanner(new Scanner(appState.getMacAddress()));

            // Initiate scanner connection
            launchActivity.appState.getScanner().connect(new ResultListener() {
                @Override
                public void onSuccess() {
                    launchActivity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            launch();
                        }
                    });
                }

                @Override
                public void onFailure(final SCANNER_ERROR scanner_error) {
                    launchActivity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            switch (scanner_error) {
                                case INVALID_STATE:
                                    btConnection = true;
                                    launch();
                                    break;
                                case BLUETOOTH_DISABLED:
                                    launchActivity.launchAlert(ALERT_TYPE.BLUETOOTH_NOT_ENABLED);
                                    return;
                                case BLUETOOTH_NOT_SUPPORTED:
                                    launchActivity.launchAlert(ALERT_TYPE.BLUETOOTH_NOT_SUPPORTED);
                                    return;
                                case SCANNER_UNBONDED:
                                    launchActivity.launchAlert(ALERT_TYPE.NOT_PAIRED);
                                    break;
                                default:
                                    launchActivity.launchAlert(ALERT_TYPE.DISCONNECTED);
                            }
                        }
                    });
                }
            });

            return;
        }

        if (!un20WakeUp) {
            appState.getScanner().un20Wakeup(new ResultListener() {
                @Override
                public void onSuccess() {
                    launchActivity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            appState.setHardwareVersion(appState.getScanner().getUcVersion());
                            launch();
                        }
                    });
                }

                @Override
                public void onFailure(final SCANNER_ERROR scanner_error) {
                    launchActivity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            switch (scanner_error) {
                                case UN20_INVALID_STATE:
                                    launch();
                                    break;
                                default:
                                    launchActivity.launchAlert(ALERT_TYPE.DISCONNECTED);
                            }
                        }
                    });
                }
            });
        }
    }
}
