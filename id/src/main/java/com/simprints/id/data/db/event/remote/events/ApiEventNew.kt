package com.simprints.id.data.db.event.remote.events

import androidx.annotation.Keep
import com.simprints.id.data.db.event.domain.events.EventLabel
import com.simprints.id.domain.modality.Modes

@Keep
class ApiEvents(val events: List<ApiEvent>)

@Keep
open class ApiEvent(val id: String,
                    val labels: List<ApiEventLabel>,
                    val payload: ApiEventPayload) {

    sealed class ApiEventLabel(val key: String) {

        class ApiProjectId(label: String) : ApiEventLabel("projectId")
        class ApiSubjectId(label: String) : ApiEventLabel("subjectId")
        class ApiAttendantId(labels: String) : ApiEventLabel("attendantId")
        class ApiModuleId(label: List<String>) : ApiEventLabel("moduleId")
        class ApiMode(label: List<Modes>) : ApiEventLabel("mode")
        class ApiSessionId(label: String) : ApiEventLabel("sessionId")
    }
}

fun List<EventLabel>.fromDomainToApi() =
    this.mapNotNull {
        when (it) {
            is EventLabel.ProjectId -> {
                ApiEvent.ApiEventLabel.ApiProjectId(it.label)
            }
            is EventLabel.SubjectId -> {
                EventLabel.SubjectId(it.value.first())
            }
            is EventLabel.AttendantId -> {
                EventLabel.AttendantId(it.value.first())
            }
            is EventLabel.ModuleId -> {
                EventLabel.ModuleId(it.value)
            }
            is EventLabel.Mode -> {
                EventLabel.Mode(it.value.map { mode -> Modes.valueOf(mode) })
            }
            is EventLabel.SessionId -> TODO()
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
