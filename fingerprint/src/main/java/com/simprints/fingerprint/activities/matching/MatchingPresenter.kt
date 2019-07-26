package com.simprints.fingerprint.activities.matching

import android.annotation.SuppressLint
import com.simprints.fingerprint.activities.matching.request.MatchingTaskIdentifyRequest
import com.simprints.fingerprint.activities.matching.request.MatchingTaskRequest
import com.simprints.fingerprint.activities.matching.request.MatchingTaskVerifyRequest
import com.simprints.fingerprint.controllers.core.crashreport.FingerprintCrashReportManager
import com.simprints.fingerprint.controllers.core.eventData.FingerprintSessionEventsManager
import com.simprints.fingerprint.controllers.core.preferencesManager.FingerprintPreferencesManager
import com.simprints.fingerprint.controllers.core.repository.FingerprintDbManager
import com.simprints.fingerprint.controllers.core.timehelper.FingerprintTimeHelper
import com.simprints.fingerprint.data.domain.person.Person
import com.simprints.fingerprint.data.domain.person.fromDomainToMatcher
import com.simprints.fingerprint.exceptions.FingerprintSimprintsException
import com.simprints.fingerprint.orchestrator.domain.ResultCode
import com.simprints.fingerprintmatcher.EVENT
import com.simprints.fingerprintmatcher.LibMatcher
import com.simprints.fingerprintmatcher.Progress
import com.simprints.fingerprintmatcher.sourceafis.MatcherEventListener
import io.reactivex.Single
import io.reactivex.SingleEmitter
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import com.simprints.fingerprintmatcher.Person as MatcherPerson

class MatchingPresenter(
    private val view: MatchingContract.View,
    private val matchingRequest: MatchingTaskRequest,
    private val dbManager: FingerprintDbManager,
    private val sessionEventsManager: FingerprintSessionEventsManager,
    private val crashReportManager: FingerprintCrashReportManager,
    private val preferencesManager: FingerprintPreferencesManager,
    private val timeHelper: FingerprintTimeHelper,
    private val libMatcherConstructor: (MatcherPerson, List<MatcherPerson>,
                                        LibMatcher.MATCHER_TYPE, MutableList<Float>, MatcherEventListener, Int) -> LibMatcher = ::LibMatcher
) : MatchingContract.Presenter {

    private lateinit var matchTaskDisposable: Disposable

    @SuppressLint("CheckResult")
    override fun start() {
        when (matchingRequest) {
            is MatchingTaskIdentifyRequest -> startMatchTask(::IdentificationTask)
            is MatchingTaskVerifyRequest -> startMatchTask(::VerificationTask)
            else -> handleUnexpectedCallout()
        }
    }

    private fun startMatchTask(matchTaskConstructor: (MatchingContract.View,
                                                      MatchingTaskRequest,
                                                      FingerprintDbManager,
                                                      FingerprintSessionEventsManager,
                                                      FingerprintCrashReportManager,
                                                      FingerprintTimeHelper,
                                                      FingerprintPreferencesManager) -> MatchTask) {
        val matchTask = matchTaskConstructor(view, matchingRequest, dbManager, sessionEventsManager, crashReportManager, timeHelper, preferencesManager)

        matchTaskDisposable = matchTask.loadCandidates()
            .doOnSuccess {
                matchTask.handlesCandidatesLoaded(it)
            }
            .flatMap { matchTask.runMatch(it, matchingRequest.probe) }
            .setMatchingSchedulers()
            .subscribeBy(
                onSuccess = {
                    matchTask.handleMatchResult(it.candidates, it.scores) },
                onError = {
                    crashReportManager.logExceptionOrSafeException(it)
                    view.makeToastMatchFailed()
                })
    }

    private fun MatchTask.runMatch(candidates: List<Person>, probe: Person): Single<MatchResult> =
        Single.create { emitter ->
            val matcherType = getMatcherType()
            val scores = mutableListOf<Float>()
            val callback = matchCallback(emitter, candidates, scores)
            val libProbe = probe.fromDomainToMatcher()
            val libCandidates = candidates.map { it.fromDomainToMatcher() }
            libMatcherConstructor(libProbe, libCandidates, matcherType, scores, callback, 1).start()
        }

    private fun MatchTask.matchCallback(
        emitter: SingleEmitter<MatchResult>,
        candidates: List<Person>,
        scores: List<Float>) = object : MatcherEventListener {

        override fun onMatcherProgress(progress: Progress?) {
            progress?.progress?.let { onMatchProgressDo(it) }
        }

        override fun onMatcherEvent(event: EVENT?) =
            when (event) {
                EVENT.MATCH_COMPLETED -> emitter.onSuccess(MatchResult(candidates, scores))
                else -> emitter.onError(FingerprintSimprintsException("Matching Error : $event"))
            }
    }

    private class MatchResult(val candidates: List<Person>, val scores: List<Float>)

    private fun handleUnexpectedCallout() {
        crashReportManager.logExceptionOrSafeException(FingerprintSimprintsException("Invalid action in MatchingActivity"))
        view.launchAlertActivity()
    }

    override fun handleBackPressed() {
        view.setResultAndFinish(ResultCode.CANCELLED, null)
    }

    override fun dispose() {
        matchTaskDisposable.dispose()
    }

    private fun <T> Single<T>.setMatchingSchedulers() =
        subscribeOn(Schedulers.computation())
            .observeOn(AndroidSchedulers.mainThread())
}
