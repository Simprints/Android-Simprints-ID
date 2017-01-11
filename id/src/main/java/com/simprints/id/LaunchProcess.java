package com.simprints.id;

import android.bluetooth.BluetoothAdapter;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.simprints.id.activities.ALERT_TYPE;
import com.simprints.id.activities.LaunchActivity;
import com.simprints.id.tools.AppState;
import com.simprints.id.tools.PermissionManager;
import com.simprints.libdata.DatabaseContext;
import com.simprints.libscanner.Scanner;

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
    public Boolean btConnection = false;
    public Boolean un20WakeUp = false;
    public Boolean permissions = false;
    public Boolean databaseUpdate = false;
    private Boolean vib = false;


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
            loadingInfoTextView.setText(R.string.launch_checking_permissions);
            Boolean permReady = PermissionManager.requestPermissions(launchActivity);

            if (!permReady) {
                return;
            } else {
                permissions = true;
            }
        }

        loadingInfoTextView.setText(R.string.updating_database);
        launchProgress.setProgress(10);

        if (!databaseUpdate) {
            return;
        }

        if (!asyncLaunched) {
            asyncLaunched = true;
            updateData();
            updateScanner();
        }

        launchProgress.setProgress(20);
        loadingInfoTextView.setText(R.string.launch_checking_api_key);

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

        launchActivity.waitingForConfirmation = true;
        confirmConsentTextView.setVisibility(View.VISIBLE);
        loadingInfoTextView.setVisibility(View.INVISIBLE);

        if (!vib) {
            vib = true;
            vibrate(launchActivity, 100);
        }
    }

    public void updateData() {
        if (!apiKey) {

            appState.setData(new DatabaseContext(appState.getApiKey(), appState.getUserId(),
                    launchActivity.getApplicationContext(), launchActivity));
            appState.getData().validateApiKey();

            return;
        }

        launch();

        if (!ccResolver) {
            if (COMMCARE_PACKAGE.equals(appState.getCallingPackage())) {
                appState.getData().resolveCommCare(launchActivity.getContentResolver());
                return;
            } else {
                ccResolver = true;
            }
        }

        launch();
    }

    public void updateScanner() {
        if (!btConnection) {
            // Initializes the session Scanner object if necessary
            if (launchActivity.appState.getScanner() == null) {
                BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
                if (adapter == null) {
                    launchActivity.launchAlert(ALERT_TYPE.BLUETOOTH_NOT_SUPPORTED);
                    return;
                }
                if (!adapter.isEnabled()) {
                    launchActivity.launchAlert(ALERT_TYPE.BLUETOOTH_NOT_ENABLED);
                    return;
                }

                List<String> pairedScanners = Scanner.getPairedScanners();
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
            launchActivity.appState.getScanner().setScannerListener(launchActivity);
            launchActivity.appState.getScanner().connect();

            return;
        }

        launch();

        if (!un20WakeUp) {
            appState.getScanner().un20Wakeup();
            return;
        }

        appState.setHardwareVersion(appState.getScanner().getUcVersion());
        launch();
    }
}
