package com.simprints.id.activities;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;

import com.simprints.id.R;

public class PrivacyActivity extends AppCompatActivity {
    private SharedPreferences sharedPref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_privacy);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_settings);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.show();
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        sharedPref = getApplicationContext().getSharedPreferences(
                getString(R.string.preference_file_key), Context.MODE_PRIVATE);

        boolean consent = sharedPref.getBoolean(getString(R.string.pref_consent_bool), false);
        CheckBox checkBox = (CheckBox) findViewById(R.id.consentCheckBox);

        checkBox.setChecked(consent);
    }

    public void onCheckBoxClicked(View view) {
        // Is the view now checked?
        SharedPreferences.Editor editor = sharedPref.edit();

        // Check which checkbox was clicked
        switch (view.getId()) {
            case R.id.consentCheckBox:
                editor.putBoolean(getString(R.string.pref_consent_bool), true);
                ((CheckBox) view).setChecked(true);
                break;
        }
        editor.apply();
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
