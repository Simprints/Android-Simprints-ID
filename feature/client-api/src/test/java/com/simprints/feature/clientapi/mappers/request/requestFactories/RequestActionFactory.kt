package com.simprints.feature.clientapi.mappers.request.requestFactories

import com.simprints.feature.clientapi.mappers.request.builders.ActionRequestBuilder
import com.simprints.feature.clientapi.mappers.request.extractors.ActionRequestExtractor
import com.simprints.feature.clientapi.mappers.request.validators.RequestActionValidator
import com.simprints.infra.orchestration.data.ActionRequest
import com.simprints.infra.orchestration.data.ActionRequestIdentifier
import io.mockk.every

internal abstract class RequestActionFactory {
    companion object {
        const val MOCK_PACKAGE = "com.test.package"
        const val MOCK_PROJECT_ID = "xppPLwmR2eUmyN6LS3SN"
        const val MOCK_USER_ID = "userId"
        const val MOCK_MODULE_ID = "moduleId"
        const val MOCK_METADATA = ""
        const val MOCK_VERIFY_GUID = "1d3a92c1-3410-40fb-9e88-4570c9abd150"
        const val MOCK_SESSION_ID = "ddf01a3c-3081-4d3e-b872-538731517cb9"
        const val MOCK_SELECTED_GUID = "5390ef82-9c1f-40a9-b833-2e97ab369208"
        const val MOCK_BIOMETRIC_DATA_SOURCE = ""
    }

    abstract fun getIdentifier(): ActionRequestIdentifier

    abstract fun getValidator(extractor: ActionRequestExtractor): RequestActionValidator

    abstract fun getBuilder(extractor: ActionRequestExtractor): ActionRequestBuilder

    abstract fun getMockExtractor(): ActionRequestExtractor

    abstract fun getValidSimprintsRequest(): ActionRequest

    open fun setMockDefaultExtractor(mockExtractor: ActionRequestExtractor) {
        every { mockExtractor.getProjectId() } returns MOCK_PROJECT_ID
        every { mockExtractor.getUserId() } returns MOCK_USER_ID
        every { mockExtractor.getModuleId() } returns MOCK_MODULE_ID
        every { mockExtractor.getMetadata() } returns MOCK_METADATA
        every { mockExtractor.getSubjectAge() } returns null
        every { mockExtractor.getBiometricDataSource() } returns MOCK_BIOMETRIC_DATA_SOURCE
        every { mockExtractor.getUnknownExtras() } returns emptyMap()
    }
}
