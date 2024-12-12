package com.simprints.feature.clientapi.mappers.request.requestFactories

import com.simprints.core.domain.tokenization.asTokenizableRaw
import com.simprints.feature.clientapi.mappers.request.builders.IdentifyRequestBuilder
import com.simprints.feature.clientapi.mappers.request.extractors.ActionRequestExtractor
import com.simprints.feature.clientapi.mappers.request.extractors.IdentifyRequestExtractor
import com.simprints.feature.clientapi.mappers.request.validators.IdentifyValidator
import com.simprints.infra.orchestration.data.ActionConstants
import com.simprints.infra.orchestration.data.ActionRequest
import com.simprints.infra.orchestration.data.ActionRequestIdentifier
import io.mockk.mockk

internal object IdentifyRequestActionFactory : RequestActionFactory() {
    override fun getIdentifier() = ActionRequestIdentifier(
        packageName = MOCK_PACKAGE,
        actionName = ActionConstants.ACTION_IDENTIFY,
        callerPackageName = "",
        contractVersion = 1,
        timestampMs = 0L,
    )

    override fun getValidSimprintsRequest() = ActionRequest.IdentifyActionRequest(
        actionIdentifier = getIdentifier(),
        projectId = MOCK_PROJECT_ID,
        moduleId = MOCK_MODULE_ID.asTokenizableRaw(),
        userId = MOCK_USER_ID.asTokenizableRaw(),
        metadata = MOCK_METADATA,
        biometricDataSource = MOCK_BIOMETRIC_DATA_SOURCE,
        unknownExtras = emptyMap(),
    )

    override fun getValidator(extractor: ActionRequestExtractor): IdentifyValidator =
        IdentifyValidator(extractor as IdentifyRequestExtractor)

    override fun getBuilder(extractor: ActionRequestExtractor): IdentifyRequestBuilder = IdentifyRequestBuilder(
        actionIdentifier = getIdentifier(),
        extractor = extractor as IdentifyRequestExtractor,
        project = mockk(),
        tokenizationProcessor = mockk(),
        validator = getValidator(extractor),
    )

    override fun getMockExtractor(): IdentifyRequestExtractor {
        val mockIdentifyExtractor = mockk<IdentifyRequestExtractor>()
        setMockDefaultExtractor(mockIdentifyExtractor)
        return mockIdentifyExtractor
    }
}
