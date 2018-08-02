package com.simprints.id.data.analytics

import com.simprints.id.data.analytics.events.Event
import com.simprints.id.data.analytics.events.RlEvent
import io.realm.RealmList
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import java.util.*
import kotlin.collections.ArrayList

open class SessionEvents : RealmObject {

    @PrimaryKey
    lateinit var id: String

    lateinit var appVersionName: String
    lateinit var libVersionName: String
    lateinit var analyticsId: String
    lateinit var language: String
    var device: Device? = null
    var startTime: Long = 0

    var relativeEndTime: Long = 0
    var relativeUploadTime: Long = 0
    var databaseInfo: DatabaseInfo? = null
    var location: Location? = null

    private lateinit var realmEvents: RealmList<RlEvent>

    fun isSessionCompleted(): Boolean = relativeEndTime > 0

    constructor() {}

    constructor(appVersionName: String,
                libVersionName: String,
                analyticsId: String,
                language: String,
                device: Device,
                startTime: Long = 0) {

        this.id = UUID.randomUUID().toString()
        this.appVersionName = appVersionName
        this.libVersionName = libVersionName
        this.analyticsId = analyticsId
        this.language = language
        this.device = device
        this.startTime = startTime
        this.realmEvents = RealmList()
    }

    fun getEvents(): ArrayList<Event> = ArrayList(realmEvents.mapNotNull { it.getEvent() })

    fun setEvents(events: ArrayList<Event>) {
        realmEvents.clear()
        realmEvents.addAll(events.map { RlEvent(it) })
    }
}
