package com.simprints.id.activities.matching;

import android.animation.ObjectAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.simprints.id.Application;
import com.simprints.id.R;
import com.simprints.id.activities.alert.AlertActivity;
import com.simprints.id.activities.IntentKeys;
import com.simprints.id.data.analytics.AnalyticsManager;
import com.simprints.id.data.prefs.PreferencesManager;
import com.simprints.id.di.AppComponent;
import com.simprints.id.domain.ALERT_TYPE;
import com.simprints.id.exceptions.unsafe.NoIntentExtrasError;
import com.simprints.id.tools.LanguageHelper;
import com.simprints.id.tools.TimeHelper;
import com.simprints.libcommon.Person;

import javax.inject.Inject;

import androidx.appcompat.app.AppCompatActivity;

import static com.simprints.id.tools.utils.AndroidResourcesHelperImpl.getStringPlural;

public class MatchingActivity extends AppCompatActivity implements MatchingContract.View {

    private final static int ALERT_ACTIVITY_REQUEST_CODE = 0;

    private MatchingContract.Presenter viewPresenter;

    private ProgressBar progressBar;
    private TextView progressText1;
    private TextView progressText2;
    private TextView resultText1;
    private TextView resultText2;
    private TextView resultText3;

    @Inject PreferencesManager preferencesManager;
    @Inject AnalyticsManager analyticsManager;
    @Inject TimeHelper timeHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppComponent component = ((Application) getApplication()).getComponent();
        component.inject(this);

        LanguageHelper.setLanguage(this, preferencesManager.getLanguage());
        setContentView(R.layout.activity_matching);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        progressBar = findViewById(R.id.pb_identification);
        progressText1 = findViewById(R.id.tv_matchingProgressStatus1);
        progressText2 = findViewById(R.id.tv_matchingProgressStatus2);
        resultText1 = findViewById(R.id.tv_matchingResultStatus1);
        resultText2 = findViewById(R.id.tv_matchingResultStatus2);
        resultText3 = findViewById(R.id.tv_matchingResultStatus3);

        // Create the Presenter, and pass it all the information and handles it needs
        final Bundle extras = getIntent().getExtras();
        if (extras == null) {
            analyticsManager.logError(new NoIntentExtrasError("Null extras passed to MatchingActivity"));
            launchAlert();
            finish();
            return;
        }
        Person probe = extras.getParcelable(IntentKeys.matchingActivityProbePersonKey);
        viewPresenter = new MatchingPresenter(this, component, probe);
    }

    @Override
    protected void onResume() {
        super.onResume();
        viewPresenter.start();
    }

    @Override
    public void setIdentificationProgress(int progress) {
        ObjectAnimator.ofInt(progressBar, "progress", progressBar.getProgress(), progress)
                .setDuration(progress * 10)
                .start();
    }

    @Override
    public void setVerificationProgress() {
        setIdentificationProgress(100);
    }

    @Override
    public void setIdentificationProgressLoadingStart() {
        progressText1.setText(R.string.loading_candidates);
        setIdentificationProgress(25);
    }

    @Override
    public void setIdentificationProgressMatchingStart(int matchSize) {
        progressText1.setText(getStringPlural(MatchingActivity.this, R.string.loaded_candidates_quantity_key, matchSize, matchSize));
        progressText2.setText(R.string.matching_fingerprints);
        setIdentificationProgress(50);
    }

    @Override
    public void setIdentificationProgressReturningStart() {
        progressText2.setText(R.string.returning_results);
        setIdentificationProgress(90);
    }

    @Override
    public void setIdentificationProgressFinished(int returnSize, int tier1Or2Matches, int tier3Matches, int tier4Matches, int matchingEndWaitTimeMillis) {
        progressText2.setText(getStringPlural(MatchingActivity.this, R.string.returned_results_quantity_key, returnSize, returnSize));

        if (tier1Or2Matches > 0) {
            resultText1.setVisibility(View.VISIBLE);
            resultText1.setText(getStringPlural(MatchingActivity.this, R.string.tier1or2_matches_quantity_key, tier1Or2Matches, tier1Or2Matches));
        }
        if (tier3Matches > 0) {
            resultText2.setVisibility(View.VISIBLE);
            resultText2.setText(getStringPlural(MatchingActivity.this, R.string.tier3_matches_quantity_key, tier3Matches, tier3Matches));
        }
        if ((tier1Or2Matches < 1 && tier3Matches < 1) || tier4Matches > 1) {
            resultText3.setVisibility(View.VISIBLE);
            resultText3.setText(getStringPlural(MatchingActivity.this, R.string.tier4_matches_quantity_key, tier4Matches, tier4Matches));
        }
        setIdentificationProgress(100);

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {
                finish();
            }
        }, matchingEndWaitTimeMillis);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void launchAlert() {
        Intent intent = new Intent(this, AlertActivity.class);
        intent.putExtra(IntentKeys.alertActivityAlertTypeKey, ALERT_TYPE.UNEXPECTED_ERROR);
        startActivityForResult(intent, ALERT_ACTIVITY_REQUEST_CODE);
    }

    @Override
    public void makeToastMatchNotRunning(String text) {
        Toast.makeText(MatchingActivity.this, text, Toast.LENGTH_LONG).show();
    }

    @Override
    public void doSetResult(int resultCode, Intent resultData) {
        setResult(resultCode, resultData);
    }

    @Override
    public void doFinish() {
        finish();
    }

    @Override
    public MatchingContract.Presenter getViewPresenter() {
        return viewPresenter;
    }

    @Override
    public void setViewPresenter(MatchingContract.Presenter presenter) {
        viewPresenter = presenter;
    }
}
