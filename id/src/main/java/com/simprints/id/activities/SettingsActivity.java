package com.simprints.id.activities;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
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
import com.simprints.libdata.tools.Constants;

public class SettingsActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {
    private SharedPref sharedPref;

    private final static int MIN_QUALITY = 40;
    private final static int MAX_QUALITY = 99;
    private final static int MIN_NB_OF_IDS = 1;
    private final static int MAX_NB_OF_IDS = 20;
    private final static int MIN_TIMEOUT = 1;
    private final static int MAX_TIMEOUT = 10;
    private final static int MIN_ID_WAIT_TIME = 0;
    private final static int MAX_ID_WAIT_TIME = 10;
    ToggleButton nudgeToggleButton;
    ToggleButton vibrateToggleButton;
    SeekBar qualitySeekBar;
    SeekBar nbOfIdsSeekBar;
    SeekBar timeoutSeekBar;
    SeekBar idWaitTimeSeekBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getBaseContext().getResources().updateConfiguration(Language.selectLanguage(
                getApplicationContext()), getBaseContext().getResources().getDisplayMetrics());
        setContentView(R.layout.activity_settings);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_settings);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.show();
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        sharedPref = new SharedPref(getApplicationContext());

        //Set language spinner
        Spinner spinner = (Spinner) findViewById(R.id.language_spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.language_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(this);
        spinner.setSelection(sharedPref.getLanguagePositionInt());

        //Set nudge mode
        boolean nudgeMode = sharedPref.getNudgeModeBool();
        nudgeToggleButton = (ToggleButton) findViewById(R.id.nudgeToggleButton);
        nudgeToggleButton.setChecked(nudgeMode);
        nudgeToggleButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                sharedPref.setNudgeModeBool(nudgeToggleButton.isChecked());
            }
        });

        //Set vibrate mode
        boolean vibrate = sharedPref.getVibrateBool();
        vibrateToggleButton = (ToggleButton) findViewById(R.id.vibrateToggleButton);
        vibrateToggleButton.setChecked(vibrate);
        vibrateToggleButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                sharedPref.setVibrateBool(vibrateToggleButton.isChecked());
            }
        });

        //Set the quality score threshold
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
                sharedPref.setQualityThresholdInt(qualitySeekBar.getProgress() + MIN_QUALITY);
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

        //Set the return # of IDs
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
                sharedPref.setReturnIdCountInt(nbOfIdsSeekBar.getProgress() + MIN_NB_OF_IDS);
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

        //Set the timeout slider
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
                sharedPref.setTimeoutInt(timeoutSeekBar.getProgress() + MIN_TIMEOUT);
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

        //Set the id wait time slider
        ((TextView) findViewById(R.id.tv_min_id_wait_time)).setText(String.valueOf(MIN_ID_WAIT_TIME));
        ((TextView) findViewById(R.id.tv_max_id_wait_time)).setText(String.valueOf(MAX_ID_WAIT_TIME));
        final TextView tv_idWaitTime = (TextView) findViewById(R.id.tv_id_wait_time);
        final int idWaitTime = sharedPref.getMatchingEndWaitTime() - MIN_ID_WAIT_TIME;
        idWaitTimeSeekBar = (SeekBar) findViewById(R.id.sb_id_wait_time);
        idWaitTimeSeekBar.setMax(MAX_ID_WAIT_TIME - MIN_ID_WAIT_TIME);
        idWaitTimeSeekBar.setProgress(idWaitTime);
        tv_idWaitTime.setText(String.format(
                getString(R.string.id_wait_time_value), idWaitTime + MIN_ID_WAIT_TIME));
        idWaitTimeSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                sharedPref.setMatchingEndWaitTime(idWaitTimeSeekBar.getProgress() + MIN_ID_WAIT_TIME);
                tv_idWaitTime.setText(String.format(
                        getString(R.string.id_wait_time_value), progress + MIN_ID_WAIT_TIME));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        //Set the sync group radio buttons
        Constants.GROUP syncGroup = sharedPref.getSyncGroup();
        switch (syncGroup) {
            case GLOBAL:
                ((RadioButton) findViewById(R.id.rb_globalSyncGroup)).setChecked(true);
                break;
            case USER:
                ((RadioButton) findViewById(R.id.rb_userSyncGroup)).setChecked(true);
                break;
        }

        //Set the match group radio buttons
        Constants.GROUP matchGroup = sharedPref.getMatchGroup();
        switch (matchGroup) {
            case GLOBAL:
                ((RadioButton) findViewById(R.id.rb_globalMatchGroup)).setChecked(true);
                break;
            case USER:
                ((RadioButton) findViewById(R.id.rb_userMatchGroup)).setChecked(true);
                break;
            case MODULE:
                ((RadioButton) findViewById(R.id.rb_moduleMatchGroup)).setChecked(true);
                break;
        }

        //Set the matcher radio buttons
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

    public void onAfisSelectionClicked(View view) {
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

    public void onSyncSelectionClicked(View view) {
        // Is the button now checked?
        boolean checked = ((RadioButton) view).isChecked();

        // Check which radio button was clicked
        switch (view.getId()) {
            case R.id.rb_userSyncGroup:
                if (checked)
                    sharedPref.setSyncGroup(Constants.GROUP.USER);
                break;
            case R.id.rb_globalSyncGroup:
                if (checked)
                    sharedPref.setSyncGroup(Constants.GROUP.GLOBAL);
                break;
        }
    }

    public void onMatchSelectionClicked(View view) {
        // Is the button now checked?
        boolean checked = ((RadioButton) view).isChecked();

        // Check which radio button was clicked
        switch (view.getId()) {
            case R.id.rb_userMatchGroup:
                if (checked)
                    sharedPref.setMatchGroup(Constants.GROUP.USER);
                break;
            case R.id.rb_moduleMatchGroup:
                if (checked)
                    sharedPref.setMatchGroup(Constants.GROUP.MODULE);
                break;
            case R.id.rb_globalMatchGroup:
                if (checked)
                    sharedPref.setMatchGroup(Constants.GROUP.GLOBAL);
                break;
        }
    }
}
