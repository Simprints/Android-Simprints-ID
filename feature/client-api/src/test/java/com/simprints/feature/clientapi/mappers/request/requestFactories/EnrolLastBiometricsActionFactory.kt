package com.simprints.feature.clientapi.mappers.request.requestFactories

import com.simprints.core.domain.tokenization.asTokenizableRaw
import com.simprints.feature.clientapi.mappers.request.builders.EnrolLastBiometricsRequestBuilder
import com.simprints.feature.clientapi.mappers.request.extractors.ActionRequestExtractor
import com.simprints.feature.clientapi.mappers.request.extractors.EnrolLastBiometricsRequestExtractor
import com.simprints.feature.clientapi.mappers.request.validators.EnrolLastBiometricsValidator
import com.simprints.infra.orchestration.data.ActionConstants
import com.simprints.infra.orchestration.data.ActionRequest
import com.simprints.infra.orchestration.data.ActionRequestIdentifier
import io.mockk.every
import io.mockk.mockk

internal object EnrolLastBiometricsActionFactory : RequestActionFactory() {
    override fun getIdentifier() = ActionRequestIdentifier(
        packageName = MOCK_PACKAGE,
        actionName = ActionConstants.ACTION_ENROL_LAST_BIOMETRICS,
        callerPackageName = "",
        contractVersion = 1,
        timestampMs = 0L,
    )

    override fun getValidSimprintsRequest() = ActionRequest.EnrolLastBiometricActionRequest(
        actionIdentifier = getIdentifier(),
        projectId = MOCK_PROJECT_ID,
        userId = MOCK_USER_ID.asTokenizableRaw(),
        moduleId = MOCK_MODULE_ID.asTokenizableRaw(),
        metadata = MOCK_METADATA,
        sessionId = MOCK_SESSION_ID,
        unknownExtras = emptyMap(),
    )

    override fun getValidator(extractor: ActionRequestExtractor): EnrolLastBiometricsValidator = EnrolLastBiometricsValidator(
        extractor as EnrolLastBiometricsRequestExtractor,
        MOCK_SESSION_ID,
        true,
    )

    override fun getBuilder(extractor: ActionRequestExtractor): EnrolLastBiometricsRequestBuilder = EnrolLastBiometricsRequestBuilder(
        actionIdentifier = getIdentifier(),
        extractor = extractor as EnrolLastBiometricsRequestExtractor,
        project = mockk(),
        tokenizationProcessor = mockk(),
        validator = getValidator(extractor),
    )

    override fun getMockExtractor(): EnrolLastBiometricsRequestExtractor {
        val mockEnrolLastBiometricsExtractor = mockk<EnrolLastBiometricsRequestExtractor>()
        setMockDefaultExtractor(mockEnrolLastBiometricsExtractor)
        every { mockEnrolLastBiometricsExtractor.getProjectId() } returns MOCK_PROJECT_ID
        every { mockEnrolLastBiometricsExtractor.getUserId() } returns MOCK_USER_ID
        every { mockEnrolLastBiometricsExtractor.getModuleId() } returns MOCK_MODULE_ID
        every { mockEnrolLastBiometricsExtractor.getMetadata() } returns MOCK_METADATA
        every { mockEnrolLastBiometricsExtractor.getSessionId() } returns MOCK_SESSION_ID
        every { mockEnrolLastBiometricsExtractor.getUnknownExtras() } returns emptyMap()
        return mockEnrolLastBiometricsExtractor
    }
}
