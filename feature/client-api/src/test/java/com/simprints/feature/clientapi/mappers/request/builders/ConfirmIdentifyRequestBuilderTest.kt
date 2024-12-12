package com.simprints.feature.clientapi.mappers.request.builders

import com.google.common.truth.Truth.assertThat
import com.simprints.feature.clientapi.mappers.request.requestFactories.ConfirmIdentityActionFactory
import com.simprints.feature.clientapi.mappers.request.requestFactories.RequestActionFactory
import com.simprints.feature.clientapi.mappers.request.requestFactories.RequestActionFactory.Companion.MOCK_PROJECT_ID
import com.simprints.feature.clientapi.mappers.request.requestFactories.RequestActionFactory.Companion.MOCK_SELECTED_GUID
import com.simprints.feature.clientapi.mappers.request.requestFactories.RequestActionFactory.Companion.MOCK_SESSION_ID
import com.simprints.feature.clientapi.mappers.request.requestFactories.RequestActionFactory.Companion.MOCK_USER_ID
import com.simprints.infra.config.store.models.Project
import com.simprints.infra.config.store.tokenization.TokenizationProcessor
import com.simprints.infra.orchestration.data.ActionConstants
import com.simprints.infra.orchestration.data.ActionRequest
import com.simprints.infra.orchestration.data.ActionRequestIdentifier
import io.mockk.every
import io.mockk.mockk
import org.junit.Test

internal class ConfirmIdentifyRequestBuilderTest {
    @Test
    fun `ConfirmActionRequest should contain mandatory fields`() {
        val extractor = ConfirmIdentityActionFactory.getMockExtractor()
        val validator = ConfirmIdentityActionFactory.getValidator(extractor)
        val tokenizationProcessor = mockk<TokenizationProcessor>()
        val project = mockk<Project> {
            every { id } returns "projectId"
        }

        val action = ConfirmIdentifyRequestBuilder(
            actionIdentifier = ActionRequestIdentifier(
                actionName = RequestActionFactory.MOCK_PACKAGE,
                packageName = ActionConstants.ACTION_CONFIRM_IDENTITY,
                callerPackageName = "",
                contractVersion = 1,
                timestampMs = 0L,
            ),
            extractor = extractor,
            project = project,
            tokenizationProcessor = tokenizationProcessor,
            validator = validator,
        ).build() as ActionRequest.ConfirmIdentityActionRequest

        assertThat(action.projectId).isEqualTo(MOCK_PROJECT_ID)
        assertThat(action.userId.toString()).isEqualTo(MOCK_USER_ID)
        assertThat(action.sessionId).isEqualTo(MOCK_SESSION_ID)
        assertThat(action.selectedGuid).isEqualTo(MOCK_SELECTED_GUID)
    }
}
