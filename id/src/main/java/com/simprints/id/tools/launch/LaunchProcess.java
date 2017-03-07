package com.simprints.id.tools.launch;

import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.simprints.id.R;
import com.simprints.id.model.ALERT_TYPE;
import com.simprints.id.activities.LaunchActivity;
import com.simprints.id.tools.AppState;
import com.simprints.id.tools.Log;
import com.simprints.id.tools.PermissionManager;
import com.simprints.id.tools.RemoteConfig;
import com.simprints.libdata.DatabaseContext;
import com.simprints.libscanner.ResultListener;
import com.simprints.libscanner.SCANNER_ERROR;
import com.simprints.libscanner.Scanner;
import com.simprints.libscanner.ScannerUtils;

import java.util.List;

import static com.simprints.id.tools.InternalConstants.COMMCARE_PACKAGE;

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
    private Boolean setContinue = false;
    private Boolean resetUI = false;


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
            return;
        }

        launchProgress.setProgress(80);
        loadingInfoTextView.setText(R.string.launch_wake_un20);

        if (!un20WakeUp) {
            return;
        }

        launchProgress.setProgress(100);

        confirmConsentTextView.setVisibility(View.VISIBLE);
        loadingInfoTextView.setVisibility(View.INVISIBLE);

        if (!setContinue) {
            setContinue = true;
            launchActivity.readyToContinue();
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
            launchActivity.appState.setScanner(new Scanner(appState.getMacAddress()));
        }

        // Initiate scanner connection
        if (!launchActivity.appState.getScanner().isConnected()) {
            launchActivity.appState.getScanner().connect(new ResultListener() {
                @Override
                public void onSuccess() {
                    btConnection = true;
                    updateScanner();
                }

                @Override
                public void onFailure(SCANNER_ERROR scanner_error) {
                    boolean test = appState.getScanner().isConnected();
                    switch (scanner_error) {
                        case INVALID_STATE:
                            btConnection = true;
                            updateScanner();
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
        }

        launch(); // Update progress bar

        if (!resetUI) {
            appState.getScanner().resetUI(new ResultListener() {
                @Override
                public void onSuccess() {
                    resetUI = true;
                    updateScanner();
                }

                @Override
                public void onFailure(SCANNER_ERROR scanner_error) {
                    switch (scanner_error) {
                        case BUSY:
                        case INVALID_STATE:
                            updateScanner();
                            break;
                        default:
                            launchActivity.launchAlert(ALERT_TYPE.DISCONNECTED);
                    }
                }
            });
            return;
        }

        launch(); // Update progress bar

        if (!un20WakeUp) {
            appState.getScanner().un20Wakeup(new ResultListener() {
                @Override
                public void onSuccess() {
                    un20WakeUp = true;
                    launch();
                }

                @Override
                public void onFailure(final SCANNER_ERROR scanner_error) {
                    switch (scanner_error) {
                        case BUSY:
                        case INVALID_STATE:
                            updateScanner();
                            break;
                        case UN20_LOW_VOLTAGE:
                            launchActivity.launchAlert(ALERT_TYPE.LOW_BATTERY);
                        default:
                            launchActivity.launchAlert(ALERT_TYPE.DISCONNECTED);
                    }
                }
            });
        }
    }
}
