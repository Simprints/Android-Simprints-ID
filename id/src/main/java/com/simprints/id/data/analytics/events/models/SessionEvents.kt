package com.simprints.id.data.analytics.events.models

import com.simprints.id.data.analytics.events.realm.RlEvent
import io.realm.RealmList
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

open class SessionEvents : RealmObject {

    @PrimaryKey var id: Long = 0

    lateinit var appVersionName: String
    lateinit var libVersionName: String
    lateinit var language: String
    var device: Device? = null
    var startTime: Long = 0

    var relativeEndTime: Long = 0
    var relativeUploadTime: Long = 0
    var databaseInfo: DatabaseInfo? = null
    var location: Location? = null
    var analyticsId: String? = null

    private lateinit var realmEvents: RealmList<RlEvent>

    fun isSessionCompleted(): Boolean = relativeEndTime > 0

    constructor()

    constructor(appVersionName: String,
                libVersionName: String,
                language: String,
                device: Device,
                startTime: Long = 0) {

        this.appVersionName = appVersionName
        this.libVersionName = libVersionName
        this.language = language
        this.device = device
        this.startTime = startTime
        this.realmEvents = RealmList()
    }

    fun getEvents(): ArrayList<Event> = ArrayList(realmEvents.mapNotNull { it.getEvent() })
    fun setEvents(events: ArrayList<Event>) = realmEvents.apply {
        clear()
        addAll(events.map { RlEvent(it) })
    }
}
