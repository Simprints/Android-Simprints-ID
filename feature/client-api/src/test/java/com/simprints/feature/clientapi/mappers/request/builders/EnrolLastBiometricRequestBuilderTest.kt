package com.simprints.feature.clientapi.mappers.request.builders

import com.google.common.truth.Truth.assertThat
import com.simprints.feature.clientapi.models.ActionRequest
import com.simprints.feature.clientapi.mappers.request.requestFactories.EnrolLastBiometricsActionFactory
import com.simprints.feature.clientapi.mappers.request.requestFactories.RequestActionFactory
import org.junit.Test

internal class EnrolLastBiometricRequestBuilderTest {

    @Test
    fun `EnrolLastBiometricActionRequest should contain mandatory fields`() {
        val extractor = EnrolLastBiometricsActionFactory.getMockExtractor()
        val validator = EnrolLastBiometricsActionFactory.getValidator(extractor)

        val action = EnrolLastBiometricsRequestBuilder(RequestActionFactory.MOCK_PACKAGE, extractor, validator).build() as ActionRequest.EnrolLastBiometricActionRequest

        assertThat(action.projectId).isEqualTo(RequestActionFactory.MOCK_PROJECT_ID)
        assertThat(action.userId).isEqualTo(RequestActionFactory.MOCK_USER_ID)
        assertThat(action.moduleId).isEqualTo(RequestActionFactory.MOCK_MODULE_ID)
        assertThat(action.sessionId).isEqualTo(RequestActionFactory.MOCK_SESSION_ID)
    }
}
