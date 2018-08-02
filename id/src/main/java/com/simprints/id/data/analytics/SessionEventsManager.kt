package com.simprints.id.data.analytics

import com.simprints.id.data.analytics.events.Event

interface SessionEventsManager {

    fun updateEndTime()
    fun updateUploadTime()
    fun updateLocation(lat: Double, long: Double)
    fun addEvent(event: Event)

    fun createSessionEvent()
    fun saveSession()
    fun updateDatabaseInfo(databaseInfo: DatabaseInfo)
}
