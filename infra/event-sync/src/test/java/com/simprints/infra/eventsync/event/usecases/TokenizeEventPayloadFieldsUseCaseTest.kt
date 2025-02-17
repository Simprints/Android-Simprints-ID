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

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)
        useCase = TokenizeEventPayloadFieldsUseCase(tokenizationProcessor)
    }

    @Test
    fun `should invoke tokenizationProcessor encrypt for TokenizableString Raw`() {
        val tokenKeyType = mockk<TokenKeyType>()
        val rawString = TokenizableString.Raw("decrypted")
        val tokenizedString = TokenizableString.Tokenized("encrypted")

        val tokenizedMap = mapOf(tokenKeyType to rawString)

        every { event.getTokenizableFields() } returns tokenizedMap
        every { tokenizationProcessor.encrypt(rawString, tokenKeyType, project) } returns tokenizedString

        useCase(event = event, project = project)

        verify { tokenizationProcessor.encrypt(rawString, tokenKeyType, project) }
        verify { event.setTokenizedFields(mapOf(tokenKeyType to tokenizedString)) }
    }

    @Test
    fun `should set the same tokenized string if already tokenized`() {
        val tokenKeyType = mockk<TokenKeyType>()
        val tokenizedString = TokenizableString.Tokenized("encrypted")
        val tokenizedMap = mapOf(tokenKeyType to tokenizedString)

        every { event.getTokenizableFields() } returns tokenizedMap

        useCase(event = event, project = project)

        verify { event.setTokenizedFields(mapOf(tokenKeyType to tokenizedString)) }
    }
}
