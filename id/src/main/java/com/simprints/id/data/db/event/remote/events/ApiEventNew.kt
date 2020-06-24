package com.simprints.id.data.db.event.remote.events

import androidx.annotation.Keep
import com.simprints.id.data.db.event.domain.events.Event
import com.simprints.id.data.db.event.domain.events.Events

@Keep
class ApiEvents(val events: List<ApiEventNew>)

@Keep
class ApiEventNew(val id: String,
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

fun Events.fromDomainToApi() = ApiEvents(events.map { it.fromDomainToApi() })

fun Event.fromDomainToApi() =
    ApiEventNew(id, getLabelsFromEvent(this), payload.fromDomainToApi())

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

