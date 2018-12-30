package com.simprints.clientapi.mockextractors

import com.simprints.clientapi.clientrequests.extractors.EnrollmentExtractor
import com.simprints.clientapi.mockextractors.MockExtractorParams.MOCK_METADATA
import com.simprints.clientapi.mockextractors.MockExtractorParams.MOCK_MODULE_ID
import com.simprints.clientapi.mockextractors.MockExtractorParams.MOCK_PROJECT_ID
import com.simprints.clientapi.mockextractors.MockExtractorParams.MOCK_USER_ID
import org.mockito.Mockito

object MockEnrollmentExtractor {

    fun getValidEnrollmentExtractorMock(): EnrollmentExtractor {
        val mockEnrollmentExtractor = Mockito.mock(EnrollmentExtractor::class.java)
        Mockito.`when`(mockEnrollmentExtractor.getProjectId()).thenReturn(MOCK_PROJECT_ID)
        Mockito.`when`(mockEnrollmentExtractor.getUserId()).thenReturn(MOCK_USER_ID)
        Mockito.`when`(mockEnrollmentExtractor.getModuleId()).thenReturn(MOCK_MODULE_ID)
        Mockito.`when`(mockEnrollmentExtractor.getMetatdata()).thenReturn(MOCK_METADATA)

        return mockEnrollmentExtractor
    }

}
