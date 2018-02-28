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

import com.simprints.id.Application;
import com.simprints.id.R;
import com.simprints.id.data.DataManager;
import com.simprints.id.tools.LanguageHelper;
import com.simprints.libdata.tools.Constants;

public class SettingsActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

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

    private DataManager dataManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Application app = ((Application) getApplication());
        dataManager = app.getDataManager();

        LanguageHelper.setLanguage(this, dataManager.getLanguage());
        setContentView(R.layout.activity_settings);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        Toolbar toolbar = findViewById(R.id.toolbar_settings);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.show();
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        //Set language spinner
        Spinner spinner = findViewById(R.id.language_spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.language_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(this);
        spinner.setSelection(dataManager.getLanguagePosition());

        //Set nudge mode
        boolean nudgeMode = dataManager.getNudgeMode();
        nudgeToggleButton = findViewById(R.id.nudgeToggleButton);
        nudgeToggleButton.setChecked(nudgeMode);
        nudgeToggleButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                dataManager.setNudgeMode(nudgeToggleButton.isChecked());
            }
        });

        //Set vibrate mode
        boolean vibrate = dataManager.getVibrateMode();
        vibrateToggleButton = findViewById(R.id.vibrateToggleButton);
        vibrateToggleButton.setChecked(vibrate);
        vibrateToggleButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                dataManager.setVibrateMode(vibrateToggleButton.isChecked());
            }
        });

        //Set the quality score threshold
        ((TextView) findViewById(R.id.minQualityTextView)).setText(String.valueOf(MIN_QUALITY));
        ((TextView) findViewById(R.id.maxQualityTextView)).setText(String.valueOf(MAX_QUALITY));
        final TextView qualityThresholdTextView = findViewById(R.id.qualityTextView);
        final int qualityThreshold = dataManager.getQualityThreshold() - MIN_QUALITY;
        qualitySeekBar = findViewById(R.id.qualitySeekBar);
        qualitySeekBar.setMax(MAX_QUALITY - MIN_QUALITY);
        qualitySeekBar.setProgress(qualityThreshold);
        qualityThresholdTextView.setText(String.format(
                getString(R.string.quality_threshold_value), qualityThreshold + MIN_QUALITY));
        qualitySeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean b) {
                dataManager.setQualityThreshold(qualitySeekBar.getProgress() + MIN_QUALITY);
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
        final TextView nbOfIdsTextView = findViewById(R.id.nbOfIdsTextView);
        final int nbOfIds = dataManager.getReturnIdCount() - MIN_NB_OF_IDS;
        nbOfIdsSeekBar = findViewById(R.id.nbOfIdsSeekBar);
        nbOfIdsSeekBar.setMax(MAX_NB_OF_IDS - MIN_NB_OF_IDS);
        nbOfIdsSeekBar.setProgress(nbOfIds);
        nbOfIdsTextView.setText(String.format(
                getString(R.string.nb_of_ids_value), nbOfIds + MIN_NB_OF_IDS));
        nbOfIdsSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean b) {
                dataManager.setReturnIdCount(nbOfIdsSeekBar.getProgress() + MIN_NB_OF_IDS);
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
        final TextView tv_timeout = findViewById(R.id.tv_timeout);
        final int timeout = dataManager.getTimeoutS() - MIN_TIMEOUT;
        timeoutSeekBar = findViewById(R.id.sb_timeout);
        timeoutSeekBar.setMax(MAX_TIMEOUT - MIN_TIMEOUT);
        timeoutSeekBar.setProgress(timeout);
        tv_timeout.setText(String.format(
                getString(R.string.timeout_value), timeout + MIN_TIMEOUT));
        timeoutSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean b) {
                dataManager.setTimeoutS(timeoutSeekBar.getProgress() + MIN_TIMEOUT);
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
        final TextView tv_idWaitTime = findViewById(R.id.tv_id_wait_time);
        final int idWaitTime = dataManager.getMatchingEndWaitTimeSeconds() - MIN_ID_WAIT_TIME;
        idWaitTimeSeekBar = findViewById(R.id.sb_id_wait_time);
        idWaitTimeSeekBar.setMax(MAX_ID_WAIT_TIME - MIN_ID_WAIT_TIME);
        idWaitTimeSeekBar.setProgress(idWaitTime);
        tv_idWaitTime.setText(String.format(
                getString(R.string.id_wait_time_value), idWaitTime + MIN_ID_WAIT_TIME));
        idWaitTimeSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                dataManager.setMatchingEndWaitTimeSeconds(idWaitTimeSeekBar.getProgress() + MIN_ID_WAIT_TIME);
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
        Constants.GROUP syncGroup = dataManager.getSyncGroup();
        switch (syncGroup) {
            case GLOBAL:
                ((RadioButton) findViewById(R.id.rb_globalSyncGroup)).setChecked(true);
                break;
            case USER:
                ((RadioButton) findViewById(R.id.rb_userSyncGroup)).setChecked(true);
                break;
        }

        //Set the match group radio buttons
        Constants.GROUP matchGroup = dataManager.getMatchGroup();
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
        int matcher = dataManager.getMatcherType();
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
                dataManager.setLanguage("");
                dataManager.setLanguagePosition(0);

                break;
            case 1:
                dataManager.setLanguage("ne");
                dataManager.setLanguagePosition(1);
                break;
            case 2:

                dataManager.setLanguage("bn");
                dataManager.setLanguagePosition(2);
                break;
            case 3:
                dataManager.setLanguage("ps");
                dataManager.setLanguagePosition(3);
                break;
            case 4:
                dataManager.setLanguage("fa-rAF");
                dataManager.setLanguagePosition(4);
            case 5:
                dataManager.setLanguage("so");
                dataManager.setLanguagePosition(5);
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
                    dataManager.setMatcherType(0);
                break;
            case R.id.radio_sourceAfis:
                if (checked)
                    dataManager.setMatcherType(1);
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
                    dataManager.setSyncGroup(Constants.GROUP.USER);
                break;
            case R.id.rb_globalSyncGroup:
                if (checked)
                    dataManager.setSyncGroup(Constants.GROUP.GLOBAL);
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
                    dataManager.setMatchGroup(Constants.GROUP.USER);
                break;
            case R.id.rb_moduleMatchGroup:
                if (checked)
                    dataManager.setMatchGroup(Constants.GROUP.MODULE);
                break;
            case R.id.rb_globalMatchGroup:
                if (checked)
                    dataManager.setMatchGroup(Constants.GROUP.GLOBAL);
                break;
        }
    }
}
