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
import com.simprints.id.R;
import com.simprints.id.backgroundSync.SyncSetup;
import com.simprints.id.tools.Analytics;
import com.simprints.id.tools.Language;
import com.simprints.id.tools.PermissionManager;
import com.simprints.id.tools.RemoteConfig;
import com.simprints.id.tools.SharedPref;
import com.simprints.libdata.DATA_ERROR;
import com.simprints.libdata.DatabaseContext;
import com.simprints.libdata.DatabaseSync;
import com.simprints.libdata.ResultListener;
import com.simprints.libdata.tools.Constants;

import io.fabric.sdk.android.Fabric;

public class FrontActivity extends AppCompatActivity {
    private ImageView syncStatus;
    private Button syncButton;
    private SharedPref sharedPref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fabric.with(this, new Crashlytics());
        getBaseContext().getResources().updateConfiguration(Language.selectLanguage(
                getApplicationContext()), getBaseContext().getResources().getDisplayMetrics());
        setContentView(R.layout.activity_front);
        Analytics.getInstance(getApplicationContext());
        RemoteConfig.init();
        sharedPref = new SharedPref(getApplicationContext());

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

        new SyncSetup(getApplicationContext()).initialize();

        PermissionManager.requestAllPermissions(FrontActivity.this);

        final Constants.GROUP syncGroup = new SharedPref(getApplicationContext()).getSyncGroup();

        syncButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DatabaseSync.sync(getApplicationContext(), sharedPref.getAppKeyString(),
                        new ResultListener() {
                            @Override
                            public void onSuccess() {
                                syncButton.setEnabled(true);
                                syncButton.setText(R.string.sync_data);
                                syncStatus.setImageResource(R.drawable.ic_menu_sync_success);
                            }

                            @Override
                            public void onFailure(DATA_ERROR error) {
                                switch (error) {
                                    case SYNC_INTERRUPTED:
                                        syncButton.setEnabled(true);
                                        syncButton.setText(R.string.sync_data);
                                        syncStatus.setImageResource(R.drawable.ic_menu_sync_failed);
                                        break;
                                    default:
                                        throw new RuntimeException();
                                }
                            }
                        }, syncGroup, null);
                syncButton.setEnabled(false);
                syncButton.setText(R.string.syncing);
                syncStatus.setImageResource(R.drawable.ic_menu_syncing);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!DatabaseContext.isSignedIn(sharedPref.getAppKeyString())) {
            syncButton.setEnabled(false);
            syncButton.setText(R.string.not_signed_in);
            syncStatus.setImageResource(R.drawable.ic_menu_sync_failed);
        }
    }
}
