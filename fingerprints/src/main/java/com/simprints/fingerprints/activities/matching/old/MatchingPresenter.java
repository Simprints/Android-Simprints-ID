package com.simprints.fingerprints.activities.matching.old;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;

import com.simprints.clientapi.simprintsrequests.responses.SimprintsIdResponse;
import com.simprints.fingerprints.di.FingerprintsComponent;
import com.simprints.id.data.analytics.crashreport.CrashReportManager;
import com.simprints.id.data.analytics.crashreport.CrashReportTag;
import com.simprints.id.data.analytics.crashreport.CrashReportTrigger;
import com.simprints.id.data.analytics.eventdata.controllers.domain.SessionEventsManager;
import com.simprints.id.data.analytics.eventdata.models.domain.session.SessionEvents;
import com.simprints.id.data.db.DATA_ERROR;
import com.simprints.id.data.db.DataCallback;
import com.simprints.id.data.db.DbManager;
import com.simprints.id.data.loginInfo.LoginInfoManager;
import com.simprints.id.data.prefs.PreferencesManager;
import com.simprints.id.domain.fingerprint.Person;
import com.simprints.id.domain.matching.IdentificationResult;
import com.simprints.id.domain.matching.Tier;
import com.simprints.id.domain.matching.VerificationResult;
import com.simprints.id.domain.responses.IdIdentificationResponse;
import com.simprints.id.domain.responses.IdVerifyResponse;
import com.simprints.id.exceptions.safe.callout.InvalidMatchingCalloutError;
import com.simprints.id.exceptions.unexpected.FailedToLoadPeopleException;
import com.simprints.id.exceptions.unexpected.UnexpectedDataException;
import com.simprints.id.session.callout.CalloutAction;
import com.simprints.id.tools.TimeHelper;
import com.simprints.libmatcher.EVENT;
import com.simprints.libmatcher.LibMatcher;
import com.simprints.libmatcher.Progress;
import com.simprints.libmatcher.sourceafis.MatcherEventListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import javax.inject.Inject;

import androidx.annotation.NonNull;
import io.reactivex.functions.BiConsumer;

import static android.app.Activity.RESULT_OK;
import static com.simprints.id.tools.DataCallbackUtils.wrapCallback;
import static com.simprints.id.domain.Constants.SIMPRINTS_VERIFY_GUID_NOT_FOUND_ONLINE;
import static com.simprints.id.domain.matching.Tier.computeTier;

public class MatchingPresenter implements MatchingContract.Presenter, MatcherEventListener {

    @NonNull
    private final MatchingContract.View matchingView;

    private Person probe;
    private List<Person> candidates = new ArrayList<>();
    private List<Float> scores = new ArrayList<>();
    private Handler handler = new Handler();
    private OnMatchStartHandlerThread onMatchStartHandlerThread;

    @NonNull @Inject PreferencesManager preferencesManager;
    @NonNull @Inject LoginInfoManager loginInfoManager;
    @NonNull @Inject TimeHelper timeHelper;
    @NonNull @Inject
    CrashReportManager crashReportManager;
    @NonNull @Inject DbManager dbManager;
    @NonNull @Inject SessionEventsManager sessionEventsManager;
    private Long startTimeVerification = 0L;
    private Long startTimeIdentification = 0L;
    private String sessionId = "";

    @SuppressLint("CheckResult")
    public MatchingPresenter(@NonNull MatchingContract.View matchingView,
                      @NonNull FingerprintsComponent component,
                      Person probe) {
        component.inject(this);
        this.matchingView = matchingView;
        this.probe = probe;
        sessionEventsManager
            .getCurrentSession()
            .subscribe(new BiConsumer<SessionEvents, Throwable>() {
                @Override
                public void accept(SessionEvents sessionEvents, Throwable throwable) {
                    if (sessionEvents != null && throwable == null) {
                        sessionId = sessionEvents.getId();
                    }
                }
            });
    }

    @Override
    public void start() {
        preferencesManager.setMsSinceBootOnMatchStart(timeHelper.now());
        // TODO : Use polymorphism
        switch (preferencesManager.getCalloutAction()) {
            case IDENTIFY:
                logMessageForCrashReport("Making identification");
                startTimeIdentification = timeHelper.now();

                final Runnable onMatchStartRunnable = new Runnable() {
                    @Override
                    public void run() {

                        onIdentifyStart();
                    }
                };

                onMatchStartHandlerThread = new OnMatchStartHandlerThread();
                onMatchStartHandlerThread.start();
                onMatchStartHandlerThread.prepareHandler();
                onMatchStartHandlerThread.postTask(onMatchStartRunnable);
                matchingView.setIdentificationProgressLoadingStart();
                break;
            case VERIFY:
                logMessageForCrashReport("Making verification");
                startTimeVerification = timeHelper.now();

                matchingView.setVerificationProgress();
                onVerifyStart();
                break;
            default:
                crashReportManager.logExceptionOrThrowable(new InvalidMatchingCalloutError("Invalid action in MatchingActivity"));
                matchingView.launchAlert();
        }
    }

    private class OnMatchStartHandlerThread extends HandlerThread {

        Handler handler;

