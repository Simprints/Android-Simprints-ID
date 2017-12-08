package com.simprints.id.activities.front;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.simprints.id.Application;
import com.simprints.id.R;
import com.simprints.id.backgroundSync.SyncService;
import com.simprints.id.data.DataManager;
import com.simprints.id.tools.LanguageHelper;
import com.simprints.id.tools.PermissionManager;
import com.simprints.id.tools.RemoteConfig;

public class FrontActivity extends AppCompatActivity implements FrontContract.View {

    private FrontContract.Presenter frontPresenter;

    private ImageView syncStatus;
    private Button syncButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Application app = ((Application) getApplication());
        DataManager dataManager = app.getDataManager();
        SyncService syncService = app.getSyncService();

        LanguageHelper.setLanguage(this, dataManager.getLanguage());
        setContentView(R.layout.activity_front);
        RemoteConfig.init();

        syncStatus = findViewById(R.id.iv_sync);
        syncButton = findViewById(R.id.bt_sync);

        initSimprintsIdVersionTextView(dataManager.getAppVersionName());
        initLibSimprintsVersionTextView(dataManager.getLibVersionName());

        PermissionManager.requestAllPermissions(FrontActivity.this, dataManager.getCallingPackage());
        syncButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                frontPresenter.sync(getApplicationContext());
            }
        });

        frontPresenter = new FrontPresenter(this, syncService);
    }

    private void initSimprintsIdVersionTextView(String simprintsIdVersion) {
        TextView simprintsIdVersionTextView = findViewById(R.id.simprintsIdVersionTextView);
        String simprintsIdVersionString =
                String.format(getString(R.string.front_simprintsId_version), simprintsIdVersion);
        simprintsIdVersionTextView.setText(simprintsIdVersionString);
    }

    private void initLibSimprintsVersionTextView(String libSimprintsVersion) {
        TextView libSimprintsVersionTextView = findViewById(R.id.libSimprintsVersionTextView);
        String libSimprintsVersionString =
                String.format(getString(R.string.front_libSimprints_version), libSimprintsVersion);
        libSimprintsVersionTextView.setText(libSimprintsVersionString);

    }

    @Override
    public void setPresenter(@NonNull FrontContract.Presenter presenter) {
        frontPresenter = presenter;
    }

    @Override
    protected void onResume() {
        super.onResume();

        frontPresenter.start();
    }

    @Override
    public void setSyncUnavailable() {
        syncButton.setText(R.string.not_signed_in);
        syncStatus.setImageResource(R.drawable.ic_menu_sync_off);
    }

    @Override
    public void setSyncInProgress() {
        syncButton.setEnabled(false);
        syncButton.setText(R.string.syncing);
        syncStatus.setImageResource(R.drawable.ic_menu_syncing);
    }

    @Override
    public void setSyncSuccess() {
        syncButton.setEnabled(true);
        syncButton.setText(R.string.sync_data);
        syncStatus.setImageResource(R.drawable.ic_menu_sync_success);
    }

    @Override
    public void setSyncFailed() {
        syncButton.setEnabled(true);
        syncButton.setText(R.string.sync_data);
        syncStatus.setImageResource(R.drawable.ic_menu_sync_failed);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        frontPresenter.stopListening();
    }
}
