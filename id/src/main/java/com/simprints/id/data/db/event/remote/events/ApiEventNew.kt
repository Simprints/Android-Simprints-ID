package com.simprints.id.data.db.event.remote.events

import androidx.annotation.Keep
import com.simprints.id.data.db.event.domain.events.Event
import com.simprints.id.domain.modality.Modes
import com.simprints.id.data.db.event.domain.events.Event.*
import com.simprints.id.data.db.event.remote.events.ApiEvent.*


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
    }
}

fun List<Event.EventLabel>.fromDomainToApi() =
    this.mapNotNull {
        when (it) {
            is EventLabel.ProjectId -> {
                ApiEventLabel.ApiProjectId(it.labels.first())
            }
            is EventLabel.SubjectId -> {
                ApiEventLabel.ApiSubjectId(it.labels.first())
            }
            is EventLabel.AttendantId -> {
                ApiEventLabel.ApiAttendantId(it.labels.first())
            }
            is EventLabel.ModuleId -> {
                ApiEventLabel.ApiModuleId(it.labels)
            }
            is EventLabel.Mode -> {
                ApiEventLabel.ApiMode(it.labels)
            }
            is EventLabel.SessionId -> {
                ApiEventLabel.ApiSessionId(it.labels.first())
            }
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
