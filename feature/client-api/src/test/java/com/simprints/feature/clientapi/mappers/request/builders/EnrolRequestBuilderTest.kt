package com.simprints.feature.clientapi.mappers.request.builders

import com.google.common.truth.Truth.assertThat
import com.simprints.feature.clientapi.mappers.request.requestFactories.EnrolActionFactory
import com.simprints.feature.clientapi.mappers.request.requestFactories.RequestActionFactory
import com.simprints.feature.clientapi.mappers.request.requestFactories.RequestActionFactory.Companion.MOCK_MODULE_ID
import com.simprints.feature.clientapi.mappers.request.requestFactories.RequestActionFactory.Companion.MOCK_PROJECT_ID
import com.simprints.feature.clientapi.mappers.request.requestFactories.RequestActionFactory.Companion.MOCK_USER_ID
import com.simprints.infra.config.store.models.Project
import com.simprints.infra.config.store.tokenization.TokenizationProcessor
import com.simprints.infra.orchestration.data.ActionConstants
import com.simprints.infra.orchestration.data.ActionRequest
import com.simprints.infra.orchestration.data.ActionRequestIdentifier
import io.mockk.every
import io.mockk.mockk
import org.junit.Test

internal class EnrolRequestBuilderTest {
    @Test
    fun `EnrolActionRequest should contain mandatory fields`() {
        val extractor = EnrolActionFactory.getMockExtractor()
        val validator = EnrolActionFactory.getValidator(extractor)
        val tokenizationProcessor = mockk<TokenizationProcessor>()
        val project = mockk<Project> {
            every { id } returns "projectId"
        }

        val action = EnrolRequestBuilder(
            actionIdentifier = ActionRequestIdentifier(
                actionName = RequestActionFactory.MOCK_PACKAGE,
                packageName = ActionConstants.ACTION_ENROL,
                callerPackageName = "",
                contractVersion = 1,
                timestampMs = 0L,
            ),
            extractor = extractor,
            project = project,
            tokenizationProcessor = tokenizationProcessor,
            validator = validator,
        ).build() as ActionRequest.EnrolActionRequest

        assertThat(action.projectId).isEqualTo(MOCK_PROJECT_ID)
        assertThat(action.userId.toString()).isEqualTo(MOCK_USER_ID)
        assertThat(action.moduleId.toString()).isEqualTo(MOCK_MODULE_ID)
    }
}
