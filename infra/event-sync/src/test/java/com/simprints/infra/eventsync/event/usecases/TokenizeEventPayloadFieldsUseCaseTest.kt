package com.simprints.infra.eventsync.event.usecases

import com.simprints.core.domain.tokenization.TokenizableString
import com.simprints.infra.config.store.models.Project
import com.simprints.infra.config.store.models.TokenKeyType
import com.simprints.infra.config.store.tokenization.TokenizationProcessor
import com.simprints.infra.events.event.domain.models.Event
import io.mockk.*
import io.mockk.impl.annotations.MockK
import org.junit.Before
import kotlin.test.Test

internal class TokenizeEventPayloadFieldsUseCaseTest {
    @MockK
    lateinit var tokenizationProcessor: TokenizationProcessor

    @MockK
    lateinit var project: Project

    @MockK
    lateinit var event: Event

    lateinit var useCase: TokenizeEventPayloadFieldsUseCase

    val tokenKeyType = TokenKeyType.AttendantId
    val tokenKeyTypeList = TokenKeyType.ModuleId
    val rawString = TokenizableString.Raw("decrypted")
    val tokenizedString = TokenizableString.Tokenized("encrypted")

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)
        useCase = TokenizeEventPayloadFieldsUseCase(tokenizationProcessor)

        every { event.setTokenizedFields(any()) } returns event
        every { event.setTokenizedListFields(any()) } returns event

        every { tokenizationProcessor.tokenizeIfNecessary(any(), any(), any()) } returns tokenizedString
    }

    @Test
    fun `should invoke tokenizeIfNecessary for TokenizableString`() {
        every { event.getTokenizableFields() } returns mapOf(tokenKeyType to rawString)
        every { event.getTokenizableListFields() } returns emptyMap()
        every { tokenizationProcessor.encrypt(rawString, tokenKeyType, project) } returns tokenizedString

        useCase(event = event, project = project)

        verify { event.setTokenizedFields(mapOf(tokenKeyType to tokenizedString)) }
        verify { event.setTokenizedListFields(emptyMap()) }
    }

    @Test
    fun `should invoke tokenizeIfNecessary for list of TokenizableString`() {
        every { event.getTokenizableFields() } returns emptyMap()
        every { event.getTokenizableListFields() } returns mapOf(tokenKeyTypeList to listOf(rawString, rawString))

        useCase(event = event, project = project)

        verify { event.setTokenizedFields(emptyMap()) }
        verify { event.setTokenizedListFields(mapOf(tokenKeyTypeList to listOf(tokenizedString, tokenizedString))) }
    }

    @Test
    fun `should handle both field and list of TokenizableString in same event`() {
        every { event.getTokenizableFields() } returns mapOf(tokenKeyType to rawString)
        every { event.getTokenizableListFields() } returns mapOf(tokenKeyTypeList to listOf(rawString, rawString))

        useCase(event = event, project = project)

        verify { event.setTokenizedFields(mapOf(tokenKeyType to tokenizedString)) }
        verify { event.setTokenizedListFields(mapOf(tokenKeyTypeList to listOf(tokenizedString, tokenizedString))) }
    }
}
