package com.simprints.id.activities;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.TextView;

import com.simprints.id.BuildConfig;
import com.simprints.id.R;
import com.simprints.id.tools.AppState;
import com.simprints.id.tools.InternalConstants;
import com.simprints.id.tools.Language;
import com.simprints.libdata.DatabaseContext;

public class AboutActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getBaseContext().getResources().updateConfiguration(Language.selectLanguage(
                getApplicationContext()), getBaseContext().getResources().getDisplayMetrics());
        setContentView(R.layout.activity_about);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_about);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.show();
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        PackageInfo pInfo;
        String version = "";
        try {
            pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            version = pInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        ((TextView) findViewById(R.id.appVersionTextView)).setText(version);

        ((TextView) findViewById(R.id.libSimprintsVersionTextView))
                .setText(InternalConstants.LIBSIMPRINTS_VERSION);

        AppState appState = AppState.getInstance();
        short firmwareVersion = 0;
        try {
            firmwareVersion = appState.getHardwareVersion();
        } catch (Exception e) {
            e.printStackTrace();
        }
        ((TextView) findViewById(R.id.firmwareVersionTextView)).setText(
                String.valueOf(firmwareVersion));

        if (BuildConfig.FLAVOR.equals("development")) {
            new AsyncTask<Void, Void, Void>() {
                @Override
                protected Void doInBackground(Void... voids) {
                    DatabaseContext.fakeSQLDb(getApplicationContext(),
                            AppState.getInstance().getApiKey(), 100);
                    return null;
                }
            }.execute();
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
}
