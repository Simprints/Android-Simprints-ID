package com.simprints.id.activities;

import android.animation.ObjectAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.crash.FirebaseCrash;
import com.simprints.id.Application;
import com.simprints.id.R;
import com.simprints.id.data.DataManager;
import com.simprints.id.model.ALERT_TYPE;
import com.simprints.id.tools.AppState;
import com.simprints.id.tools.FormatResult;
import com.simprints.id.tools.Language;
import com.simprints.id.tools.Log;
import com.simprints.id.tools.SharedPref;
import com.simprints.libcommon.Person;
import com.simprints.libdata.DATA_ERROR;
import com.simprints.libdata.DataCallback;
import com.simprints.libdata.models.enums.VERIFY_GUID_EXISTS_RESULT;
import com.simprints.libdata.tools.Constants.GROUP;
import com.simprints.libmatcher.EVENT;
import com.simprints.libmatcher.LibMatcher;
import com.simprints.libmatcher.Progress;
import com.simprints.libmatcher.sourceafis.MatcherEventListener;
import com.simprints.libsimprints.Constants;
import com.simprints.libsimprints.Identification;
import com.simprints.libsimprints.Verification;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import static com.simprints.id.tools.ResourceHelper.getStringPlural;
import static com.simprints.id.tools.TierHelper.computeTier;

public class MatchingActivity extends AppCompatActivity implements MatcherEventListener {

    private final static int ALERT_ACTIVITY_REQUEST_CODE = 0;

    private MatchingView matchingView;

    private DataManager dataManager;
    private AppState appState;
    private Person probe;
    private List<Person> candidates = new ArrayList<>();
    private List<Float> scores = new ArrayList<>();
    private Handler handler = new Handler();
    private OnMatchStartHandlerThread onMatchStartHandlerThread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initViews();

        Application app = ((Application) getApplication());
        dataManager = app.getDataManager();
        appState = app.getAppState();
        appState.logMatchStart();

        Bundle extras = getIntent().getExtras();
        probe = extras.getParcelable("Person");

