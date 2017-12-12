package com.simprints.id.activities.front;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import com.simprints.id.Application;
import com.simprints.id.R;
import com.simprints.id.data.DataManager;
import com.simprints.id.tools.LanguageHelper;
import com.simprints.id.tools.PermissionManager;
import com.simprints.id.tools.RemoteConfig;

public class FrontActivity extends AppCompatActivity implements FrontContract.View {

    private FrontContract.Presenter frontPresenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Application app = ((Application) getApplication());
        DataManager dataManager = app.getDataManager();

        LanguageHelper.setLanguage(this, dataManager.getLanguage());
        setContentView(R.layout.activity_front);
        RemoteConfig.init();

        initSimprintsIdVersionTextView(dataManager.getAppVersionName());
        initLibSimprintsVersionTextView(dataManager.getLibVersionName());

        PermissionManager.requestAllPermissions(FrontActivity.this, dataManager.getCallingPackage());

        frontPresenter = new FrontPresenter(this);
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

}
