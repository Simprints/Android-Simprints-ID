package com.simprints.fingerprintmatcher;

import android.os.AsyncTask;
import android.util.Log;

import com.simprints.libcommon.Fingerprint;
import com.simprints.libcommon.Person;
import com.simprints.fingerprintmatcher.sourceafis.MatcherEventListener;
import com.simprints.fingerprintmatcher.sourceafis.matching.ParallelMatcher;
import com.simprints.fingerprintmatcher.sourceafis.simple.SourceAfisEngine;
import com.simprints.fingerprintmatcher.sourceafis.simple.SourceFinger;
import com.simprints.fingerprintmatcher.sourceafis.simple.SourceFingerprint;
import com.simprints.fingerprintmatcher.sourceafis.simple.SourcePerson;
import com.simprints.fingerprintmatcher.sourceafis.templates.Template;
import com.simprints.libsimprints.FingerIdentifier;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

@SuppressWarnings("unused")
public class LibMatcher {

    public enum MATCHER_TYPE {
        SOURCEAFIS_VERIFY,
        SOURCEAFIS_IDENTIFY,
        SIMAFIS_VERIFY,
        SIMAFIS_IDENTIFY
    }


    private static void log(String s) {
        Log.d("LibMatcher", s);
    }

    private final Person probe;
    private final List<Person> candidates;
    private final MATCHER_TYPE matcherType;
    private final List<Float> scores;
    private final int updatePeriod;
    private MatcherEventListener listener;
    private MatchTask matchTask;


    public LibMatcher(@NonNull Person probe, @NonNull List<Person> candidates,
                      @NonNull MATCHER_TYPE matcherType, @NonNull List<Float> scores,
                      @Nullable MatcherEventListener listener, int updatePeriod)
    {
        this.probe = probe;
        this.candidates = candidates;
        this.matcherType = matcherType;
        this.updatePeriod = updatePeriod;
        this.scores = scores;
        this.scores.clear();
        this.matchTask = null;
        this.listener = listener;
    }

    private void onMatcherEvent(EVENT event) {
        if (listener != null) {
            listener.onMatcherEvent(event);
        }
    }

    private void onMatcherProgress(Progress progress) {
        if (listener != null) {
            listener.onMatcherProgress(progress);
        }
    }

    public void start() {
        if (matchTask != null) {
            onMatcherEvent(EVENT.MATCH_ALREADY_RUNNING);
        } else {
            matchTask = new MatchTask();
            matchTask.execute();
        }
    }

    /* Cancels the match, without resetting the scores computed until there
     */
    public void cancel() {
        if (matchTask == null) {
            onMatcherEvent(EVENT.MATCH_NOT_RUNNING);
        } else {
            matchTask.cancel(false);
        }
    }

    private final static Map<FingerIdentifier, SourceFinger> fingerToSourceFinger;

    static {
        fingerToSourceFinger = new HashMap<>();
        fingerToSourceFinger.put(FingerIdentifier.LEFT_THUMB, SourceFinger.LEFT_THUMB);
        fingerToSourceFinger.put(FingerIdentifier.LEFT_INDEX_FINGER, SourceFinger.LEFT_INDEX);
        fingerToSourceFinger.put(FingerIdentifier.LEFT_3RD_FINGER, SourceFinger.LEFT_MIDDLE);
        fingerToSourceFinger.put(FingerIdentifier.LEFT_4TH_FINGER, SourceFinger.LEFT_RING);
        fingerToSourceFinger.put(FingerIdentifier.LEFT_5TH_FINGER, SourceFinger.LEFT_LITTLE);
        fingerToSourceFinger.put(FingerIdentifier.RIGHT_THUMB, SourceFinger.RIGHT_THUMB);
        fingerToSourceFinger.put(FingerIdentifier.RIGHT_INDEX_FINGER, SourceFinger.RIGHT_INDEX);
        fingerToSourceFinger.put(FingerIdentifier.RIGHT_3RD_FINGER, SourceFinger.RIGHT_MIDDLE);
        fingerToSourceFinger.put(FingerIdentifier.RIGHT_4TH_FINGER, SourceFinger.RIGHT_RING);
        fingerToSourceFinger.put(FingerIdentifier.RIGHT_5TH_FINGER, SourceFinger.RIGHT_LITTLE);
    }


    private SourceFingerprint toSourceFingerprint(Fingerprint fp) {
        SourceFingerprint srcFp = new SourceFingerprint();
        srcFp.setIsoTemplate(fp.getTemplateBytes());
        srcFp.setFinger(fingerToSourceFinger.get(fp.getTemplateBytes()));
        return srcFp;
    }

    private SourcePerson toSourcePerson(Person p) {
        List<Fingerprint> fps = p.getFingerprints();
        SourceFingerprint[] srcFps = new SourceFingerprint[fps.size()];
        for (int i = 0; i < fps.size(); i++) {
            srcFps[i] = toSourceFingerprint(fps.get(i));
        }
        return new SourcePerson(srcFps);
    }

