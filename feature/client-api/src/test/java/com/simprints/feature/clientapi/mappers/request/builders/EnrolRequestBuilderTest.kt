package com.simprints.feature.clientapi.mappers.request.builders

import com.google.common.truth.Truth.assertThat
import com.simprints.feature.clientapi.models.ActionRequest
import com.simprints.feature.clientapi.mappers.request.requestFactories.EnrolActionFactory
import com.simprints.feature.clientapi.mappers.request.requestFactories.RequestActionFactory
import org.junit.Test

internal class EnrolRequestBuilderTest {

    @Test
    fun `EnrolActionRequest should contain mandatory fields`() {
        val extractor = EnrolActionFactory.getMockExtractor()
        val validator = EnrolActionFactory.getValidator(extractor)

        val action = EnrolRequestBuilder(RequestActionFactory.MOCK_PACKAGE, extractor, validator).build() as ActionRequest.EnrolActionRequest

        assertThat(action.projectId).isEqualTo(RequestActionFactory.MOCK_PROJECT_ID)
        assertThat(action.userId).isEqualTo(RequestActionFactory.MOCK_USER_ID)
        assertThat(action.moduleId).isEqualTo(RequestActionFactory.MOCK_MODULE_ID)
    }
}
