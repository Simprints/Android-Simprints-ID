package com.simprints.feature.logincheck.usecases

import com.simprints.infra.orchestration.data.ActionConstants
import com.simprints.infra.orchestration.data.ActionRequest
import com.simprints.infra.orchestration.data.ActionRequestIdentifier

internal object ActionFactory {

    fun getFlowRequest(
        extras: Map<String, Any> = emptyMap(),
    ) = ActionRequest.EnrolActionRequest(
        actionIdentifier = ActionRequestIdentifier(
            packageName = "com.simprints.id",
            actionName = ActionConstants.ACTION_ENROL,
        ),
        projectId = MOCK_PROJECT_ID,
        userId = MOCK_USER_ID,
        moduleId = MOCK_MODULE_ID,
        unknownExtras = extras.toList(),
        metadata = "",
    )

    fun getFolowUpRequest() = ActionRequest.ConfirmIdentityActionRequest(
        actionIdentifier = ActionRequestIdentifier(
            packageName = "com.simprints.id",
            actionName = ActionConstants.ACTION_CONFIRM_IDENTITY,
        ),
        projectId = MOCK_PROJECT_ID,
        userId = MOCK_USER_ID,
        sessionId = "sessionId",
        selectedGuid = "selectedGuid",
        unknownExtras = emptyList()
    )

    const val MOCK_USER_ID = "userId"
    const val MOCK_MODULE_ID = "moduleId"
    const val MOCK_PROJECT_ID = "projectId"
}
