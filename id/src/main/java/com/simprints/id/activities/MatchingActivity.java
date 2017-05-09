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
import com.simprints.id.model.Callout;
import com.simprints.id.tools.AppState;
import com.simprints.id.tools.Language;
import com.simprints.id.tools.Log;
import com.simprints.id.tools.SharedPref;
import com.simprints.libcommon.Person;
import com.simprints.libdata.DATA_ERROR;
import com.simprints.libdata.DataCallback;
import com.simprints.libdata.models.enums.VERIFY_GUID_EXISTS_RESULT;
import com.simprints.libmatcher.EVENT;
import com.simprints.libmatcher.LibMatcher;
import com.simprints.libmatcher.Progress;
import com.simprints.libmatcher.sourceafis.MatcherEventListener;
import com.simprints.libsimprints.Constants;
import com.simprints.libsimprints.Identification;
import com.simprints.libsimprints.Tier;
import com.simprints.libdata.tools.Constants.GROUP;
import com.simprints.libsimprints.Verification;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public class MatchingActivity extends AppCompatActivity implements MatcherEventListener {

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
        appState.logMatchStart();

        progressBar = (ProgressBar) findViewById(R.id.pb_identification);
        int progressBarColor = ContextCompat.getColor(this, R.color.simprints_blue);
        progressBar.getIndeterminateDrawable().setColorFilter(
                progressBarColor, PorterDuff.Mode.SRC_IN);
        progressBar.getProgressDrawable().setColorFilter(
                progressBarColor, PorterDuff.Mode.SRC_IN);

        Bundle extras = getIntent().getExtras();
        probe = extras.getParcelable("Person");

        Callout callout = appState.getCallout();

        // Do different things depending on the callout
        switch (callout) {
            case IDENTIFY:
                final GROUP matchGroup = new SharedPref(getApplicationContext()).getMatchGroup();

                appState.getData().loadPeople(candidates, matchGroup, new DataCallback() {
                    @Override
                    public void onSuccess() {
                        Log.d(MatchingActivity.this, String.format(Locale.UK,
                                "Successfully loaded %d candidates", candidates.size()));

                        int matcherType = new SharedPref(getApplicationContext()).getMatcherTypeInt();

                        final LibMatcher.MATCHER_TYPE matcher_type;

                        switch (matcherType) {
                            case 0:
                                matcher_type = LibMatcher.MATCHER_TYPE.SIMAFIS_IDENTIFY;
                                break;
                            case 1:
                                matcher_type = LibMatcher.MATCHER_TYPE.SOURCEAFIS_IDENTIFY;
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
                    }

                    @Override
                    public void onFailure(DATA_ERROR data_error) {
                        throw new RuntimeException();
                    }
                });
                break;
            case VERIFY:
                final String guid = appState.getGuid();

                appState.getData().loadPerson(candidates, guid, new DataCallback() {
                    @Override
                    public void onSuccess() {
                        Log.d(MatchingActivity.this, "Successfully loaded candidate");

                        int matcherType = new SharedPref(getApplicationContext()).getMatcherTypeInt();

                        final LibMatcher.MATCHER_TYPE matcher_type;

                        switch (matcherType) {
                            case 0:
                                matcher_type = LibMatcher.MATCHER_TYPE.SIMAFIS_VERIFY;
                                break;
                            case 1:
                                matcher_type = LibMatcher.MATCHER_TYPE.SOURCEAFIS_VERIFY;
                                break;
                            default:
                                matcher_type = LibMatcher.MATCHER_TYPE.SIMAFIS_VERIFY;
                        }

                        // Start lengthy operation in a background thread
                        new Thread(new Runnable() {
                            public void run() {
                                LibMatcher matcher = new LibMatcher(probe, candidates,
                                        matcher_type, scores, MatchingActivity.this, 1);
                                matcher.start();
                            }
                        }).start();

                    }

                    @Override
                    public void onFailure(DATA_ERROR data_error) {
                        throw new RuntimeException();
                    }
                });
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
            case MATCH_NOT_RUNNING: {
                Toast.makeText(MatchingActivity.this, event.details(), Toast.LENGTH_LONG).show();
                break;
            }
            case MATCH_COMPLETED: {
                Callout callout = appState.getCallout();
                switch (callout) {
                    case IDENTIFY: {
                        int nbOfResults = new SharedPref(getApplicationContext()).getReturnIdCountInt();

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

                        if (appState.getData() != null) {
                            appState.getData().saveIdentification(probe, topCandidates, appState.getSessionId());
                        }

                        // finish
                        Intent resultData;
                        resultData = new Intent(Constants.SIMPRINTS_IDENTIFY_INTENT);
                        resultData.putExtra(Constants.SIMPRINTS_IDENTIFICATIONS, topCandidates);
                        resultData.putExtra(Constants.SIMPRINTS_SESSION_ID, appState.getSessionId());
                        setResult(RESULT_OK, resultData);
                        break;
                    }
                    case VERIFY: {
                        Verification verification;
                        VERIFY_GUID_EXISTS_RESULT guidExistsResult;
                        int resultCode;

                        if (candidates.size() > 0 && scores.size() > 0) {
                            int score = scores.get(0).intValue();
                            verification = new Verification(score, computeTier(score));
                            guidExistsResult = VERIFY_GUID_EXISTS_RESULT.GUID_FOUND;
                            resultCode = RESULT_OK;
                        } else {
                            verification = null;
                            guidExistsResult = VERIFY_GUID_EXISTS_RESULT.GUID_NOT_FOUND_UNKNOWN;
                            resultCode = Constants.SIMPRINTS_VERIFY_GUID_NOT_FOUND_ONLINE;
                        }

                        if (appState.getData() != null) {
                            appState.getData().saveVerification(probe, verification, appState.getSessionId(), guidExistsResult);
                        }

                        // finish
                        Intent resultData;
                        resultData = new Intent(Constants.SIMPRINTS_VERIFY_INTENT);
                        resultData.putExtra(Constants.SIMPRINTS_VERIFICATIONS, verification);
                        resultData.putExtra(Constants.SIMPRINTS_SESSION_ID, appState.getSessionId());
                        setResult(resultCode, resultData);
                        break;
                    }
                }
                finish();
                break;
            }
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
