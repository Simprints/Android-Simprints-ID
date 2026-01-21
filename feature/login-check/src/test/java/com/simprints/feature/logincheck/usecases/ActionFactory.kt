package com.simprints.feature.logincheck.usecases

import com.simprints.core.domain.tokenization.asTokenizableRaw
import com.simprints.infra.orchestration.data.ActionConstants
import com.simprints.infra.orchestration.data.ActionRequest
import com.simprints.infra.orchestration.data.ActionRequestIdentifier

internal object ActionFactory {
    fun getEnrolRequest(unknownExtras: Map<String, String> = emptyMap()) = ActionRequest.EnrolActionRequest(
        actionIdentifier = ActionRequestIdentifier(
            packageName = "com.simprints.id",
            actionName = ActionConstants.ACTION_ENROL,
            callerPackageName = "",
            contractVersion = 1,
            timestampMs = 0L,
        ),
        projectId = MOCK_PROJECT_ID,
        userId = MOCK_USER_ID,
        moduleId = MOCK_MODULE_ID,
        unknownExtras = unknownExtras,
        biometricDataSource = MOCK_BIOMETRIC_DATA_SOURCE,
        metadata = "",
    )

    fun getIdentifyRequest(extras: Map<String, String> = emptyMap()) = ActionRequest.IdentifyActionRequest(
        actionIdentifier = ActionRequestIdentifier(
            packageName = "com.simprints.id",
            actionName = ActionConstants.ACTION_IDENTIFY,
            callerPackageName = "",
            contractVersion = 1,
            timestampMs = 0L,
        ),
        projectId = MOCK_PROJECT_ID,
        userId = MOCK_USER_ID,
        moduleId = MOCK_MODULE_ID,
        unknownExtras = extras,
        biometricDataSource = MOCK_BIOMETRIC_DATA_SOURCE,
        metadata = "",
    )

    fun getVerifyRequest(extras: Map<String, String> = emptyMap()) = ActionRequest.VerifyActionRequest(
        actionIdentifier = ActionRequestIdentifier(
            packageName = "com.simprints.id",
            actionName = ActionConstants.ACTION_VERIFY,
            callerPackageName = "",
            contractVersion = 1,
            timestampMs = 0L,
        ),
        projectId = MOCK_PROJECT_ID,
        userId = MOCK_USER_ID,
        moduleId = MOCK_MODULE_ID,
        unknownExtras = extras,
        biometricDataSource = MOCK_BIOMETRIC_DATA_SOURCE,
        metadata = "",
        verifyGuid = MOCK_GUID,
    )

    fun getEnrolLastRequest() = ActionRequest.EnrolLastBiometricActionRequest(
        actionIdentifier = ActionRequestIdentifier(
            packageName = "com.simprints.id",
            actionName = ActionConstants.ACTION_ENROL_LAST_BIOMETRICS,
            callerPackageName = "",
            contractVersion = 1,
            timestampMs = 0L,
        ),
        projectId = MOCK_PROJECT_ID,
        userId = MOCK_USER_ID,
        moduleId = MOCK_MODULE_ID,
        sessionId = "sessionId",
        metadata = "",
        unknownExtras = emptyMap(),
    )

    fun getConfirmationRequest() = ActionRequest.ConfirmIdentityActionRequest(
        actionIdentifier = ActionRequestIdentifier(
            packageName = "com.simprints.id",
            actionName = ActionConstants.ACTION_CONFIRM_IDENTITY,
            callerPackageName = "",
            contractVersion = 1,
            timestampMs = 0L,
        ),
        projectId = MOCK_PROJECT_ID,
        userId = MOCK_USER_ID,
        sessionId = "sessionId",
        selectedGuid = "selectedGuid",
        metadata = "",
        unknownExtras = emptyMap(),
    )

    val MOCK_USER_ID = "userId".asTokenizableRaw()
    val MOCK_MODULE_ID = "moduleId".asTokenizableRaw()
    const val MOCK_PROJECT_ID = "projectId"
    private const val MOCK_BIOMETRIC_DATA_SOURCE = ""
    private const val MOCK_GUID = "123e4567-e89b-12d3-a456-426614174000"
}
