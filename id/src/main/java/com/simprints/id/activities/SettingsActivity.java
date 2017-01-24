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
import com.simprints.id.tools.Language;
import com.simprints.id.tools.SharedPref;

public class SettingsActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {
    private SharedPref sharedPref;

    private final static int MIN_QUALITY = 40;
    private final static int MAX_QUALITY = 99;
    private final static int MIN_NB_OF_IDS = 1;
    private final static int MAX_NB_OF_IDS = 20;
    private final static int MIN_TIMEOUT = 1;
    private final static int MAX_TIMEOUT = 10;
    ToggleButton nudgeToggleButton;
    SeekBar qualitySeekBar;
    SeekBar nbOfIdsSeekBar;
    SeekBar timeoutSeekBar;

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

        sharedPref = new SharedPref(getApplicationContext());

        Spinner spinner = (Spinner) findViewById(R.id.language_spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.language_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(this);
        spinner.setSelection(sharedPref.getLanguagePositionInt());

        boolean nudgeMode = sharedPref.getNudgeModeBool();
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
        final int qualityThreshold = sharedPref.getQualityThresholdInt() - MIN_QUALITY;
        qualitySeekBar = (SeekBar) findViewById(R.id.qualitySeekBar);
        qualitySeekBar.setMax(MAX_QUALITY - MIN_QUALITY);
        qualitySeekBar.setProgress(qualityThreshold);
        qualityThresholdTextView.setText(String.format(
                getString(R.string.quality_threshold_value), qualityThreshold + MIN_QUALITY));
        qualitySeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
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
        final int nbOfIds = sharedPref.getReturnIdCountInt() - MIN_NB_OF_IDS;
        nbOfIdsSeekBar = (SeekBar) findViewById(R.id.nbOfIdsSeekBar);
        nbOfIdsSeekBar.setMax(MAX_NB_OF_IDS - MIN_NB_OF_IDS);
        nbOfIdsSeekBar.setProgress(nbOfIds);
        nbOfIdsTextView.setText(String.format(
                getString(R.string.nb_of_ids_value), nbOfIds + MIN_NB_OF_IDS));
        nbOfIdsSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
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

        ((TextView) findViewById(R.id.tv_minTimeout)).setText(String.valueOf(MIN_TIMEOUT));
        ((TextView) findViewById(R.id.tv_maxTimeout)).setText(String.valueOf(MAX_TIMEOUT));
        final TextView tv_timeout = (TextView) findViewById(R.id.tv_timeout);
        final int timeout = sharedPref.getTimeoutInt() - MIN_TIMEOUT;
        timeoutSeekBar = (SeekBar) findViewById(R.id.sb_timeout);
        timeoutSeekBar.setMax(MAX_TIMEOUT - MIN_TIMEOUT);
        timeoutSeekBar.setProgress(timeout);
        tv_timeout.setText(String.format(
                getString(R.string.timeout_value), timeout + MIN_TIMEOUT));
        timeoutSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean b) {
                saveTimeout();
                tv_timeout.setText(String.format(
                        getString(R.string.timeout_value), progress + MIN_TIMEOUT));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        int matcher = sharedPref.getMatcherTypeInt();
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
        sharedPref.setQualityThresholdInt(qualitySeekBar.getProgress() + MIN_QUALITY);
    }


    private void saveNbOfIds() {
        sharedPref.setReturnIdCountInt(nbOfIdsSeekBar.getProgress() + MIN_NB_OF_IDS);
    }

    private void saveTimeout() {
        sharedPref.setTimeoutInt(timeoutSeekBar.getProgress() + MIN_TIMEOUT);
    }

    private void saveNudgeMode() {
        sharedPref.setNudgeModeBool(nudgeToggleButton.isChecked());
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
                sharedPref.setLanguageString("");
                sharedPref.setLanguagePositionInt(0);

                break;
            case 1:
                sharedPref.setLanguageString("ne");
                sharedPref.setLanguagePositionInt(1);
                break;
            case 2:

                sharedPref.setLanguageString("bn");
                sharedPref.setLanguagePositionInt(2);
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
                    sharedPref.setMatcherTypeInt(0);
                break;
            case R.id.radio_sourceAfis:
                if (checked)
                    sharedPref.setMatcherTypeInt(1);
                break;
        }
    }
}
