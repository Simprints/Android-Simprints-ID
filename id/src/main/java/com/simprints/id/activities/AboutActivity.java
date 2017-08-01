package com.simprints.id.activities;


import android.app.ProgressDialog;
import android.net.Uri;
import android.os.Bundle;
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

    private StatisticsView statisticsView;
    private RecoveryView recoveryView;

    private AppState appState;

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

        recoveryView.registerRecoverDbListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                recoverDb();
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

        RecoveryView() {
            initRecoverDbButton();
            initRecoverDialog();
        }

        private void initRecoverDbButton() {
            recoverDbButton = (Button) findViewById(R.id.bt_recoverDb);
            recoverDbButtonListeners = new ArrayList<>();
            ViewHelper.registerOnClickButtonListeners(recoverDbButton, recoverDbButtonListeners);
        }

        private void initRecoverDialog() {
            recoveryDialog = new ProgressDialog(AboutActivity.this);
            recoveryDialog.setIndeterminate(true);
            recoveryDialog.setCanceledOnTouchOutside(false);
        }

        void registerRecoverDbListener(View.OnClickListener onClickListener) {
            recoverDbButtonListeners.add(onClickListener);
        }

        void setStartRecovering() {
            recoveryDialog.setMessage("Resolving Database...");
            recoveryDialog.show();
        }

        void setFinishRecovering() {
            recoveryDialog.cancel();
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
        recoveryView.setStartRecovering();

        appState.getData().recoverRealmDb("db-7.json", Constants.GROUP.GLOBAL, new DataCallback() {
            @Override
            public void onSuccess() {
                recoveryView.setFinishRecovering();
            }

            @Override
            public void onFailure(DATA_ERROR data_error) {
                recoveryView.setFinishRecovering();
            }
        });
    }
}
