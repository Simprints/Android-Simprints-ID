package com.simprints.id.data.db.event.remote.models

import androidx.annotation.Keep
import com.simprints.id.data.db.event.domain.models.EventLabels
import com.simprints.id.data.db.event.remote.models.ApiEventLabels.Companion.ATTENDANT_ID_LABEL_KEY
import com.simprints.id.data.db.event.remote.models.ApiEventLabels.Companion.DEVICE_ID_LABEL_KEY
import com.simprints.id.data.db.event.remote.models.ApiEventLabels.Companion.MODE_LABEL_KEY
import com.simprints.id.data.db.event.remote.models.ApiEventLabels.Companion.MODULE_ID_LABEL_KEY
import com.simprints.id.data.db.event.remote.models.ApiEventLabels.Companion.PROJECT_ID_LABEL_KEY
import com.simprints.id.data.db.event.remote.models.ApiEventLabels.Companion.SESSION_ID_LABEL_KEY
import com.simprints.id.data.db.event.remote.models.ApiEventLabels.Companion.SUBJECT_ID_LABEL_KEY
import com.simprints.id.domain.modality.Modes

@Keep
class ApiEventLabels : HashMap<String, List<String>>() {

    companion object {
        const val PROJECT_ID_LABEL_KEY = "projectId"
        const val SUBJECT_ID_LABEL_KEY = "subjectId"
        const val ATTENDANT_ID_LABEL_KEY = "attendantId"
        const val MODE_LABEL_KEY = "mode"
        const val MODULE_ID_LABEL_KEY = "moduleId"
        const val SESSION_ID_LABEL_KEY = "sessionId"
        const val DEVICE_ID_LABEL_KEY = "deviceId"

    }
}

fun ApiEventLabels.fromApiToDomain() =
    EventLabels(
        projectId = this[PROJECT_ID_LABEL_KEY]?.firstOrNull(),
        subjectId = this[SUBJECT_ID_LABEL_KEY]?.firstOrNull(),
        attendantId = this[ATTENDANT_ID_LABEL_KEY]?.firstOrNull(),
        moduleIds = this[MODULE_ID_LABEL_KEY] ?: emptyList(),
        mode = (this[MODE_LABEL_KEY] ?: emptyList()).map { Modes.valueOf(it) },
        sessionId = this[SESSION_ID_LABEL_KEY]?.firstOrNull(),
        deviceId = this[DEVICE_ID_LABEL_KEY]?.firstOrNull()
    )

fun EventLabels.fromDomainToApi(): ApiEventLabels {
    val api = ApiEventLabels()
    projectId?.let { api.put(PROJECT_ID_LABEL_KEY, listOf(it)) }
    subjectId?.let { api.put(SUBJECT_ID_LABEL_KEY, listOf(it)) }
    attendantId?.let { api.put(ATTENDANT_ID_LABEL_KEY, listOf(it)) }
    moduleIds?.let { api.put(MODULE_ID_LABEL_KEY, it) }
    mode?.let { api.put(MODE_LABEL_KEY, it.map { it.name }) }
    sessionId?.let { api.put(SESSION_ID_LABEL_KEY, listOf(it)) }
    deviceId?.let { api.put(DEVICE_ID_LABEL_KEY, listOf(it)) }
    return api
}
