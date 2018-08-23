package com.simprints.id.data.analytics.eventData.realm

import com.simprints.id.data.analytics.eventData.models.events.Event
import com.simprints.id.data.analytics.eventData.models.session.DatabaseInfo
import com.simprints.id.data.analytics.eventData.models.session.Device
import com.simprints.id.data.analytics.eventData.models.session.Location
import com.simprints.id.data.analytics.eventData.models.session.SessionEvents
import io.realm.RealmList
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

open class RlSession: RealmObject {

    @PrimaryKey
    lateinit var id: String
    lateinit var appVersionName: String
    lateinit var libVersionName: String
    lateinit var language: String
    lateinit var projectId: String
    var startTime: Long = 0L
    lateinit var realmEvents: RealmList<RlEvent>

    var relativeEndTime: Long = 0L
    var relativeUploadTime: Long = 0L
    var device: Device? = null
    var databaseInfo: DatabaseInfo? = null
    var location: Location? = null
    var analyticsId: String? = null


    constructor(){}

    constructor(sessionEvents: SessionEvents) : this() {
        this.id = sessionEvents.id
        this.appVersionName = sessionEvents.appVersionName
        this.libVersionName = sessionEvents.libVersionName
        this.language = sessionEvents.language
        this.projectId = sessionEvents.projectId
        this.startTime = sessionEvents.startTime
        this.realmEvents = RealmList()
        setEvents(sessionEvents.events)
        this.relativeEndTime = sessionEvents.relativeEndTime
        this.relativeUploadTime = sessionEvents.relativeUploadTime
        this.device = sessionEvents.device
        this.databaseInfo = sessionEvents.databaseInfo
        this.location = sessionEvents.location
        this.analyticsId = sessionEvents.analyticsId
    }

    fun getEvents(): ArrayList<Event> = ArrayList(realmEvents.mapNotNull { it.getEvent() })
    fun setEvents(events: ArrayList<Event>) = realmEvents.apply {
        clear()
        addAll(events.map { RlEvent(it) })
    }
}
