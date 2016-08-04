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
import com.simprints.id.tools.AppState;
import com.simprints.id.tools.Log;
import com.simprints.libcommon.Person;
import com.simprints.libdata.Data;
import com.simprints.libdata.EVENT;
import com.simprints.libmatcher.LibMatcher;
import com.simprints.libmatcher.Progress;
import com.simprints.libsimprints.Constants;
import com.simprints.libsimprints.Identification;
import com.simprints.libsimprints.Tier;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public class MatchingActivity extends AppCompatActivity implements Data.DataListener, LibMatcher.MatcherListener {

    private AppState appState;
    private Person probe;
    private List<Person> candidates;
    private ProgressBar progressBar;
    private Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_matching);

        appState = AppState.getInstance();
        appState.getData().setDataListener(this);

        progressBar = (ProgressBar) findViewById(R.id.progress_bar);
        int progressBarColor = ContextCompat.getColor(this, R.color.simprints_blue);
        progressBar.getIndeterminateDrawable().setColorFilter(
                progressBarColor, PorterDuff.Mode.SRC_IN);
        progressBar.getProgressDrawable().setColorFilter(
                progressBarColor, PorterDuff.Mode.SRC_IN);

        Bundle extras = getIntent().getExtras();
        probe = extras.getParcelable("Person");

        appState.getData().loadPeople(appState.getApiKey());
    }

    @Override
    public void onDataEvent(EVENT event) {
        switch (event) {
            case GET_PEOPLE_SUCCESS:
                candidates = appState.getData().getPeople();
                Log.d(MatchingActivity.this, String.format(Locale.UK,
                        "Succesfully loaded %d candidates", candidates.size()));

                // Start lengthy operation in a background thread
                new Thread(new Runnable() {
                    public void run() {
                        LibMatcher matcher = new LibMatcher(probe, candidates, LibMatcher.MATCHER_TYPE.SOURCEAFIS, 10);
                        matcher.setMatcherListener(MatchingActivity.this);
                        matcher.start();
                    }
                }).start();
                break;
            case GET_PEOPLE_FAILURE:
                Log.d(this, "Error loading candidates");
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
    public void onMatcherEvent(com.simprints.libmatcher.EVENT e) {
        switch (e) {
            case MATCH_ALREADY_RUNNING:
            case MATCH_CANCELLED:
            case MATCH_NOT_RUNNING:
                Toast.makeText(MatchingActivity.this, e.details(), Toast.LENGTH_LONG).show();
                break;
            case MATCH_COMPLETED:
                // Get the top 5 (or less) candidates and their scores
                Collections.sort(candidates, new Comparator<Person>() {
                    @Override
                    public int compare(Person person1, Person person2) {
                        return person2.getScore() - person1.getScore();
                    }
                });
                ArrayList<Identification> top5Candidates = new ArrayList<>();
                for (Person candidate : candidates) {
                    top5Candidates.add(new Identification(candidate.getGuid(),
                            candidate.getScore(), computeTier(candidate.getScore())));
                    if (top5Candidates.size() == 5) {
                        break;
                    }
                }
                // finish
                Intent resultData = new Intent(Constants.SIMPRINTS_IDENTIFY_INTENT);
                resultData.putExtra(Constants.SIMPRINTS_IDENTIFICATIONS, top5Candidates);
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
