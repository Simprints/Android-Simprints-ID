package com.simprints.fingerprints.activities.matching

import android.annotation.SuppressLint
import android.app.Activity
import android.util.Log
import com.simprints.fingerprints.di.FingerprintsComponent
import com.simprints.id.data.analytics.crashreport.CrashReportManager
import com.simprints.id.data.analytics.crashreport.CrashReportTag
import com.simprints.id.data.analytics.crashreport.CrashReportTrigger
import com.simprints.id.data.analytics.eventdata.controllers.domain.SessionEventsManager
import com.simprints.id.data.db.local.LocalDbManager
import com.simprints.id.data.loginInfo.LoginInfoManager
import com.simprints.id.data.prefs.PreferencesManager
import com.simprints.id.domain.GROUP
import com.simprints.id.domain.GROUP.*
import com.simprints.id.domain.fingerprint.Person
import com.simprints.id.exceptions.SimprintsException
import com.simprints.id.exceptions.safe.callout.InvalidMatchingCalloutError
import com.simprints.id.session.callout.CalloutAction.IDENTIFY
import com.simprints.id.session.callout.CalloutAction.VERIFY
import com.simprints.id.tools.TimeHelper
import com.simprints.libmatcher.EVENT
import com.simprints.libmatcher.LibMatcher
import com.simprints.libmatcher.Progress
import com.simprints.libmatcher.sourceafis.MatcherEventListener
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
    private val probe: Person
) : MatchingContract.Presenter {

    @Inject lateinit var sessionEventsManager: SessionEventsManager
    @Inject lateinit var preferencesManager: PreferencesManager
    @Inject lateinit var crashReportManager: CrashReportManager
    @Inject lateinit var localDbManager: LocalDbManager
    @Inject lateinit var loginInfoManager: LoginInfoManager
    @Inject lateinit var timeHelper: TimeHelper

    private lateinit var sessionId: String
    private lateinit var matchTask: Disposable

    init {
        component.inject(this)
    }

    @SuppressLint("CheckResult")
    override fun start() {
        sessionEventsManager.getCurrentSession().subscribeBy { sessionId = it.id }
        preferencesManager.msSinceBootOnMatchStart = timeHelper.now()

        when (preferencesManager.calloutAction) {
            IDENTIFY -> handleStartIdentify()
            VERIFY -> handleStartVerify()
            else -> handleUnexpectedCallout()
        }
    }

    private fun handleStartIdentify() {
        matchTask = getPeopleFromGroup(preferencesManager.matchGroup)
            .doOnSuccess { handleLoadPeopleSuccess(it) }
            .runIdentification(getIdentifyMatcherType())
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy {
                handleIdentificationResult(it.candidates, it.scores)
            }
    }

    private fun getPeopleFromGroup(group: GROUP): Single<List<Person>> =
        when (group) {
            GLOBAL -> localDbManager.loadPeopleFromLocal()
            USER -> localDbManager.loadPeopleFromLocal(userId = loginInfoManager.getSignedInUserIdOrEmpty())
            MODULE -> localDbManager.loadPeopleFromLocal(moduleId = preferencesManager.moduleId)
        }

    private fun handleLoadPeopleSuccess(candidates: List<Person>) {
        logMessageForCrashReport(String.format(Locale.UK,
            "Successfully loaded %d candidates", candidates.size))
        view.setIdentificationProgressMatchingStart(candidates.size)
    }

    private fun getIdentifyMatcherType() =
        when (preferencesManager.matcherType) {
            0 -> LibMatcher.MATCHER_TYPE.SIMAFIS_IDENTIFY
            1 -> LibMatcher.MATCHER_TYPE.SOURCEAFIS_IDENTIFY
            else -> LibMatcher.MATCHER_TYPE.SIMAFIS_IDENTIFY
        }

    private fun Single<out List<Person>>.runIdentification(matcherType: LibMatcher.MATCHER_TYPE): Single<MatchResult> =
        flatMap { candidates ->
            Single.create<MatchResult> {
                val scores = mutableListOf<Float>()

                LibMatcher(probe, candidates, matcherType, scores, matchCallback(it, candidates, scores), 1).start()
            }
        }

    private fun matchCallback(emitter: SingleEmitter<MatchResult>, candidates: List<Person>, scores: List<Float>) =
        object : MatcherEventListener {

            override fun onMatcherProgress(progress: Progress?) {
                progress?.progress?.let {
                    (view as Activity).runOnUiThread { view.setIdentificationProgress(it) }
                }
            }

            override fun onMatcherEvent(event: EVENT?) =
                when (event) {
                    EVENT.MATCH_COMPLETED -> emitter.onSuccess(MatchResult(candidates, scores))
                    else -> emitter.onError(SimprintsException("Matching Error : $event")) // STOPSHIP
                }
        }

    private fun handleIdentificationResult(candidates: List<Person>, scores: List<Float>) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    private fun handleStartVerify() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
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
}
