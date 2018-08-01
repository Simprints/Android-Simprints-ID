package com.simprints.id.data.analytics

interface SessionEventsManager {

    fun updateEndTime()
    fun updateUploadTime()
    fun addEvent(event: Event)

    fun createSessionEvent()
    fun saveSession()
}
