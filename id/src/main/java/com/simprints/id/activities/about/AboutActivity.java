package com.simprints.id.activities.about;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import com.simprints.id.Application;
import com.simprints.id.R;
import com.simprints.id.data.DataManager;
import com.simprints.id.tools.AlertLauncher;
import com.simprints.id.tools.LanguageHelper;


public class AboutActivity extends AppCompatActivity implements AboutContract.View {

    private AboutContract.Presenter viewPresenter;

    TextView tv_appVersion;
    TextView tv_libsimprintsVersion;
    TextView tv_scannerVersion;

    TextView tv_userDbCount;
    TextView tv_moduleDbCount;
    TextView tv_globalDbCount;

    private Button recoverDbButton;
    private ProgressDialog recoveryDialog;
    private AlertDialog errorDialog;
    private AlertDialog successDialog;
    DataManager dataManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Application app = ((Application) getApplication());
        dataManager = app.getDataManager();
        LanguageHelper.setLanguage(this, dataManager.getLanguage());

        setContentView(R.layout.activity_about);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        Toolbar toolbar = findViewById(R.id.toolbar_about);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.show();
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        tv_appVersion = findViewById(R.id.tv_appVersion);
        tv_libsimprintsVersion = findViewById(R.id.tv_libsimprintsVersion);
        tv_scannerVersion = findViewById(R.id.tv_scannerVersion);

        tv_userDbCount = findViewById(R.id.tv_userDbCount);
        tv_moduleDbCount = findViewById(R.id.tv_moduleDbCount);
        tv_globalDbCount = findViewById(R.id.tv_globalDbCount);

        recoverDbButton = findViewById(R.id.bt_recoverDb);
        recoverDbButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setRecoverDbUnavailable();
                setStartRecovering();
                viewPresenter.recoverDb();
            }
        });

        recoveryDialog = new ProgressDialog(this);
        recoveryDialog.setIndeterminate(true);
        recoveryDialog.setCanceledOnTouchOutside(false);

        errorDialog = new AlertDialog.Builder(this)
                .setTitle(getString(R.string.error))
                .setMessage(getString(R.string.error_recovery_message))
                .setNegativeButton(R.string.dismiss, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).create();

        successDialog = new AlertDialog.Builder(this)
                .setMessage(getString(R.string.success_recovery_message))
                .setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).create();

        // Create the presenter and pass it the information it needs
        viewPresenter = new AboutPresenter(this, dataManager);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Tell the Presenter to do what it needs when the Activity is loaded
        viewPresenter.start();
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

    @Override
    public void setVersionData(String appVersion, String libsimprintsVersion, String scannerVersion) {
        tv_appVersion.setText(appVersion);
        tv_libsimprintsVersion.setText(libsimprintsVersion);
        tv_scannerVersion.setText(scannerVersion);
    }

    @Override
    public void setDbCountData(String userCount, String moduleCount, String globalCount) {
        tv_userDbCount.setText(userCount);
        tv_moduleDbCount.setText(moduleCount);
        tv_globalDbCount.setText(globalCount);
    }

    @Override
    public void setRecoverDbAvailable() {
        recoverDbButton.setEnabled(true);
    }

    @Override
    public void setRecoverDbUnavailable() {
        recoverDbButton.setEnabled(false);
    }

    @Override
    public void setStartRecovering() {
        recoveryDialog.setMessage(getString(R.string.recovering_db));
        recoveryDialog.show();
    }

    @Override
    public void setSuccessRecovering() {
        recoveryDialog.cancel();
        successDialog.show();
    }

    @Override
    public void setRecoveringFailed(String errorMessage) {
        recoveryDialog.cancel();
        if (errorMessage != null) errorDialog.setMessage(errorMessage);
        errorDialog.show();
    }

    @Override
    public AboutContract.Presenter getViewPresenter() {
        return viewPresenter;
    }

    @Override
    public void setViewPresenter(AboutContract.Presenter presenter) {
        viewPresenter = presenter;
    }
}
