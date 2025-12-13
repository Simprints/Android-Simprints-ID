package com.simprints.feature.clientapi.mappers.request.requestFactories

import com.simprints.core.domain.tokenization.asTokenizableRaw
import com.simprints.feature.clientapi.mappers.request.builders.ConfirmIdentifyRequestBuilder
import com.simprints.feature.clientapi.mappers.request.extractors.ActionRequestExtractor
import com.simprints.feature.clientapi.mappers.request.extractors.ConfirmIdentityRequestExtractor
import com.simprints.feature.clientapi.mappers.request.validators.ConfirmIdentityValidator
import com.simprints.infra.config.sync.ConfigManager
import com.simprints.infra.events.EventRepository
import com.simprints.infra.events.event.domain.models.IdentificationCallbackEvent
import com.simprints.infra.orchestration.data.ActionConstants
import com.simprints.infra.orchestration.data.ActionRequest
import com.simprints.infra.orchestration.data.ActionRequestIdentifier
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk

internal object ConfirmIdentityActionFactory : RequestActionFactory() {
    override fun getIdentifier() = ActionRequestIdentifier(
        packageName = MOCK_PACKAGE,
        actionName = ActionConstants.ACTION_CONFIRM_IDENTITY,
        callerPackageName = "",
        contractVersion = 1,
        timestampMs = 0L,
    )

    override fun getValidSimprintsRequest() = ActionRequest.ConfirmIdentityActionRequest(
        actionIdentifier = getIdentifier(),
        projectId = MOCK_PROJECT_ID,
        userId = MOCK_USER_ID.asTokenizableRaw(),
        sessionId = MOCK_SESSION_ID,
        selectedGuid = MOCK_SELECTED_GUID,
        metadata = MOCK_METADATA,
        unknownExtras = emptyMap(),
    )

    override fun getValidator(extractor: ActionRequestExtractor): ConfirmIdentityValidator {
        val mockEventRepository = mockk<EventRepository>()
        // Return a valid identification callback event with the selected GUID
        coEvery { mockEventRepository.getEventsFromScope(any()) } returns listOf(
            mockk<IdentificationCallbackEvent> {
                every { payload } returns mockk {
                    every { scores } returns listOf(
                        mockk {
                            every { guid } returns MOCK_SELECTED_GUID
                        },
                    )
                }
            },
        )

        val mockConfigManager = mockk<ConfigManager>(relaxed = true)

        return ConfirmIdentityValidator(
            extractor as ConfirmIdentityRequestExtractor,
            MOCK_SESSION_ID,
            mockEventRepository,
            configManager = mockConfigManager,
        )
    }

    override fun getBuilder(extractor: ActionRequestExtractor): ConfirmIdentifyRequestBuilder = ConfirmIdentifyRequestBuilder(
        actionIdentifier = getIdentifier(),
        extractor = extractor as ConfirmIdentityRequestExtractor,
        project = mockk(),
        tokenizationProcessor = mockk(),
        validator = getValidator(extractor),
    )

    override fun getMockExtractor(): ConfirmIdentityRequestExtractor {
        val mockConfirmIdentifyExtractor = mockk<ConfirmIdentityRequestExtractor>()
        setMockDefaultExtractor(mockConfirmIdentifyExtractor)
        every { mockConfirmIdentifyExtractor.getSessionId() } returns MOCK_SESSION_ID
        every { mockConfirmIdentifyExtractor.getSelectedGuid() } returns MOCK_SELECTED_GUID
        return mockConfirmIdentifyExtractor
    }
}
