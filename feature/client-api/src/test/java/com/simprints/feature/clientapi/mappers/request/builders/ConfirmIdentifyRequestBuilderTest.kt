package com.simprints.feature.clientapi.mappers.request.builders

import com.google.common.truth.Truth.assertThat
import com.simprints.feature.clientapi.models.ActionRequest
import com.simprints.feature.clientapi.mappers.request.requestFactories.ConfirmIdentityActionFactory
import com.simprints.feature.clientapi.mappers.request.requestFactories.RequestActionFactory
import com.simprints.feature.clientapi.models.ActionRequestIdentifier
import com.simprints.feature.clientapi.models.IntegrationConstants
import org.junit.Test

internal class ConfirmIdentifyRequestBuilderTest {

    @Test
    fun `ConfirmActionRequest should contain mandatory fields`() {
        val extractor = ConfirmIdentityActionFactory.getMockExtractor()
        val validator = ConfirmIdentityActionFactory.getValidator(extractor)

        val action = ConfirmIdentifyRequestBuilder(
            ActionRequestIdentifier(
                RequestActionFactory.MOCK_PACKAGE,
                IntegrationConstants.ACTION_CONFIRM_IDENTITY,
            ),
            extractor,
            validator
        ).build() as ActionRequest.ConfirmActionRequest

        assertThat(action.projectId).isEqualTo(RequestActionFactory.MOCK_PROJECT_ID)
        assertThat(action.userId).isEqualTo(RequestActionFactory.MOCK_USER_ID)
        assertThat(action.sessionId).isEqualTo(RequestActionFactory.MOCK_SESSION_ID)
        assertThat(action.selectedGuid).isEqualTo(RequestActionFactory.MOCK_SELECTED_GUID)
    }
}
