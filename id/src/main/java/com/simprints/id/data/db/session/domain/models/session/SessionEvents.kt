package com.simprints.id.data.db.session.domain.models.session

import androidx.annotation.Keep
import com.simprints.id.data.db.session.domain.models.events.Event
import com.simprints.id.data.db.session.domain.models.events.EventType
import com.simprints.id.tools.TimeHelper
import java.util.*

@Keep
open class SessionEvents(var projectId: String,
                         var appVersionName: String,
                         var libVersionName: String = "",
                         var language: String,
                         var device: Device,
                         var startTime: Long = 0,
                         var databaseInfo: DatabaseInfo,
                         val id: String = UUID.randomUUID().toString(),
                         val events: ArrayList<Event> = arrayListOf()) {

    companion object {
        // When the sync starts, any open activeSession started GRACE_PERIOD ms
        // before it will be considered closed
        const val GRACE_PERIOD: Long = 1000 * 60 * 5 // 5 minutes
     }


    var relativeEndTime: Long = 0
    var relativeUploadTime: Long = 0
    var location: Location? = null
    var analyticsId: String? = null

    // Function and not kotlin properties to avoid them to build serialised/deserialised
    fun isClosed(): Boolean = relativeEndTime > 0
    fun isOpen(): Boolean = !isClosed()

    fun isPossiblyInProgress(timeHelper: TimeHelper): Boolean =
        timeHelper.msBetweenNowAndTime(startTime) < GRACE_PERIOD

    fun hasEvent(eventType: EventType) =
        events.any { it.type == eventType }

    fun closeIfRequired(timeHelper: TimeHelper) {
        if (!isClosed()) {
            relativeEndTime = timeRelativeToStartTime(timeHelper.now())
        }
    }

    fun timeRelativeToStartTime(time: Long): Long = time - startTime

}
