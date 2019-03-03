package com.simprints.fingerprints.activities.matching

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Activity.RESULT_OK
import android.content.Intent
import android.util.Log
import com.simprints.fingerprints.di.FingerprintsComponent
import com.simprints.id.data.analytics.crashreport.CrashReportManager
import com.simprints.id.data.analytics.crashreport.CrashReportTag
import com.simprints.id.data.analytics.crashreport.CrashReportTrigger
import com.simprints.id.data.analytics.eventdata.controllers.domain.SessionEventsManager
import com.simprints.id.data.db.DbManager
import com.simprints.id.data.prefs.PreferencesManager
import com.simprints.id.domain.fingerprint.Fingerprint
import com.simprints.id.domain.fingerprint.Person
import com.simprints.id.domain.matching.IdentificationResult
import com.simprints.id.domain.matching.Tier
import com.simprints.id.domain.matching.Tier.Companion.computeTier
import com.simprints.id.domain.matching.VerificationResult
import com.simprints.id.domain.requests.IdentifyRequest
import com.simprints.id.domain.requests.Request
import com.simprints.id.domain.requests.VerifyRequest
import com.simprints.id.domain.responses.*
import com.simprints.id.exceptions.SimprintsException
import com.simprints.id.exceptions.safe.callout.InvalidMatchingCalloutError
import com.simprints.id.tools.TimeHelper
import com.simprints.libmatcher.EVENT
import com.simprints.libmatcher.LibMatcher
import com.simprints.libmatcher.LibMatcher.MATCHER_TYPE.*
import com.simprints.libmatcher.Progress
import com.simprints.libmatcher.sourceafis.MatcherEventListener
import com.simprints.libsimprints.FingerIdentifier
import io.reactivex.Single
import io.reactivex.SingleEmitter
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import java.util.*
import javax.inject.Inject

