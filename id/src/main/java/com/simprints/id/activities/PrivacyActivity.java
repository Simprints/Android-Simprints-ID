package com.simprints.id.activities;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.CheckBox;

import com.simprints.id.Application;
import com.simprints.id.R;
import com.simprints.id.data.prefs.PreferencesManager;
import com.simprints.id.tools.LanguageHelper;

import javax.inject.Inject;

public class PrivacyActivity extends AppCompatActivity {

    @Inject PreferencesManager preferencesManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((Application) getApplication()).getComponent().inject(this);

        LanguageHelper.setLanguage(this, preferencesManager.getLanguage());
        setContentView(R.layout.activity_privacy);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        Toolbar toolbar = findViewById(R.id.toolbar_settings);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.show();
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        boolean consent = preferencesManager.getConsent();
        CheckBox checkBox = findViewById(R.id.consentCheckBox);

        checkBox.setChecked(consent);
    }

    public void onCheckBoxClicked(View view) {
        // Check which checkbox was clicked
        switch (view.getId()) {
            case R.id.consentCheckBox:
                preferencesManager.setConsent(true);
                ((CheckBox) view).setChecked(true);
                break;
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
