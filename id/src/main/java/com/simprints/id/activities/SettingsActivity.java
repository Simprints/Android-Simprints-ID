package com.simprints.id.activities;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.simprints.id.R;

public class SettingsActivity extends AppCompatActivity {

    private final static int MIN_QUALITY = 40;
    private final static int MAX_QUALITY = 99;
    private final static int MIN_NB_OF_IDS = 1;
    private final static int MAX_NB_OF_IDS = 20;

    private SharedPreferences sharedPref;
    ToggleButton nudgeToggleButton;
    SeekBar qualitySeekbar;
    SeekBar nbOfIdsSeekbar;

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


        boolean nudgeMode = sharedPref.getBoolean(getString(R.string.pref_nudge_mode_bool), true);
        nudgeToggleButton = (ToggleButton) findViewById(R.id.nudgeToggleButton);
        nudgeToggleButton.setChecked(nudgeMode);
        nudgeToggleButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                saveNudgeMode();
            }
        });

        ((TextView) findViewById(R.id.minQualityTextView)).setText(String.valueOf(MIN_QUALITY));
        ((TextView) findViewById(R.id.maxQualityTextView)).setText(String.valueOf(MAX_QUALITY));
        final TextView qualityThresholdTextView = (TextView) findViewById(R.id.qualityTextView);
        final int qualityThreshold = sharedPref.getInt(
                getString(R.string.pref_quality_theshold), 60) - MIN_QUALITY;
        qualitySeekbar = (SeekBar) findViewById(R.id.qualitySeekBar);
        qualitySeekbar.setMax(MAX_QUALITY-MIN_QUALITY);
        qualitySeekbar.setProgress(qualityThreshold);
        qualityThresholdTextView.setText(String.format(
                getString(R.string.quality_threshold_value), qualityThreshold + MIN_QUALITY));
        qualitySeekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean b) {
                saveQualityThreshold();
                qualityThresholdTextView.setText(String.format(
                        getString(R.string.quality_threshold_value), progress + MIN_QUALITY));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        ((TextView) findViewById(R.id.minNbOfIdsTextView)).setText(String.valueOf(MIN_NB_OF_IDS));
        ((TextView) findViewById(R.id.maxNbOfIdsTextView)).setText(String.valueOf(MAX_NB_OF_IDS));
        final TextView nbOfIdsTextView = (TextView) findViewById(R.id.nbOfIdsTextView);
        final int nbOfIds = sharedPref.getInt(getString(R.string.pref_nb_of_ids), 10) - MIN_NB_OF_IDS;
        nbOfIdsSeekbar = (SeekBar) findViewById(R.id.nbOfIdsSeekBar);
        nbOfIdsSeekbar.setMax(MAX_NB_OF_IDS - MIN_NB_OF_IDS);
        nbOfIdsSeekbar.setProgress(nbOfIds);
        nbOfIdsTextView.setText(String.format(
                getString(R.string.nb_of_ids_value), nbOfIds + MIN_NB_OF_IDS));
        nbOfIdsSeekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean b) {
                saveNbOfIds();
                nbOfIdsTextView.setText(String.format(
                        getString(R.string.nb_of_ids_value), progress + MIN_NB_OF_IDS));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
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

    private void saveQualityThreshold() {
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putInt(getString(R.string.pref_quality_theshold),
                qualitySeekbar.getProgress() + MIN_QUALITY);
        editor.apply();
    }


    private void saveNbOfIds() {
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putInt(getString(R.string.pref_nb_of_ids),
                nbOfIdsSeekbar.getProgress() + MIN_NB_OF_IDS);
        editor.apply();
    }

    private void saveNudgeMode() {
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putBoolean(getString(R.string.pref_nudge_mode_bool), nudgeToggleButton.isChecked());
        editor.apply();
    }

    @Override
    public void onPause() {
        super.onPause();
        saveQualityThreshold();
        saveNbOfIds();
    }
}
