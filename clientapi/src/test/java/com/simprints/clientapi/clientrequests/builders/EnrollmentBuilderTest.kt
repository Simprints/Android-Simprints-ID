package com.simprints.clientapi.clientrequests.builders

import com.simprints.clientapi.clientrequests.requests.ApiVersion
import com.simprints.clientapi.clientrequests.requests.ClientEnrollmentRequest
import com.simprints.clientapi.clientrequests.requests.legacy.LegacyClientEnrollmentRequest
import com.simprints.clientapi.clientrequests.validators.EnrollmentValidator
import com.simprints.clientapi.mockextractors.MockEnrollmentExtractor.getValidEnrollmentExtractorMock
import com.simprints.clientapi.mockextractors.MockExtractorParams.MOCK_METADATA
import com.simprints.clientapi.mockextractors.MockExtractorParams.MOCK_MODULE_ID
import com.simprints.clientapi.mockextractors.MockExtractorParams.MOCK_PROJECT_ID
import com.simprints.clientapi.mockextractors.MockExtractorParams.MOCK_USER_ID
import org.junit.Assert.assertEquals
import org.junit.Test
import org.mockito.Mockito.`when`

class EnrollmentBuilderTest {

    @Test
    fun buildClientRequest_shouldSucceed() {
        val request = EnrollmentBuilder(
            getValidEnrollmentExtractorMock(),
            EnrollmentValidator(getValidEnrollmentExtractorMock())
        ).build() as ClientEnrollmentRequest

        assertEquals(request.apiVersion, ApiVersion.V2)
        assertEquals(request.projectId, MOCK_PROJECT_ID)
        assertEquals(request.moduleId, MOCK_MODULE_ID)
        assertEquals(request.userId, MOCK_USER_ID)
        assertEquals(request.metadata, MOCK_METADATA)
    }

    @Test
    fun buildLegacyClientRequest() {
        val extractor = getValidEnrollmentExtractorMock()
        `when`(extractor.getLegacyApiKey()).thenReturn("API_KEY")
        val request = EnrollmentBuilder(extractor, EnrollmentValidator(extractor)).build()
            as LegacyClientEnrollmentRequest

        assertEquals(request.apiVersion, ApiVersion.V1)
        assertEquals(request.apiKey, "API_KEY")
        assertEquals(request.moduleId, MOCK_MODULE_ID)
        assertEquals(request.userId, MOCK_USER_ID)
        assertEquals(request.metadata, MOCK_METADATA)
    }

}
