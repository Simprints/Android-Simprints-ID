package com.simprints.clientapi.requestFactories

import com.simprints.clientapi.clientrequests.builders.ClientRequestBuilder
import com.simprints.clientapi.clientrequests.extractors.ClientRequestExtractor
import com.simprints.clientapi.clientrequests.validators.ClientRequestValidator
import org.mockito.Mockito

abstract class MockClientRequestFactory {

    companion object {
        const val MOCK_PROJECT_ID = "projectId"
        const val MOCK_USER_ID = "userId"
        const val MOCK_MODULE_ID = "moduleId"
        const val MOCK_METADATA = ""
        const val MOCK_VERIFY_GUID = "1d3a92c1-3410-40fb-9e88-4570c9abd150"
    }

    abstract fun getValidator(extractor: ClientRequestExtractor): ClientRequestValidator

    abstract fun getBuilder(extractor: ClientRequestExtractor): ClientRequestBuilder

    abstract fun getValidMockExtractor(): ClientRequestExtractor

    protected fun setMockDefaultExtractor(mockExtractor: ClientRequestExtractor) {
        Mockito.`when`(mockExtractor.getProjectId()).thenReturn(MOCK_PROJECT_ID)
        Mockito.`when`(mockExtractor.getUserId()).thenReturn(MOCK_USER_ID)
        Mockito.`when`(mockExtractor.getModuleId()).thenReturn(MOCK_MODULE_ID)
        Mockito.`when`(mockExtractor.getMetatdata()).thenReturn(MOCK_METADATA)
    }

}
