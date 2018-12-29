package com.simprints.clientapi.mockextractors

import com.simprints.clientapi.clientrequests.extractors.EnrollmentExtractor
import com.simprints.clientapi.mockextractors.MockExtractorParams.MOCK_METADATA
import com.simprints.clientapi.mockextractors.MockExtractorParams.MOCK_MODULE_ID
import com.simprints.clientapi.mockextractors.MockExtractorParams.MOCK_PROJECT_ID
import com.simprints.clientapi.mockextractors.MockExtractorParams.MOCK_USER_ID
import org.mockito.Mockito

object MockEnrollmentExtractor {

    val mockEnrollmentExtractor: EnrollmentExtractor = Mockito.mock(EnrollmentExtractor::class.java)

    init {
        Mockito.`when`(mockEnrollmentExtractor.getProjectId()).thenReturn(MOCK_PROJECT_ID)
        Mockito.`when`(mockEnrollmentExtractor.getUserId()).thenReturn(MOCK_USER_ID)
        Mockito.`when`(mockEnrollmentExtractor.getModuleId()).thenReturn(MOCK_MODULE_ID)
        Mockito.`when`(mockEnrollmentExtractor.getMetatdata()).thenReturn(MOCK_METADATA)
    }
}
