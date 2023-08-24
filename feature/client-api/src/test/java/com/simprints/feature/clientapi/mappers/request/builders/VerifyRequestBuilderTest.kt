package com.simprints.feature.clientapi.mappers.request.builders

import com.google.common.truth.Truth.assertThat
import com.simprints.feature.clientapi.models.ActionRequest
import com.simprints.feature.clientapi.mappers.request.requestFactories.RequestActionFactory
import com.simprints.feature.clientapi.mappers.request.requestFactories.VerifyActionFactory
import org.junit.Test

internal class VerifyRequestBuilderTest {

    @Test
    fun `VerifyActionRequest should contain mandatory fields`() {
        val extractor = VerifyActionFactory.getMockExtractor()
        val validator = VerifyActionFactory.getValidator(extractor)

        val action = VerifyRequestBuilder(RequestActionFactory.MOCK_PACKAGE, extractor, validator).build() as ActionRequest.VerifyActionRequest

        assertThat(action.projectId).isEqualTo(RequestActionFactory.MOCK_PROJECT_ID)
        assertThat(action.userId).isEqualTo(RequestActionFactory.MOCK_USER_ID)
        assertThat(action.moduleId).isEqualTo(RequestActionFactory.MOCK_MODULE_ID)
        assertThat(action.verifyGuid).isEqualTo(RequestActionFactory.MOCK_VERIFY_GUID)
    }
}