        OnMatchStartHandlerThread() {
            super("onMatchStartHandlerThread");
        }

        void postTask(Runnable task) {
            handler.post(task);
        }

        void prepareHandler() {
            handler = new Handler(getLooper());
        }
    }

    private void onIdentifyStart() {
        dbManager.loadPeople(
            candidates,
            preferencesManager.getMatchGroup(),
            wrapCallback("loading people", newOnLoadPeopleCallback()));
    }

    private DataCallback newOnLoadPeopleCallback() {
        return new DataCallback() {
            @Override
            public void onSuccess(boolean isDataFromRemote) {
                logMessageForCrashReport(String.format(Locale.UK,
                    "Successfully loaded %d candidates", candidates.size()));
                matchingView.setIdentificationProgressMatchingStart(candidates.size());

                int matcherType = preferencesManager.getMatcherType();

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
//                        LibMatcher matcher = new LibMatcher(probe, candidates,
//                            matcher_type, scores, MatchingPresenter.this, 1);
//                        matcher.start();
                    }
                }).start();
            }

            @Override
            public void onFailure(DATA_ERROR data_error) {
                crashReportManager.logExceptionOrThrowable(new FailedToLoadPeopleException("Failed to load people during identification: " + data_error.details()));
                matchingView.launchAlert();
            }
        };
    }

    private void onVerifyStart() {
        final String guid = preferencesManager.getPatientId();
        dbManager.loadPerson(
            candidates,
            loginInfoManager.getSignedInProjectIdOrEmpty(),
            guid,
            wrapCallback("loading people", newOnLoadPersonCallback()));
    }

    private DataCallback newOnLoadPersonCallback() {
        return new DataCallback() {
            @Override
            public void onSuccess(boolean isDataFromRemote) {
                logMessageForCrashReport("Successfully loaded candidate");

                int matcherType = preferencesManager.getMatcherType();

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
//                        LibMatcher matcher = new LibMatcher(probe, candidates,
//                            matcher_type, scores, MatchingPresenter.this, 1);
//                        matcher.start();
                    }
                }).start();

            }

            @Override
            public void onFailure(DATA_ERROR dataError) {
                crashReportManager.logExceptionOrThrowable(UnexpectedDataException.forDataError(dataError, "MatchingActivity.onVerifyStart()"));
                matchingView.launchAlert();
            }
        };
    }

    @Override
    public void onMatcherEvent(EVENT event) {
        switch (event) {
            case MATCH_NOT_RUNNING: {
                matchingView.makeToastMatchNotRunning(event.details());
                break;
            }
            case MATCH_COMPLETED: {
                CalloutAction callout = preferencesManager.getCalloutAction();
                switch (callout) {
                    case IDENTIFY: {
                        onMatchStartHandlerThread.quit();
                        matchingView.setIdentificationProgressReturningStart();
                        int nbOfResults = preferencesManager.getReturnIdCount();

                        ArrayList<IdentificationResult> topCandidates = new ArrayList<>();

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

                            topCandidates.add(new IdentificationResult(candidate.getPatientId(),
                                scores.get(idx[i]).intValue(), computeTier(scores.get(idx[i]))));
                        }

                        sessionEventsManager.addOneToManyEventInBackground(startTimeIdentification, topCandidates, candidates.size());

                        // signOut
                        int tier1Or2Matches = 0;
                        int tier3Matches = 0;
                        int tier4Matches = 0;
                        for (IdentificationResult identification : topCandidates) {
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
                        resultData = new Intent();
                        resultData.putExtra(
                            SimprintsIdResponse.BUNDLE_KEY,
                            new IdIdentificationResponse(topCandidates, sessionId).toDomainClientApiIdentification());
                        matchingView.doSetResult(RESULT_OK, resultData);
                        matchingView.setIdentificationProgressFinished(topCandidates.size(),
                            tier1Or2Matches, tier3Matches, tier4Matches, preferencesManager.getMatchingEndWaitTimeSeconds() * 1000);

                        break;
                    }
                    case VERIFY: {
                        final VerificationResult verification;
                        int resultCode;

                        if (candidates.size() > 0 && scores.size() > 0) {
                            int score = scores.get(0).intValue();
                            verification = new VerificationResult(preferencesManager.getPatientId(), score, computeTier(score));
                            resultCode = RESULT_OK;
                        } else {
                            verification = null;
                            resultCode = SIMPRINTS_VERIFY_GUID_NOT_FOUND_ONLINE;
                        }

                        sessionEventsManager.addOneToOneMatchEventInBackground(probe.getPatientId(), startTimeVerification, verification);

                        // signOut
                        Intent resultData;
                        resultData = new Intent();
                        resultData.putExtra(
                            SimprintsIdResponse.BUNDLE_KEY,
                            new IdVerifyResponse(
                                verification.getGuidVerified(),
                                (int) verification.getConfidence(),
                                Tier.valueOf(verification.getTier().name())).toDomainClientApiVerify());
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

    private void logMessageForCrashReport(String message) {
        crashReportManager.logMessageForCrashReport(CrashReportTag.MATCHING, CrashReportTrigger.UI, Log.INFO, message);
    }
}
