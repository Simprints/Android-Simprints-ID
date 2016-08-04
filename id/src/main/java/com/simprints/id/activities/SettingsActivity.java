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
import android.widget.EditText;

import com.simprints.id.R;

public class SettingsActivity extends AppCompatActivity {

    private SharedPreferences sharedPref;
    private EditText qualityEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_settings);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.show();
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        sharedPref = getApplicationContext().getSharedPreferences(
                getString(R.string.preference_file_key), Context.MODE_PRIVATE);

        boolean nudge = sharedPref.getBoolean(getString(R.string.pref_nudge_mode_bool), true);
        CheckBox checkBox = (CheckBox) findViewById(R.id.nudgeCheckBox);
        checkBox.setChecked(nudge);

        int qualityScore = sharedPref.getInt(getString(R.string.pref_quality_theshold), 60);
        qualityEditText = (EditText) findViewById(R.id.qualityEditText);
        qualityEditText.setText(String.valueOf(qualityScore));
    }

    public void onCheckBoxClicked(View view) {
        // Is the view now checked?
        boolean checked = ((CheckBox) view).isChecked();
        SharedPreferences.Editor editor = sharedPref.edit();

        // Check which checkbox was clicked
        switch (view.getId()) {
            case R.id.nudgeCheckBox:
                editor.putBoolean(getString(R.string.pref_nudge_mode_bool), checked);
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

    @Override
    public void onPause() {
        super.onPause();

        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putInt(getString(R.string.pref_quality_theshold),
                Integer.parseInt(qualityEditText.getText().toString()));
        editor.apply();
    }
}
