package com.simprints.feature.clientapi.mappers.request.builders

import com.google.common.truth.Truth.assertThat
import com.simprints.feature.clientapi.models.ActionRequest
import com.simprints.feature.clientapi.mappers.request.requestFactories.IdentifyRequestActionFactory
import com.simprints.feature.clientapi.mappers.request.requestFactories.RequestActionFactory
import com.simprints.feature.clientapi.models.ActionRequestIdentifier
import com.simprints.feature.clientapi.models.IntegrationConstants
import org.junit.Test

internal class IdentifyRequestBuilderTest  {

    @Test
    fun `IdentifyActionRequest should contain mandatory fields`() {
        val extractor = IdentifyRequestActionFactory.getMockExtractor()
        val validator = IdentifyRequestActionFactory.getValidator(extractor)

        val action = IdentifyRequestBuilder(
            ActionRequestIdentifier(
                RequestActionFactory.MOCK_PACKAGE,
                IntegrationConstants.ACTION_IDENTIFY,
            ),
            extractor,
            validator
        ).build() as ActionRequest.IdentifyActionRequest

        assertThat(action.projectId).isEqualTo(RequestActionFactory.MOCK_PROJECT_ID)
        assertThat(action.userId).isEqualTo(RequestActionFactory.MOCK_USER_ID)
        assertThat(action.moduleId).isEqualTo(RequestActionFactory.MOCK_MODULE_ID)
    }
}
