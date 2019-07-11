package com.simprints.id.domain.moduleapi.app.requests

import com.simprints.moduleapi.app.requests.confirmations.IAppIdentifyConfirmation

data class AppIdentityConfirmationRequest(override val projectId: String,
                                          val sessionId: String,
                                          val selectedGuid: String) : AppBaseRequest {

    constructor(appRequest: IAppIdentifyConfirmation) :
        this(appRequest.projectId,
            appRequest.sessionId,
            appRequest.selectedGuid)

    fun toMap() = mapOf(
        KEY_PROJECT_ID to projectId,
        KEY_SESSION_ID to sessionId,
        KEY_SELECTED_GUID to selectedGuid
    )

    companion object {
        private const val KEY_PROJECT_ID = "project_id"
        private const val KEY_SESSION_ID = "session_id"
        private const val KEY_SELECTED_GUID = "selected_guid"

        fun fromMap(map: Map<String, Any>) = AppIdentityConfirmationRequest(
            map[KEY_PROJECT_ID].toString(),
            map[KEY_SESSION_ID].toString(),
            map[KEY_SELECTED_GUID].toString()
        )
    }

}
