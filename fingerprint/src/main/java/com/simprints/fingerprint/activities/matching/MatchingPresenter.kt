package com.simprints.fingerprint.activities.matching

import android.annotation.SuppressLint
import com.simprints.fingerprint.data.domain.matching.request.MatchingActIdentifyRequest
import com.simprints.fingerprint.data.domain.matching.request.MatchingActRequest
import com.simprints.fingerprint.data.domain.matching.request.MatchingActVerifyRequest
import com.simprints.fingerprint.exceptions.FingerprintSimprintsException
import com.simprints.fingerprint.tools.utils.TimeHelper
import com.simprints.fingerprintmatcher.EVENT
import com.simprints.fingerprintmatcher.LibMatcher
import com.simprints.fingerprintmatcher.Progress
import com.simprints.fingerprintmatcher.sourceafis.MatcherEventListener
import com.simprints.id.data.analytics.crashreport.CrashReportManager
import com.simprints.id.data.analytics.eventdata.controllers.domain.SessionEventsManager
import com.simprints.id.data.db.DbManager
import com.simprints.id.domain.fingerprint.Fingerprint
import com.simprints.id.domain.fingerprint.Person
import com.simprints.libsimprints.FingerIdentifier
import io.reactivex.Single
import io.reactivex.SingleEmitter
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers

class MatchingPresenter(
    private val view: MatchingContract.View,
    private val matchingRequest: MatchingActRequest,
    private val dbManager: DbManager,
    private val sessionEventsManager: SessionEventsManager,
    private val crashReportManager: CrashReportManager,
    private val timeHelper: TimeHelper,
    private val libMatcherConstructor: (com.simprints.fingerprintmatcher.Person, List<com.simprints.fingerprintmatcher.Person>,
                                        LibMatcher.MATCHER_TYPE, MutableList<Float>, MatcherEventListener, Int) -> LibMatcher = ::LibMatcher
) : MatchingContract.Presenter {

    private lateinit var matchTaskDisposable: Disposable

    @SuppressLint("CheckResult")
    override fun start() {
        when (matchingRequest) {
            is MatchingActIdentifyRequest -> startMatchTask(::IdentificationTask)
            is MatchingActVerifyRequest -> startMatchTask(::VerificationTask)
            else -> handleUnexpectedCallout()
        }
    }

    private fun startMatchTask(matchTaskConstructor: (MatchingContract.View,
                                                      MatchingActRequest,
                                                      DbManager,
                                                      SessionEventsManager, CrashReportManager, TimeHelper) -> MatchTask) {
        val matchTask = matchTaskConstructor(view, matchingRequest, dbManager, sessionEventsManager, crashReportManager, timeHelper)

        matchTaskDisposable = matchTask.loadCandidates()
            .doOnSuccess { matchTask.handlesCandidatesLoaded(it) }
            .flatMap { matchTask.runMatch(it, matchingRequest.probe) }
            .setMatchingSchedulers()
            .subscribeBy(
                onSuccess = { matchTask.handleMatchResult(it.candidates, it.scores) },
                onError = {
                    crashReportManager.logExceptionOrThrowable(it)
                    view.makeToastMatchFailed()
                })
    }

    private fun MatchTask.runMatch(candidates: List<Person>, probe: Person): Single<MatchResult> =
        Single.create<MatchResult> { emitter ->
            val matcherType = getMatcherType()
            val scores = mutableListOf<Float>()
            val callback = matchCallback(emitter, candidates, scores)
            val libProbe = probe.toLibCommonPerson()
            val libCandidates = candidates.map { it.toLibCommonPerson() }
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
                else -> emitter.onError(FingerprintSimprintsException("Matching Error : $event")) // STOPSHIP : make custom exception
            }
    }

    private class MatchResult(val candidates: List<Person>, val scores: List<Float>)

    private fun Person.toLibCommonPerson() =
        com.simprints.fingerprintmatcher.Person(patientId, fingerprints.map { it.toLibCommonFingerprint() }) // STOPSHIP : Change LibMatcher interface

    private fun Fingerprint.toLibCommonFingerprint() =
        com.simprints.fingerprintmatcher.Fingerprint(FingerIdentifier.values()[fingerId.ordinal], templateBytes) // STOPSHIP : Change LibMatcher interface

    private fun handleUnexpectedCallout() {
        crashReportManager.logExceptionOrThrowable(FingerprintSimprintsException("Invalid action in MatchingActivity"))// STOPSHIP : make custom exception
        view.launchAlert()
    }

    override fun dispose() {
        matchTaskDisposable.dispose()
    }

    private fun <T> Single<T>.setMatchingSchedulers() =
        subscribeOn(Schedulers.computation())
            .observeOn(AndroidSchedulers.mainThread())
}