class MatchingPresenter(
    component: FingerprintsComponent,
    private val view: MatchingContract.View,
    private val probe: Person,
    private val appRequest: Request
) : MatchingContract.Presenter {

    @Inject lateinit var sessionEventsManager: SessionEventsManager
    @Inject lateinit var preferencesManager: PreferencesManager
    @Inject lateinit var crashReportManager: CrashReportManager
    @Inject lateinit var dbManager: DbManager
    @Inject lateinit var timeHelper: TimeHelper

    private lateinit var sessionId: String
    private lateinit var matchTask: Disposable
    private var matchStartTime = 0L

    init {
        component.inject(this)
    }

    @SuppressLint("CheckResult")
    override fun start() {
        sessionEventsManager.getCurrentSession().subscribeBy { sessionId = it.id }
        matchStartTime = timeHelper.now()

        when (appRequest) {
            is IdentifyRequest -> handleStartIdentify(appRequest)
            is VerifyRequest -> handleStartVerify(appRequest)
            else -> handleUnexpectedCallout()
        }
    }

    private fun handleStartIdentify(identifyRequest: IdentifyRequest) {
        matchTask = dbManager.loadPeople(preferencesManager.matchGroup)
            .doOnSuccess { handleLoadPeopleSuccess(it) }
            .runMatch(getMatcherType(identifyRequest), view::setIdentificationProgress)
            .setMatchingSchedulers()
            .subscribeBy {
                handleIdentificationResult(it.candidates, it.scores)
            }
    }

    private fun handleStartVerify(verifyRequest: VerifyRequest) {
        matchTask = dbManager.loadPerson(appRequest.projectId, verifyRequest.verifyGuid).map { listOf(it.person) }
            .doOnSuccess { handleLoadPersonSuccess(it) }
            .runMatch(getMatcherType(verifyRequest))
            .setMatchingSchedulers()
            .subscribeBy {
                handleVerificationResult(it.candidates, it.scores)
            }
    }

    private fun handleLoadPeopleSuccess(candidates: List<Person>) {
        logMessageForCrashReport(String.format(Locale.UK,
            "Successfully loaded %d candidates", candidates.size))
        view.setIdentificationProgressMatchingStart(candidates.size)
    }

    private fun handleLoadPersonSuccess(candidates: List<Person>) {
        logMessageForCrashReport(String.format(Locale.UK,
            "Successfully loaded %d candidates", candidates.size))
    }

    private fun getMatcherType(appRequest: Request) =
        when (appRequest) {
            is IdentifyRequest -> when (preferencesManager.matcherType) {
                0 -> SIMAFIS_IDENTIFY
                1 -> SOURCEAFIS_IDENTIFY
                else -> SIMAFIS_IDENTIFY
            }
            is VerifyRequest -> when (preferencesManager.matcherType) {
                0 -> SIMAFIS_VERIFY
                1 -> SOURCEAFIS_VERIFY
                else -> SIMAFIS_VERIFY
            }
            else -> throw UnsupportedOperationException()
        }

    private fun Single<out List<Person>>.runMatch(
        matcherType: LibMatcher.MATCHER_TYPE,
        onMatchProgressDo: (progress: Int) -> Unit = {}
    ): Single<MatchResult> =
        flatMap { candidates ->
            Single.create<MatchResult> { emitter ->
                val scores = mutableListOf<Float>()
                val callback = matchCallback(emitter, candidates, scores, onMatchProgressDo)
                val libProbe = probe.toLibCommonPerson()
                val libCandidates = candidates.map { it.toLibCommonPerson() }
                LibMatcher(libProbe, libCandidates, matcherType, scores, callback, 1).start()
            }
        }

    private fun matchCallback(
        emitter: SingleEmitter<MatchResult>,
        candidates: List<Person>,
        scores: List<Float>,
        onProgressDo: (progress: Int) -> Unit = {}
    ) =
        object : MatcherEventListener {

            override fun onMatcherProgress(progress: Progress?) {
                progress?.progress?.let {
                    (view as Activity).runOnUiThread { onProgressDo(it) }
                }
            }

            override fun onMatcherEvent(event: EVENT?) =
                when (event) {
                    EVENT.MATCH_COMPLETED -> emitter.onSuccess(MatchResult(candidates, scores))
                    else -> emitter.onError(SimprintsException("Matching Error : $event")) // STOPSHIP
                }
        }

    private fun handleIdentificationResult(candidates: List<Person>, scores: List<Float>) {
        view.setIdentificationProgressReturningStart()

        val topCandidates = candidates
            .zip(scores)
            .sortedByDescending { (_, score) -> score }
            .take(preferencesManager.returnIdCount)
            .map { (candidate, score) ->
                IdentificationResult(candidate.patientId, score.toInt(), computeTier(score))
            }

        sessionEventsManager.addOneToManyEventInBackground(matchStartTime, topCandidates, candidates.size)

        val tier1Or2Matches = topCandidates.count { (_, _, tier) -> tier == Tier.TIER_1 || tier == Tier.TIER_2 }
        val tier3Matches = topCandidates.count { (_, _, tier) -> tier == Tier.TIER_3 }
        val tier4Matches = topCandidates.count { (_, _, tier) -> tier == Tier.TIER_4 }


        val resultData = Intent().putExtra(Response.BUNDLE_KEY,
            IdentifyResponse(topCandidates, sessionId))
        view.doSetResult(RESULT_OK, resultData)
        view.setIdentificationProgressFinished(topCandidates.size, tier1Or2Matches, tier3Matches, tier4Matches, preferencesManager.matchingEndWaitTimeSeconds * 1000)
    }

    private fun handleVerificationResult(candidates: List<Person>, scores: List<Float>) {

        val candidate = candidates.first()
        val score = scores.first()

        val verificationResult = VerificationResult(candidate.patientId, score.toInt(), computeTier(score))

        sessionEventsManager.addOneToOneMatchEventInBackground(candidates.first().patientId, matchStartTime, verificationResult)
        val resultData = Intent().putExtra(Response.BUNDLE_KEY,
            VerifyResponse(candidate.patientId, score.toInt(), computeTier(score)))
        view.doSetResult(RESULT_OK, resultData)
        view.doFinish()
    }

    private fun handleUnexpectedCallout() {
        crashReportManager.logExceptionOrThrowable(InvalidMatchingCalloutError("Invalid action in MatchingActivity"))
        view.launchAlert()
    }

    private fun logMessageForCrashReport(message: String) {
        crashReportManager.logMessageForCrashReport(CrashReportTag.MATCHING, CrashReportTrigger.UI, Log.INFO, message)
    }

    override fun dispose() {
        matchTask.dispose()
    }

    class MatchResult(val candidates: List<Person>, val scores: List<Float>)

    private fun <T> Single<T>.setMatchingSchedulers() =
        subscribeOn(Schedulers.computation())
            .observeOn(AndroidSchedulers.mainThread())

    private fun Person.toLibCommonPerson() =
        com.simprints.libcommon.Person(patientId, fingerprints.map { it.toLibCommonFingerprint() }) // STOPSHIP

    private fun Fingerprint.toLibCommonFingerprint() =
        com.simprints.libcommon.Fingerprint(FingerIdentifier.values()[fingerId.ordinal], templateBytes) // STOPSHIP
}
