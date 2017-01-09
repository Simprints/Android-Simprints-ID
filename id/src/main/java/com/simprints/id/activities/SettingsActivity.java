package com.simprints.id.activities;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.simprints.id.R;
import com.simprints.id.tools.Analytics;
import com.simprints.id.tools.Language;
import com.simprints.id.tools.SharedPrefHelper;

public class SettingsActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {
    private SharedPrefHelper sharedPrefHelper;

    private final static int MIN_QUALITY = 40;
    private final static int MAX_QUALITY = 99;
    private final static int MIN_NB_OF_IDS = 1;
    private final static int MAX_NB_OF_IDS = 20;
    ToggleButton nudgeToggleButton;
    SeekBar qualitySeekbar;
    SeekBar nbOfIdsSeekbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getBaseContext().getResources().updateConfiguration(Language.selectLanguage(
                getApplicationContext()), getBaseContext().getResources().getDisplayMetrics());
        setContentView(R.layout.activity_settings);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_settings);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.show();
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        sharedPrefHelper = new SharedPrefHelper(getApplicationContext());

        Spinner spinner = (Spinner) findViewById(R.id.language_spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.language_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(this);
        spinner.setSelection(sharedPrefHelper.getLanguagePositionInt());

        boolean nudgeMode = sharedPrefHelper.getNudgeModeBool();
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
        final int qualityThreshold = sharedPrefHelper.getQualityThresholdInt() - MIN_QUALITY;
        qualitySeekbar = (SeekBar) findViewById(R.id.qualitySeekBar);
        qualitySeekbar.setMax(MAX_QUALITY - MIN_QUALITY);
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
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        ((TextView) findViewById(R.id.minNbOfIdsTextView)).setText(String.valueOf(MIN_NB_OF_IDS));
        ((TextView) findViewById(R.id.maxNbOfIdsTextView)).setText(String.valueOf(MAX_NB_OF_IDS));
        final TextView nbOfIdsTextView = (TextView) findViewById(R.id.nbOfIdsTextView);
        final int nbOfIds = sharedPrefHelper.getReturnIdCountInt() - MIN_NB_OF_IDS;
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
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        int matcher = sharedPrefHelper.getMatcherTypeInt();
        if (matcher == 0) {
            ((RadioButton) findViewById(R.id.radio_simAfis)).setChecked(true);
        } else if (matcher == 1) {
            ((RadioButton) findViewById(R.id.radio_sourceAfis)).setChecked(true);

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

    private void saveQualityThreshold() {
        sharedPrefHelper.setQualityThresholdInt(qualitySeekbar.getProgress() + MIN_QUALITY);
    }


    private void saveNbOfIds() {
        sharedPrefHelper.setReturnIdCountInt(nbOfIdsSeekbar.getProgress() + MIN_NB_OF_IDS);
    }

    private void saveNudgeMode() {
        sharedPrefHelper.setNudgeModeBool(nudgeToggleButton.isChecked());
    }

    @Override
    public void onPause() {
        super.onPause();
        saveQualityThreshold();
        saveNbOfIds();
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int pos, long i) {
        switch (pos) {
            case 0:
                sharedPrefHelper.setLanguageString("");
                sharedPrefHelper.setLanguagePositionInt(0);

                break;
            case 1:
                sharedPrefHelper.setLanguageString("ne");
                sharedPrefHelper.setLanguagePositionInt(1);
                break;
            case 2:

                sharedPrefHelper.setLanguageString("bn");
                sharedPrefHelper.setLanguagePositionInt(2);
                break;
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }

    public void onRadioButtonClicked(View view) {
        // Is the button now checked?
        boolean checked = ((RadioButton) view).isChecked();

        // Check which radio button was clicked
        switch (view.getId()) {
            case R.id.radio_simAfis:
                if (checked)
                    sharedPrefHelper.setMatcherTypeInt(0);
                break;
            case R.id.radio_sourceAfis:
                if (checked)
                    sharedPrefHelper.setMatcherTypeInt(1);
                break;
        }
    }
}
