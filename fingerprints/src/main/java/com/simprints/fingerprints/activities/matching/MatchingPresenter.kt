package com.simprints.fingerprints.activities.matching

import android.annotation.SuppressLint
import com.simprints.fingerprints.di.FingerprintsComponent
import com.simprints.id.data.analytics.crashreport.CrashReportManager
import com.simprints.id.data.analytics.eventdata.controllers.domain.SessionEventsManager
import com.simprints.id.data.db.DbManager
import com.simprints.id.data.prefs.PreferencesManager
import com.simprints.id.domain.fingerprint.Fingerprint
import com.simprints.id.domain.fingerprint.Person
import com.simprints.id.domain.requests.IdentifyRequest
import com.simprints.id.domain.requests.Request
import com.simprints.id.domain.requests.VerifyRequest
import com.simprints.id.exceptions.SimprintsException
import com.simprints.id.exceptions.safe.callout.InvalidMatchingCalloutError
import com.simprints.id.tools.TimeHelper
import com.simprints.libmatcher.EVENT
import com.simprints.libmatcher.LibMatcher
import com.simprints.libmatcher.Progress
import com.simprints.libmatcher.sourceafis.MatcherEventListener
import com.simprints.libsimprints.FingerIdentifier
import io.reactivex.Single
import io.reactivex.SingleEmitter
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
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

    private lateinit var matchTaskDisposable: Disposable

    init {
        component.inject(this)
    }

    @SuppressLint("CheckResult")
    override fun start() {
        when (appRequest) {
            is IdentifyRequest -> startMatchTask(::IdentificationTask)
            is VerifyRequest -> startMatchTask(::VerificationTask)
            else -> handleUnexpectedCallout()
        }
    }

    private fun startMatchTask(matchTaskConstructor: (MatchingContract.View, DbManager, PreferencesManager,
                                                      SessionEventsManager, CrashReportManager, TimeHelper) -> MatchTask) {
        val matchTask = matchTaskConstructor(view, dbManager, preferencesManager, sessionEventsManager, crashReportManager, timeHelper)

        matchTaskDisposable = matchTask.loadCandidates(appRequest)
            .doOnSuccess { matchTask.handlesCandidatesLoaded(it) }
            .flatMap { matchTask.runMatch(it, probe) }
            .setMatchingSchedulers()
            .subscribeBy {
                matchTask.handleMatchResult(it.candidates, it.scores)
            }
    }

    private fun MatchTask.runMatch(candidates: List<Person>, probe: Person): Single<MatchResult> =
        Single.create<MatchResult> { emitter ->
            val matcherType = getMatcherType()
            val scores = mutableListOf<Float>()
            val callback = matchCallback(emitter, candidates, scores)
            val libProbe = probe.toLibCommonPerson()
            val libCandidates = candidates.map { it.toLibCommonPerson() }
            LibMatcher(libProbe, libCandidates, matcherType, scores, callback, 1).start()
        }

    private fun MatchTask.matchCallback(
        emitter: SingleEmitter<MatchResult>,
        candidates: List<Person>,
        scores: List<Float>
    ) =
        object : MatcherEventListener {

            override fun onMatcherProgress(progress: Progress?) {
                progress?.progress?.let { onMatchProgressDo(it) }
            }

            override fun onMatcherEvent(event: EVENT?) =
                when (event) {
                    EVENT.MATCH_COMPLETED -> emitter.onSuccess(MatchResult(candidates, scores))
                    else -> emitter.onError(SimprintsException("Matching Error : $event")) // STOPSHIP
                }
        }

    private class MatchResult(val candidates: List<Person>, val scores: List<Float>)

    private fun Person.toLibCommonPerson() =
        com.simprints.libcommon.Person(patientId, fingerprints.map { it.toLibCommonFingerprint() }) // STOPSHIP

    private fun Fingerprint.toLibCommonFingerprint() =
        com.simprints.libcommon.Fingerprint(FingerIdentifier.values()[fingerId.ordinal], templateBytes) // STOPSHIP

    private fun handleUnexpectedCallout() {
        crashReportManager.logExceptionOrThrowable(InvalidMatchingCalloutError("Invalid action in MatchingActivity"))
        view.launchAlert()
    }

    override fun dispose() {
        matchTaskDisposable.dispose()
    }

    private fun <T> Single<T>.setMatchingSchedulers() =
        subscribeOn(Schedulers.computation())
            .observeOn(AndroidSchedulers.mainThread())
}
