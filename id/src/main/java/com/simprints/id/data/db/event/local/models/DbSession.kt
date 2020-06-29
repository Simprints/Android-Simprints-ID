package com.simprints.id.data.db.event.local.models

import com.simprints.id.data.db.event.domain.events.Event
import com.simprints.id.data.db.event.domain.events.session.Device
import com.simprints.id.data.db.event.domain.events.session.SessionEvent
import com.simprints.id.data.db.event.local.toDomainEvent
import io.realm.RealmList
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

open class DbSession : RealmObject {

    @PrimaryKey
    lateinit var id: String
    lateinit var appVersionName: String
    lateinit var libVersionName: String
    lateinit var language: String
    lateinit var projectId: String
    lateinit var databaseInfo: DbDatabaseInfo

    var startTime: Long = 0L
    lateinit var realmEvents: RealmList<DbEvent>

    var relativeEndTime: Long = 0L
    var relativeUploadTime: Long = 0L
    var device: DbDevice? = null
    var location: DbLocation? = null
    var analyticsId: String? = null

    constructor()

    constructor(sessionEvent: SessionEvent) : this() {
        this.id = sessionEvent.id
        this.appVersionName = sessionEvent.appVersionName
        this.libVersionName = sessionEvent.libVersionName
        this.language = sessionEvent.language
        this.projectId = sessionEvent.projectId
        this.startTime = sessionEvent.startTime
        this.realmEvents = RealmList()
        setEvents(sessionEvent.getEvents())
        this.relativeEndTime = sessionEvent.relativeEndTime
        this.relativeUploadTime = sessionEvent.relativeUploadTime
        this.device = DbDevice(sessionEvent.device)
        this.databaseInfo = DbDatabaseInfo(sessionEvent.databaseInfo)

        sessionEvent.location?.let {
            this.location = DbLocation(it)
        }

        this.analyticsId = sessionEvent.analyticsId
    }

    private fun setEvents(events: List<Event>) = realmEvents.apply {
        clear()
        addAll(events.map { DbEvent(it) })
    }

    fun timeRelativeToStartTime(time: Long): Long = time - startTime
}

fun DbSession.toDomain(): SessionEvent {
    val session = SessionEvent(id = id,
        projectId = projectId,
        appVersionName = appVersionName,
        libVersionName = libVersionName,
        language = language,
        device = device?.toDomainDevice() ?: Device(),
        startTime = startTime,
        databaseInfo = databaseInfo.toDomainDatabaseInfo(),
        events = ArrayList(realmEvents.mapNotNull { it.toDomainEvent() }))

    session.relativeEndTime = this.relativeEndTime
    session.relativeUploadTime = this.relativeUploadTime
    session.databaseInfo = this.databaseInfo.toDomainDatabaseInfo()
    session.location = this.location?.toDomainLocation()
    session.analyticsId = this.analyticsId
    return session
}
