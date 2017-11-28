package com.simprints.id.activities;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.crashlytics.android.Crashlytics;
import com.simprints.id.Application;
import com.simprints.id.R;
import com.simprints.id.backgroundSync.SyncService;
import com.simprints.id.data.DataManager;
import com.simprints.id.tools.AppState;
import com.simprints.id.tools.Language;
import com.simprints.id.tools.PermissionManager;
import com.simprints.id.tools.RemoteConfig;
import com.simprints.libdata.DATA_ERROR;
import com.simprints.libdata.DataCallback;

import io.fabric.sdk.android.Fabric;

public class FrontActivity extends AppCompatActivity {
    private ImageView syncStatus;
    private Button syncButton;
    private DataCallback dataCallback;

    private DataManager dataManager;

    // Singletons
    private SyncService syncService;
    private AppState appState;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Application app = ((Application) getApplication());
        dataManager = app.getDataManager();
        syncService = app.getSyncService();
        appState = app.getAppState();

        Fabric.with(this, new Crashlytics());
        getBaseContext().getResources().updateConfiguration(
                Language.selectLanguage(dataManager.getLanguage()),
                getBaseContext().getResources().getDisplayMetrics());
        setContentView(R.layout.activity_front);

        // TODO: is that necessary?
        app.getAnalytics();

        RemoteConfig.init();

        syncStatus = (ImageView) findViewById(R.id.iv_sync);
        syncButton = (Button) findViewById(R.id.bt_sync);

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

        dataCallback = new DataCallback() {
            @Override
            public void onSuccess() {
                syncButton.setEnabled(true);
                syncButton.setText(R.string.sync_data);
                syncStatus.setImageResource(R.drawable.ic_menu_sync_success);
            }

            @Override
            public void onFailure(DATA_ERROR data_error) {
                switch (data_error) {
                    case SYNC_INTERRUPTED:
                        syncButton.setEnabled(true);
                        syncButton.setText(R.string.sync_data);
                        syncStatus.setImageResource(R.drawable.ic_menu_sync_failed);
                        break;
                    default:
                        throw new RuntimeException();
                }
            }
        };

        syncButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                syncButton.setEnabled(false);
                syncButton.setText(R.string.syncing);
                syncStatus.setImageResource(R.drawable.ic_menu_syncing);
                if (!syncService.startAndListen(getApplicationContext(), dataCallback)) {
                    syncButton.setText(R.string.not_signed_in);
                    syncStatus.setImageResource(R.drawable.ic_menu_sync_off);
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        syncService.stopListening(dataCallback);
    }
}
