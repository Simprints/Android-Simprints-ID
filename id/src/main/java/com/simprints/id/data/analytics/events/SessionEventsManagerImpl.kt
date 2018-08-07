package com.simprints.id.data.analytics.events

import android.content.Context
import android.os.Build
import com.simprints.id.data.analytics.AnalyticsManager
import com.simprints.id.data.analytics.events.models.*
import com.simprints.id.data.prefs.PreferencesManager
import com.simprints.id.tools.TimeHelper
import com.simprints.id.tools.extensions.deviceId
import io.reactivex.Completable
import io.reactivex.rxkotlin.subscribeBy

class SessionEventsManagerImpl(private val ctx: Context,
                               private val analyticsManager: AnalyticsManager,
                               private val eventDbManager: LocalEventDbManager,
                               private val preferencesManager: PreferencesManager,
                               private val timeHelper: TimeHelper) : SessionEventsManager {

    lateinit var session: SessionEvents

    override fun createSession(): Completable {
        session = SessionEvents(
            appVersionName = preferencesManager.appVersionName,
            libVersionName = preferencesManager.libVersionName,
            startTime = timeHelper.msSinceBoot(),
            language = preferencesManager.language,
            device =
            Device(
                androidSdkVersion = Build.VERSION.SDK_INT.toString(),
                deviceModel = Build.MANUFACTURER + "_" + Build.MODEL,
                deviceId = ctx.deviceId)
        )

        return insertOrUpdateSession()
                .andThen(updateAnalyticsId())
    }

    override fun closeSession() {
        updateEndTime()
    }

    override fun saveSession() {
        insertOrUpdateSession().subscribeBy(onError = {
            it.printStackTrace()
        })
    }

    private fun updateAnalyticsId():Completable = analyticsManager.analyticsId.flatMapCompletable {
        session.analyticsId = it
        insertOrUpdateSession()
    }

    private fun insertOrUpdateSession():Completable = eventDbManager.insertOrUpdateSessionEvents(session)

    override fun updateEndTime() {
        session.relativeEndTime = msSinceStartTime()
        saveSession()
    }

    override fun updateUploadTime() {
        session.relativeUploadTime = msSinceStartTime()
        saveSession()
    }

    override fun addEvent(event: Event) {
        if (event is CallbackEvent) {
            event.relativeStartTime = msSinceStartTime()
        }

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

    private fun msSinceStartTime(): Long = timeHelper.msSinceBoot() - session.startTime
}
