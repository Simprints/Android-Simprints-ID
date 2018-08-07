package com.simprints.id.data.analytics.events

import com.simprints.id.data.analytics.events.models.DatabaseInfo
import com.simprints.id.data.analytics.events.models.Event
import io.reactivex.Completable

interface SessionEventsManager {

    fun updateEndTime()
    fun updateUploadTime()
    fun updateLocation(lat: Double, long: Double)
    fun addEvent(event: Event)

    fun createSession(): Completable
    fun saveSession()
    fun updateDatabaseInfo(databaseInfo: DatabaseInfo)
    fun closeSession()
}
