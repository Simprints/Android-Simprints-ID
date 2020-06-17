package com.simprints.id.orchestrator.steps.core.requests

import kotlinx.android.parcel.Parcelize

@Parcelize
data class GuidSelectionRequest(val projectId: String,
                                val sessionId: String,
                                val selectedGuid: String) : CoreRequest {

    fun toMap() = mapOf(
        KEY_PROJECT_ID to projectId,
        KEY_SESSION_ID to sessionId,
        KEY_SELECTED_GUID to selectedGuid
    )

    companion object {
        private const val KEY_PROJECT_ID = "project_id"
        private const val KEY_SESSION_ID = "session_id"
        private const val KEY_SELECTED_GUID = "selected_guid"

        fun fromMap(map: Map<String, Any>) = GuidSelectionRequest(
            map[KEY_PROJECT_ID].toString(),
            map[KEY_SESSION_ID].toString(),
            map[KEY_SELECTED_GUID].toString()
        )
    }
}