    /**
     * Computes the aggregate matching scores of a probe against a list of candidate
     * Each score is the average of the scores of the fingers that the probe and a candidate
     * have in common
     */
    class MatchTask extends AsyncTask<Void, Integer, Void> {

        @Override
        protected Void doInBackground(Void... voids) {
            int nextPercentage;
            log(String.format(Locale.UK, "Matching with %s", matcherType.name()));

            switch (matcherType) {
                case SOURCEAFIS_VERIFY: {
                    SourceAfisEngine srcAfis = new SourceAfisEngine();
                    SourcePerson srcProbe = toSourcePerson(probe);
                    SourcePerson srcCandidate = toSourcePerson(candidates.get(0));
                    scores.add(srcAfis.verify(srcProbe, srcCandidate));
                    break;
                }

                case SOURCEAFIS_IDENTIFY: {
                    // Convert to sourceAfis objects
                    SourceAfisEngine srcAfis = new SourceAfisEngine();
                    SourcePerson srcProbe = toSourcePerson(probe);
                    SourcePerson[] srcCandidates = new SourcePerson[candidates.size()];
                    for (int i = 0; i < candidates.size(); i++) {
                        srcCandidates[i] = toSourcePerson(candidates.get(i));
                    }

                    // We'll aggregate in scores, then divide by nbCommonFingerprints to get average
                    // Note that both array are initialized to 0 by default
                    int[] nbCommonFingerprints = new int[candidates.size()];
                    float[] aggregatesScores = new float[candidates.size()];
                    ParallelMatcher matcher = srcAfis.getMatcher();

                    nextPercentage = updatePeriod;
                    int fingerprintNo = 0;
                    int nbFingerprints = srcProbe.getFingerprints().size();

                    for (SourceFingerprint fp : srcProbe.getFingerprints()) {
                        List<Integer> personsByFingerprint = new ArrayList<>();
                        List<Template> candidateTemplates = srcAfis.flattenHierarchy(srcCandidates, fp.getFinger(), personsByFingerprint);
                        ParallelMatcher.PreparedProbe probeIndex = matcher.prepare(fp.getDecoded());
                        float[] scores = matcher.Match(probeIndex, candidateTemplates);
                        for (int i = 0; i < scores.length; ++i) {
                            aggregatesScores[personsByFingerprint.get(i)] += scores[i];
                            nbCommonFingerprints[personsByFingerprint.get(i)] += 1;
                        }
                        // Send progress update if needed
                        while ((fingerprintNo + 1) * 100 / nbFingerprints >= nextPercentage) {
                            publishProgress(nextPercentage);
                            nextPercentage += updatePeriod;
                        }
                        fingerprintNo++;
                    }
                    for (int i = 0; i < candidates.size(); i++) {
                        scores.add((nbCommonFingerprints[i] == 0)
                                ? 0f
                                : aggregatesScores[i] / nbCommonFingerprints[i]);
                    }
                    break;
                }

                case SIMAFIS_VERIFY: {
                    List<Fingerprint> probeFingers = probe.getFingerprints();

                    Person candidate = candidates.get(0);
                    // Computes similarity scores of the fingerprints that the candidate has in common
                    // with the probe, using simAfis
                    int nbCommonFingers = 0;
                    float aggregateScore = 0;
                    for (Fingerprint pFinger : probeFingers) {
                        Fingerprint cFinger = candidate.getFingerprint(pFinger.getFingerId());
                        if (cFinger != null) {
                            float libafisResult = JNILibAfis.verify(pFinger.getTemplateDirectBuffer(), cFinger.getTemplateDirectBuffer());
                            log(String.format(Locale.UK, "LibAfis returned a score of %.3f", libafisResult));
                            aggregateScore += libafisResult;
                            nbCommonFingers++;
                        }
                    }

                    // The match score is the average of the similarity scores
                    if (nbCommonFingers > 0) {
                        aggregateScore /= nbCommonFingers;
                    }
                    log(String.format(Locale.UK, "Candidate no %d, score: %.3f", 0, aggregateScore));
                    scores.add(aggregateScore);

                    break;
                }
                
                case SIMAFIS_IDENTIFY: {
                    int nbCores = JNILibAfis.getNbCores();
                    log(String.format(Locale.UK, "Matching using all %d cores", nbCores));
                    float[] results = JNILibAfis.identify(probe, candidates, nbCores);
                    for (float result : results) {
                        scores.add(result);
                    }
                    break;
                }
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            onMatcherProgress(new Progress(values[0]));
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            matchTask = null;
            onMatcherEvent(EVENT.MATCH_COMPLETED);
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            matchTask = null;
            onMatcherEvent(EVENT.MATCH_CANCELLED);
        }
    }
}
