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
import com.simprints.libdata.Connection;
import com.simprints.libdata.DatabaseContext;
import com.simprints.libdata.DatabaseEventListener;
import com.simprints.libdata.DatabaseSync;
import com.simprints.libdata.Event;

import io.fabric.sdk.android.Fabric;

public class FrontActivity extends AppCompatActivity implements DatabaseEventListener {
    private ImageView syncStatus;
    private Button syncButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fabric.with(this, new Crashlytics());
        getBaseContext().getResources().updateConfiguration(Language.selectLanguage(
                getApplicationContext()), getBaseContext().getResources().getDisplayMetrics());
        setContentView(R.layout.activity_front);
        Analytics.getInstance(getApplicationContext());

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

        PermissionManager.requestPermissions(FrontActivity.this);

        setSyncButton();

        syncButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DatabaseSync.sync(getApplicationContext(), FrontActivity.this);
                syncButton.setEnabled(false);
                syncButton.setText(R.string.syncing);
                syncStatus.setImageResource(R.drawable.ic_menu_syncing);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        Connection.setListener(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        Connection.release();
    }

    @Override
    public void onBackPressed() {
        finish();
        super.onBackPressed();
    }

    @Override
    public void onDataEvent(Event event) {
        switch (event) {
            case SYNC_INTERRUPTED:
                setSyncButton();
                syncStatus.setImageResource(R.drawable.ic_menu_sync_failed);
                break;
            case SYNC_SUCCESS:
                setSyncButton();
                syncStatus.setImageResource(R.drawable.ic_menu_sync_success);
                break;
            case CONNECTED:
                setSyncButton();
                break;
            case DISCONNECTED:
                syncButton.setEnabled(false);
                syncButton.setText(R.string.offline);
                syncStatus.setImageResource(R.drawable.ic_menu_sync_off);
                break;
        }
    }

    private void setSyncButton() {
        if (!DatabaseContext.signedIn()) {
            syncButton.setEnabled(false);
            syncButton.setText(R.string.not_signed_in);
            syncStatus.setImageResource(R.drawable.ic_menu_sync_failed);
        } else {
            syncButton.setEnabled(true);
            syncButton.setText(R.string.sync_data);
            syncStatus.setImageResource(R.drawable.ic_menu_sync_ready);
        }
    }
}
