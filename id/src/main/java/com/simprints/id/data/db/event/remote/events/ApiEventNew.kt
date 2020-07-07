package com.simprints.id.data.db.event.remote.events

import androidx.annotation.Keep
import com.simprints.id.data.db.event.domain.events.Event
import com.simprints.id.data.db.event.domain.events.EventLabel
import com.simprints.id.data.db.event.domain.events.EventPayload
import com.simprints.id.data.db.event.domain.events.Events
import com.simprints.id.data.db.event.remote.events.subject.*

@Keep
class ApiEvents(val events: List<ApiEvent>)

@Keep
open class ApiEvent(val id: String,
                    val labels: Map<String, List<String>>,
                    val payload: ApiEventPayload) {
    companion object {
        const val PROJECT_ID_LABEL = "projectId"
        const val ATTENDANT_ID_LABEL = "attendantId"
        const val MODULE_ID_LABEL = "moduleId"
        const val SUBJECT_ID_LABEL = "subjectId"
        const val MODE_LABEL = "mode"
    }
}

fun ApiEvent.fromApiToDomainOrNullIfNoBiometricReferences() = try {
    val payload = payload.fromApiToDomain()
    payload?.let {
        Event(id, EventLabel.fromApiToDomain(labels), it)
    }
} catch (t: Throwable) {
    throw IllegalStateException("Did not get all the labels from cloud")
}


fun ApiEventPayload.fromApiToDomain(): EventPayload? = when (this) {
    is ApiEnrolmentRecordCreationPayload -> this.fromApiToDomainOrNullIfNoBiometricReferences()
    is ApiEnrolmentRecordDeletionPayload -> this.fromApiToDomainDeletion()
    is ApiEnrolmentRecordMovePayload -> this.fromApiToDomainAndNullForCreationIfBiometricRefsAreNull()
    else -> throw IllegalStateException("Invalid payload type for events")
}

fun Events.fromDomainToApi() = ApiEvents(events.map { it.fromDomainToApi() })

fun Event.fromDomainToApi() =
    ApiEvent(id, getLabelsFromEvent(this), payload.fromDomainToApi())

fun getLabelsFromEvent(event: Event): Map<String, List<String>> = emptyMap()
// StopShip: fix in PS-1000
//    with(event) {
//        mapOf(
//            PROJECT_ID_LABEL to projectId,
//            MODULE_ID_LABEL to moduleId,
//            ATTENDANT_ID_LABEL to attendantId,
//            SUBJECT_ID_LABEL to subjectId,
//            MODE_LABEL to mode.map { it.name }
//        )
//    }

