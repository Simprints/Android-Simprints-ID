package com.simprints.id.data.db.event.remote.events

import androidx.annotation.Keep
import com.simprints.id.data.db.event.domain.events.Event.EventLabel
import com.simprints.id.data.db.event.domain.events.Event.EventLabel.*
import com.simprints.id.data.db.event.remote.events.ApiEvent.ApiEventLabel
import com.simprints.id.data.db.event.remote.events.ApiEvent.ApiEventLabel.*
import com.simprints.id.domain.modality.Modes

@Keep
class ApiEvents(val events: List<ApiEvent>)

@Keep
open class ApiEvent(val id: String,
                    val labels: List<ApiEventLabel>,
                    val payload: ApiEventPayload) {

    sealed class ApiEventLabel(val key: String, val labels: List<String>) {

        class ApiProjectId(labelValue: String) : ApiEventLabel("projectId", listOf(labelValue))
        class ApiSubjectId(labelValue: String) : ApiEventLabel("subjectId", listOf(labelValue))
        class ApiAttendantId(labelValue: String) : ApiEventLabel("attendantId", listOf(labelValue))
        class ApiModuleId(labelValues: List<String>) : ApiEventLabel("moduleId", labelValues)
        class ApiMode(labelValues: List<String>) : ApiEventLabel("mode", labelValues)
        class ApiSessionId(labelValue: String) : ApiEventLabel("sessionId", listOf(labelValue))
        class ApiDeviceId(labelValue: String) : ApiEventLabel("deviceId", listOf(labelValue))
    }
}

fun List<EventLabel>.fromDomainToApi() =
    this.map {
        when (it) {
            is ProjectId -> {
                ApiProjectId(it.labelValue)
            }
            is SubjectId -> {
                ApiSubjectId(it.labelValue)
            }
            is AttendantId -> {
                ApiAttendantId(it.labelValue)
            }
            is ModuleId -> {
                ApiModuleId(it.labelValues)
            }
            is Mode -> {
                ApiMode(it.labelValues.map { mode -> mode.name })
            }
            is SessionId -> {
                ApiSessionId(it.labelValue)
            }
            is DeviceId -> {
                ApiDeviceId(it.labelValue)
            }
        }
    }

fun List<ApiEventLabel>.fromApiToDomain() =
    this.map {
        when (it) {
            is ApiProjectId -> {
                ProjectId(it.labels.first())
            }
            is ApiSubjectId -> {
                SubjectId(it.labels.first())
            }
            is ApiAttendantId -> {
                AttendantId(it.labels.first())
            }
            is ApiModuleId -> {
                ModuleId(it.labels)
            }
            is ApiMode -> {
                Mode(it.labels.map { Modes.valueOf(it) })
            }
            is ApiSessionId -> {
                SessionId(it.labels.first())
            }
            is ApiDeviceId -> {
                DeviceId(it.labels.first())
            }
        }
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
//fun Event.fromDomainToApi() =
//    ApiEvent(id, getLabelsFromEvent(this), payload.fromDomainToApi())
//
//fun getLabelsFromEvent(event: Event): Map<String, List<String>> = emptyMap()
//// StopShip: fix in PS-1000
////    with(event) {
////        mapOf(
////            PROJECT_ID_LABEL to projectId,
////            MODULE_ID_LABEL to moduleId,
////            ATTENDANT_ID_LABEL to attendantId,
////            SUBJECT_ID_LABEL to subjectId,
////            MODE_LABEL to mode.map { it.name }
////        )
////    }
//
