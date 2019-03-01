package com.simprints.fingerprints.activities.matching

import android.app.Activity
import android.util.Log
import com.simprints.id.data.analytics.crashreport.CrashReportManager
import com.simprints.id.data.analytics.crashreport.CrashReportTag
import com.simprints.id.data.analytics.crashreport.CrashReportTrigger
import com.simprints.id.data.analytics.eventdata.controllers.domain.SessionEventsManager
import com.simprints.id.data.db.DbManager
import com.simprints.id.data.prefs.PreferencesManager
import com.simprints.id.domain.fingerprint.Fingerprint
import com.simprints.id.domain.fingerprint.Person
import com.simprints.id.domain.requests.Request
import com.simprints.id.exceptions.SimprintsException
import com.simprints.id.tools.TimeHelper
import com.simprints.libmatcher.EVENT
import com.simprints.libmatcher.LibMatcher
import com.simprints.libmatcher.Progress
import com.simprints.libmatcher.sourceafis.MatcherEventListener
import com.simprints.libsimprints.FingerIdentifier
import io.reactivex.Single
import io.reactivex.SingleEmitter

internal abstract class MatchTask(protected val view: MatchingContract.View,
                                  protected val dbManager: DbManager,
                                  protected val preferencesManager: PreferencesManager,
                                  protected val sessionEventsManager: SessionEventsManager,
                                  private val crashReportManager: CrashReportManager,
                                  timeHelper: TimeHelper) {

    protected val matchStartTime = timeHelper.now()

    internal abstract fun loadCandidates(appRequest: Request): Single<List<Person>>

    internal abstract fun handlesCandidatesLoaded(candidates: List<Person>)

    internal fun runMatch(candidates: List<Person>, probe: Person): Single<MatchResult> =
        Single.create<MatchResult> { emitter ->
            val matcherType = getMatcherType()
            val scores = mutableListOf<Float>()
            val callback = matchCallback(emitter, candidates, scores)
            val libProbe = probe.toLibCommonPerson()
            val libCandidates = candidates.map { it.toLibCommonPerson() }
            LibMatcher(libProbe, libCandidates, matcherType, scores, callback, 1).start()
        }

    protected abstract fun getMatcherType(): LibMatcher.MATCHER_TYPE

    private fun matchCallback(
        emitter: SingleEmitter<MatchResult>,
        candidates: List<Person>,
        scores: List<Float>
    ) =
        object : MatcherEventListener {

            override fun onMatcherProgress(progress: Progress?) {
                progress?.progress?.let {
                    (view as Activity).runOnUiThread { onMatchProgressDo(it) }
                }
            }

            override fun onMatcherEvent(event: EVENT?) =
                when (event) {
                    EVENT.MATCH_COMPLETED -> emitter.onSuccess(MatchResult(candidates, scores))
                    else -> emitter.onError(SimprintsException("Matching Error : $event")) // STOPSHIP
                }
        }

    protected abstract fun onMatchProgressDo(progress: Int)

    internal abstract fun handleMatchResult(candidates: List<Person>, scores: List<Float>)

    internal class MatchResult(val candidates: List<Person>, val scores: List<Float>)

    protected fun logMessageForCrashReport(message: String) {
        crashReportManager.logMessageForCrashReport(CrashReportTag.MATCHING, CrashReportTrigger.UI, Log.INFO, message)
    }

    private fun Person.toLibCommonPerson() =
        com.simprints.libcommon.Person(patientId, fingerprints.map { it.toLibCommonFingerprint() }) // STOPSHIP

    private fun Fingerprint.toLibCommonFingerprint() =
        com.simprints.libcommon.Fingerprint(FingerIdentifier.values()[fingerId.ordinal], templateBytes) // STOPSHIP
}
