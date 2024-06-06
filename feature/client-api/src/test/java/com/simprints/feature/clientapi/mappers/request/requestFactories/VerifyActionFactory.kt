package com.simprints.feature.clientapi.mappers.request.requestFactories

import com.simprints.core.domain.tokenization.asTokenizableRaw
import com.simprints.feature.clientapi.mappers.request.builders.VerifyRequestBuilder
import com.simprints.feature.clientapi.mappers.request.extractors.ActionRequestExtractor
import com.simprints.feature.clientapi.mappers.request.extractors.VerifyRequestExtractor
import com.simprints.feature.clientapi.mappers.request.validators.VerifyValidator
import com.simprints.infra.orchestration.data.ActionConstants
import com.simprints.infra.orchestration.data.ActionRequest
import com.simprints.infra.orchestration.data.ActionRequestIdentifier
import io.mockk.every
import io.mockk.mockk

internal object VerifyActionFactory : RequestActionFactory() {

    override fun getIdentifier() = ActionRequestIdentifier(
        packageName = MOCK_PACKAGE,
        actionName = ActionConstants.ACTION_VERIFY,
    )

    override fun getValidSimprintsRequest() = ActionRequest.VerifyActionRequest(
        actionIdentifier = getIdentifier(),
        projectId = MOCK_PROJECT_ID,
        moduleId = MOCK_MODULE_ID.asTokenizableRaw(),
        userId = MOCK_USER_ID.asTokenizableRaw(),
        metadata = MOCK_METADATA,
        verifyGuid = MOCK_VERIFY_GUID,
        biometricDataSource = MOCK_BIOMETRIC_DATA_SOURCE,
        callerPackageName = MOCK_CALLER_PACKAGE_NAME,
        unknownExtras = emptyMap()
    )

    override fun getBuilder(extractor: ActionRequestExtractor): VerifyRequestBuilder =
        VerifyRequestBuilder(
            actionIdentifier = getIdentifier(),
            extractor = extractor as VerifyRequestExtractor,
            project = mockk(),
            tokenizationProcessor = mockk(),
            validator = getValidator(extractor)
        )

    override fun getValidator(extractor: ActionRequestExtractor): VerifyValidator =
        VerifyValidator(extractor as VerifyRequestExtractor)

    override fun getMockExtractor(): VerifyRequestExtractor {
        val mockVerifyExtractor = mockk<VerifyRequestExtractor>()
        setMockDefaultExtractor(mockVerifyExtractor)
        every { mockVerifyExtractor.getVerifyGuid() } returns MOCK_VERIFY_GUID
        return mockVerifyExtractor
    }

}
