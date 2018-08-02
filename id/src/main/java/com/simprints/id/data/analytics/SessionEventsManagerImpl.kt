package com.simprints.id.data.analytics

import android.content.Context
import android.os.Build
import com.simprints.id.data.analytics.events.Event
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
        eventDbManager.insertOrUpdateSessionEvents(session).subscribeBy(onError = {
            it.printStackTrace()
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
        session.setEvents(session.getEvents().also { it.add(event) })
        saveSession()
    }

    override fun updateLocation(lat: Double, long: Double) {
        if (session.location == null || (session.location?.latitude == 0.0 && session.location?.longitude == 0.0)) {
            session.location = Location(lat, long)
            saveSession()
        }
    }

    override fun updateDatabaseInfo(databaseInfo: DatabaseInfo) {
        session.databaseInfo = databaseInfo
        saveSession()
    }
}
