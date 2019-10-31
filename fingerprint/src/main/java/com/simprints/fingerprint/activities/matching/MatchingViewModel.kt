package com.simprints.fingerprint.activities.matching

import android.annotation.SuppressLint
import android.content.Intent
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.simprints.fingerprint.activities.alert.FingerprintAlert
import com.simprints.fingerprint.activities.matching.request.MatchingTaskRequest
import com.simprints.fingerprint.controllers.core.crashreport.FingerprintCrashReportManager
import com.simprints.fingerprint.controllers.core.eventData.FingerprintSessionEventsManager
import com.simprints.fingerprint.controllers.core.flow.Action
import com.simprints.fingerprint.controllers.core.flow.MasterFlowManager
import com.simprints.fingerprint.controllers.core.preferencesManager.FingerprintPreferencesManager
import com.simprints.fingerprint.controllers.core.repository.FingerprintDbManager
import com.simprints.fingerprint.controllers.core.timehelper.FingerprintTimeHelper
import com.simprints.fingerprint.data.domain.fingerprint.Fingerprint
import com.simprints.fingerprint.data.domain.fingerprint.FingerprintIdentity
import com.simprints.fingerprint.data.domain.fingerprint.fromDomainToMatcher
import com.simprints.fingerprint.data.domain.fingerprint.fromDomainToMatcherPerson
import com.simprints.fingerprint.exceptions.FingerprintSimprintsException
import com.simprints.fingerprint.exceptions.unexpected.request.InvalidRequestForMatchingActivityException
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
import timber.log.Timber
import java.util.*
import com.simprints.fingerprintmatcher.Person as LibPerson
import com.simprints.fingerprintmatcher.Person as MatcherPerson

class MatchingViewModel(private val dbManager: FingerprintDbManager,
                        private val sessionEventsManager: FingerprintSessionEventsManager,
                        private val crashReportManager: FingerprintCrashReportManager,
                        private val preferencesManager: FingerprintPreferencesManager,
                        private val timeHelper: FingerprintTimeHelper,
                        private val masterFlowManager: MasterFlowManager) : ViewModel() {

    val result = MutableLiveData<FinishResult>()
    val progress = MutableLiveData(0)
    val alert = MutableLiveData<FingerprintAlert>()
    val hasLoadingBegun = MutableLiveData<Boolean>()
    val matchBeginningSummary = MutableLiveData<IdentificationBeginningSummary>()
    val matchFinishedSummary = MutableLiveData<IdentificationFinishedSummary>()
    val hasMatchFailed = MutableLiveData<Boolean>()

    private lateinit var matchingRequest: MatchingTaskRequest

    private lateinit var matchTaskDisposable: Disposable
    private lateinit var libMatcherConstructor: (MatcherPerson, List<MatcherPerson>,
                                                 LibMatcher.MATCHER_TYPE, MutableList<Float>, MatcherEventListener, Int) -> LibMatcher

    @SuppressLint("CheckResult")
    fun start(matchingRequest: MatchingTaskRequest,
                       libMatcherConstructor: (MatcherPerson, List<MatcherPerson>,
                                               LibMatcher.MATCHER_TYPE, MutableList<Float>, MatcherEventListener, Int) -> LibMatcher = ::LibMatcher) {
        this.matchingRequest = matchingRequest
        this.libMatcherConstructor = libMatcherConstructor
        when (masterFlowManager.getCurrentAction()) {
            Action.IDENTIFY -> startMatchTask(::IdentificationTask)
            Action.VERIFY -> startMatchTask(::VerificationTask)
            else -> handleUnexpectedCallout()
        }
    }

    private fun startMatchTask(matchTaskConstructor: (MatchingViewModel,
                                                      MatchingTaskRequest,
                                                      FingerprintDbManager,
                                                      FingerprintSessionEventsManager,
                                                      FingerprintCrashReportManager,
                                                      FingerprintTimeHelper,
                                                      FingerprintPreferencesManager) -> MatchTask) {
        val matchTask = matchTaskConstructor(this, matchingRequest, dbManager, sessionEventsManager, crashReportManager, timeHelper, preferencesManager)

        matchTaskDisposable = matchTask.loadCandidates()
            .doOnSuccess { matchTask.handlesCandidatesLoaded(it) }
            .flatMap { matchTask.runMatch(it, matchingRequest.probeFingerprintSamples) }
            .setMatchingSchedulers()
            .subscribeBy(
                onSuccess = {
                    matchTask.handleMatchResult(it.candidates, it.scores)
                },
                onError = {
                    Timber.e(it)
                    crashReportManager.logExceptionOrSafeException(it)
                    hasMatchFailed.postValue(true)
                    result.postValue(FinishResult(ResultCode.CANCELLED, null, 0))
                })
    }

    private fun MatchTask.runMatch(candidates: List<FingerprintIdentity>, probeFingerprints: List<Fingerprint>): Single<MatchResult> =
        Single.create { emitter ->
            val matcherType = getMatcherType()
            val scores = mutableListOf<Float>()
            val callback = matchCallback(emitter, candidates, scores)
            val libProbe = LibPerson(UUID.randomUUID().toString(), probeFingerprints.map { it.fromDomainToMatcher() })
            val libCandidates = candidates.map { it.fromDomainToMatcherPerson() }
            libMatcherConstructor(libProbe, libCandidates, matcherType, scores, callback, 1).start()
        }

    private fun MatchTask.matchCallback(
        emitter: SingleEmitter<MatchResult>,
        candidates: List<FingerprintIdentity>,
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

    private fun handleUnexpectedCallout() {
        crashReportManager.logExceptionOrSafeException(InvalidRequestForMatchingActivityException("Invalid action in MatchingActivity"))
        alert.postValue(FingerprintAlert.UNEXPECTED_ERROR)
    }

    override fun onCleared() {
        if (::matchTaskDisposable.isInitialized) matchTaskDisposable.dispose()
    }

    private fun <T> Single<T>.setMatchingSchedulers() =
        subscribeOn(Schedulers.computation())
            .observeOn(AndroidSchedulers.mainThread())

    private class MatchResult(val candidates: List<FingerprintIdentity>, val scores: List<Float>)

    data class IdentificationBeginningSummary(val matchSize: Int)

    data class IdentificationFinishedSummary(
        val returnSize: Int,
        val veryGoodMatches: Int,
        val goodMatches: Int,
        val fairMatches: Int
    )

    data class FinishResult(
        val resultCode: ResultCode,
        val data: Intent?,
        val finishDelayMillis: Int
    )
}
