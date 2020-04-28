package com.simprints.id.domain.moduleapi.core.requests

import com.simprints.id.domain.moduleapi.app.requests.AppRequest
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
        private const val KEY_USER_ID = "user_id"
        private const val KEY_SESSION_ID = "session_id"
        private const val KEY_SELECTED_GUID = "selected_guid"

        fun fromMap(map: Map<String, Any>) = AppRequest.AppIdentityConfirmationRequest(
            map[KEY_PROJECT_ID].toString(),
            map[KEY_USER_ID].toString(),
            map[KEY_SESSION_ID].toString(),
            map[KEY_SELECTED_GUID].toString()
        )
    }
}
