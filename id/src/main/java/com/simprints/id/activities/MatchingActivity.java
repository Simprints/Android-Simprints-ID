package com.simprints.id.activities;

import android.content.Intent;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.simprints.id.R;
import com.simprints.id.tools.Analytics;
import com.simprints.id.tools.AppState;
import com.simprints.id.tools.Language;
import com.simprints.id.tools.Log;
import com.simprints.id.tools.SharedPrefHelper;
import com.simprints.libcommon.Person;
import com.simprints.libdata.DatabaseEventListener;
import com.simprints.libdata.Event;
import com.simprints.libmatcher.EVENT;
import com.simprints.libmatcher.LibMatcher;
import com.simprints.libmatcher.Progress;
import com.simprints.libmatcher.sourceafis.MatcherEventListener;
import com.simprints.libsimprints.Constants;
import com.simprints.libsimprints.Identification;
import com.simprints.libsimprints.Tier;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public class MatchingActivity extends AppCompatActivity implements DatabaseEventListener, MatcherEventListener {

    private AppState appState;
    private Person probe;
    private List<Person> candidates = new ArrayList<>();
    private List<Float> scores = new ArrayList<>();
    private ProgressBar progressBar;
    private Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getBaseContext().getResources().updateConfiguration(Language.selectLanguage(
                getApplicationContext()), getBaseContext().getResources().getDisplayMetrics());
        setContentView(R.layout.activity_matching);

        appState = AppState.getInstance();
        appState.getData().setListener(this);

        progressBar = (ProgressBar) findViewById(R.id.pb_identification);
        int progressBarColor = ContextCompat.getColor(this, R.color.simprints_blue);
        progressBar.getIndeterminateDrawable().setColorFilter(
                progressBarColor, PorterDuff.Mode.SRC_IN);
        progressBar.getProgressDrawable().setColorFilter(
                progressBarColor, PorterDuff.Mode.SRC_IN);

        Bundle extras = getIntent().getExtras();
        probe = extras.getParcelable("Person");

        appState.getData().loadPeople(candidates);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Analytics.getInstance(getApplicationContext()).setActivity(this, "Matching Screen");
    }

    @Override
    public void onDataEvent(Event event) {
        switch (event) {
            case LOAD_PEOPLE_FINISHED:
                Log.d(MatchingActivity.this, String.format(Locale.UK,
                        "Successfully loaded %d candidates", candidates.size()));

                int matcherType = new SharedPrefHelper(getApplicationContext()).getMatcherTypeInt();

                final LibMatcher.MATCHER_TYPE matcher_type;

                switch (matcherType) {
                    case 0:
                        matcher_type = LibMatcher.MATCHER_TYPE.SIMAFIS_IDENTIFY;
                        break;
                    case 1:
                        matcher_type = LibMatcher.MATCHER_TYPE.SOURCEAFIS;
                        break;
                    default:
                        matcher_type = LibMatcher.MATCHER_TYPE.SIMAFIS_IDENTIFY;
                }

                // Start lengthy operation in a background thread
                new Thread(new Runnable() {
                    public void run() {
                        LibMatcher matcher = new LibMatcher(probe, candidates,
                                matcher_type, scores, MatchingActivity.this, 1);
                        matcher.start();
                    }
                }).start();
                break;
        }
    }

    private Tier computeTier(float score) {
        if (score < 20) {
            return Tier.TIER_5;
        } else if (score < 40) {
            return Tier.TIER_4;
        } else if (score < 60) {
            return Tier.TIER_3;
        } else if (score < 80) {
            return Tier.TIER_2;
        } else {
            return Tier.TIER_1;
        }
    }

    @Override
    public void onMatcherEvent(EVENT event) {
        switch (event) {
            case MATCH_NOT_RUNNING:
                Toast.makeText(MatchingActivity.this, event.details(), Toast.LENGTH_LONG).show();
                break;
            case MATCH_COMPLETED:
                int nbOfResults = new SharedPrefHelper(getApplicationContext()).getReturnIdCountInt();

                ArrayList<Identification> topCandidates = new ArrayList<>();

                // Sort the indices of the person by decreasing score
                final Integer[] idx = new Integer[candidates.size()];
                for (int i = 0; i < candidates.size(); i++) {
                    idx[i] = i;
                }

                Arrays.sort(idx, new Comparator<Integer>() {
                    @Override
                    public int compare(final Integer i1, final Integer i2) {
                        return Float.compare(scores.get(i2), scores.get(i1));
                    }
                });

                for (int i = 0; i < Math.min(nbOfResults, candidates.size()); i++) {
                    Person candidate = candidates.get(idx[i]);

                    topCandidates.add(new Identification(candidate.getGuid(),
                            scores.get(idx[i]).intValue(), computeTier(scores.get(idx[i]))));
                }

                if (appState.getData() != null && topCandidates.size() > 0) {
                    appState.getData().saveIdentification(probe, topCandidates);
                }

                // finish
                Intent resultData = new Intent(Constants.SIMPRINTS_IDENTIFY_INTENT);
                resultData.putExtra(Constants.SIMPRINTS_IDENTIFICATIONS, topCandidates);
                setResult(RESULT_OK, resultData);
                finish();
                break;
        }
    }

    @Override
    public void onMatcherProgress(final Progress progress) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                progressBar.setProgress(progress.getProgress());
            }
        });
    }
}
