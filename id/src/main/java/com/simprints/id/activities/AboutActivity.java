package com.simprints.id.activities;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.TextView;

import com.simprints.id.R;
import com.simprints.id.tools.AppState;
import com.simprints.id.tools.InternalConstants;
import com.simprints.id.tools.Language;
import com.simprints.libdata.tools.Constants;


public class AboutActivity extends AppCompatActivity {

//    private ProgressDialog ccResolverDialog;
    private AppState appState;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getBaseContext().getResources().updateConfiguration(Language.selectLanguage(
                getApplicationContext()), getBaseContext().getResources().getDisplayMetrics());
        setContentView(R.layout.activity_about);

        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_about);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.show();
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        appState = AppState.getInstance();

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

        final AppState appState = AppState.getInstance();
        short firmwareVersion = 0;
        try {
            firmwareVersion = appState.getHardwareVersion();
        } catch (Exception e) {
            e.printStackTrace();
        }

        ((TextView) findViewById(R.id.firmwareVersionTextView)).setText(
                String.valueOf(firmwareVersion));

//        ccResolverDialog = new ProgressDialog(this);
//        ccResolverDialog.setIndeterminate(true);
//        ccResolverDialog.setCanceledOnTouchOutside(false);
//        ccResolverDialog.setMessage("Resolving Database...");
//
//        findViewById(R.id.bt_resolveCc).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                ccResolverDialog.show();
//                appState.getData().resolveCommCare(getApplicationContext().getContentResolver(),
//                        new ResultListener() {
//                            @Override
//                            public void onSuccess() {
//                                ccResolverDialog.cancel();
//                            }
//
//                            @Override
//                            public void onFailure(DATA_ERROR data_error) {
//                                ccResolverDialog.cancel();
//                            }
//                        });
//            }
//        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        ((TextView) findViewById(R.id.tv_userDbCount))
                .setText((Long.toString(appState.getData()
                        .getPeopleCount(Constants.GROUP.USER))));
        ((TextView) findViewById(R.id.tv_moduleDbCount))
                .setText((Long.toString(appState.getData()
                        .getPeopleCount(Constants.GROUP.MODULE))));
        ((TextView) findViewById(R.id.tv_globalDbCount))
                .setText((Long.toString(appState.getData()
                        .getPeopleCount(Constants.GROUP.GLOBAL))));
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
