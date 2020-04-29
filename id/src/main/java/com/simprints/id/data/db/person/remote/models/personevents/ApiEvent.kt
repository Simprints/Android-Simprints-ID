package com.simprints.id.data.db.person.remote.models.personevents

import androidx.annotation.Keep
import com.simprints.id.data.db.person.domain.personevents.Event
import com.simprints.id.data.db.person.domain.personevents.Events
import com.simprints.id.data.db.person.remote.models.personevents.ApiEvent.Companion.ATTENDANT_ID_LABEL
import com.simprints.id.data.db.person.remote.models.personevents.ApiEvent.Companion.MODE_LABEL
import com.simprints.id.data.db.person.remote.models.personevents.ApiEvent.Companion.MODULE_ID_LABEL
import com.simprints.id.data.db.person.remote.models.personevents.ApiEvent.Companion.PROJECT_ID_LABEL
import com.simprints.id.data.db.person.remote.models.personevents.ApiEvent.Companion.SUBJECT_ID_LABEL

@Keep
class ApiEvents(val events: List<ApiEvent>)

@Keep
class ApiEvent(val id: String,
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
    ApiEvent(id, getLabelsFromEvent(this), payload.fromDomainToApi())

fun getLabelsFromEvent(event: Event): Map<String, List<String>> =
    with(event) {
        mapOf(
            PROJECT_ID_LABEL to projectId,
            MODULE_ID_LABEL to moduleId,
            ATTENDANT_ID_LABEL to attendantId,
            SUBJECT_ID_LABEL to subjectId,
            MODE_LABEL to mode.map { it.name }
        )
    }

