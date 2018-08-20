package com.simprints.id.data.analytics.eventData.models.session

import com.simprints.id.data.analytics.eventData.models.events.ArtificialTerminationEvent
import com.simprints.id.data.analytics.eventData.models.events.Event
import com.simprints.id.data.analytics.eventData.realm.RlSessionEvents
import com.simprints.id.tools.TimeHelper
import com.simprints.id.tools.json.SkipSerialisationProperty
import java.util.*

open class SessionEvents {
    // STOPSHIP : investigate migration

    companion object {
        // When the sync starts, any open activeSession started GRACE_PERIOD ms
        // before it will be considered closed
        const val GRACE_PERIOD = 1000 * 60 * 5 // 5 minutes
     }

    val id: String

    @SkipSerialisationProperty
    var projectId: String

    var appVersionName: String
    var libVersionName: String
    var language: String
    var device: Device
    var startTime: Long = 0L
    var events: ArrayList<Event>

    var relativeEndTime: Long = 0
    var relativeUploadTime: Long = 0
    var databaseInfo: DatabaseInfo? = null
    var location: Location? = null
    var analyticsId: String? = null

    // Function and not kotlin properties to avoid them to get serialised/deserialised
    fun isClose(): Boolean = relativeEndTime > 0
    fun isOpen(): Boolean = !isClose()

    constructor(rlSessionEvents: RlSessionEvents) : this(
        id = rlSessionEvents.id,
        projectId = rlSessionEvents.projectId,
        appVersionName = rlSessionEvents.appVersionName,
        libVersionName = rlSessionEvents.libVersionName,
        language = rlSessionEvents.language,
        device = rlSessionEvents.device ?: Device(),
        startTime = rlSessionEvents.startTime) {
        this.events = ArrayList(rlSessionEvents.getEvents())
        this.relativeEndTime = rlSessionEvents.relativeEndTime
        this.relativeUploadTime = rlSessionEvents.relativeUploadTime
        this.databaseInfo = rlSessionEvents.databaseInfo
        this.location = rlSessionEvents.location
        this.analyticsId = rlSessionEvents.analyticsId
    }

    constructor(id: String = UUID.randomUUID().toString(),
                projectId: String,
                appVersionName: String,
                libVersionName: String,
                language: String,
                device: Device,
                startTime: Long = 0) {
        this.id = id
        this.projectId = projectId
        this.appVersionName = appVersionName
        this.libVersionName = libVersionName
        this.language = language
        this.device = device
        this.startTime = startTime
        this.events = ArrayList()
    }

    fun addArtificialTerminationIfRequired(timeHelper: TimeHelper, reason: ArtificialTerminationEvent.Reason) {
        if (relativeEndTime == 0L) {
            events.add(ArtificialTerminationEvent(nowRelativeToStartTime(timeHelper), reason))
        }
    }

    fun closeIfRequired(timeHelper: TimeHelper) {
        if (!isClose()) {
            relativeEndTime = nowRelativeToStartTime(timeHelper)
        }
    }

    fun timeRelativeToStartTime(time: Long): Long = time - startTime
    fun nowRelativeToStartTime(timeHelper: TimeHelper): Long = timeRelativeToStartTime(timeHelper.msSinceBoot())

    fun isPossiblyInProgress(timeHelper: TimeHelper): Boolean =
        timeHelper.msBetweenNowAndTime(startTime) < GRACE_PERIOD
}
