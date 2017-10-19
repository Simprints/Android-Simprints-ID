package com.simprints.id.activities;


import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import com.simprints.id.R;
import com.simprints.id.tools.AppState;
import com.simprints.id.tools.InternalConstants;
import com.simprints.id.tools.Language;
import com.simprints.id.tools.ViewHelper;
import com.simprints.libdata.DATA_ERROR;
import com.simprints.libdata.DataCallback;
import com.simprints.libdata.tools.Constants;

import java.util.ArrayList;
import java.util.List;

public class AboutActivity extends AppCompatActivity {

    private static boolean recoveryRunning = false;

    private StatisticsView statisticsView;
    private RecoveryView recoveryView;

    private AppState appState;
    private RecoverDbHandlerThread recoverDbHandlerThread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initViews();

        appState = AppState.getInstance();

        statisticsView.setVersionData(
                appState.getAppVersion() != null ? appState.getAppVersion() : "null",
                InternalConstants.LIBSIMPRINTS_VERSION,
                appState.getHardwareVersion() > -1 ? String.valueOf(appState.getHardwareVersion()) : "null");

        statisticsView.setDbCountData(
                Long.toString(appState.getData().getPeopleCount(Constants.GROUP.USER)),
                Long.toString(appState.getData().getPeopleCount(Constants.GROUP.MODULE)),
                Long.toString(appState.getData().getPeopleCount(Constants.GROUP.GLOBAL)));

        if (recoveryRunning) recoveryView.setRecoverDbUnavailable();
        else recoveryView.setRecoverDbAvailable();

        final Runnable recoverDbRunnable = new Runnable() {
            @Override
            public void run() {
                recoverDb();
            }
        };

        recoveryView.registerRecoverDbListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                recoveryRunning = true;
                recoveryView.setRecoverDbUnavailable();
                recoveryView.setStartRecovering();
                recoverDbHandlerThread = new RecoverDbHandlerThread("recoverDbHandlerThread");
                recoverDbHandlerThread.start();
                recoverDbHandlerThread.prepareHandler();
                recoverDbHandlerThread.postTask(recoverDbRunnable);
            }
        });

    }

    private void initViews() {
        getBaseContext().getResources().updateConfiguration(Language.selectLanguage(
                getApplicationContext()), getBaseContext().getResources().getDisplayMetrics());
        setContentView(R.layout.activity_about);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        new MasterView();
        statisticsView = new StatisticsView();
        recoveryView = new RecoveryView();
    }

    private class MasterView {

        MasterView() {
            initToolbar();
            initActionBar();
        }

        private void initToolbar() {
            Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_about);
            setSupportActionBar(toolbar);
        }

        private void initActionBar() {
            ActionBar actionBar = getSupportActionBar();
            if (actionBar != null) {
                actionBar.show();
                actionBar.setDisplayHomeAsUpEnabled(true);
            }
        }
    }

    private class StatisticsView {

        TextView tv_appVersion;
        TextView tv_libsimprintsVersion;
        TextView tv_scannerVersion;

        TextView tv_userDbCount;
        TextView tv_moduleDbCount;
        TextView tv_globalDbCount;

        StatisticsView() {
            initTextViews();
        }

        private void initTextViews() {
            tv_appVersion = (TextView) findViewById(R.id.tv_appVersion);
            tv_libsimprintsVersion = (TextView) findViewById(R.id.tv_libsimprintsVersion);
            tv_scannerVersion = (TextView) findViewById(R.id.tv_scannerVersion);

            tv_userDbCount = (TextView) findViewById(R.id.tv_userDbCount);
            tv_moduleDbCount = (TextView) findViewById(R.id.tv_moduleDbCount);
            tv_globalDbCount = (TextView) findViewById(R.id.tv_globalDbCount);
        }

        void setVersionData(String appVersion, String libsimprintsVersion, String scannerVersion) {
            tv_appVersion.setText(appVersion);
            tv_libsimprintsVersion.setText(libsimprintsVersion);
            tv_scannerVersion.setText(scannerVersion);
        }

        void setDbCountData(String userCount, String moduleCount, String globalCount) {
            tv_userDbCount.setText(userCount);
            tv_moduleDbCount.setText(moduleCount);
            tv_globalDbCount.setText(globalCount);
        }
    }

    private class RecoveryView {

        private Button recoverDbButton;
        private List<View.OnClickListener> recoverDbButtonListeners;
        private ProgressDialog recoveryDialog;
        private AlertDialog errorDialog;
        private AlertDialog successDialog;

        RecoveryView() {
            initRecoverDbButton();
            initDialogs();
        }

        private void initRecoverDbButton() {
            recoverDbButton = (Button) findViewById(R.id.bt_recoverDb);
            recoverDbButtonListeners = new ArrayList<>();
            ViewHelper.registerOnClickButtonListeners(recoverDbButton, recoverDbButtonListeners);
        }

        private void initDialogs() {
            recoveryDialog = new ProgressDialog(AboutActivity.this);
            recoveryDialog.setIndeterminate(true);
            recoveryDialog.setCanceledOnTouchOutside(false);

            errorDialog = new AlertDialog.Builder(AboutActivity.this)
                    .setTitle(getString(R.string.error))
                    .setMessage(getString(R.string.error_recovery_message))
                    .setNegativeButton(R.string.dismiss, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    }).create();

            successDialog = new AlertDialog.Builder(AboutActivity.this)
                    .setMessage(getString(R.string.success_recovery_message))
                    .setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    }).create();
        }

        void registerRecoverDbListener(View.OnClickListener onClickListener) {
            recoverDbButtonListeners.add(onClickListener);
        }

        void setRecoverDbAvailable() {
            recoverDbButton.setEnabled(true);
        }

        void setRecoverDbUnavailable() {
            recoverDbButton.setEnabled(false);
        }

        void setStartRecovering() {
            recoveryDialog.setMessage(getString(R.string.recovering_db));
            recoveryDialog.show();
        }

        void setSuccessRecovering() {
            recoveryDialog.cancel();
            successDialog.show();
        }

        void setErrorRecovering(String errorMessage) {
            recoveryDialog.cancel();
            if (errorMessage != null) errorDialog.setMessage(errorMessage);
            errorDialog.show();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void recoverDb() {
        String androidId = appState.getDeviceId() != null? appState.getDeviceId() : "no-device-id";
        appState.getData().recoverRealmDb(androidId + "_" + Long.toString(System.currentTimeMillis()) + ".json",
                androidId,
                Constants.GROUP.GLOBAL,
                new DataCallback() {
            @Override
            public void onSuccess() {
                recoverDbHandlerThread.quit();
                recoveryRunning = false;
                try {
                    recoveryView.setSuccessRecovering();
                    recoveryView.setRecoverDbAvailable();
                } catch (WindowManager.BadTokenException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(DATA_ERROR data_error) {
                recoverDbHandlerThread.quit();
                recoveryRunning = false;
                try {
                    recoveryView.setErrorRecovering(data_error.details());
                    recoveryView.setRecoverDbAvailable();
                } catch (WindowManager.BadTokenException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private class RecoverDbHandlerThread extends HandlerThread {

        Handler handler;

        RecoverDbHandlerThread(String name) {
            super(name);
        }

        void postTask(Runnable task) {
            handler.post(task);
        }

        void prepareHandler() {
            handler = new Handler(getLooper());
        }
    }
}
