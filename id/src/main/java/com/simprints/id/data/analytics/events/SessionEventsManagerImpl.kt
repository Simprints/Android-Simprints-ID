package com.simprints.id.data.analytics.events

import android.content.Context
import android.os.Build
import com.simprints.id.data.analytics.events.models.*
import com.simprints.id.data.loginInfo.LoginInfoManager
import com.simprints.id.data.prefs.PreferencesManager
import com.simprints.id.session.callout.CalloutAction
import com.simprints.id.tools.TimeHelper
import com.simprints.id.tools.extensions.deviceId
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.rxkotlin.subscribeBy

// Class to manage the current session
class SessionEventsManagerImpl(private val ctx: Context,
                               private val sessionEventsLocalDbManager: SessionEventsLocalDbManager,
                               override val loginInfoManager: LoginInfoManager,
                               private val preferencesManager: PreferencesManager,
                               private val timeHelper: TimeHelper) : SessionEventsManager {

    var session: SessionEvents? = null

    //as default, the manager tries to load the last open session for a specific project
    override fun getCurrentSession(projectId: String): Single<SessionEvents> = session?.let {
        Single.just(it)
    } ?: sessionEventsLocalDbManager.loadSessions(projectId, true).map { it[0] }.doOnSuccess {
        session = it
    }

    override fun createSession(projectId: String): Single<SessionEvents> =
        if (projectId.isNotEmpty()) {
            val sessionToSave = SessionEvents(
                projectId = projectId,
                appVersionName = preferencesManager.appVersionName,
                libVersionName = preferencesManager.libVersionName,
                startTime = timeHelper.msSinceBoot(),
                language = preferencesManager.language,
                device = Device(
                    androidSdkVersion = Build.VERSION.SDK_INT.toString(),
                    deviceModel = Build.MANUFACTURER + "_" + Build.MODEL,
                    deviceId = ctx.deviceId)
            ).also { session = it }

            closeLastSessionsIfPending(projectId)
                .andThen(insertOrUpdateSession(sessionToSave))
                .toSingle { sessionToSave }
        } else {
            Single.error<SessionEvents>(Throwable("project ID empty"))
        }

    override fun updateSession(block: (sessionEvents: SessionEvents) -> Unit, projectId: String): Completable =
        getCurrentSession(projectId).flatMapCompletable {
            block(it)
            insertOrUpdateSession(it)
        }.onErrorComplete() // StopShip: if it fails, because events are low priority, it swallows the exception

    override fun updateSessionInBackground(block: (sessionEvents: SessionEvents) -> Unit, projectId: String) {
        updateSession(block, projectId).subscribeBy(onComplete = {}, onError = { it.printStackTrace() })
    }

    private fun closeLastSessionsIfPending(projectId: String): Completable =
        sessionEventsLocalDbManager.loadSessions(projectId, true).flatMapCompletable {
            it.forEach {
                it.addArtificialTerminationIfRequired(timeHelper, ArtificialTerminationEvent.Reason.NEW_SESSION)
                it.closeIfRequired(timeHelper)
                insertOrUpdateSession(it).blockingAwait()
            }
            Completable.complete()
        }

    override fun insertOrUpdateSession(session: SessionEvents): Completable = sessionEventsLocalDbManager.insertOrUpdateSessionEvents(session)

    override fun addGuidSelectionEventToLastIdentificationIfExists(projectId: String, selectedGuid: String): Completable =
        sessionEventsLocalDbManager.loadSessions(projectId, false).flatMapCompletable { sessions ->
            sessions.firstOrNull()?.let { lastSessionClosed ->
                lastSessionClosed.events
                    .filterIsInstance(CalloutEvent::class.java)
                    .filter { it.parameters.action == CalloutAction.IDENTIFY }
                    .take(1)
                    .also {
                        lastSessionClosed.events.add(GuidSelectionEvent(
                            lastSessionClosed.nowRelativeToStartTime(timeHelper),
                            selectedGuid
                        ))
                        return@flatMapCompletable insertOrUpdateSession(lastSessionClosed)
                    }

                return@flatMapCompletable Completable.error(Throwable("No sessions closed"))
            }
        }
}
