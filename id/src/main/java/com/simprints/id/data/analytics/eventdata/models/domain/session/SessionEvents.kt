package com.simprints.id.data.analytics.eventdata.models.domain.session

import com.google.gson.GsonBuilder
import com.simprints.id.BuildConfig
import com.simprints.id.data.analytics.eventdata.models.domain.events.ArtificialTerminationEvent
import com.simprints.id.data.analytics.eventdata.models.domain.events.Event
import com.simprints.id.tools.TimeHelper
import timber.log.Timber
import java.util.*

open class SessionEvents(var projectId: String,
                         var appVersionName: String,
                         var libVersionName: String = "",
                         var language: String,
                         var device: Device,
                         var startTime: Long = 0,
                         val id: String = UUID.randomUUID().toString()) {

    companion object {
        // When the sync starts, any open activeSession started GRACE_PERIOD ms
        // before it will be considered closed
        const val GRACE_PERIOD: Long = 1000 * 60 * 5 // 5 minutes
     }

    var events: ArrayList<Event> = ArrayList()
    var relativeEndTime: Long = 0
    var relativeUploadTime: Long = 0
    var databaseInfo: DatabaseInfo? = null
    var location: Location? = null
    var analyticsId: String? = null

    // Function and not kotlin properties to avoid them to get serialised/deserialised
    fun isClosed(): Boolean = relativeEndTime > 0
    fun isOpen(): Boolean = !isClosed()

    fun addArtificialTerminationIfRequired(timeHelper: TimeHelper, reason: ArtificialTerminationEvent.Reason) {
        if (isOpen()) {
            addEvent(ArtificialTerminationEvent(timeRelativeToStartTime(timeHelper.now()), reason))
        }
    }

    fun closeIfRequired(timeHelper: TimeHelper) {
        if (!isClosed()) {
            relativeEndTime = timeRelativeToStartTime(timeHelper.now())
        }
    }

    fun timeRelativeToStartTime(time: Long): Long = time - startTime

    fun isPossiblyInProgress(timeHelper: TimeHelper): Boolean =
        timeHelper.msBetweenNowAndTime(startTime) < GRACE_PERIOD


    fun addEvent(event: Event) {
        if(BuildConfig.DEBUG) {
            Timber.d("Add event: ${GsonBuilder().create().toJson(event)}")
        }
        events.add(event)
    }
}
