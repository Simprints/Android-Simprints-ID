package com.simprints.id.data.db.event.remote.events

import androidx.annotation.Keep
import com.simprints.id.data.db.event.domain.models.Event
import com.simprints.id.data.db.event.domain.models.EventLabels
import okhttp3.internal.toImmutableMap

@Keep
class ApiEvents(val events: List<ApiEvent>)

@Keep
open class ApiEvent(val id: String,
                    val labels: Map<String, List<String>>,
                    val payload: ApiEventPayload)

fun EventLabels.fromDomainToApi(): Map<String, List<String>> {
    val api = mutableMapOf<String, List<String>>()
    projectId?.let { api.put("projectId", listOf(it)) }
    subjectId?.let { api.put("subjectId", listOf(it)) }
    attendantId?.let { api.put("attendantId", listOf(it)) }
    moduleIds?.let { api.put("moduleIds", it) }
    mode?.let { api.put("mode", it.map { it.name }) }
    sessionId?.let { api.put("sessionId", listOf(it)) }
    deviceId?.let { api.put("deviceId", listOf(it)) }
    return api.toImmutableMap()
}

fun Event.fromDomainToApi() =
    ApiEvent(id, labels.fromDomainToApi(), payload.fromDomainToApi())
