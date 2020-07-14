package com.simprints.id.data.db.event.remote.events

import androidx.annotation.Keep
import com.simprints.id.data.db.event.domain.events.Event
import com.simprints.id.data.db.event.domain.events.Event.EventLabel
import com.simprints.id.data.db.event.domain.events.Event.EventLabel.*

@Keep
class ApiEvents(val events: List<ApiEvent>)

@Keep
open class ApiEvent(val id: String,
                    val labels: Map<String, List<String>>,
                    val payload: ApiEventPayload)

fun EventLabel.fromDomainToApi(): Pair<String, List<String>> =
    when (this) { //StopShip: label as constants
        is ProjectId -> "projectId" to listOf(labelValue)
        is SubjectId -> "subjectId" to listOf(labelValue)
        is AttendantId -> "attendantId" to listOf(labelValue)
        is ModuleId -> "moduleId" to labelValues
        is Mode -> "mode" to labelValues.map { it.name }
        is SessionId -> "sessionId" to listOf(labelValue)
        is DeviceId -> "deviceId" to listOf(labelValue)
    }


//
//fun ApiEvent.fromApiToDomainOrNullIfNoBiometricReferences() = try {
//    val payload = payload.fromApiToDomain()
//    payload?.let {
//        Event(id, EventLabel.fromApiToDomain(labels), it)
//    }
//} catch (t: Throwable) {
//    throw IllegalStateException("Did not get all the labels from cloud")
//}
//
//
//fun ApiEventPayload.fromApiToDomain(): EventPayload? = when (this) {
//    is ApiEnrolmentRecordCreationPayload -> this.fromApiToDomainOrNullIfNoBiometricReferences()
//    is ApiEnrolmentRecordDeletionPayload -> this.fromApiToDomainDeletion()
//    is ApiEnrolmentRecordMovePayload -> this.fromApiToDomainAndNullForCreationIfBiometricRefsAreNull()
//    else -> throw IllegalStateException("Invalid payload type for events")
//}
//
//fun Events.fromDomainToApi() = ApiEvents(events.map { it.fromDomainToApi() })
//
fun Event.fromDomainToApi() =
    ApiEvent(id, labels.map { it.fromDomainToApi() }.toMap(), payload.fromDomainToApi())
