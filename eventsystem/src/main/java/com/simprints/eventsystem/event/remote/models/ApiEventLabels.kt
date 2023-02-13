package com.simprints.eventsystem.event.remote.models

import androidx.annotation.Keep
import com.simprints.eventsystem.event.domain.models.EventLabels
import com.simprints.eventsystem.event.remote.models.ApiEventLabels.Companion.DEVICE_ID_LABEL_KEY
import com.simprints.eventsystem.event.remote.models.ApiEventLabels.Companion.PROJECT_ID_LABEL_KEY
import com.simprints.eventsystem.event.remote.models.ApiEventLabels.Companion.SESSION_ID_LABEL_KEY

@Keep
class ApiEventLabels : HashMap<String, List<String>>() {

    companion object {
        const val PROJECT_ID_LABEL_KEY = "projectId"
        const val SESSION_ID_LABEL_KEY = "sessionId"
        const val DEVICE_ID_LABEL_KEY = "deviceId"

    }
}

fun ApiEventLabels.fromApiToDomain() =
    EventLabels(
        projectId = this[PROJECT_ID_LABEL_KEY]?.firstOrNull(),
        sessionId = this[SESSION_ID_LABEL_KEY]?.firstOrNull(),
        deviceId = this[DEVICE_ID_LABEL_KEY]?.firstOrNull()
    )

fun EventLabels.fromDomainToApi(): ApiEventLabels {
    val api = ApiEventLabels()
    projectId?.let { api.put(PROJECT_ID_LABEL_KEY, listOf(it)) }
    sessionId?.let { api.put(SESSION_ID_LABEL_KEY, listOf(it)) }
    deviceId?.let { api.put(DEVICE_ID_LABEL_KEY, listOf(it)) }
    return api
}
