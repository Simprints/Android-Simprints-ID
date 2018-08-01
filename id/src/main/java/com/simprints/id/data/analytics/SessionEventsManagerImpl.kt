package com.simprints.id.data.analytics

import android.content.Context
import android.os.Build
import com.simprints.id.data.db.local.LocalEventDbManager
import com.simprints.id.data.prefs.PreferencesManager
import com.simprints.id.tools.TimeHelper
import com.simprints.id.tools.extensions.deviceId
import io.reactivex.rxkotlin.subscribeBy
import java.util.*

class SessionEventsManagerImpl(val ctx: Context,
                               private val eventDbManager: LocalEventDbManager,
                               val preferencesManager: PreferencesManager,
                               val timeHelper: TimeHelper) : SessionEventsManager {

    lateinit var session: SessionEvents

    override fun createSessionEvent() {
        session = SessionEvents(
            appVersionName = preferencesManager.appVersionName,
            libVersionName = preferencesManager.libVersionName,
            analyticsId = UUID.randomUUID().toString(), //StopShip: Implement analytics id
            startTime = timeHelper.msSinceBoot(),
            language = preferencesManager.language,
            device =
                Device(
                    androidSdkVersion = Build.VERSION.SDK_INT.toString(),
                    deviceModel = Build.MANUFACTURER + "_" + Build.MODEL,
                    deviceId = ctx.deviceId)
        )

        saveSession()
    }

    override fun saveSession() {
        eventDbManager.insertOrUpdateSessionEvents(session).subscribeBy(onComplete = {
        }, onError = {
        })
    }

    override fun updateEndTime() {
        session.relativeEndTime = timeHelper.msSinceBoot() - session.startTime
        saveSession()
    }

    override fun updateUploadTime() {
        session.relativeUploadTime = timeHelper.msSinceBoot() - session.startTime
        saveSession()
    }

    override fun addEvent(event: Event) {
        session.events.add(event)
        saveSession()
    }
}
