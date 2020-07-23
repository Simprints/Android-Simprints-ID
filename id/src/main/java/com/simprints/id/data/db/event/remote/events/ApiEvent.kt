package com.simprints.id.data.db.event.remote.events

import androidx.annotation.Keep
import com.simprints.id.data.db.event.domain.models.Event
import com.simprints.id.data.db.event.domain.models.EventLabel
import com.simprints.id.data.db.event.domain.models.EventLabel.*
import com.simprints.id.data.db.event.domain.models.EventLabel.EventLabelKey.*

@Keep
class ApiEvents(val events: List<ApiEvent>)

@Keep
open class ApiEvent(val id: String,
                    val labels: Map<String, List<String>>,
                    val payload: ApiEventPayload)

fun EventLabel.fromDomainToApi(): Pair<String, List<String>> =
    when (this.key) {
        PROJECT_ID -> "projectId" to (this as ProjectIdLabel).values
        SUBJECT_ID -> "subjectId" to (this as SubjectIdLabel).values
        ATTENDANT_ID -> "attendantId" to (this as AttendantIdLabel).values
        MODULE_IDS -> "moduleId" to (this as ModuleIdsLabel).values
        MODES -> "mode" to (this as ModuleIdsLabel).values
        SESSION_ID -> "sessionId" to (this as SessionIdLabel).values
        DEVICE_ID -> "deviceId" to (this as DeviceIdLabel).values
    }

fun Event.fromDomainToApi() =
    ApiEvent(id, labels.map { it.fromDomainToApi() }.toMap(), payload.fromDomainToApi())
