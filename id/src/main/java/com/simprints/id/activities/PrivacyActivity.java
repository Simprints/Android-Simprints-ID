package com.simprints.id.activities;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;

import com.simprints.id.R;
import com.simprints.id.tools.Language;
import com.simprints.id.tools.SharedPrefHelper;

public class PrivacyActivity extends AppCompatActivity {
    private SharedPrefHelper sharedPref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getBaseContext().getResources().updateConfiguration(Language.selectLanguage(
                getApplicationContext()), getBaseContext().getResources().getDisplayMetrics());
        setContentView(R.layout.activity_privacy);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_settings);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.show();
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        sharedPref = new SharedPrefHelper(getApplicationContext());

        boolean consent = sharedPref.getConsentBool();
        CheckBox checkBox = (CheckBox) findViewById(R.id.consentCheckBox);

        checkBox.setChecked(consent);
    }

    public void onCheckBoxClicked(View view) {
        // Check which checkbox was clicked
        switch (view.getId()) {
            case R.id.consentCheckBox:
                sharedPref.setConsentBool(true);
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
