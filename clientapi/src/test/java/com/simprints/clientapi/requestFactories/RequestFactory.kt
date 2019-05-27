package com.simprints.clientapi.requestFactories

import com.simprints.clientapi.clientrequests.builders.ClientRequestBuilder
import com.simprints.clientapi.clientrequests.extractors.ClientRequestExtractor
import com.simprints.clientapi.clientrequests.validators.ClientRequestValidator
import com.simprints.clientapi.domain.ClientBase
import com.simprints.clientapi.domain.requests.IntegrationInfo
import com.simprints.testtools.common.syntax.whenever

abstract class RequestFactory {

    companion object {
        const val MOCK_PROJECT_ID = "projectId"
        const val MOCK_USER_ID = "userId"
        const val MOCK_MODULE_ID = "moduleId"
        const val MOCK_METADATA = ""
        const val MOCK_VERIFY_GUID = "1d3a92c1-3410-40fb-9e88-4570c9abd150"
        const val MOCK_SESSION_ID = "ddf01a3c-3081-4d3e-b872-538731517cb9"
        const val MOCK_SELECTED_GUID = "5390ef82-9c1f-40a9-b833-2e97ab369208"
    }

    abstract fun getValidator(extractor: ClientRequestExtractor): ClientRequestValidator

    abstract fun getBuilder(extractor: ClientRequestExtractor): ClientRequestBuilder

    abstract fun getMockExtractor(): ClientRequestExtractor

    abstract fun getValidSimprintsRequest(integrationInfo: IntegrationInfo): ClientBase

    open fun setMockDefaultExtractor(mockExtractor: ClientRequestExtractor) {
        whenever(mockExtractor) { getProjectId() } thenReturn MOCK_PROJECT_ID
        whenever(mockExtractor) { getUserId() } thenReturn MOCK_USER_ID
        whenever(mockExtractor) { getModuleId() } thenReturn MOCK_MODULE_ID
        whenever(mockExtractor) { getMetadata() } thenReturn MOCK_METADATA
    }
}