        switch (dataManager.getCallout()) {
            case IDENTIFY:
                final Runnable onMatchStartRunnable = new Runnable() {
                    @Override
                    public void run() {
                        onIdentifyStart();
                    }
                };

                onMatchStartHandlerThread = new OnMatchStartHandlerThread("onMatchStartHandlerThread");
                onMatchStartHandlerThread.start();
                onMatchStartHandlerThread.prepareHandler();
                onMatchStartHandlerThread.postTask(onMatchStartRunnable);
                matchingView.setIdentificationProgressLoadingStart();
                break;
            case VERIFY:
                matchingView.setVerificationProgress();
                onVerifyStart();
                break;
            default:
                FirebaseCrash.report(new IllegalArgumentException("Illegal callout in MatchingActivity.onCreate()"));
                launchAlert(ALERT_TYPE.UNEXPECTED_ERROR);
        }
    }

    private void initViews() {
        getBaseContext().getResources().updateConfiguration(Language.selectLanguage(
                getApplicationContext()), getBaseContext().getResources().getDisplayMetrics());
        setContentView(R.layout.activity_matching);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        matchingView = new MatchingView();
    }

    private class MatchingView {

        private ProgressBar progressBar;
        private TextView progressText1;
        private TextView progressText2;
        private TextView resultText1;
        private TextView resultText2;
        private TextView resultText3;

        MatchingView() {
            initProgressBar();
            initProgressTextViews();
        }

        private void initProgressBar() {
            progressBar = (ProgressBar) findViewById(R.id.pb_identification);
        }

        private void initProgressTextViews() {
            progressText1 = (TextView) findViewById(R.id.tv_matchingProgressStatus1);
            progressText2 = (TextView) findViewById(R.id.tv_matchingProgressStatus2);
            resultText1 = (TextView) findViewById(R.id.tv_matchingResultStatus1);
            resultText2 = (TextView) findViewById(R.id.tv_matchingResultStatus2);
            resultText3 = (TextView) findViewById(R.id.tv_matchingResultStatus3);
        }

        void setProgress(int progress) {
            ObjectAnimator.ofInt(progressBar, "progress", progressBar.getProgress(), progress)
                    .setDuration(progress * 10)
                    .start();
        }

        void setVerificationProgress() {
            setProgress(100);
        }

        void setIdentificationProgressLoadingStart() {
            progressText1.setText(R.string.loading_candidates);
            setProgress(25);
        }

        void setIdentificationProgressMatchingStart(int matchSize) {
            progressText1.setText(getStringPlural(MatchingActivity.this, R.string.loaded_candidates_quantity_key, matchSize, matchSize));
            progressText2.setText(R.string.matching_fingerprints);
            setProgress(50);
        }

        void setIdentificationProgressReturningStart() {
            progressText2.setText(R.string.returning_results);
            setProgress(90);
        }

        void setIdentificationProgressFinished(int returnSize, int tier1Or2Matches, int tier3Matches, int tier4Matches) {
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
            setProgress(100);

            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                public void run() {
                    finish();
                }
            }, new SharedPref(getApplicationContext()).getMatchingEndWaitTime() * 1000);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private void launchAlert(ALERT_TYPE alertType) {
        Intent intent = new Intent(this, AlertActivity.class);
        intent.putExtra("alertType", alertType);
        startActivityForResult(intent, ALERT_ACTIVITY_REQUEST_CODE);
    }

    private class OnMatchStartHandlerThread extends HandlerThread {

        Handler handler;

        OnMatchStartHandlerThread(String name) {
            super(name);
        }

        void postTask(Runnable task) {
            handler.post(task);
        }

        void prepareHandler() {
            handler = new Handler(getLooper());
        }
    }

    private void onIdentifyStart() {
        final GROUP matchGroup = new SharedPref(getApplicationContext()).getMatchGroup();

        appState.getData().loadPeople(candidates, matchGroup, new DataCallback() {
            @Override
            public void onSuccess() {
                Log.d(MatchingActivity.this, String.format(Locale.UK,
                        "Successfully loaded %d candidates", candidates.size()));
                matchingView.setIdentificationProgressMatchingStart(candidates.size());

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
                FirebaseCrash.report(new Exception("Unknown error returned in onFailure MatchingActivity.onIdentifyStart()"));
                launchAlert(ALERT_TYPE.UNEXPECTED_ERROR);
            }
        });
    }

    private void onVerifyStart() {
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
                FirebaseCrash.report(new Exception("Unknown error returned in onFailure MatchingActivity.onVerifyStart()"));
                launchAlert(ALERT_TYPE.UNEXPECTED_ERROR);
            }
        });
    }

    @Override
    public void onMatcherEvent(EVENT event) {
        switch (event) {
            case MATCH_NOT_RUNNING: {
                Toast.makeText(MatchingActivity.this, event.details(), Toast.LENGTH_LONG).show();
                break;
            }
            case MATCH_COMPLETED: {
                switch (dataManager.getCallout()) {
                    case IDENTIFY: {
                        onMatchStartHandlerThread.quit();
                        matchingView.setIdentificationProgressReturningStart();
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
                            appState.getData().saveIdentification(probe, candidates.size(), topCandidates, appState.getSessionId());
                        }

                        // finish
                        int tier1Or2Matches = 0;
                        int tier3Matches = 0;
                        int tier4Matches = 0;
                        for (Identification identification : topCandidates) {
                            switch (identification.getTier()) {
                                case TIER_1:
                                case TIER_2:
                                    tier1Or2Matches++;
                                    break;
                                case TIER_3:
                                    tier3Matches++;
                                    break;
                                case TIER_4:
                                    tier4Matches++;
                                    break;
                            }
                        }

                        Intent resultData;
                        resultData = new Intent(Constants.SIMPRINTS_IDENTIFY_INTENT);
                        FormatResult.put( resultData, topCandidates, appState);
                        setResult(RESULT_OK, resultData);
                        matchingView.setIdentificationProgressFinished(topCandidates.size(),
                                tier1Or2Matches, tier3Matches, tier4Matches);
                        break;
                    }
                    case VERIFY: {
                        Verification verification;
                        VERIFY_GUID_EXISTS_RESULT guidExistsResult;
                        int resultCode;

                        if (candidates.size() > 0 && scores.size() > 0) {
                            int score = scores.get(0).intValue();
                            verification = new Verification(score, computeTier(score), appState.getGuid());
                            guidExistsResult = VERIFY_GUID_EXISTS_RESULT.GUID_FOUND;
                            resultCode = RESULT_OK;
                        } else {
                            verification = null;
                            guidExistsResult = VERIFY_GUID_EXISTS_RESULT.GUID_NOT_FOUND_UNKNOWN;
                            resultCode = Constants.SIMPRINTS_VERIFY_GUID_NOT_FOUND_ONLINE;
                        }

                        if (appState.getData() != null) {
                            appState.getData().saveVerification(probe, appState.getGuid(), verification, appState.getSessionId(), guidExistsResult);
                        }

                        // finish
                        Intent resultData;
                        resultData = new Intent(Constants.SIMPRINTS_VERIFY_INTENT);
                        FormatResult.put(resultData, verification, appState);
                        setResult(resultCode, resultData);
                        finish();
                        break;
                    }
                }
                break;
            }
        }
    }

    // TODO Remove or fix this
    @Override
    public void onMatcherProgress(final Progress progress) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                matchingView.setProgress(progress.getProgress());
            }
        });
    }
}
