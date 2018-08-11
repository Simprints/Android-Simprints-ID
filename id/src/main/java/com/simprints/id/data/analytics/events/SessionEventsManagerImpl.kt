package com.simprints.id.data.analytics.events

import android.content.Context
import android.os.Build
import com.simprints.id.data.analytics.events.models.ArtificialTerminationEvent
import com.simprints.id.data.analytics.events.models.Device
import com.simprints.id.data.analytics.events.models.Location
import com.simprints.id.data.analytics.events.models.SessionEvents
import com.simprints.id.data.loginInfo.LoginInfoManager
import com.simprints.id.data.prefs.PreferencesManager
import com.simprints.id.tools.TimeHelper
import com.simprints.id.tools.extensions.deviceId
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.rxkotlin.subscribeBy

// Class to manage the current session
class SessionEventsManagerImpl(private val ctx: Context,
                               private val dbManagerSessionEvents: SessionEventsLocalDbManager,
                               override val loginInfoManager: LoginInfoManager,
                               private val preferencesManager: PreferencesManager,
                               private val timeHelper: TimeHelper) : SessionEventsManager {

    var session: SessionEvents? = null

    //as default, the manager tries to load the last open session for a specific project
    override fun getCurrentSession(projectId: String): Single<SessionEvents> = session?.let {
        Single.just(it)
    } ?: dbManagerSessionEvents.loadLastOpenSession(projectId).doOnSuccess {
        session = it
    }

    override fun createSession(projectId: String): Single<SessionEvents> =
        if (projectId.isNotEmpty()) {
            session = SessionEvents(
                projectId = projectId,
                appVersionName = preferencesManager.appVersionName,
                libVersionName = preferencesManager.libVersionName,
                startTime = timeHelper.msSinceBoot(),
                language = preferencesManager.language,
                device = Device(
                    androidSdkVersion = Build.VERSION.SDK_INT.toString(),
                    deviceModel = Build.MANUFACTURER + "_" + Build.MODEL,
                    deviceId = ctx.deviceId)
            )

            closeLastSessionsIfPending(projectId)
                .andThen(insertOrUpdateSession(session!!))
                .toSingle { session }

        } else {
            Single.error<SessionEvents>(Throwable("project ID empty"))
        }

    override fun updateSession(block: (sessionEvents: SessionEvents) -> Unit, projectId: String): Completable =
        getCurrentSession(projectId).flatMapCompletable {
            block(it)
            insertOrUpdateSession(it)
        }

    override fun updateSessionInBackground(block: (sessionEvents: SessionEvents) -> Unit, projectId: String) {
        updateSession(block, projectId).subscribeBy(onComplete = {}, onError = { it.printStackTrace() })
    }

    override fun updateLocation(lat: Double, lon: Double): Completable =
        updateSession({
            it.location = Location(lat, lon)
        })

    private fun closeLastSessionsIfPending(projectId: String): Completable =
        dbManagerSessionEvents.loadSessions(projectId).flatMapCompletable {
            it.forEach {
                it.addArtificialTerminationIfRequired(timeHelper, ArtificialTerminationEvent.Reason.NEW_SESSION)
                it.closeIfRequired(timeHelper)
                insertOrUpdateSession(it).blockingAwait()
            }
            Completable.complete()
        }


    override fun insertOrUpdateSession(session: SessionEvents): Completable = dbManagerSessionEvents.insertOrUpdateSessionEvents(session)
}
