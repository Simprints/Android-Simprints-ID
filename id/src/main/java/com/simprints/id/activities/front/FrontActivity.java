package com.simprints.id.activities.front;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
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
import com.simprints.id.tools.Language;
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

        getBaseContext().getResources().updateConfiguration(
                Language.selectLanguage(dataManager.getLanguage()),
                getBaseContext().getResources().getDisplayMetrics());
        setContentView(R.layout.activity_front);
        RemoteConfig.init();

        syncStatus = findViewById(R.id.iv_sync);
        syncButton = findViewById(R.id.bt_sync);

        PackageInfo pInfo;
        String version = "";
        try {
            pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            version = pInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        ((TextView) findViewById(R.id.versionTextView)).setText(String.format("Simprints ID: %s", version));
        ((TextView) findViewById(R.id.libSimprintsTextView)).setText(R.string.front_libSimprints_version);

        PermissionManager.requestAllPermissions(FrontActivity.this, dataManager.getCallingPackage());
        syncButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                frontPresenter.sync(getApplicationContext());
            }
        });

        frontPresenter = new FrontPresenter(this, syncService);
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
