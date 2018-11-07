package com.simprints.id.data.analytics.eventData.models.session

import com.simprints.id.data.analytics.eventData.models.events.ArtificialTerminationEvent
import com.simprints.id.data.analytics.eventData.models.events.Event
import com.simprints.id.data.analytics.eventData.realm.RlSession
import com.simprints.id.tools.TimeHelper
import com.simprints.id.tools.json.SkipSerialisationField
import java.util.*

open class SessionEvents {

    companion object {
        // When the sync starts, any open activeSession started GRACE_PERIOD ms
        // before it will be considered closed
        const val GRACE_PERIOD: Long = 1000 * 60 * 5 // 5 minutes
     }

    val id: String

    @SkipSerialisationField
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
    fun isClosed(): Boolean = relativeEndTime > 0
    fun isOpen(): Boolean = !isClosed()

    constructor(rlSession: RlSession) : this(
        id = rlSession.id,
        projectId = rlSession.projectId,
        appVersionName = rlSession.appVersionName,
        libVersionName = rlSession.libVersionName,
        language = rlSession.language,
        device = rlSession.device ?: Device(),
        startTime = rlSession.startTime) {
        this.events = ArrayList(rlSession.getEvents())
        this.relativeEndTime = rlSession.relativeEndTime
        this.relativeUploadTime = rlSession.relativeUploadTime
        this.databaseInfo = rlSession.databaseInfo
        this.location = rlSession.location
        this.analyticsId = rlSession.analyticsId
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
        if (isOpen()) {
            events.add(ArtificialTerminationEvent(nowRelativeToStartTime(timeHelper), reason))
        }
    }

    fun closeIfRequired(timeHelper: TimeHelper) {
        if (!isClosed()) {
            relativeEndTime = nowRelativeToStartTime(timeHelper)
        }
    }

    fun timeRelativeToStartTime(time: Long): Long = time - startTime
    fun nowRelativeToStartTime(timeHelper: TimeHelper): Long = timeRelativeToStartTime(timeHelper.now())

    fun isPossiblyInProgress(timeHelper: TimeHelper): Boolean =
        timeHelper.msBetweenNowAndTime(startTime) < GRACE_PERIOD
}
