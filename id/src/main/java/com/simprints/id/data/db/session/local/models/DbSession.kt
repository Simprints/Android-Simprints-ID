package com.simprints.id.data.db.session.local.models

import com.simprints.id.data.db.session.domain.models.events.Event
import com.simprints.id.data.db.session.domain.models.session.Device
import com.simprints.id.data.db.session.domain.models.session.SessionEvents
import com.simprints.id.data.db.session.local.toDomainEvent
import com.simprints.id.domain.modality.Modality
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
    lateinit var realmModalities: RealmList<DbModality>

    var relativeEndTime: Long = 0L
    var relativeUploadTime: Long = 0L
    var device: DbDevice? = null
    var location: DbLocation? = null
    var analyticsId: String? = null

    constructor()

    constructor(sessionEvents: SessionEvents) : this() {
        this.id = sessionEvents.id
        this.appVersionName = sessionEvents.appVersionName
        this.libVersionName = sessionEvents.libVersionName
        this.language = sessionEvents.language
        this.projectId = sessionEvents.projectId
        this.startTime = sessionEvents.startTime
        this.realmEvents = RealmList()
        setEvents(sessionEvents.getEvents())
        this.relativeEndTime = sessionEvents.relativeEndTime
        this.relativeUploadTime = sessionEvents.relativeUploadTime
        this.device = DbDevice(sessionEvents.device)
        this.databaseInfo = DbDatabaseInfo(sessionEvents.databaseInfo)
        this.realmModalities = RealmList()
        setModalities(sessionEvents.modalities)

        sessionEvents.location?.let {
            this.location = DbLocation(it)
        }

        this.analyticsId = sessionEvents.analyticsId
    }

    private fun setEvents(events: List<Event>) = realmEvents.apply {
        clear()
        addAll(events.map { DbEvent(it) })
    }

    private fun setModalities(modalities: List<Modality>) = realmModalities.apply {
        clear()
        addAll(modalities.map { DbModality(it) })
    }

    fun timeRelativeToStartTime(time: Long): Long = time - startTime
}

fun DbSession.toDomain(): SessionEvents {
    val session = SessionEvents(id = id,
        projectId = projectId,
        appVersionName = appVersionName,
        libVersionName = libVersionName,
        language = language,
        device = device?.toDomainDevice() ?: Device(),
        startTime = startTime,
        databaseInfo = databaseInfo.toDomainDatabaseInfo(),
        events = ArrayList(realmEvents.mapNotNull { it.toDomainEvent() }),
        modalities = ArrayList(realmModalities.map { it.toDomain() })
    )

    session.relativeEndTime = this.relativeEndTime
    session.relativeUploadTime = this.relativeUploadTime
    session.databaseInfo = this.databaseInfo.toDomainDatabaseInfo()
    session.location = this.location?.toDomainLocation()
    session.analyticsId = this.analyticsId
    return session
}
