package com.simprints.feature.clientapi.mappers.request.requestFactories

import com.simprints.core.domain.tokenization.asTokenizableRaw
import com.simprints.feature.clientapi.mappers.request.builders.EnrolRequestBuilder
import com.simprints.feature.clientapi.mappers.request.extractors.ActionRequestExtractor
import com.simprints.feature.clientapi.mappers.request.extractors.EnrolRequestExtractor
import com.simprints.feature.clientapi.mappers.request.validators.EnrolValidator
import com.simprints.infra.orchestration.data.ActionConstants
import com.simprints.infra.orchestration.data.ActionRequest
import com.simprints.infra.orchestration.data.ActionRequestIdentifier
import io.mockk.mockk

internal object EnrolActionFactory : RequestActionFactory() {
    override fun getIdentifier() = ActionRequestIdentifier(
        packageName = MOCK_PACKAGE,
        actionName = ActionConstants.ACTION_ENROL,
        callerPackageName = "",
        contractVersion = 1,
        timestampMs = 0L,
    )

    override fun getValidSimprintsRequest() = ActionRequest.EnrolActionRequest(
        actionIdentifier = getIdentifier(),
        projectId = MOCK_PROJECT_ID,
        userId = MOCK_USER_ID.asTokenizableRaw(),
        metadata = MOCK_METADATA,
        moduleId = MOCK_MODULE_ID.asTokenizableRaw(),
        biometricDataSource = MOCK_BIOMETRIC_DATA_SOURCE,
        unknownExtras = emptyMap(),
    )

    override fun getBuilder(extractor: ActionRequestExtractor): EnrolRequestBuilder = EnrolRequestBuilder(
        actionIdentifier = getIdentifier(),
        extractor = extractor as EnrolRequestExtractor,
        project = mockk(),
        tokenizationProcessor = mockk(),
        validator = getValidator(extractor),
    )

    override fun getValidator(extractor: ActionRequestExtractor): EnrolValidator = EnrolValidator(extractor as EnrolRequestExtractor)

    override fun getMockExtractor(): EnrolRequestExtractor {
        val mockEnrolmentExtractor = mockk<EnrolRequestExtractor>()
        setMockDefaultExtractor(mockEnrolmentExtractor)
        return mockEnrolmentExtractor
    }
}
