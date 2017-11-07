package com.simprints.id.activities.front;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.crashlytics.android.Crashlytics;
import com.simprints.id.R;
import com.simprints.id.tools.Analytics;
import com.simprints.id.tools.Language;
import com.simprints.id.tools.PermissionManager;
import com.simprints.id.tools.RemoteConfig;

import io.fabric.sdk.android.Fabric;

public class FrontActivity extends AppCompatActivity implements FrontContract.View {

    private FrontContract.Presenter frontPresenter;

    private ImageView syncStatus;
    private Button syncButton;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Fabric.with(this, new Crashlytics());
        getBaseContext().getResources().updateConfiguration(Language.selectLanguage(
                getApplicationContext()), getBaseContext().getResources().getDisplayMetrics());
        setContentView(R.layout.activity_front);
        Analytics.getInstance(getApplicationContext());
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

        PermissionManager.requestAllPermissions(FrontActivity.this);
        syncButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                frontPresenter.sync(getApplicationContext());
            }
        });

        frontPresenter = new FrontPresenter(this);
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
