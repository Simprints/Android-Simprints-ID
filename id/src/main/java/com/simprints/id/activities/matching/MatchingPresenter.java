package com.simprints.id.activities.matching;


import android.content.Intent;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.annotation.NonNull;

import com.simprints.id.data.DataManager;
import com.simprints.id.exceptions.unsafe.FailedToLoadPeopleError;
import com.simprints.id.exceptions.unsafe.InvalidMatchingCalloutError;
import com.simprints.id.model.ALERT_TYPE;
import com.simprints.id.model.Callout;
import com.simprints.id.tools.AppState;
import com.simprints.id.tools.FormatResult;
import com.simprints.id.tools.Log;
import com.simprints.libcommon.Person;
import com.simprints.libdata.DATA_ERROR;
import com.simprints.libdata.DataCallback;
import com.simprints.libdata.models.enums.VERIFY_GUID_EXISTS_RESULT;
import com.simprints.libdata.tools.Constants;
import com.simprints.libmatcher.EVENT;
import com.simprints.libmatcher.LibMatcher;
import com.simprints.libmatcher.Progress;
import com.simprints.libmatcher.sourceafis.MatcherEventListener;
import com.simprints.libsimprints.Identification;
import com.simprints.libsimprints.Verification;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import static android.app.Activity.RESULT_OK;
import static com.simprints.id.tools.TierHelper.computeTier;

class MatchingPresenter implements MatchingContract.Presenter, MatcherEventListener {

    @NonNull
    private final MatchingContract.View matchingView;

    private Person probe;
    private List<Person> candidates = new ArrayList<>();
    private List<Float> scores = new ArrayList<>();
    private Handler handler = new Handler();
    private OnMatchStartHandlerThread onMatchStartHandlerThread;

    @NonNull
    private DataManager dataManager;

    MatchingPresenter(@NonNull MatchingContract.View matchingView,
                      @NonNull DataManager dataManager,
                      @NonNull AppState appState,
                      Person probe) {
        appState.logMatchStart();
        this.dataManager = dataManager;
        this.probe = probe;

        this.matchingView = matchingView;
        this.matchingView.setPresenter(this);
    }

    @Override
    public void start() {
        // TODO : Use polymorphism
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
                dataManager.logError(new InvalidMatchingCalloutError("Invalid callout in MatchingActivity"));
                matchingView.launchAlert(ALERT_TYPE.UNEXPECTED_ERROR);
        }
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
        final Constants.GROUP matchGroup = dataManager.getMatchGroup();

        dataManager.loadPeople(candidates, matchGroup, new DataCallback() {
            @Override
            public void onSuccess() {
                Log.INSTANCE.d(MatchingPresenter.this, String.format(Locale.UK,
                        "Successfully loaded %d candidates", candidates.size()));
                matchingView.setIdentificationProgressMatchingStart(candidates.size());

                int matcherType = dataManager.getMatcherType();

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
                                matcher_type, scores, MatchingPresenter.this, 1);
                        matcher.start();
                    }
                }).start();
            }

            @Override
            public void onFailure(DATA_ERROR data_error) {
                dataManager.logError(new FailedToLoadPeopleError("Failed to load people during identification: " + data_error.details()));
                matchingView.launchAlert(ALERT_TYPE.UNEXPECTED_ERROR);
            }
        });
    }

    private void onVerifyStart() {
        final String guid = dataManager.getPatientId();

        dataManager.loadPerson(candidates, guid, new DataCallback() {
            @Override
            public void onSuccess() {
                Log.INSTANCE.d(MatchingPresenter.this, "Successfully loaded candidate");

                int matcherType = dataManager.getMatcherType();

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
                                matcher_type, scores, MatchingPresenter.this, 1);
                        matcher.start();
                    }
                }).start();

            }

            @Override
            public void onFailure(DATA_ERROR data_error) {
                dataManager.logError(new Exception("Failed to load person during verification: " + data_error.details()));
                matchingView.launchAlert(ALERT_TYPE.UNEXPECTED_ERROR);
            }
        });
    }

    @Override
    public void onMatcherEvent(EVENT event) {
        switch (event) {
            case MATCH_NOT_RUNNING: {
                matchingView.makeToastMatchNotRunning(event.details());
                break;
            }
            case MATCH_COMPLETED: {
                Callout callout = dataManager.getCallout();
                switch (callout) {
                    case IDENTIFY: {
                        onMatchStartHandlerThread.quit();
                        matchingView.setIdentificationProgressReturningStart();
                        int nbOfResults = dataManager.getReturnIdCount();

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

                        dataManager.saveIdentification(probe, candidates.size(), topCandidates);

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
                        resultData = new Intent(com.simprints.libsimprints.Constants.SIMPRINTS_IDENTIFY_INTENT);
                        FormatResult.put(resultData, topCandidates, dataManager);
                        matchingView.doSetResult(RESULT_OK, resultData);
                        matchingView.setIdentificationProgressFinished(topCandidates.size(),
                                tier1Or2Matches, tier3Matches, tier4Matches, dataManager.getMatchingEndWaitTimeSeconds() * 1000);
                        break;
                    }
                    case VERIFY: {
                        Verification verification;
                        VERIFY_GUID_EXISTS_RESULT guidExistsResult;
                        int resultCode;

                        if (candidates.size() > 0 && scores.size() > 0) {
                            int score = scores.get(0).intValue();
                            verification = new Verification(score, computeTier(score), dataManager.getPatientId());
                            guidExistsResult = VERIFY_GUID_EXISTS_RESULT.GUID_FOUND;
                            resultCode = RESULT_OK;
                        } else {
                            verification = null;
                            guidExistsResult = VERIFY_GUID_EXISTS_RESULT.GUID_NOT_FOUND_UNKNOWN;
                            resultCode = com.simprints.libsimprints.Constants.SIMPRINTS_VERIFY_GUID_NOT_FOUND_ONLINE;
                        }
                        dataManager.saveVerification(probe, verification, guidExistsResult);

                        // finish
                        Intent resultData;
                        resultData = new Intent(com.simprints.libsimprints.Constants.SIMPRINTS_VERIFY_INTENT);
                        FormatResult.put(resultData, verification, dataManager.getResultFormat());
                        matchingView.doSetResult(resultCode, resultData);
                        matchingView.doFinish();
                        break;
                    }
                }
                break;
            }
        }
    }

    // TODO : Fix or remove this
    @Override
    public void onMatcherProgress(final Progress progress) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                matchingView.setIdentificationProgress(progress.getProgress());
            }
        });
    }
}
